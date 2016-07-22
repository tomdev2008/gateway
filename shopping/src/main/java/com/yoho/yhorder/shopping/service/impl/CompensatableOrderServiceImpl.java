package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.transaction.YHTxCoordinator;
import com.yoho.core.transaction.annoation.YHTransaction;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.constants.OrdersMateKey;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.common.model.Coupon;
import com.yoho.yhorder.common.model.VirtualInfo;
import com.yoho.yhorder.common.utils.MathUtils;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.model.OrderPromotionInfo;
import com.yoho.yhorder.dal.model.OrdersCoupons;
import com.yoho.yhorder.dal.model.OrdersMeta;
import com.yoho.yhorder.shopping.compensatable.*;
import com.yoho.yhorder.shopping.model.Order;
import com.yoho.yhorder.shopping.model.OrderCreationContext;
import com.yoho.yhorder.shopping.model.OrderGoods;
import com.yoho.yhorder.shopping.model.OrderReceiver;
import com.yoho.yhorder.shopping.service.IOrderCreationService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wujiexiang on 16/5/23.
 */
@Component("compensatableOrderServiceImpl")
public class CompensatableOrderServiceImpl implements IOrderCreationService {

    private final static Logger logger = LoggerFactory.getLogger("orderSubmitLog");

    @Autowired
    private StorageService storageService;

    @Autowired
    private RedEnvelopeService redEnvelopeService;

    @Autowired
    private YohoCoinService yohoCoinService;

    @Autowired
    private CompensatableCouponService couponService;

    @Autowired
    private PromotionCodeService promotionCodeService;

    @Autowired
    private LimitCodeService limitCodeService;

    @Autowired
    private DBOrderSubmitService dbOrderSubmitService;

    @Autowired
    YHTxCoordinator tx;

    @Override
    @YHTransaction
    public void create(OrderCreationContext context) {

        logger.info("enter create local order in shopping_cart_submit, uid {}, order code {}, user info {}, order info {} ",
                context.getOrder().getUid(), context.getOrder().getOrderCode(),
                context.getUserInfo(), context.getOrder());

        Order order = context.getOrder();

        try {
            //执行可补偿的远程调用

            doCompensatableCreate(order);

            //执行数据库
            /**
             * 7.入库
             * 7.1订单表 orders
             * 7.2订单商品表 orders_goods
             * 7.3优惠码使用表 orders_coupons
             * 7.4订单明细表 order_promotion_info
             * 7.5限购商品记录表 order_ext_attribute
             * 7.6收获订单表 order_default_preferences
             * 7.7清空购物车中的商品 shopping_cart_items
             * 7.8shopping_key下单使用表 shopping_tag(已删除)
             * 7.9电子发票 orders_meta
             */
            doDBCreate(order);

        } catch (Exception ex) {
            //controller已经打印日志了
            tx.rollback();
            if (!(ex instanceof ServiceException)) {
                throw new ServiceException(ServiceError.SHOPPING_SUBMIT_ORDER_FAIL, ex);
            }
            throw ex;
        }


        logger.info("exit create local order in shopping_cart_submit, uid {}, order code {} ",
                context.getOrder().getUid(), context.getOrder().getOrderCode());

    }

    private void doCompensatableCreate(Order order) {
        //减库库
        storageService.decreaseStorage(order.getUid(), order.getOrderCode(), order.getGoodsList());

        //使用红包
        redEnvelopeService.useRedEnvelopes(order.getUid(), order.getOrderCode(), order.getUseRedEnvelopes());

        //使用yoho币
        yohoCoinService.useYohoCoin(order.getUid(), order.getOrderCode(), MathUtils.toInt(order.getYohoCoinNum(), order.getYohoCoinRatio()));

        //优惠券
        couponService.usePromotionCoupon(order.getUid(), order.getOrderCode(), order.getOrderCoupon());

        //优惠码
        promotionCodeService.useOrderPromotionCode(order.getUid(), order.getOrderCode(), order.getPromotionCodeChargeResult());

        //限购码
        limitCodeService.addLimitCodeUseRecord(order.getUid(), order.getOrderCode(), order.getChargeType(), order.getShoppingItems());
    }


    /**
     * 订单入库
     * @param order
     */
    private void doDBCreate(Order order) {

        dbOrderSubmitService.createOrder(order);
    }

    @Transactional
    @Component
    public static class DBOrderSubmitService {
        @Autowired
        protected IOrdersMapper ordersMapper;

        @Autowired
        private IOrdersGoodsMapper ordersGoodsMapper;

        @Autowired
        private OrdersCouponsMapper ordersCouponsMapper;

        @Autowired
        protected IShoppingCartItemsDAO shoppingCartItemsDAO;

        @Autowired
        protected IOrderDefaultPreferencesDAO orderDefaultPreferencesMapper;

        @Autowired
        private OrderPromotionInfoMapper orderPromotionInfoMapper;

        @Autowired
        private IOrderExtAttributeDAO orderExtAttributeDAO;

        @Autowired
        private IOrdersMetaDAO ordersMetaDAO;

        @Database(DataSource = "yh_orders")
        public void createOrder(Order order) {
            //创建主单
            Integer orderId = addOrder(order);
            order.setOrderId(orderId);
            //订单商品
            addOrderGoods(order.getOrderCode(),orderId, order.getUid(), order.getGoodsList());
            //记录orders_coupons表
            addOrderCoupon(order);

            //订单金额明细
            addOrderPromotionInfo(order);

            //限购商品记录
            addOrderExtAttribute(order);

            //订单收获地址
            addDefaultPreferences(order);

            //清空购物车
            clearCartGoods(order);

            addOrderMeta(order);
        }

        private Integer addOrder(Order order) {
            logger.debug("enter create local order info for database in shopping_cart_submit, order info {}", order);
            //BO - > DO

            if (order.getLastOrderAmount() == 0) {
                //订单已支付
                order.setPaymentStatus("Y");
            }

            Orders ordersDo = new Orders();
            ordersDo.setOrderCode(order.getOrderCode());
            ordersDo.setUid(order.getUid());
            ordersDo.setExpressNumber(order.getExpressNumber());
            ordersDo.setOrderType(order.getOrderType().byteValue());
            ordersDo.setIsInvoice(order.getNeedInvoice());
//        ordersDo.setInvoicesType(String.valueOf(order.getInvoicesType()));
//        ordersDo.setInvoicesPayable(order.getInvoicesPayable());
            //yoho币在算费的时候，被稀释成货币单位，但保存到数据中还是使用yoho币数量
            ordersDo.setYohoCoinNum(MathUtils.toInt(order.getYohoCoinNum(), order.getYohoCoinRatio()));
            ordersDo.setPaymentType(order.getPaymentType().byteValue());
            ordersDo.setPayment(order.getPayment().byteValue());
            ordersDo.setBankCode(order.getBankCode());
            ordersDo.setPaymentStatus(order.getPaymentStatus());
            ordersDo.setShippingTypeId(order.getShippingTypeId().byteValue());
            ordersDo.setShippingCost(new BigDecimal(order.getShippingCost()));
            ordersDo.setExpressId(order.getExpressId().byteValue());
            ordersDo.setUserName(order.getUserName());
            ordersDo.setPhone(order.getReceiver().getPhone());
            ordersDo.setMobile(order.getReceiver().getMobile());
            try {
                ordersDo.setAreaCode(new Integer(order.getReceiver().getAreaCode()));
            } catch (Exception e) {
                ordersDo.setAreaCode(0);
                logger.warn("[{}] area code {} format is error", order.getUid(), order.getReceiver().getAreaCode());
            }
            try {
                ordersDo.setZipCode(new Integer(order.getReceiver().getZipCode()));
            } catch (Exception e) {
                ordersDo.setZipCode(0);
                logger.warn("[{}] zip code {} format is error", order.getUid(), order.getReceiver().getZipCode());
            }

            ordersDo.setAddress(order.getReceiver().getAddress());

            ordersDo.setRemark(order.getRemark());
            ordersDo.setInvoicesType(order.getInvoiceType() + "");
            ordersDo.setInvoicesPayable(order.getInvoicePayable());
            ordersDo.setReceivingTime(order.getReceivingTime());
            ordersDo.setReceiptTime(String.valueOf(order.getReceiptTime()));
            ordersDo.setExceptionStatus(order.getExceptionStatus().byteValue());
            ordersDo.setIsLock(order.getIsLock());
            ordersDo.setIsArrive(order.getIsArrive());
            ordersDo.setStatus(order.getStatus().byteValue());
            ordersDo.setIsCancel(order.getIsCancel());
            ordersDo.setCancelType(order.getCancelType().byteValue());
            ordersDo.setExchangeStatus(order.getExchangeStatus().byteValue());
            ordersDo.setRefundStatus(order.getRefundStatus().byteValue());
            ordersDo.setArriveTime(order.getArriveTime());
            ordersDo.setShipmentTime(order.getShipmentTime());
            ordersDo.setAmount(new BigDecimal(order.getAmount()));
            ordersDo.setIsPrintPrice(order.getIsPrintPrice());
            ordersDo.setIsPreContact(order.getIsPreContact());
            ordersDo.setIsNeedRapid(order.getIsNeedRapid());
            ordersDo.setAttribute(order.getAttribute().byteValue());
            ordersDo.setActivitiesId(order.getActivitiesId().shortValue());

            ordersDo.setUserLevel(order.getUserLevel());
            ordersDo.setIsJit(order.getIsJit());
            ordersDo.setProvince(order.getReceiver().getProvince());
            ordersDo.setCity(order.getReceiver().getCity());
            ordersDo.setDistrict(order.getReceiver().getDistrict());
            ordersDo.setEmail(order.getReceiver().getEmail());
            ordersDo.setIsAdvance(order.getIsAdvance());
            ordersDo.setIsMultiPackage(order.getIsMultiPackage());

            int second = getCurrentTimeSecond();
            ordersDo.setCreateTime(second);
            ordersDo.setDeliverYohoCoin(order.getDeliverYohoCoin());
            order.setCreateTime(second);

            try {
                ordersMapper.insertShoppingOrder(ordersDo);
            } catch (Exception ex) {
                /**
                 * 捕获异常并打印日志，然后继续抛出异常
                 */
                logger.error("exception happened when add local order info to database in shopping_cart_submit, uid{},  order code {},  order info {}, ordersDo {}",
                        order.getUid(), order.getOrderCode(), order, ordersDo, ex);
                throw ex;
            }

            logger.info("add local order info to database in shopping_cart_submit success, order code {}, order db id {}, order info {}, ordersDo {}",
                    order.getOrderCode(), ordersDo.getId(), order, ordersDo);

            return ordersDo.getId();
        }

        /**
         * 从1970到现在的秒数
         *
         * @return
         */
        private int getCurrentTimeSecond() {
            Calendar calendar = Calendar.getInstance();
            return (int) (calendar.getTimeInMillis() / 1000);
        }


        private void addOrderGoods(Long orderCode,Integer orderId, Integer uid, List<OrderGoods> orderGoodsList) {
            List<OrdersGoods> list = new ArrayList<>();
            for (OrderGoods goods : orderGoodsList) {
                OrdersGoods goodsDo = new OrdersGoods();
                goodsDo.setUid(uid);
                goodsDo.setProductId(goods.getProduct_id());
                goodsDo.setProductSkn(goods.getProduct_skn());
                goodsDo.setBrandId(goods.getBrand_id());
                goodsDo.setGoodsId(goods.getGoods_id());
                goodsDo.setGoodsType(goods.getGoods_type().byteValue());
                goodsDo.setErpSkuId(goods.getErp_sku_id());
                goodsDo.setSalesPrice(new BigDecimal(goods.getSales_price()).longValue());
                goodsDo.setGoodsPrice(new BigDecimal(goods.getGoods_price()));
                goodsDo.setGoodsAmount(new BigDecimal(goods.getGoods_amount()));
                goodsDo.setNum(goods.getNum());
                goodsDo.setSizeId(goods.getSize_id());
                goodsDo.setSizeName(goods.getSize_name());
                goodsDo.setColorId(goods.getColor_id().byteValue());
                goodsDo.setColorName(goods.getColor_name());
                goodsDo.setOrderId(orderId);
                goodsDo.setProductName(goods.getProduct_name());
                goodsDo.setOrderCode(orderCode);

                goodsDo.setCouponsCutAmount(BigDecimal.valueOf(goods.getDiscountPerSku().couponsAmount));
                goodsDo.setPromoCodeCutAmount(BigDecimal.valueOf(goods.getDiscountPerSku().promotionCodeAmount));
                goodsDo.setRedenvelopeCutAmount(BigDecimal.valueOf(goods.getDiscountPerSku().redEnvelopeAmount));
                goodsDo.setYohoCoinCutNum(goods.getDiscountPerSku().yohoCoinNum);

                goodsDo.setRealVipPrice(BigDecimal.valueOf(goods.getReal_vip_price()));

                goodsDo.setShopId(goods.getShop_id());
                goodsDo.setSupplierId(goods.getSupplier_id());
                goodsDo.setVipDiscountRate(BigDecimal.valueOf(goods.getVip_discount()));
                goodsDo.setGetYohoCoin(goods.getGet_yoho_coin());
                goodsDo.setIsJit(goods.getIs_jit());

                goodsDo.setPromotionCutAmount(BigDecimal.valueOf(goods.getDiscountPerSku().promotionAmount));

                goodsDo.setVipCutAmount(BigDecimal.valueOf(goods.getVip_discount_money()));

                goodsDo.setProductSkc(goods.getProduct_skc());

                list.add(goodsDo);
            }
            //已经在checkorder方法中校验orderGoodsList是否为空，这边就不在校验
            //批量更新
            try {
                ordersGoodsMapper.batchInsertOrderGoods(list);
            } catch (Exception ex) {

                logger.error("exception happend when add order goods to database in shopping_cart_submit, order id {}, uid {},order goods {}.",
                        orderId, uid, orderGoodsList, ex);

                /**
                 * 异常一定要抛出
                 */
                throw ex;
            }
            logger.info("add order goods to database success in shopping_cart_submit,order id {}, uid {}, order goods {}.",
                    orderId, uid, orderGoodsList);
        }

        private void addOrderCoupon(Order order) {
            Coupon coupon = order.getOrderCoupon();
            if (coupon != null && StringUtils.isNotEmpty(coupon.getCoupon_code())) {
                //添加订单与优惠券的关联
                OrdersCoupons ordersCoupons = new OrdersCoupons();
                ordersCoupons.setOrderId(order.getOrderId());
                ordersCoupons.setCouponsCode(coupon.getCoupon_code());
                ordersCoupons.setCouponsId(coupon.getCoupon_id());
                ordersCouponsMapper.insert(ordersCoupons);
                logger.info("update relationship with coupon and order in shopping_cart_submit, uid {}, order code {}, ordersCoupons {}",
                        order.getUid(), order.getOrderCode(), ordersCoupons);
            }
        }

        private void clearCartGoods(Order order) {
            int uid = order.getUid();
            List<Integer> itemsIds = order.getShoppingCartItemIds();
            logger.info("clear shopping cart items,uid is {},itemsIds are {}", uid, itemsIds);
            if (CollectionUtils.isNotEmpty(itemsIds)) {
                shoppingCartItemsDAO.deleteShoppingCartGoodsByCartID(uid, StringUtils.join(itemsIds, ","));
            }
        }

        private void addDefaultPreferences(Order order) {
            OrderReceiver receiver = order.getReceiver();
            JSONObject preferencesJSON = new JSONObject();
            UserAddressRspBO userAddressRspBO = order.getUserAddressRspBO();
            if (userAddressRspBO != null) {
                JSONObject delivery_addressJSON = new JSONObject();
                delivery_addressJSON.put("id", userAddressRspBO.getId());
                delivery_addressJSON.put("uid", userAddressRspBO.getUid());
                delivery_addressJSON.put("addressee_name", userAddressRspBO.getAddresseeName());
                delivery_addressJSON.put("address", userAddressRspBO.getAddress());
                delivery_addressJSON.put("area_code", userAddressRspBO.getAreaCode());
                delivery_addressJSON.put("zip_code", userAddressRspBO.getZipCode());
                delivery_addressJSON.put("mobile", userAddressRspBO.getMobile());
                delivery_addressJSON.put("phone", userAddressRspBO.getPhone());
                delivery_addressJSON.put("is_default", userAddressRspBO.getIsDefault());
                delivery_addressJSON.put("email", userAddressRspBO.getEmail());
                JSONObject areaNames = new JSONObject();
                areaNames.put("province", receiver.getProvince());
                areaNames.put("city", receiver.getCity());
                areaNames.put("county", receiver.getDistrict());
                areaNames.put("is_support", userAddressRspBO.getArea().getIs_support());
                areaNames.put("is_delivery", userAddressRspBO.getArea().getIs_delivery());

                delivery_addressJSON.put("areaNames", areaNames);
                preferencesJSON.put("delivery_address", delivery_addressJSON);
            } else {
                preferencesJSON.put("delivery_address", new JSONArray());
            }
            preferencesJSON.put("payment_type", order.getPaymentType());
            preferencesJSON.put("shipping_manner", order.getReceiver().getShippingManner());
            preferencesJSON.put("receiving_time", order.getReceivingTime());
            preferencesJSON.put("uid", order.getUid());

            orderDefaultPreferencesMapper.insertDefaultPreferences(order.getUid(), preferencesJSON.toJSONString());

            logger.info("insertDefaultPreferences success,uid is {},order code is {}", order.getUid(), order.getOrderCode());
        }

        private void addOrderPromotionInfo(Order order) {
            //-----------------------addOrderPromotionInfo-------
            OrderPromotionInfo orderPromotionInfo = new OrderPromotionInfo();
            orderPromotionInfo.setOrderCode(order.getOrderCode());
            orderPromotionInfo.setUid(order.getUid());
            JSONObject orderPromotioJSON = new JSONObject();
            orderPromotioJSON.put("last_order_amount", order.getLastOrderAmount());
            orderPromotioJSON.put("shipping_cost", order.getShippingCost());
            orderPromotioJSON.put("order_amount", order.getOrderAmount());
            orderPromotioJSON.put("coupons_amount", order.getOrderCoupon().getCoupon_amount());
            orderPromotioJSON.put("promotion_amount", order.getDiscountAmount());
            orderPromotioJSON.put("use_yoho_coin", order.getUseYohoCoin());
            orderPromotioJSON.put("vip_cutdown_amount", order.getVipCutdownAmount());
            orderPromotioJSON.put("promotioncode_discount_amount", order.getPromotionCodeChargeResult().getDiscountAmount());
            orderPromotioJSON.put("use_red_envelopes", order.getUseRedEnvelopes());
            orderPromotioJSON.put("use_yoho_coin_shipping_cost", order.getYohoCoinShippingCost());  //添加有货币抵运费信息
            orderPromotionInfo.setOrderPromotion(orderPromotioJSON.toJSONString());
            orderPromotionInfoMapper.insert(orderPromotionInfo);

            logger.info("insert orderPromotionInfo success,uid is {},order code is {},do is {}",
                    order.getUid(), order.getOrderCode(), orderPromotionInfo);
        }

        private void addOrderExtAttribute(Order order) {
            //--------------------------order_ext_attribute--------------
            if (Constants.isLimitCodeChargeType(order.getChargeType())) {
                //限购码
                JSONObject json = new JSONObject();
                json.put(Constants.ORDER_EXTATTR_KEY_PRODUCT_SKU_LIST, order.getShoppingItems());
                orderExtAttributeDAO.insert(order.getOrderCode(), order.getUid(), json.toJSONString());

                logger.info("order type is limitcode, insert order_ext_attribute success,uid is {},order code is {},ext attribute is {}",
                        order.getUid(), order.getOrderCode(), json);
            }
        }

        private void addOrderMeta(Order order) {
            List<OrdersMeta> metaList = new ArrayList<>();

            OrdersMeta ordersMeta = buildVirtualMetaIfNecessary(order);
            if (ordersMeta != null) {
                metaList.add(ordersMeta);
            }

            ordersMeta = buildInviceIfNecessary(order);
            if (ordersMeta != null) {
                metaList.add(ordersMeta);
            }

            if (metaList.size() > 0) {
                ordersMetaDAO.insertBatch(metaList);
            }

            logger.info("insert orders_meta success,order code is {},order meta {}", order.getOrderCode(), metaList);
        }

        private OrdersMeta buildVirtualMetaIfNecessary(Order order) {
            VirtualInfo virtualInfo = order.getVirtualInfo();
            if (virtualInfo != null) {
                OrdersMeta ordersMeta = new OrdersMeta();
                ordersMeta.setOrdersId(order.getOrderId());
                ordersMeta.setMetaKey(OrdersMateKey.VIRTUAL_INFO);
                ordersMeta.setMetaValue(JSONObject.toJSONString(virtualInfo));

                return ordersMeta;
            }

            return null;
        }

        private OrdersMeta buildInviceIfNecessary(Order order) {
            //记录发票
            if (order.getInvoiceTypes() != null) {
                InvoiceBo invoiceBo = new InvoiceBo();
                invoiceBo.setOrderId(order.getOrderId());
                invoiceBo.setContent(order.getInvoiceContent());
                if (order.getInvoiceContent() != null && order.getInvoiceContent() > 0) {
                    invoiceBo.setContentValue(ShoppingConfig.INVOICE_CONTENT_MAP.get(order.getInvoiceContent()));
                }
                invoiceBo.setMobilePhone(order.getReceiverMobile());
                invoiceBo.setTitle(order.getInvoiceHeader());
                invoiceBo.setType(order.getInvoiceTypes());
                OrdersMeta ordersMeta = new OrdersMeta();
                ordersMeta.setOrdersId(invoiceBo.getOrderId());
                ordersMeta.setMetaKey(OrdersMateKey.ELECTRONIC_INVOICE);
                ordersMeta.setMetaValue(JSONObject.toJSONString(invoiceBo));

                return ordersMeta;
            }

            return null;
        }
    }
}

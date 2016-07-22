package com.yoho.yhorder.shopping.utils;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.service.model.order.response.shopping.ShoppingGoods;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.common.model.Coupon;
import com.yoho.yhorder.common.model.VirtualInfo;
import com.yoho.yhorder.common.utils.OrderPackageUtils;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.model.Order;
import com.yoho.yhorder.shopping.model.OrderGoods;
import com.yoho.yhorder.shopping.model.OrderReceiver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujiexiang on 16/5/23.
 */
public class OrderBuilder {

    private final Logger logger = LoggerFactory.getLogger("orderSubmitLog");

    private Order order = null;

    public OrderBuilder() {
        order = new Order();
    }

    public OrderBuilder addBasicInfo(ShoppingSubmitRequest request) {
        order.setUid(request.getUid());
        Integer paymentType = request.getPayment_type();
        order.setPaymentType(paymentType);

//        String saleChannel = request.getSale_channel();
//        Integer addressId = request.getAddress_id();
//
//        UserAddressRspBO userAddressRspBO = validateAndGetUserAddress(request.getUid(),addressId,saleChannel);
//        order.setUserAddressRspBO(userAddressRspBO);
//        OrderReceiver receiver = getOrderReceiver(userAddressRspBO);
//        receiver.setShippingManner(request.getDelivery_way());
//        if (isYohoodSite(saleChannel) && isInValidAddressId(addressId)) {
//            receiver.setAddress("上海市长宁区兴义路99号,YOHOOD现场");
//            receiver.setProvince("上海");
//            receiver.setCity("上海");
//            receiver.setDistrict("长宁区");
//        }
//        order.setReceiver(receiver);
//
//        //快递方式
//        order.setShippingTypeId(request.getDelivery_way());
//
//        //设置用户名
//        order.setUserName(truncateUserName(receiver.getConsigneeName()));

        /**
         *   'need_invoice' => $this->needInvoice,
         'invoice_type' => $this->invoiceType,
         'invoice_payable' => $this->invoicePayable,
         */


        Integer invoicesTypeId = request.getInvoices_type_id();
        if (invoicesTypeId == null) {//老版本
            invoicesTypeId = 0;
        }
        //电子发票
        if (request.getInvoices_type()!= null && request.getInvoices_type()>0){
            invoicesTypeId = request.getInvoice_content();
        }
        String invoicesTitle = request.getInvoices_title();
        String remark = request.getRemark();

        String needInvoice = Constants.NO_NEED_INVOICE_CHAR;
        if (invoicesTypeId > 0) {
            needInvoice = Constants.NEED_INVOICE_CHAR;
        }

        //引入电子发票的逻辑
        if (request.getInvoices_type()!= null && request.getInvoices_type()>0){
            needInvoice = Constants.NEED_INVOICE_CHAR;
        }

        order.setNeedInvoice(needInvoice);
        //发票内容
        order.setInvoiceType(invoicesTypeId);
        //发票类型，电子 纸质
        order.setInvoiceTypes(request.getInvoices_type());
        order.setInvoicePayable(truncateInvoicesTitle(invoicesTitle));
        order.setInvoiceHeader(request.getInvoices_title());
        order.setInvoiceContent(request.getInvoice_content());
        order.setReceiverMobile(request.getReceiverMobile());
        //remark备注信息中，还有表情，插入数据库中失败，现过滤这些。
        order.setRemark(truncateRemark(remark));

        String clientType = request.getClient_type();
        order.setClientType(clientType);
        order.setOrderType(ShoppingConfig.getOrderType(clientType));

        Integer receiptTime = request.getDelivery_time() == null ? 0 : request.getDelivery_time();
        order.setReceiptTime(receiptTime);

        //如果配送方式为现场自提自动加入活动
        if (request.getDelivery_way() == 3) {
            order.setActivitiesId(5);
        }

        //第三方联盟数据
        order.setUnionData(request.getQhy_union());

        //客户端ip
        order.setClientIP(request.getClient_ip());

        //order.setChargeType(request.getCharge_type());

        order.setIsPrintPrice(request.getIs_print_price());
        order.setIsPreContact(request.getIs_pre_contact());

        return this;
    }

    /**
     * 快递方式
     * @return
     */
    public OrderBuilder addDeliveryWay(ShoppingSubmitRequest request) {
        //快递方式
        Integer deliveryWay = request.getDelivery_way();
        order.setShippingTypeId(deliveryWay);
        return this;
    }

    public OrderBuilder addUserAddressRspBO(UserAddressRspBO userAddressRspBO) {
        order.setUserAddressRspBO(userAddressRspBO);
        return this;
    }

    public OrderBuilder addReceiver(OrderReceiver receiver) {

        order.setReceiver(receiver);

        //设置用户名
        order.setUserName(truncateUserName(receiver.getConsigneeName()));

        return this;
    }

    public OrderBuilder addGoodsList(ChargeContext chargeContext) throws ServiceException{
        List<OrderGoods> orderGoodsList = new ArrayList<>();
        ChargeParam cartOrder = chargeContext.getChargeParam();
        List<ChargeGoods> mainGoodsList = new ArrayList<ChargeGoods>();

        if (Constants.PRESALE_CART_TYPE.equals(cartOrder.getCartType())) {
            mainGoodsList = chargeContext.getMainGoods();
        } else if (Constants.ORDINARY_CART_TYPE.equals(cartOrder.getCartType())) {
            mainGoodsList.addAll(chargeContext.getMainGoods());
            mainGoodsList.addAll(chargeContext.getMainGoodsGift());
            mainGoodsList.addAll(chargeContext.getMainGoodsPriceGift());
            mainGoodsList.addAll(chargeContext.getOutletGoods());
        }

        if (CollectionUtils.isEmpty(mainGoodsList)) {
            // throw new Exception('订单商品不嫩为空.');
            logger.error("create local order failed in shopping_cart_submit, SHOPPING_ORDER_GOODS_IS_EMPTY , uid {}, order code {}, order {}",
                    order.getUid(), order.getOrderCode(), order);

            throw new ServiceException(ServiceError.SHOPPING_ORDER_GOODS_IS_EMPTY);
        }

        int totalYohoCoin = 0;
        for (ChargeGoods chargeGoods : mainGoodsList) {
            ShoppingGoods shoppingGoods = chargeGoods.getShoppingGoods();
            logger.debug("shopping goods is {}", shoppingGoods);
            OrderGoods orderGoods = new OrderGoods();
            if (!ShoppingConfig.ORDERGOODS_TYPE_TO_CODE_MAP.containsKey(shoppingGoods.getGoods_type())) {
                continue;
            }
            orderGoods.setGoods_type(ShoppingConfig.ORDERGOODS_TYPE_TO_CODE_MAP.get(shoppingGoods.getGoods_type()));
            orderGoods.setErp_sku_id(new Integer(shoppingGoods.getProduct_sku()));
            orderGoods.setGoods_price(new Double(shoppingGoods.getLast_price()));
            orderGoods.setGoods_amount(Double.parseDouble(shoppingGoods.getLast_price()) * Double.parseDouble(shoppingGoods.getBuy_number()));
            orderGoods.setNum(new Integer(shoppingGoods.getBuy_number()));
            if (shoppingGoods.getActivities_id() != null && shoppingGoods.getActivities_id() > 0) {
                order.setActivitiesId(shoppingGoods.getActivities_id());
            }
            orderGoods.setProduct_skn(new Integer(shoppingGoods.getProduct_skn()));
            orderGoods.setProduct_name(shoppingGoods.getProduct_name());
            orderGoods.setColor_id(new Integer(shoppingGoods.getColor_id()));
            orderGoods.setColor_name(shoppingGoods.getColor_name());
            orderGoods.setProduct_id(shoppingGoods.getProduct_id());
            orderGoods.setBrand_id(new Integer(shoppingGoods.getBrand_id()));
            orderGoods.setGoods_id(new Integer(shoppingGoods.getGoods_id()));
            orderGoods.setProduct_sku(new Integer(shoppingGoods.getProduct_sku()));
            orderGoods.setBuy_number(new Integer(shoppingGoods.getBuy_number()));
            orderGoods.setSize_id(new Integer(shoppingGoods.getSize_id()));
            orderGoods.setSize_name(shoppingGoods.getSize_name());
            orderGoods.setSales_price(shoppingGoods.getSales_price());
            orderGoods.setReal_price(shoppingGoods.getReal_price());
            orderGoods.setLast_price(new Double(shoppingGoods.getLast_price()));
            orderGoods.setGet_yoho_coin(new Integer(shoppingGoods.getGet_yoho_coin()));
            orderGoods.setVip_discount(shoppingGoods.getVip_discount());
            orderGoods.setReal_vip_price(shoppingGoods.getReal_vip_price());
            orderGoods.setVip_discount_money(shoppingGoods.getVip_discount_money());
            orderGoods.setIs_jit(shoppingGoods.getIs_jit());
            orderGoods.setShop_id(shoppingGoods.getShop_id());
            orderGoods.setSupplier_id(shoppingGoods.getSupplier_id());
            orderGoods.setBuyLimit(shoppingGoods.getBuy_limit());
            orderGoods.setSmallSortId(shoppingGoods.getSmall_sort_id());
            orderGoods.setMiddleSortId(shoppingGoods.getMiddle_sort_id());

            orderGoods.setDiscountPerSku(chargeGoods.getDiscountPerSku());

            orderGoods.setProduct_skc(MyStringUtils.string2int(shoppingGoods.getProduct_skc()));

            orderGoodsList.add(orderGoods);

            String shoppingCartGoodsId = shoppingGoods.getShopping_cart_goods_id();
            if (StringUtils.isEmpty(shoppingCartGoodsId)) {
                //不可能啊
                logger.warn("[{}] product sku {}, shopping cart item id is null", order.getUid(), shoppingGoods.getProduct_sku());
            } else {
                int _shoppingCartGoodsId = new Integer(shoppingCartGoodsId);
                if (_shoppingCartGoodsId > 0) {
                    //非购物车为0
                    order.getShoppingCartItemIds().add(_shoppingCartGoodsId);
                }
            }

            // 算此单总计赠送多少有货币
            totalYohoCoin += null == orderGoods.getGet_yoho_coin() ? 0 : orderGoods.getGet_yoho_coin();

            logger.debug("order goods is {}", orderGoods);
        }

        order.setDeliverYohoCoin(totalYohoCoin);	// 设置订单的总返回有货币

        order.setGoodsList(orderGoodsList);

        return this;
    }

    public OrderBuilder addShoppingItems(ChargeContext chargeContext) {
        order.setShoppingItems(chargeContext.getChargeParam().getShoppingItemList());

        return this;
    }

    public OrderBuilder addChargeResult(ChargeContext chargeContext) {
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        order.setUseYohoCoin(chargeTotal.getUseYohoCoin());
        order.setUseRedEnvelopes(chargeTotal.getUseRedEnvelopes());
        order.setUserLevel(chargeContext.getUserInfo().getUserLevel());
        order.setOrderAmount(chargeTotal.getOrderAmount());
        order.setLastOrderAmount(chargeTotal.getLastOrderAmount());
        order.setAmount(chargeTotal.getLastOrderAmount());
        order.setYohoCoinNum(chargeTotal.getUseYohoCoin());
        order.setYohoCoinRatio(chargeContext.getUserInfo().getOrderYohoCoin().ratio());
        //添加yoho币for运费
        order.setYohoCoinShippingCost(chargeTotal.getYohoCoinShippingCost());
        order.setDiscountAmount(chargeTotal.getDiscountAmount());
        order.setCouponUseAmount(chargeTotal.getCouponUseAmount());
        order.setVipCutdownAmount(chargeTotal.getVipCutdownAmount());
        Coupon coupon = new Coupon();
        coupon.setCoupon_id(chargeTotal.getCouponId());
        coupon.setCoupon_code(chargeTotal.getCouponCode());
        coupon.setCoupon_amount(chargeTotal.getCouponAmount());
        coupon.setCoupon_title(chargeTotal.getCouponTitle());

        order.setOrderCoupon(coupon);

        ChargeParam chargeParam = chargeContext.getChargeParam();
        order.setPaymentType(chargeParam.getPaymentType());
        // 暂时没用到   'payment' => $this->payment,
        if (chargeParam.getShippingManner() == 1) {
            //普通快递
            order.setShippingCost(chargeTotal.getShippingCost());
        } else {
            order.setShippingCost(chargeTotal.getShippingCost() + chargeTotal.getFastShoppingCost());
        }

        //ChargeParam chargeParam = chargeContext.getChargeParam();
        //产品属性,在sku分类的时候，根据sku属性会进行设置
        order.setAttribute(chargeParam.getAttribute());
        //JIT
        if (chargeParam.isJit()) {
            order.setIsJit(Constants.IS_JIT_STR);
        }

        //优惠码
        order.setPromotionCodeChargeResult(chargeTotal.getPromotionCodeChargeResult());

        order.setMustOnlinePayment(chargeTotal.isMustOnlinePayment());
        order.setMustOnlinePaymentReason(chargeTotal.getMustOnlinePaymentReason());

        order.setChargeType(chargeParam.getChargeTypeName());

        order.setIsAdvance(chargeParam.isPreSaleCart() ? "Y" : "N");

        order.setIsMultiPackage(OrderPackageUtils.canSplitSubOrder(chargeTotal.getPackageList().size()) ? "Y" : "N");

        order.setSubOrderNum(chargeTotal.getPackageList().size());

        return this;
    }


    /**
     * 使用的促销
     *
     * @param chargeContext
     * @return
     */
    public OrderBuilder addPromotionInfoList(ChargeContext chargeContext) {
        order.setPromotionInfoList(chargeContext.getChargeTotal().getPromotionInfoList());
        return this;
    }

    public OrderBuilder addVirtualInfo(VirtualInfo virtualInfo) {
        order.setVirtualInfo(virtualInfo);
        return this;
    }

    /**
     * 订单号
     *
     * @param orderCode
     * @return
     */
    public OrderBuilder addOrderCode(long orderCode) {
        order.setOrderCode(orderCode);
        return this;
    }

    public Order build() {
        return order;
    }


    /**
     * orders表中用户名字段长度为10，截取用户名称
     *
     * @param userName
     * @return
     */
    private String truncateUserName(String userName) {
        if (StringUtils.isNotEmpty(userName) && userName.length() > Constants.ORDERS_TABLE_USER_NAME_FIELD_LENGTH) {
            String userNameToUse = userName.substring(0, Constants.ORDERS_TABLE_USER_NAME_FIELD_LENGTH);
            logger.debug("username {} length over {},truncate result is {}", userName, Constants.ORDERS_TABLE_USER_NAME_FIELD_LENGTH, userNameToUse);
            return userNameToUse;
        }
        return userName;
    }

    /**
     * orders表中remark字段长度为255，不能还有表情
     *
     * @param remark
     * @return
     */
    private String truncateRemark(String remark) {
        return truncateString(remark, Constants.ORDERS_TABLE_REMARK_FIELD_LENGTH);
    }

    /**
     * 发票抬头
     *
     * @param title
     * @return
     */
    private String truncateInvoicesTitle(String title) {
        return truncateString(title, Constants.ORDERS_TABLE_INVOICES_PAYABLE_FIELD_LENGTH);
    }

    private String truncateString(String str, int length) {
        if (StringUtils.isNotEmpty(str)) {
            String filterStr = MyStringUtils.filterUtf8mb4String(str);
            if (StringUtils.isNotEmpty(filterStr) && filterStr.length() > length) {
                filterStr = filterStr.substring(0, length);
            }
            return filterStr;
        }
        return str;
    }

}

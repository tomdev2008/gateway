package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.request.BatchUpdateStorageRequest;
import com.yoho.product.request.UpdateStorageRequest;
import com.yoho.product.response.VoidResponse;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.request.YohoCurrencyReqBO;
import com.yoho.service.model.response.YohoCurrencyRspBO;
import com.yoho.yhorder.common.model.ERPOrder;
import com.yoho.yhorder.common.model.ERPOrderGoods;
import com.yoho.yhorder.common.model.ERPPromotion;
import com.yoho.yhorder.shopping.cache.StatCacheService;
import com.yoho.yhorder.shopping.model.*;
import com.yoho.yhorder.shopping.service.IOrderCreationService;
import com.yoho.yhorder.shopping.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by JXWU on 2015/12/5.
 */
@Component("erpOrderServiceImpl")
public class ErpOrderServiceImpl implements IOrderCreationService {

    private final Logger logger = LoggerFactory.getLogger("orderSubmitLog");

    @Value("${erp.order.create.url}")
    private String erpCreateOrderUrl;

    @Autowired
    private ServiceCaller serviceCaller;

    @Resource
    private StatCacheService statCacheService;

    @Override
    public void create(OrderCreationContext context) {

        logger.info("enter create erp order in shopping_cart_submit, uid {}, order code {}, \nuser info \n{}, \norder info \n{}\n ",
                context.getOrder().getUid(), context.getOrder().getOrderCode(),
                context.getUserInfo(), context.getOrder());

        //1.构建订单
        Integer uid = context.getOrder().getUid();
        String logPrefix = "user(" + uid + ")->submit->erp->order";

        ERPOrder erpOrder = bulidOrder(context);

        logger.info("after build erp order in create erp order of shopping_cart_submit, user id {}, order code {}, \nerp order \n{}\n ",
                context.getOrder().getUid(), context.getOrder().getOrderCode(), erpOrder);

        //2.校验订单
        checkOrder(erpOrder);
        //3.如某些字段值为null，则设置默认值
        // ############### 默认值的订单数据项 ##################
        ReflectTool.injectDefaultValue(erpOrder, OrderConfig.ERP_ORDER_DEFAULT_REQUEST_DATA_MAP);
        //4.提交订单
        JSONObject result = submit(logPrefix, erpOrder);

        //5.订单提交成功
        Order order = context.getOrder();
        if (order.getLastOrderAmount() == 0) {
            //订单已支付
            order.setPaymentStatus("Y");
        }
        /**
         *  /**
         * 修改订单状态
         * if ($package->lastOrderAmount == 0) {
         $package->paymentStatus = 1;
         }
         */

        //5.1更新yoho币 由erp后天向前台同步
        //updateYohoCurrency(result);
        //5.2更新库存

        updateProductStorage(result);
        logger.info("exit create erp order in shopping_cart_submit, user id {}, order code {}, result {} ",
                context.getOrder().getUid(), context.getOrder().getOrderCode(), result);
    }

    /**
     * 更新用户yoho
     *
     * @param result
     */
    private void updateYohoCurrency(JSONObject result) {
        Integer uid = result.getJSONObject("data").getInteger("uid");
        String yohoCurrency = result.getJSONObject("data").getString("yoho_currency");
        logger.info("update user {} yoho currency to {}", uid, yohoCurrency);
        YohoCurrencyReqBO yohoCurrencyReqBO = new YohoCurrencyReqBO();
        yohoCurrencyReqBO.setUid(uid);
        yohoCurrencyReqBO.setCurrency(yohoCurrency);
        serviceCaller.call(ShoppingConfig.USERS_UPDATE_YOHOCURRENCY_REST_URL, yohoCurrencyReqBO, YohoCurrencyRspBO.class);

    }

    private void updateProductStorage(JSONObject result) {

        try {

            JSONObject productStorageJSON = result.getJSONObject("data").getJSONObject("product_storage");
            if (productStorageJSON != null) {
                Set<String> skus = productStorageJSON.keySet();
                List<UpdateStorageRequest> updateStorageRequestList = new ArrayList<>();
                for (String sku : skus) {
                    UpdateStorageRequest storageRequest = new UpdateStorageRequest();
                    storageRequest.setSkuId(new Integer(sku));
                    storageRequest.setStorageNum(productStorageJSON.getInteger(sku));
                    updateStorageRequestList.add(storageRequest);
                }
                if (CollectionUtils.isNotEmpty(updateStorageRequestList)) {
                    BatchUpdateStorageRequest batchUpdateStorageRequest = new BatchUpdateStorageRequest();
                    batchUpdateStorageRequest.setUpdateStorageRequest(updateStorageRequestList);
                    VoidResponse response = serviceCaller.call(ShoppingConfig.PRODUCT_BATCHUPDATE_STORAGEBYSKUID_REST_URL, batchUpdateStorageRequest, VoidResponse.class);
                    logger.info("update product storage success in shopping_cart_submit, call service {}, request {}, result {}",
                            ShoppingConfig.PRODUCT_BATCHUPDATE_STORAGEBYSKUID_REST_URL,
                            updateStorageRequestList,
                            (response.getCode() == 200 ? "success" : "faild"));
                }
            }
        } catch (Exception e) {
            logger.error("update storage failed with exception. json: {}", result, e);
        }
    }

    protected ERPOrder bulidOrder(OrderCreationContext context) {
        ERPOrder erpOrder = new ERPOrder();
        Order order = context.getOrder();
        //添加erp订单主要字段
        copyOrderMandatoryField(erpOrder, order);
        //添加促销信息
        erpOrder.setFit_promotions(appendOrderPromotions(order.getOrderCode(), order.getPromotionInfoList()));
        //添加商品列表
        erpOrder.setGoods_list(appendOrderGoodsList(order));
        return erpOrder;
    }

    protected void checkOrder(ERPOrder erpOrder) {
        if (CollectionUtils.isEmpty(erpOrder.getGoods_list())) {
            //throw new Exception('缺少提交ERP订单商品.', 500);
            throw new ServiceException(ServiceError.SHOPPING_ERP_ORDER_GOODS_IS_EMPTY);
        }

        // ############### 必选的订单数据项 ##################
        for (String key : OrderConfig.ERP_ORDER_MUST_HAVE_DATA_FIELDS) {
            //throw new Exception('提交ERP缺少' . $key . '字段.', 500);
            // MyAssert.isNull(StringUtils.generateGetterOrSetter("get", key), erpOrder, 500, "提交ERP缺少缺少字段" + key, ServiceError.SHOPPING_BUSCHECK_ERP_ORDER_MISSING_FIELDS);
            MyAssert.isNull(MyStringUtils.generateGetterOrSetter("get", key), erpOrder, ServiceError.SHOPPING_BUSCHECK_ERP_ORDER_MISSING_FIELDS);
        }

        // ############### 订单商品数据项 ##################
        for (ERPOrderGoods goods : erpOrder.getGoods_list()) {
            for (String key : OrderConfig.ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS) {
                // throw new Exception('提交ERP订单商品缺少字段,' . $goodsKey, 500);
                MyAssert.isNull(MyStringUtils.generateGetterOrSetter("get", key), goods, ServiceError.SHOPPING_BUSCHECK_ERP_ORDER_GOODS_MISSING_FIELDS);
            }
        }

        //  ############### 快递信息数据项 ##################
        for (String key : OrderConfig.ERP_ORDER_RECEIVER_MUST_DATA_FIELDS) {
            //throw new Exception('配送信息缺少字段' . $key, 500);
            Object value = MyAssert.isNull(MyStringUtils.generateGetterOrSetter("get", key), erpOrder, ServiceError.SHOPPING_BUSCHECK_ERP_ORDER_RECEIVER_MISSING_FIELDS);
            erpOrder.getReceiver_info().put(key, value);
        }

        // ############### 促销数据项 ##################
        if (CollectionUtils.isNotEmpty(erpOrder.getFit_promotions())) {
            for (ERPPromotion promotion : erpOrder.getFit_promotions()) {
                for (String key : OrderConfig.ERP_ORDER_FIT_PROMOTIONS_MUST_HAVE_DATA_FIELDS) {
                    // throw new Exception('提交ERP促销数据错误.', 500);
                    MyAssert.isNull(MyStringUtils.generateGetterOrSetter("get", key), promotion, ServiceError.SHOPPING_ERP_ORDER_PROMOTION_MISSING_FIELDS);
                }
            }
        }
    }


    public List<ERPOrderGoods> appendOrderGoodsList(Order order) {
        List<ERPOrderGoods> erpOrderGoodsList = new ArrayList<ERPOrderGoods>();
        List<OrderGoods> goodsList = order.getGoodsList();
        if (CollectionUtils.isNotEmpty(goodsList)) {
            for (OrderGoods goods : goodsList) {
                ERPOrderGoods erpOrderGoods = new ERPOrderGoods();
                erpOrderGoods.setProduct_skn(goods.getProduct_skn());
                erpOrderGoods.setProduct_name(goods.getProduct_name());
                erpOrderGoods.setColor_id(goods.getColor_id());
                erpOrderGoods.setColor_name(goods.getColor_name());
                erpOrderGoods.setProduct_id(goods.getProduct_id());
                erpOrderGoods.setBrand_id(goods.getBrand_id());
                erpOrderGoods.setGoods_id(goods.getGoods_id());
                erpOrderGoods.setErp_sku_id(goods.getProduct_sku());
                erpOrderGoods.setProduct_sku(goods.getProduct_sku());
                erpOrderGoods.setBuy_number(goods.getBuy_number());
                erpOrderGoods.setNum(goods.getBuy_number());
                erpOrderGoods.setSize_id(goods.getSize_id());
                erpOrderGoods.setSize_name(goods.getSize_name());
                erpOrderGoods.setSale_price(goods.getSales_price());
                erpOrderGoods.setReal_price(goods.getReal_price());
                erpOrderGoods.setLast_price(goods.getLast_price());
                erpOrderGoods.setGet_yoho_coin(goods.getGet_yoho_coin());
                erpOrderGoods.setVip_discount(goods.getVip_discount());
                erpOrderGoods.setReal_vip_price(goods.getReal_vip_price());
                erpOrderGoods.setVip_discount_money(goods.getVip_discount_money());
                erpOrderGoods.setGoods_type(goods.getGoods_type());
                erpOrderGoods.setIs_jit(goods.getIs_jit());
                erpOrderGoods.setShop_id(goods.getShop_id());
                erpOrderGoods.setSupplier_id(goods.getSupplier_id());

                //每件sku分摊的优惠券
                erpOrderGoods.setCoupons_per(goods.getDiscountPerSku().couponsAmount);
                //每件sku分摊的优惠码
                erpOrderGoods.setPromo_code_per(goods.getDiscountPerSku().promotionCodeAmount);
                //每件sku分摊的yoho
                erpOrderGoods.setYoho_coin_per(goods.getDiscountPerSku().yohoCoinNum);
                //每件sku分摊的红包
                erpOrderGoods.setRed_envelope_per(goods.getDiscountPerSku().redEnvelopeAmount);

                erpOrderGoods.setProduct_skc(goods.getProduct_skc());

                erpOrderGoodsList.add(erpOrderGoods);
            }
        }
        return erpOrderGoodsList;
    }

    private List<ERPPromotion> appendOrderPromotions(Long orderCode, List<PromotionBO> promotionInfoList) {
        List<ERPPromotion> fit_promotions = new ArrayList<ERPPromotion>();
        if (CollectionUtils.isNotEmpty(promotionInfoList)) {
            for (PromotionBO bo : promotionInfoList) {
                ERPPromotion promotion = new ERPPromotion();
                promotion.setOrder_code(orderCode);
                promotion.setPromotion_id(bo.getPromotion_id());
                promotion.setCutdown_amount(bo.getCutdown_amount());
                promotion.setPromotion_title(bo.getPromotion_title());
                fit_promotions.add(promotion);
            }
        }
        return fit_promotions;
    }

    private void copyOrderMandatoryField(ERPOrder erpOrder, Order order) {
        erpOrder.setRedenvelopesnum(order.getUseRedEnvelopes());
        erpOrder.setOrder_code(order.getOrderCode());
        erpOrder.setUid(order.getUid());
        erpOrder.setUser_level(order.getUserLevel());
        erpOrder.setOrder_amount(order.getOrderAmount());
        erpOrder.setLast_order_amount(order.getLastOrderAmount());
        erpOrder.setAmount(order.getAmount());
        erpOrder.setOrder_type(order.getOrderType());
        erpOrder.setNeed_invoice(order.getNeedInvoice());
        erpOrder.setInvoice_type(order.getInvoiceType());
        erpOrder.setInvoice_types(order.getInvoiceTypes());
        erpOrder.setInvoice_payable(order.getInvoicePayable());

        //有货币
        double yohoCoin4Shipping=order.getYohoCoinShippingCost();   //有货币分摊运费
        double yohoCoinAll=order.getYohoCoinNum().doubleValue(); //总共使用有货币

        //添加yoho币for运费
        erpOrder.setYohocoin_shipping_cost(YHMath.mul(yohoCoin4Shipping, order.getYohoCoinRatio()));
        //yoho币被稀释成货币，提交给erp需要转换为yoho币，目前是关闭，等erp完成后只需要完成ratio
        erpOrder.setYoho_coin_num(YHMath.mul(yohoCoinAll, order.getYohoCoinRatio()));

        //yoho币被稀释成货币，提交给erp需要转换为yoho币
        erpOrder.setUse_yoho_coin(YHMath.mul(yohoCoinAll, order.getYohoCoinRatio()));

        erpOrder.setOrders_coupons(order.getOrderCoupon());
        erpOrder.setCoupon_id(order.getOrderCoupon().getCoupon_id());
        erpOrder.setCoupon_code(order.getOrderCoupon().getCoupon_code());
        erpOrder.setCoupon_amount(order.getOrderCoupon().getCoupon_amount());
        erpOrder.setCoupon_title(order.getOrderCoupon().getCoupon_title());
        erpOrder.setPayment_type(order.getPaymentType());
        erpOrder.setShipping_cost(order.getShippingCost());
        erpOrder.setReceipt_time(order.getReceiptTime());

        erpOrder.setReceipt_time_type(order.getReceiptTime());//送货时间
        //erpOrder.setReceipt_time_type(order.getReceiptTimeType());
        erpOrder.setOrder_referer(order.getOrderReferer());
        erpOrder.setRemark(order.getRemark());
        erpOrder.setIs_print_price(order.getIsPrintPrice());
        erpOrder.setIs_contact(order.getIsContact());
        erpOrder.setIs_need_rapid(order.getIsNeedRapid());
        erpOrder.setAttribute(order.getAttribute());
        erpOrder.setActivities_id(order.getActivitiesId());
        erpOrder.setConsignee_name(order.getReceiver().getConsigneeName());
        erpOrder.setPhone(order.getReceiver().getPhone());
        erpOrder.setMobile(order.getReceiver().getMobile());
        erpOrder.setProvince(order.getReceiver().getProvince());
        erpOrder.setCity(order.getReceiver().getCity());
        erpOrder.setDistrict(order.getReceiver().getDistrict());
        erpOrder.setAddress(order.getReceiver().getAddress());
        erpOrder.setZip_code(order.getReceiver().getZipCode());
        erpOrder.setEmail(order.getReceiver().getEmail());
        erpOrder.setShipping_manner(order.getReceiver().getShippingManner());
        erpOrder.setArea_code(order.getReceiver().getAreaCode());
        erpOrder.setIs_jit(order.getIsJit());

        //优惠码
        erpOrder.setPromo_id(order.getPromotionCodeChargeResult().getPromotionId());
        erpOrder.setPromo_code(order.getPromotionCodeChargeResult().getPromotionCode());
        erpOrder.setPromo_code_amount(order.getPromotionCodeChargeResult().getDiscountAmount());
        erpOrder.setPromo_code_discount(order.getPromotionCodeChargeResult().getDiscount());

        erpOrder.setIs_multi_package(order.getIsMultiPackage());
        erpOrder.setSub_order_num(order.getSubOrderNum());
    }


    /**
     * 提交给ERP 订单创建服务
     *
     * @param logPrefix
     * @param erpOrder
     * @return
     */
    private JSONObject submit(String logPrefix, ERPOrder erpOrder) {
        boolean failed = false;
        JSONObject responseJsonObject = null;
        Exception exception = null;
        String responseText = null;
        //构造list
        String mobile = erpOrder.getMobile();
        erpOrder.hideMobile(mobile);

        ERPOrder[] array = new ERPOrder[]{erpOrder};
        String jsonForLog = JSON.toJSONString(array);
        try {
            logger.info("submit order to erp in shopping_cart_submit, uid {}, order code {}, \nsubmit info \n{}\n ",
                    erpOrder.getUid(), erpOrder.getOrder_code(), jsonForLog);


            erpOrder.unHideMobile(mobile);
            String json = JSON.toJSONString(array);

            LinkedMultiValueMap<String, Object> request = new LinkedMultiValueMap<>();
            request.add("data", json);


            responseText = this.serviceCaller.post("erp.submitOrder", this.erpCreateOrderUrl, request, String.class,null).get();

            responseJsonObject = JSON.parseObject(responseText);
            /**erp order create 返回结果格式
             * {"code":200,"message":"\u521b\u5efa\u8ba2\u5355\u5b8c\u6210.","data":{"order_codes":[201508591188],"product_storage":{"646612":14},"yoho_currency":null,"uid":3237043},"md5":"eb57cd927f436c4b6f3fe9e5aaf5ddc5"}
             */
            if (responseJsonObject.getIntValue("code") != 200) {
                //返回code不为200，erp order校验不通过。
                failed = true;
            }

            //code == 900, 库存不足，直接更新库存
            if (responseJsonObject.getIntValue("code") == 900) {
                this.updateProductStorage(responseJsonObject);
            }

        } catch (Exception ex) {
            exception = ex;

            /**
             * 打印异常堆栈
             */
            logger.error("exception happened when submit order to erp in shopping_cart_submit, uid {}, order code {},responseText {}",
                    erpOrder.getUid(), erpOrder.getOrder_code(), responseText, ex);
        }
        if (failed || (exception != null)) {

            logger.error("submit order to erp failed in shopping_cart_submit, uid {}, order code {}, result info {}, submit info {}, erp order {} ",
                    erpOrder.getUid(), erpOrder.getOrder_code(), responseJsonObject, jsonForLog, erpOrder);

            if (failed) {
                //erp order校验不通过。
                String message = responseJsonObject.getString("message");
                message = message == null ? "订单创建失败,请稍后再试" : message;
                //erp校验次数+1
                statCacheService.incrementErpSubmit(responseJsonObject.getString("code"));

                ServiceException se =  new ServiceException(ServiceError.SHOPPING_BUSCHECK_ERP_ORDER_CREATION_RETURN_CODE_NOT_200);
                se.setParams(message);
                throw se;
                // throw new BusinessCheckException(500, jsonObject.getString("message"), ServiceError.SHOPPING_BUSCHECK_ERP_ORDER_CREATION_RETURN_CODE_NOT_200);
            }
            //失败次数+1
            statCacheService.incrementErpSubmit("timeout");
            //throw new Exception('创建订单失败,请稍后再试.');
            throw new ServiceException(ServiceError.SHOPPING_ERP_ORDER_CREATION_ERROR);

        }

        //成功+1
        statCacheService.incrementErpSubmit("success");

        logger.info("submit order to erp success in shopping_cart_submit, uid {}, order code {}",
                erpOrder.getUid(), erpOrder.getOrder_code());
        return responseJsonObject;
    }

}

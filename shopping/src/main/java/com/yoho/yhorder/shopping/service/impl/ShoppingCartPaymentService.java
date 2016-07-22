package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.service.model.order.model.DeliveryAddressBO;
import com.yoho.service.model.order.model.OrderDefaultPreferencesBO;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.shopping.*;
import com.yoho.service.model.response.AreaRspBo;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.common.utils.OrderPackageUtils;
import com.yoho.yhorder.common.utils.PrivacyUtils;
import com.yoho.yhorder.dal.IOrderDefaultPreferencesDAO;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.model.Gate;
import com.yoho.yhorder.dal.model.OrderDefaultPreferences;
import com.yoho.yhorder.shopping.cache.GateCacheService;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.model.PaymentSetting;
import com.yoho.yhorder.shopping.service.ExternalDegradeService;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2015/11/24.
 */
@Service
public class ShoppingCartPaymentService {

    private final Logger logger = LoggerFactory.getLogger("addPaymentComputeLog");

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private GateCacheService gateCacheManager;

    @Autowired
    protected IOrderDefaultPreferencesDAO orderDefaultPreferencesMapper;

    @Autowired
    private ChargeContextFactory changeContextFactory;

    @Autowired
    private ChargerService chargerService;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private ExternalDegradeService externalDegradeService;
    
    @Autowired
    private ShoppingCartRedis shoppingCartRedis;


    public ShoppingPaymentResponse payment(ShoppingCartRequest request) {
        logger.info("enter shopping cart payment service,request is {}", request);

        int uid = request.getUid();
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), request.getShopping_key());
        
        //1.获取订单默认支付、快递方式、邮寄地址、发票设置，用户在订单确认可以修改这些设置。
        PaymentSetting defaultPaymentSetting = getDefaultPaymentSetting(uid);

        //2.算费
        //2.1新建算费参数
        ChargeParam chargeParam = newPaymentChargeParam(defaultPaymentSetting.getShippingManner(), request);
        //2.2新建普通商品算费context，context中包括需要算费的sku、用户信息等
        //查询数据库表shopping_cart_items的自选sku
        ChargeContext chargeContext = changeContextFactory.build(true, chargeParam);

        //2.2.1 默认使用多少yoho币
        chargeParam.setUseYohoCoin(chargeContext.getUserInfo().getRatedYohoCoinFor(request.getYoho_coin_mode()));

        //2.2.2用户有红包可用则直接使用
        if (canUseRedEnvelopes(request.getEnabled_RedEnvelopes())) {
            //设置用户可用的红包
            double userRedEnvelopes = chargeContext.getUserInfo().getRedEnvelopes();
            if (userRedEnvelopes > 0) {
                chargeParam.setUseRedEnvelopes(userRedEnvelopes);
            }
        }
        //2.3算费，算费结果在context对象的ChargeTotal中。
        chargerService.charge(chargeContext);

        //3.根据算费结果修改支付、快递设置
        resetPaymentTypeSetting(chargeContext, defaultPaymentSetting);

        //4.构造返回结果
        ShoppingPaymentResponse response = getShoppingPaymentResponse(request.getEnabled_RedEnvelopes(),defaultPaymentSetting, chargeContext);

        logger.info("exit shopping cart payment service,## request is: {}, ##　Shopping_cart_data is: {} ## payment_way is:{}\n",
                request, response.getShopping_cart_data(),response.getPayment_way());

        return response;

    }

    /**
     * 返回payment结果
     *
     * @param defaultPaymentSetting
     * @param chargeContext
     * @return
     */
    private ShoppingPaymentResponse getShoppingPaymentResponse(int enabledRedEnvelopes,PaymentSetting defaultPaymentSetting, ChargeContext chargeContext) {

        ChargeParam chargeParam = chargeContext.getChargeParam();
        int uid = chargeParam.getUid();

        ShoppingChargeResult shoppingChargeResult = chargeContext.getChargeResult();

        ShoppingPaymentResponse paymentResponse = new ShoppingPaymentResponse();
        boolean canUseRedEnvelopes = canUseRedEnvelopes(enabledRedEnvelopes) && chargeParam.getChargeType().usingRedEnvelopes();
        if (canUseRedEnvelopes) {
            paymentResponse = new ShoppingPaymentResponseWithRedEnvelopes();
        }
        paymentResponse.setShopping_cart_tag(chargeParam.getShoppingTag());

        paymentResponse.setDelivery_address(defaultPaymentSetting.getDeliveryAddress());

        //模糊手机号码
        DeliveryAddressBO deliveryAddressBO = paymentResponse.getDelivery_address();
        if (deliveryAddressBO != null) {
            deliveryAddressBO.setMobile(PrivacyUtils.mobile(deliveryAddressBO.getMobile()));
        }

        paymentResponse.setGoods_list(shoppingChargeResult.getGoods_list());


        paymentResponse.setUid(uid);

        ShoppingChargeTotal shoppingChargeTotal = shoppingChargeResult.getShopping_cart_data();

        shoppingChargeTotal.setPackage_list(chargeContext.getChargeTotal().getPackageList());

        shoppingChargeTotal.setIs_multi_package(OrderPackageUtils.canSplitSubOrder(shoppingChargeTotal.getPackage_list().size()) ? "Y" : "N");

        paymentResponse.setShopping_cart_data(shoppingChargeTotal);

        //从默认设置中获取发票信息
        paymentResponse.setInvoices(defaultPaymentSetting.getShoppingInvoice());

        //设置订单支付方式
        paymentResponse.setPayment_way(getPaymentWay(defaultPaymentSetting));
        //快递方式
        paymentResponse.setDelivery_way(getDeliveryWay(shoppingChargeResult.getShopping_cart_data(), defaultPaymentSetting));
        //收货时间get
        paymentResponse.setDelivery_time(getDeliveryTime(defaultPaymentSetting));

        //设置用户可用的yoho币
        paymentResponse.setYoho_coin(chargeContext.getUserInfo().getOrderYohoCoin().ratedYohoCoin());
        //默认使用掉的yoho币
        paymentResponse.setUse_yoho_coin(chargeContext.getChargeTotal().getUseYohoCoin());

        if(canUsePromotionCode() && chargeParam.getChargeType().usingPromotionCode())
        {
            //可以使用优惠码
            paymentResponse.setPromo_code_enabled(1);
        }

        //红包使用开关
        if (canUseRedEnvelopes) {
            ShoppingPaymentResponseWithRedEnvelopes paymentResponseWithRedEnvelopes = (ShoppingPaymentResponseWithRedEnvelopes) paymentResponse;
            //设置用户可用的红包
            double userRedEnvelopes = chargeContext.getUserInfo().getRedEnvelopes();
            if (userRedEnvelopes > 0) {
                paymentResponseWithRedEnvelopes.setRed_envelopes(userRedEnvelopes);
            }
            //默认使用掉的红包
            paymentResponseWithRedEnvelopes.setUse_red_envelopes(chargeContext.getChargeTotal().getUseRedEnvelopes());
            //使用红包
            paymentResponseWithRedEnvelopes.setEnabledRedEnvelopes(1);
            return paymentResponseWithRedEnvelopes;
        }

        return paymentResponse;
    }

    /**
     * 根据用户等级、算费结果修改支付、快递默认设置
     */
    public void resetPaymentTypeSetting(ChargeContext chargeContext, PaymentSetting defaultPaymentSetting) {
        //如果是白金会员并且支持加急，则快递默认顺丰
        if (chargeContext.getUserInfo().getUserLevel() > 2 && "Y".equals(defaultPaymentSetting.getRapidExpressSupport())) {
            defaultPaymentSetting.setShippingManner(2);
        }
        ChargeTotal chargeTotal = chargeContext.getChargeTotal();
        //################### 判断订单金额是否可以货到付款 or 是否必须在线支付 ###################
        if (chargeTotal.getLastOrderAmount() > Constants.MAX_COD_AMOUNT || chargeTotal.isMustOnlinePayment()) {
            //不支持货到付款
            defaultPaymentSetting.setCodPaymentSupport("N");
            //在线支付
            defaultPaymentSetting.setOnlinePayment("Y");
            //不能货到付款 Cash on delivery
            defaultPaymentSetting.setCod("N");
        }

        // ################### 是否必须在线支付 ###################
//        if (chargeTotal.isMustOnlinePayment()) {
//            //普通快递
//            defaultPaymentSetting.setShippingManner(1);
//            //不支持加急快递
//            defaultPaymentSetting.setRapidExpressSupport("N");
//            //不支持上门换货
//            defaultPaymentSetting.setRapidExpressDelivery("N");
//        }

        //################### 预售商品不能货到付款 ###################
        if (Constants.PRESALE_CART_TYPE.equals(chargeContext.getChargeParam().getCartType())) {
            //不支持货到付款
            defaultPaymentSetting.setCodPaymentSupport("N");
            //在线支付
            defaultPaymentSetting.setOnlinePayment("Y");
            //不支持货到付款
            defaultPaymentSetting.setCod("N");
        }

        defaultPaymentSetting.setCodPaymentSupportMessage(chargeTotal.getMustOnlinePaymentReason());
    }

    /**
     * 新建一个购物车payment订单
     *
     * @return
     */
    private ChargeParam newPaymentChargeParam(int shippingManer, final ShoppingCartRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setCartType(request.getCart_type());
        chargeParam.parseProductSkuListParameterAndSetupChargeType(request.getCart_type(),request.getProduct_sku_list());
        chargeParam.setNeedCalcShippingCost(true);
        chargeParam.setUid(request.getUid());
        chargeParam.setShippingManner(shippingManer);
        chargeParam.setSelected(true);
        chargeParam.setUserAgent(request.getUser_agent());



        return chargeParam;
    }

    /**
     * 支付方式
     *
     * @param paymentSetting
     * @return
     */
    public List<Map<String, Object>> getPaymentWay(PaymentSetting paymentSetting) {
        //payment_way
        List<Map<String, Object>> paymentWayList = new ArrayList<Map<String, Object>>();
        Map<String, Object> paymentWayMap = new HashMap<String, Object>();
        paymentWayMap.put("payment_id", 15);
        paymentWayMap.put("payment_type", 1);
        paymentWayMap.put("payment_type_name", "在线支付(推荐)");
        paymentWayMap.put("payment_name", "支付宝");
        paymentWayMap.put("default", paymentSetting.getOnlinePayment());
        paymentWayMap.put("is_support", "Y");
        paymentWayMap.put("is_support_message", "");
        paymentWayList.add(paymentWayMap);

        paymentWayMap = new HashMap<>();
        paymentWayMap.put("payment_id", 0);
        paymentWayMap.put("payment_type", 2);
        paymentWayMap.put("payment_type_name", "货到付款");
        paymentWayMap.put("payment_name", "货到付款");
        paymentWayMap.put("default", paymentSetting.getCod());
        paymentWayMap.put("is_support", paymentSetting.getCodPaymentSupport());
        paymentWayMap.put("is_support_message", paymentSetting.getCodPaymentSupportMessage());
        paymentWayList.add(paymentWayMap);

        return paymentWayList;
    }

    /**
     * 快递方式
     *
     * @param chargeTotal
     * @param paymentSetting
     * @return
     */
    public List<Map<String, Object>> getDeliveryWay(ShoppingChargeTotal chargeTotal, PaymentSetting paymentSetting) {
        List<Map<String, Object>> deliveryWayList = new ArrayList<Map<String, Object>>();
        Map<String, Object> deliveryWayMap = new HashMap<>();
        deliveryWayMap.put("delivery_way_id", 1);
        deliveryWayMap.put("delivery_way_name", "普通快递");
        deliveryWayMap.put("delivery_way_cost", chargeTotal.getShipping_cost());
        deliveryWayMap.put("default", paymentSetting.getShippingManner() == 1 ? 'Y' : 'N');
        deliveryWayMap.put("is_support", "Y");
        deliveryWayMap.put("is_delivery", "Y");
        deliveryWayList.add(deliveryWayMap);

        deliveryWayMap = new HashMap<>();
        deliveryWayMap.put("delivery_way_id", 2);
        deliveryWayMap.put("delivery_way_name", "顺丰速运");
        deliveryWayMap.put("delivery_way_cost", chargeTotal.getFast_shopping_cost());
        deliveryWayMap.put("default", paymentSetting.getShippingManner() == 2 ? 'Y' : 'N');
        deliveryWayMap.put("is_support", paymentSetting.getRapidExpressSupport());
        deliveryWayMap.put("is_delivery", paymentSetting.getRapidExpressDelivery());
        deliveryWayList.add(deliveryWayMap);

        return deliveryWayList;
    }

    /**
     * 收货日期
     *
     * @return
     */
    private List<Map<String, Object>> getDeliveryTime(PaymentSetting paymentSetting) {
        List<Map<String, Object>> deliveryTimeList = new ArrayList<Map<String, Object>>();
        Map<String, Object> deliveryTimeMap = new HashMap<>();
        deliveryTimeMap.put("delivery_time_id", 1);
        deliveryTimeMap.put("delivery_time_string", "只工作日送货");
        deliveryTimeMap.put("default", "1".equals(paymentSetting.getReceivingTime()) ? "Y" : "N");
        deliveryTimeList.add(deliveryTimeMap);

        deliveryTimeMap = new HashMap<>();
        deliveryTimeMap.put("delivery_time_id", 2);
        deliveryTimeMap.put("delivery_time_string", "工作日、双休日和节假日均送货");
        deliveryTimeMap.put("default", "2".equals(paymentSetting.getReceivingTime()) ? "Y" : "N");
        deliveryTimeList.add(deliveryTimeMap);

        deliveryTimeMap = new HashMap<>();
        deliveryTimeMap.put("delivery_time_id", 3);
        deliveryTimeMap.put("delivery_time_string", "只双休日、节假日送货(工作日不送货)");
        deliveryTimeMap.put("default", "3".equals(paymentSetting.getReceivingTime()) ? "Y" : "N");
        deliveryTimeList.add(deliveryTimeMap);

        return deliveryTimeList;

    }

    /**
     * 获取用户订单支付、快递、发票等默认设置
     *
     * @param uid
     * @return
     */
    public PaymentSetting getDefaultPaymentSetting(int uid) {
        PaymentSetting paymentSetting = new PaymentSetting();
        //1.设置支付方式、快递方式
        //查询订单order_default_preferences配置
        Orders userLastOrder = null;
        OrderDefaultPreferencesBO orderDefaultDeliveryAddressBO = getOrderDefaultDeliveryAddress(uid);
        if (orderDefaultDeliveryAddressBO != null) {
            paymentSetting.setOnlinePayment("1".equals(orderDefaultDeliveryAddressBO.getPayment_type()) ? "Y" : "N");//在线支付
            paymentSetting.setCod("2".equals(orderDefaultDeliveryAddressBO.getPayment_type()) ? "Y" : "N");//货到付款
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(orderDefaultDeliveryAddressBO.getShipping_manner())) {
                paymentSetting.setShippingManner(Integer.parseInt(orderDefaultDeliveryAddressBO.getShipping_manner()));//配送方式 1快递  2加急
            }
            paymentSetting.setReceivingTime(orderDefaultDeliveryAddressBO.getReceiving_time());
        } else {
            //查询用户最后一个订单
            userLastOrder = getUserLastOrder(uid);
            if (userLastOrder != null) {

                paymentSetting.setOnlinePayment("1".equals(userLastOrder.getPaymentType()) ? "Y" : "N");//在线支付
                paymentSetting.setCod("2".equals(userLastOrder.getPaymentType()) ? "Y" : "N");//货到付款
                paymentSetting.setShippingManner(userLastOrder.getShippingTypeId());//配送方式 1快递  2加急
                paymentSetting.setReceivingTime(userLastOrder.getReceivingTime() == null ? "0" : String.valueOf(userLastOrder.getReceivingTime()));
            }
        }
        //2.设置发票信息
        ShoppingInvoice shoppingInvoice = new ShoppingInvoice();
        Integer invoiceTypeId = 0;
        String invoicesTypeName = "";
        String invoicesTitle = "";
        if (userLastOrder != null) {
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(userLastOrder.getInvoicesType())) {
                invoiceTypeId = new Integer(userLastOrder.getInvoicesType());
            }
            invoicesTypeName = ShoppingConfig.INVOICES_TYPE_MAP.containsKey(invoiceTypeId) ? ShoppingConfig.INVOICES_TYPE_MAP.get(invoiceTypeId) : "";
            invoicesTitle = userLastOrder.getInvoicesPayable();
        }
        shoppingInvoice.setInvoices_type_id(invoiceTypeId);
        shoppingInvoice.setInvoices_type_name(invoicesTypeName);
        shoppingInvoice.setInvoices_title(invoicesTitle);
        shoppingInvoice.setInvoices_type_list(ShoppingConfig.INVOICES_TYPE_LIST);
        shoppingInvoice.setInvoiceContentList(ShoppingConfig.INVOICE_CONTENT_LIST);
        paymentSetting.setShoppingInvoice(shoppingInvoice);

        //3.设置快递地址
        //3.1查询用户设置默认地址
        DeliveryAddressBO userAddressBO = getUserDefaultAddress(uid);

        /**
         * 用户设置了默认地址，就使用默认地址，
         * 没有则判断上一次订单地址，
         * 上一次订单地址没有失效，则使用，
         * 否则使用用户设置的一个地址
         */

        if (userAddressBO != null && "Y".equals(userAddressBO.getIs_default())) {
            //用户设置的默认地址
            paymentSetting.setDeliveryAddress(userAddressBO);
        } else {
            boolean deliveryAddressOK = false;
            if (orderDefaultDeliveryAddressBO != null && orderDefaultDeliveryAddressBO.getDelivery_address() != null) {
                //2.2用户没有设置默认的地址
                if (orderDefaultDeliveryAddressBO.getDelivery_address().getAddress_id() != null) {
                    paymentSetting.setDeliveryAddress(orderDefaultDeliveryAddressBO.getDelivery_address());
                    deliveryAddressOK = true;
                }
            }
            //上一次订单地址失效了，使用用户设置的地址
            if (!deliveryAddressOK && userAddressBO != null) {
                paymentSetting.setDeliveryAddress(userAddressBO);
            }
        }

//        if (userAddressBO != null) {
//            //只要用户设置了，就优先使用
//            paymentSetting.setDeliveryAddress(userAddressBO);
//        } else if (orderDefaultDeliveryAddressBO != null && orderDefaultDeliveryAddressBO.getDelivery_address() != null) {
//            //2.2用户没有设置默认的地址
//            if (orderDefaultDeliveryAddressBO.getDelivery_address().getAddress_id() != null) {
//                paymentSetting.setDeliveryAddress(orderDefaultDeliveryAddressBO.getDelivery_address());
//            }
//        }

        //根据快递地址信息变更加急和快递方式
        paymentSetting.resetRapidAndShipping();


        return paymentSetting;
    }

    /**
     * 查询表order_default_preferences获取订单支付、快递信息
     *
     * @param uid
     * @return
     */
    private OrderDefaultPreferencesBO getOrderDefaultDeliveryAddress(int uid) {
        OrderDefaultPreferencesBO orderDefaultPreferencesBO = null;
        OrderDefaultPreferences orderDefaultPreferences = orderDefaultPreferencesMapper.selectByPrimaryKey(uid);

        if (orderDefaultPreferences != null) {
            JSONObject jsonObject = null;
            try {
                //OrderDefaultPreferences == Boolean 难道是人为设置的？
                jsonObject = JSON.parseObject(orderDefaultPreferences.getOrderDefaultPreferences());
            } catch (Exception e) {
                logger.error("parse order default preferences error,uid is {},orderDefaultPreferences is {}", uid, orderDefaultPreferences.getOrderDefaultPreferences(), e);
            }

            if (jsonObject != null) {
                orderDefaultPreferencesBO = new OrderDefaultPreferencesBO();
                orderDefaultPreferencesBO.setPayment_type(jsonObject.getString("payment_type"));
                orderDefaultPreferencesBO.setShipping_manner(jsonObject.getString("shipping_manner"));
                orderDefaultPreferencesBO.setReceiving_time(jsonObject.getString("receiving_time"));
                orderDefaultPreferencesBO.setUid(jsonObject.getString("uid"));

                if (!jsonObject.containsKey("delivery_address")) {
                    return null;
                }
                DeliveryAddressBO deliveryAddressBO = new DeliveryAddressBO();
                try {
                    JSONObject deliveryAddressObject = jsonObject.getJSONObject("delivery_address");
                    if (deliveryAddressObject != null) {
                        String addressId = deliveryAddressObject.getString("id");
                        //需要确认addressId是否还存在
                        UserAddressRspBO userAddressRspBO = null;
                        try {
                            userAddressRspBO = externalService.queryUserAddressByUidAndAddressId(uid,Integer.parseInt(addressId));
                        } catch (Exception e) {
                            logger.info("getUserAddressByUidAndAddressId({},{}) error", uid, addressId, e);
                        }

                        if (userAddressRspBO == null) {
                            //根据addressId查询地址，没有，可能已删除
                            return null;
                        }

                        deliveryAddressBO.setAddress_id(addressId);
                        deliveryAddressBO.setConsignee(deliveryAddressObject.getString("addressee_name"));
                        deliveryAddressBO.setMobile(deliveryAddressObject.getString("mobile"));
                        deliveryAddressBO.setPhone(deliveryAddressObject.getString("phone"));
                        deliveryAddressBO.setAddress(deliveryAddressObject.getString("address"));
                        deliveryAddressBO.setArea_code(deliveryAddressObject.getString("area_code"));
                        deliveryAddressBO.setArea_code(deliveryAddressObject.getString("area_code"));
                        String province = "";
                        String city = "";
                        String county = "";
                        String is_support = "N";
                        String is_delivery = "N";
                        if (deliveryAddressObject.containsKey("areaNames")) {
                            JSONObject areaNamesObject = deliveryAddressObject.getJSONObject("areaNames");
                            province = areaNamesObject.getString("province");
                            city = areaNamesObject.getString("city");
                            county = areaNamesObject.getString("county");
                            is_support = areaNamesObject.getString("is_support");
                            is_delivery = areaNamesObject.getString("is_delivery");
                        }

                        deliveryAddressBO.setArea(province + " " + city + " " + county);
                        deliveryAddressBO.setIs_support(is_support);
                        deliveryAddressBO.setIs_delivery(is_delivery);

                        orderDefaultPreferencesBO.setDelivery_address(deliveryAddressBO);
                    }
                } catch (Exception e) {
                    logger.error("parse order default preferences error,uid is {},josn is {}", uid, jsonObject, e);
                }

            }
        }
        return orderDefaultPreferencesBO;
    }

    /**
     * 查询用户最后一条订单
     *
     * @param uid
     * @return
     */
    private Orders getUserLastOrder(int uid) {
        return ordersMapper.selectLastOrderByUid(uid);
    }


    /**
     * 从个人信息中心获取用户默认地址
     *
     * @param uid
     * @return
     */
    private DeliveryAddressBO getUserDefaultAddress(int uid) {
        UserAddressRspBO userAddressRspBO = externalDegradeService.queryUserDefaultAddress(uid);
        if (userAddressRspBO == null || StringUtils.isEmpty(userAddressRspBO.getId())) {
            return null;
        }
        DeliveryAddressBO deliveryAddressBO = new DeliveryAddressBO();
        deliveryAddressBO.setAddress_id(String.valueOf(userAddressRspBO.getId()));
        deliveryAddressBO.setConsignee(userAddressRspBO.getAddresseeName());
        deliveryAddressBO.setAddress(userAddressRspBO.getAddress());
        deliveryAddressBO.setArea_code(userAddressRspBO.getAreaCode());
        deliveryAddressBO.setPhone(userAddressRspBO.getPhone());
        deliveryAddressBO.setMobile(userAddressRspBO.getMobile());
        deliveryAddressBO.setIs_default(userAddressRspBO.getIsDefault());

        AreaRspBo areaRspBo = userAddressRspBO.getArea();
        deliveryAddressBO.setIs_delivery(areaRspBo.getIsDelivery());
        deliveryAddressBO.setIs_support(areaRspBo.getIsSupport());

        String province = "";
        String city = "";
        String county = "";
        //province
        String areaCode = userAddressRspBO.getAreaCode();
        if (areaCode.endsWith("0000")) {
            //后四位为0，表示为省
            province = areaRspBo.getCaption() == null ? "" : areaRspBo.getCaption();
        } else if (areaCode.endsWith("00")) {
            //后两位为0，表示为市
            AreaRspBo parentArea = areaRspBo.getParent();
            if (parentArea != null) {
                province = parentArea.getCaption() == null ? "" : parentArea.getCaption();
            }
            city = areaRspBo.getCaption() == null ? "" : areaRspBo.getCaption();
        } else {
            county = areaRspBo.getCaption() == null ? "" : areaRspBo.getCaption();
            AreaRspBo cityArea = areaRspBo.getParent();
            if (cityArea != null) {
                city = cityArea.getCaption() == null ? "" : cityArea.getCaption();
                AreaRspBo provinceArea = cityArea.getParent();
                if (provinceArea != null) {
                    province = provinceArea.getCaption() == null ? "" : provinceArea.getCaption();
                }
            }
        }
        deliveryAddressBO.setArea(province + " " + city + " " + county);
        return deliveryAddressBO;

    }

    /**
     * 红包
     * @return
     */
    private boolean canUseRedEnvelopes(int enabledRedEnvelopes) {
        if (enabledRedEnvelopes < 1) {
            return false;
        }
        return gateCacheManager.isOpenRedEnvelope();
    }


    /**
     * 优惠码
     * @return
     */
    private boolean canUsePromotionCode()
    {
        Gate gate = gateCacheManager.getGateFor(GateCacheService.CART_USE_PROMOTION_CODE);
        if (gate != null && gate.getStatus()) {
            return true;
        }
        return false;
    }
}

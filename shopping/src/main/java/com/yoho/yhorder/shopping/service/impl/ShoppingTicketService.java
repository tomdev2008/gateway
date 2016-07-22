package com.yoho.yhorder.shopping.service.impl;

import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.shopcart.ProductBasicInfoBo;
import com.yoho.product.model.shopcart.ProductShopCartBo;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.service.model.order.request.ShoppingTicketRequest;
import com.yoho.service.model.order.response.shopping.ShoppingChargeResult;
import com.yoho.service.model.order.response.shopping.ShoppingSubmitResponse;
import com.yoho.service.model.order.response.shopping.ShoppingTicketChargeTotal;
import com.yoho.service.model.order.response.shopping.ShoppingTicketQueryResult;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.cache.redis.CacheEnum;
import com.yoho.yhorder.common.cache.redis.OrderRedis;
import com.yoho.yhorder.common.cache.redis.RedisValueOperation;
import com.yoho.yhorder.common.utils.PhoneUtil;
import com.yoho.yhorder.dal.IOrdersGoodsMapper;
import com.yoho.yhorder.dal.YohoodTicketInfoMapper;
import com.yoho.yhorder.dal.YohoodTicketsMapper;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.event.ShoppingTicketIssueEvent;
import com.yoho.yhorder.shopping.model.OrderCreationContext;
import com.yoho.yhorder.shopping.model.OrderGoods;
import com.yoho.yhorder.shopping.model.ProductTicket;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.service.IShoppingTicketService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class ShoppingTicketService implements IShoppingTicketService {

    private final Logger logger = LoggerFactory.getLogger("ticketLog");

    //最小购买数量
    private final static int MIN_BUY_NUM = 1;

    //最大购买数量
    private final static int MAX_BUY_NUM = 4;

    @Autowired
    private ChargeContextFactory changeContextFactory;

    @Autowired
    private ChargerService chargerService;

    @Autowired
    private YohoodTicketsMapper yohoodTicketsMapper;
    @Autowired
    private YohoodTicketInfoMapper yohoodTicketInfoMapper;

    @Autowired
    private ShoppingCartSubmitService shoppingCartSubmitService;

    @Value("${sendSMS.password}")
    private String sendSMSPassword;

    @Autowired
    private ServiceCaller service;

    @Autowired
    private OrderRedis orderRedis;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private RedisValueOperation redisValueOperation;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public ShoppingTicketQueryResult addAndQuery(ShoppingTicketRequest request) {
        logger.info("enter add ticket service,request is {}", request);

        validateRequest(request, false);

        ProductTicket productTicket = checkProductAttributeAndGetTicket(request);

        //校验用户购买票的数量
        checkTicketSkuBuyNumber(productTicket);

        // 设置ticket算费参数
        ChargeParam ticketChargeParam = newTicketChargeParam(request, productTicket);
        // 构建算费上下文
        ChargeContext ticketChargeContext = changeContextFactory.build(false, ticketChargeParam);

        // 计算结果
        chargerService.charge(ticketChargeContext);
        // 获取算费结果
        ShoppingChargeResult ticketChargeResult = ticketChargeContext.getChargeResult();
        // 配置返回结果
        ShoppingTicketQueryResult ticketResult = new ShoppingTicketQueryResult();
        ticketResult.setGoods_list(ticketChargeResult.getGoods_list());
        // 设置电子票的chargeTotal
        ShoppingTicketChargeTotal chargeTotal = new ShoppingTicketChargeTotal();
        chargeTotal.setGoods_count(ticketChargeResult.getShopping_cart_data().getGoods_count());
        chargeTotal.setLast_order_amount(ticketChargeResult.getShopping_cart_data().getLast_order_amount());
        chargeTotal.setOrder_amount(ticketChargeResult.getShopping_cart_data().getOrder_amount());
        chargeTotal.setPromotion_formula_list(ticketChargeResult.getShopping_cart_data().getPromotion_formula_list());
        chargeTotal.setUse_yoho_coin(ticketChargeContext.getChargeTotal().getUseYohoCoin());
        ticketResult.setShopping_cart_data(chargeTotal);

        ticketResult.setUid(request.getUid());
        ticketResult.setYoho_coin(MathUtils.round(ticketChargeContext.getUserInfo().getOrderYohoCoin().ratedYohoCoin()));

        logger.info("exit ticket add and query,request is {},response is {}", request, ticketResult);
        return ticketResult;
    }

    /**
     * 虚拟商品算费参数对象
     *
     * @param request
     * @return
     */
    private ChargeParam newTicketChargeParam(final ShoppingTicketRequest request, ProductTicket productTicket) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setShoppingKey("0");
        chargeParam.setUid(request.getUid());
        chargeParam.setCartType(Constants.ORDINARY_CART_TYPE);
        chargeParam.setChargeType(Constants.TICKET_CHARGE_TYPE);
        chargeParam.setSaleChannel(request.getSale_channel());
        chargeParam.setNeedCalcShippingCost(false);
        chargeParam.setNeedQueryYohoCoin(true);
        chargeParam.setNeedQueryRedEnvelopes(false);
        chargeParam.setNeedAuditCodPay(false);
        chargeParam.setUserAgent(request.getUser_agent());
        chargeParam.setUseYohoCoin(productTicket.getUseYohoCoin());
        chargeParam.setAttribute(Constants.ATTRIBUTE_VIRTUAL);
        ShoppingItem item = new ShoppingItem();
        item.setBuyNumber(productTicket.getBuy_number());
        item.setSku(productTicket.getProduct_sku());
        item.setSkn(productTicket.getProduct_skn());
        List<ShoppingItem> items = new ArrayList<ShoppingItem>();
        items.add(item);
        chargeParam.setShoppingItemList(items);
        return chargeParam;
    }

    @Override
    public ShoppingSubmitResponse submitTicket(ShoppingTicketRequest request) {
        logger.info("enter submit ticket service,request is {}", request);

        //防止用户刷票,两个云可能防不住
        checkRepeatableSubmit(request.getUid());

        validateRequest(request, true);

        orderRedis.removeSkuBuyNumberByUid(request.getUid());

        ProductTicket productTicket = checkProductAttributeAndGetTicket(request);

        checkTicketSkuBuyNumber(productTicket);

        // 设置ticket算费参数
        ChargeParam ticketChargeParam = newTicketChargeParam(request, productTicket);
        // 构建算费上下文
        ChargeContext ticketChargeContext = changeContextFactory.build(false, ticketChargeParam);
        // 计算结果
        chargerService.charge(ticketChargeContext);

        // 创建订单
        ShoppingSubmitRequest submitRequest = new ShoppingSubmitRequest();
        submitRequest.setUser_agent(request.getUser_agent());
        submitRequest.setBuy_number(productTicket.getBuy_number());
        submitRequest.setUse_yoho_coin(request.getUse_yoho_coin());
        submitRequest.setPayment_type(1);
        submitRequest.setSelected("Y");
        submitRequest.setEnabled_RedEnvelopes(0);
        submitRequest.setProduct_sku(request.getProduct_sku());
        submitRequest.setUid(request.getUid());
        submitRequest.setMobile(request.getMobile());
        submitRequest.setAddress_id(0);
        submitRequest.setClient_type(request.getClient_type());
        submitRequest.setQhy_union(request.getQhy_union());
        submitRequest.setUser_agent(request.getUser_agent());


        OrderCreationContext context = shoppingCartSubmitService.submitTicket(submitRequest, ticketChargeContext);

        List<OrderGoods> goods = context.getOrder().getGoodsList();

        if ((context.getOrder().getLastOrderAmount() == 0) && CollectionUtils.isNotEmpty(goods)) {
            logger.info("order last order amount is zero,will issue ticket,order code is {}", context.getOrder().getOrderCode());
            OrderGoods good = goods.get(0);
            if (good != null) {
                // 发放门票
                issueTicket(context.getOrder().getUid(), context.getOrder().getOrderCode());
            }
        }
        // 3.返回结果
        ShoppingSubmitResponse response = shoppingCartSubmitService.getSubmitResponseForm(context);

        logger.info("exit submit ticket service,request is {},response is {}", request, response);

        return response;
    }

    /**
     * 校验产品属性等
     *
     * @param request
     * @return
     */
    private ProductTicket checkProductAttributeAndGetTicket(ShoppingTicketRequest request) {

        ProductTicket productTicket = new ProductTicket();
        productTicket.setBuy_number(request.getBuy_number());
        productTicket.setProduct_sku(request.getProduct_sku());
        productTicket.setUid(request.getUid());
        productTicket.setUseYohoCoin(request.getUse_yoho_coin());

        //校验产品属性,库存,状态
        ProductShopCartBo productInfo = checkAndGetProduct(productTicket);

        productTicket.setStorage_number(productInfo.getStorageBo().getStorageNum());
        productTicket.setProduct_skn(productInfo.getProductBasicInfoBo().getErpProductId());

        return productTicket;
    }


    /**
     * 校验购买数量不能超过指定数量
     *
     * @param ticket
     */
    private void checkTicketSkuBuyNumber(ProductTicket ticket) {

        int uid = ticket.getUid();
        int sku = ticket.getProduct_sku();
        int expectedBuyNumber = ticket.getBuy_number();


        int buyNumberInDB = 0;
        boolean hasCache = orderRedis.existSkuByNumber(uid, sku);
        if (!hasCache) {
            //没有缓存查询数据库
            buyNumberInDB = querySkuBuyNumberExcludeCanceledOrder(uid, sku);
            orderRedis.incrementSkuBuyNumber(uid, sku, buyNumberInDB);
        }
        int buyNumberInCache = orderRedis.getSkuBuyNumberIfExceptionReturnMaxInteger(uid, sku);

        int actualBuyNumber = Math.max(buyNumberInDB,buyNumberInCache);

        if ((expectedBuyNumber + actualBuyNumber) > MAX_BUY_NUM) {

            logger.warn("Ticket check fail, user buy sku beyond limit,uid is {},sku is {},actual buyNumber is {},expected buyNumber is {}", uid, sku, actualBuyNumber, expectedBuyNumber);

            throw new ServiceException(ServiceError.SHOPPING_TICKET_HAS_BUY);
        }

    }

    /**
     * 查询sku的购物数量,
     *
     * @param uid
     * @param sku
     * @return
     */
    private int querySkuBuyNumberExcludeCanceledOrder(int uid, int sku) {
        int buyNumber = ordersGoodsMapper.selectSkuBuyNumberByUidAndSku(uid, sku);
        return buyNumber;
    }

    /**
     * 校验产品属性
     *
     * @param productTicket
     */
    private ProductShopCartBo checkAndGetProduct(ProductTicket productTicket) {
        // 检查库存
        List<Integer> skus = new LinkedList<>();
        skus.add(productTicket.getProduct_sku());
        ProductShopCartBo[] productBoArray = externalService.queryProductShoppingCartBySkuids(skus);
        if (ArrayUtils.isEmpty(productBoArray)) {
            logger.warn("Ticket check fail, not find any product info,sku:{},uid:{}", skus, productTicket.getUid());
            throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_STORAGE_NUM_NOT_SUPPORT);
        }

        ProductShopCartBo productShopCartBo = productBoArray[0];

        if (productShopCartBo.getStorageBo() == null || productShopCartBo.getStorageBo().getStorageNum() < productTicket.getBuy_number()) {
            logger.warn("Ticket check fail,storage is not enough. sku:{},uid:{}", skus, productTicket.getProduct_sku(), productTicket.getUid());
            throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_STORAGE_NUM_NOT_SUPPORT);
        }
        // 检查商品属性
        ProductBasicInfoBo productInfo = productShopCartBo.getProductBasicInfoBo();
        if (productInfo == null || productInfo.getStatus() == 0 || productInfo.getAttribute() != 3) {
            logger.warn("Ticket check fail, uid {} ,sku:{},product info is {}", productTicket.getUid(), productTicket.getProduct_sku(), productInfo);
            throw new ServiceException(ServiceError.SHOPPING_PRODUCT_ATTRIBUTE_NOT_SUPPORT_TICKET);
        }

        return productShopCartBo;
    }

    private void validateRequest(ShoppingTicketRequest request, boolean checkMobile) {
        if (request == null) {
            logger.warn("Ticket addAndQuery fail, request is null.");
            throw new ServiceException(ServiceError.SHOPPING_REQUESTPARAMS_IS_NULL);
        }

        //校验app客户端版本,app 4.8.1以下不可用
        checkClientTypeAndAppVersion(request);

        if (request.getUid() == null || request.getUid() < 1) {
            logger.warn("Ticket addAndQuery fail, request uid is empty.");
            throw new ServiceException(ServiceError.SHOPPING_UID_IS_NULL);
        }
        if (request.getProduct_sku() == null || request.getProduct_sku() < 1) {
            logger.warn("Ticket addAndQuery fail, request Product is empty.");
            throw new ServiceException(ServiceError.SHOPPING_PRODUCTSKU_IS_NULL);
        }

        if (request.getBuy_number() > MAX_BUY_NUM) {
            logger.warn("Ticket addAndQuery fail, request buy_number is big than {}.", MAX_BUY_NUM);
            throw new ServiceException(ServiceError.SHOPPING_REQUESTPARAMS_IS_NULL);
        }

        if (request.getBuy_number() < MIN_BUY_NUM) {
            logger.warn("Ticket addAndQuery fail, request buy_number is less than {}.", MIN_BUY_NUM);
            throw new ServiceException(ServiceError.SHOPPING_REQUESTPARAMS_IS_NULL);

        }

        if (checkMobile && !PhoneUtil.mobileVerify(request.getMobile())) {
            logger.warn("Ticket addAndQuery fail, request mobile{} is not invalid.", request.getMobile());
            throw new ServiceException(ServiceError.SHOPPING_USER_MOBILE_IS_INVALID);
        }

    }

    private void checkClientTypeAndAppVersion(ShoppingTicketRequest request) {
        boolean oldVersion = false;
        try {

            //判断版本,老版本没有传这个参数
            if (request.getBuy_number() == null) {
                oldVersion = true;
            } else if (("iphone".equalsIgnoreCase(request.getClient_type()) || "android".equalsIgnoreCase(request.getClient_type())) && "4.8.1".compareTo(request.getApp_version()) > 0) {
                oldVersion = true;
            }
        } catch (Exception ex) {
            oldVersion = true;
            logger.warn("String.compareTo function error", ex);
        }

        if (oldVersion) {
            //老版本
            logger.warn("version is old,must upgrade");
            throw new ServiceException(ServiceError.SHOPPING_TICKET_MUST_UPGRADE_VERSION);
        }
    }

    // 发放电子票
    public void issueTicket(int uid, Long orderCode) {
        ShoppingTicketIssueEvent event = new ShoppingTicketIssueEvent();
        event.setUid(uid);
        event.setOrderCode(orderCode);
        try {
            publisher.publishEvent(event);
            logger.info("publish event success,event is {}", event);
        } catch (Exception ex) {
            logger.warn("publish event fail,event is {} ", event, ex);
        }
    }

    private void checkRepeatableSubmit(int uid) {
        String postKey = String.valueOf(uid);
        if (redisValueOperation.hasKey(CacheEnum.USER_REPEATABLESUBMIT, postKey)) {
            logger.info("uid {} repeated submit,", uid);
            throw new ServiceException(ServiceError.SHOPPING_ORDER_REPEATABL_SUBMIT);
        } else {
            redisValueOperation.put(CacheEnum.USER_REPEATABLESUBMIT, postKey, "1");
        }
    }

    public static void main(String[] args)
    {
        System.out.println("************");
        System.out.println("4.8.1".compareTo("4.8.0.1607111"));
        System.out.println("4.8.1".compareTo("4.8.0"));
        System.out.println("4.8.1".compareTo("4.8.1.11"));
        System.out.println("4.8.1".compareTo("4.0"));
        System.out.println("************");
    }
}
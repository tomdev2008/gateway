package com.yoho.yhorder.shopping.service.impl;

import com.yoho.core.common.utils.YHMath;
import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.StorageBo;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.service.model.order.response.shopping.ShoppingSubmitResponse;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.cache.redis.OrderRedis;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.common.cache.redis.UserOrderCache;
import com.yoho.yhorder.common.model.Coupon;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.model.OrderCodeQueue;
import com.yoho.yhorder.dal.model.UserBlacklist;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.PromotionCodeChargeResult;
import com.yoho.yhorder.shopping.event.OrderSubmitEvent;
import com.yoho.yhorder.shopping.event.UnionPushOrderEvent;
import com.yoho.yhorder.shopping.model.*;
import com.yoho.yhorder.shopping.service.ExternalDegradeService;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.service.IOrderCreationService;
import com.yoho.yhorder.shopping.union.UnionContext;
import com.yoho.yhorder.shopping.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by JXWU on 2015/11/30.
 * 对如下操作进行异步处理
 * 1.发yoho币使用站内信
 * 2.打印大数据需要的日志
 * 3.jit拆单
 * 4.自动取消
 * 5.货到付款功能限定
 */
@Service
public class ShoppingCartSubmitService {

    private final Logger logger = LoggerFactory.getLogger("orderSubmitLog");

    @Autowired
    protected ServiceCaller serviceCaller;

    @Autowired
    private IShoppingTagDAO shoppingTagMapper;

    @Autowired
    private IOrderCodeQueueDAO orderCodeQueueDAO;

    @Autowired
    private IOrderCodeListDAO orderCodeListDAO;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    protected IShoppingCartItemsDAO shoppingCartItemsDAO;

    @Autowired
    protected IOrdersMapper ordersMapper;

    @Autowired
    protected IOrderDefaultPreferencesDAO orderDefaultPreferencesMapper;

    @Autowired
    private ChargeContextFactory changeContextFactory;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private ChargerService chargerService;

    @Autowired
    private ShoppingCartRedis shoppingCartRedis;

    @Autowired
    private ExternalDegradeService externalDegradeService;

    @Autowired
    @Qualifier("compensatableOrderServiceImpl")
    private IOrderCreationService compensatableOrderService;

    @Autowired
    @Qualifier("mqErpOrderServiceImpl")
    private IOrderCreationService mqErpOrderService;


    private static String LIMIT_CODE_SKU_KEY = "yh:order:limitcode:";

    @Autowired
    private YHValueOperations<String, String> valueOperations;

    @Resource(name = "yhRedisTemplate")
    private YHRedisTemplate<String, String> redisTemplate;

    @Autowired
    private OrderRedis orderRedis;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private UserOrderCache userOrderCache;

    @Autowired
    private OrderBuildeService orderBuildeService;

    public ShoppingSubmitResponse submit(ShoppingSubmitRequest request) {

        logger.info("enter shopping cart submit service, uid {}, request info {}", request.getUid(), request);
        //add 失效购物车列表缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), null);
        //1.算费
        //1.1新建算费参数
        ChargeParam chargeParam = newSubmitChargeParam(request);
        //1.2新建普通商品算费context，context中包括需要算费的sku、用户信息等
        //查询数据库表shopping_cart_items的自选sku
        ChargeContext chargeContext = changeContextFactory.build(true, chargeParam);
        //1.3算费，算费结果在context对象的ChargeTotal中。
        chargerService.charge(chargeContext);

        //终于走到最后一大步，创建订单啦

        //2.构建订单创建上下文
        OrderCreationContext context = createOrderCreationContext(request, chargeContext);

        //2.1创建订单
        create(request.getUser_agent(), context);

        //3.返回结果
        ShoppingSubmitResponse response = getSubmitResponseForm(context);

        //下单后，清除各类订单统计缓存
        userOrderCache.clearOrderCountCache(request.getUid());

        logger.info("exit create order in shopping cart submit, uid {}, request info {}, \n\n response info {} \n\n",
                request.getUid(), request, response);

        return response;
    }

    public OrderCreationContext submitTicket(ShoppingSubmitRequest request, ChargeContext chargeContext) {
        // 构建订单创建上下文
        OrderCreationContext context = createOrderCreationContext(request, chargeContext);
        // 创建订单
        create(request.getUser_agent(), context);

        return context;
    }

    private void create(String userAgent, OrderCreationContext context) {
        //1.校验
        preCreate(context);

        Order order = context.getOrder();
        //2.生成订单code
        generateOrderCode(order);

        doCreate(context);

        //5.支付
        //6.其他信息更新
        postCreate(userAgent, context);
    }

    private void generateOrderCode(Order order) {
        Long orderCode = getNextOrderCode(order.getUid());
        logger.info("generate order code success in shopping_cart_submit, uid {}, order code {}", order.getUid(), orderCode);
        order.setOrderCode(orderCode);
    }

    /**
     * 订单创建前预处理，主要是校验
     * 校验:
     * 1.商品金额不能为0
     * 2.支付方式,货到付款限制
     * 3.订单实收金额限制
     * 3.1 货到付款不能超过5000元
     * 3.2 其他不能超过20000元
     * 4.重新计算订单金额,与实际的订单金额不能超过1元
     * 计算公式
     *  (for each good_price * buy number) + 运费 - 优惠券 - 优惠码 - 红包 - yoho币
     * 5.黑名单 erp后台设置的
     * 6.黑名单 24h内订单自动取消6次
     * 7.校验库存
     * 8.校验限量商品的购买数量
     * 9.校验用户的yoho币
     * 10.校验限购商品的库存
     * @param context
     */
    private void preCreate(OrderCreationContext context) {

        //校验订单商品金额是否为0
        checkZeroOrder(context.getOrder());

        //校验支付方式
        checkOrderPaymentType(context);

        //校验订单金额是否超过限制
        checkLastOrderAmountOverLimit(context);

        //重新计算订单金额,与收收金额比较
        checkOrderAmountCorrect(context);

        //校验黑名单
        checkBackList(context);

        //校验redis中的黑名单
        checkRedisBackList(context);
        //库存
        checkStorage(context);
        //校验skn购买数量
        checkGoodsBuyLimit(context.getOrder().getUid(), context.getOrder().getGoodsList());
        //校验用户使用的yoho币是否超过用户实际数量
        checkYohoCoin(context.getOrder());

        //校验限购码商品的库存,不能超买
        checkLimitcodeStorage(context.getOrder());
    }

    private void checkLastOrderAmountOverLimit(OrderCreationContext context) throws ServiceException {
        Order order = context.getOrder();
        //订单最终金额
        double lastOrderAmount = order.getLastOrderAmount();
        //支付金额
        int paymentType = order.getPaymentType();

        if (paymentType == 2 && lastOrderAmount > 5000) {
            //货到付款，金额不能超过5000
            logger.warn("order can't pay by cash,uid is {},last order amount is {},payment type is {}",
                    order.getUid(),
                    order.getLastOrderAmount(),
                    order.getPaymentType());
            throw new ServiceException(ServiceError.SHOPPING_CASH_PAYMENT_AMOUNT_OVER_LIMIT);
        } else if (lastOrderAmount > 20000) {
            logger.warn("order last amount over 20000 limit,uid is {},last order amount is {},payment type is {}",
                    order.getUid(),
                    order.getLastOrderAmount(),
                    order.getPaymentType());
            throw new ServiceException(ServiceError.SHOPPING_ONLINE_PAY_ORDER_AMOUNT_OVERLIMIT);
        }
    }

    /**
     * 重新计算订单金额,防止公司亏钱
     * 订单实付金额=商品金额+运费-优惠券-优惠码-红包-yoho币
     * @param context
     * @throws ServiceException
     */
    private void checkOrderAmountCorrect(OrderCreationContext context) throws ServiceException {

        Order order = context.getOrder();
        Assert.notNull(order, "order must not be null");
        Assert.notEmpty(order.getGoodsList(), "order goods must not be empty");

        //商品金额
        double orderAmount = 0;
        for (OrderGoods orderGoods : order.getGoodsList()) {
            orderAmount = YHMath.add(orderAmount, YHMath.mul(orderGoods.getGoods_price(), orderGoods.getBuy_number()));
        }

        //
        double reCaclLastOrderAmount = YHMath.add(orderAmount, order.getShippingCost());

        //优惠券
        Coupon coupon = order.getOrderCoupon();
        if (coupon != null && StringUtils.isNotEmpty(coupon.getCoupon_code())) {
            reCaclLastOrderAmount = YHMath.sub(reCaclLastOrderAmount, coupon.getCoupon_amount());
        }
        //优惠码
        PromotionCodeChargeResult promotionCodeChargeResult = order.getPromotionCodeChargeResult();
        if (promotionCodeChargeResult != null && promotionCodeChargeResult.isValid() && promotionCodeChargeResult.getDiscountAmount() > 0) {
            reCaclLastOrderAmount = YHMath.sub(reCaclLastOrderAmount, promotionCodeChargeResult.getDiscountAmount());
        }
        //红包
        double useRedEnvelopes = order.getUseRedEnvelopes();
        if (useRedEnvelopes > 0) {
            reCaclLastOrderAmount = YHMath.sub(reCaclLastOrderAmount, useRedEnvelopes);
        }
        //yoho币
        double yohoCoinAmount = order.getYohoCoinNum();
        if (yohoCoinAmount > 0) {
            reCaclLastOrderAmount = YHMath.sub(reCaclLastOrderAmount, yohoCoinAmount);
        }

        double realLastOrderAmount = order.getLastOrderAmount();

        double lostAmount = YHMath.sub(reCaclLastOrderAmount, realLastOrderAmount);

        //订单金额计算不一致 输出订单金额日志
        if(Math.abs(lostAmount)>0) {
            logger.info("after redo calc,order amount is {},last order amount is {},real last order amount {} ,lost amount is {}", orderAmount, reCaclLastOrderAmount, realLastOrderAmount, lostAmount);
            if (Math.abs(lostAmount) > 1) {
                //实收金额超过1元
                throw new ServiceException(ServiceError.SHOPPING_ORDER_AMOUNT_INCORRECT);
            }
        }

    }

    private void checkOrderPaymentType(OrderCreationContext context) throws ServiceException {
        Order order = context.getOrder();
        int paymentType = order.getPaymentType();
        if (paymentType != 1 && order.isMustOnlinePayment()) {
            //必须在线支付
            logger.warn("order must be paid online,uid is {},last order amount is {},payment type is {}",
                    order.getUid(),
                    order.getLastOrderAmount(),
                    order.getPaymentType());
            ServiceException se = new ServiceException(ServiceError.SHOPPING_ORDER_MUST_ONLINE_PAY);
            se.setParams(new String[]{order.getMustOnlinePaymentReason()});
            throw se;
        }
    }

    /**
     * erp黑名单判断
     * 在redis链接不上的时候,放空
     * @param context
     * @throws ServiceException
     */
    private void checkBackList(OrderCreationContext context) throws ServiceException {
        Order order = context.getOrder();
        String clientIp = order.getClientIP();
        long ip = MyStringUtils.ip2Long(clientIp);
        int uid = order.getUid();
        
        UserBlacklist[] userBlacklistArray = externalDegradeService.queryUserBlacklist(uid,ip);

        boolean hasBlack = hasBlack(uid, ip, userBlacklistArray);

        if (hasBlack) {
            logger.warn("[{}] or {} is in user_blacklist", uid, clientIp);
            //throw new Exception('由于您的帐号在我公司的商城上存在不良消费记录，短期内将暂时无法购买，请您谅解。');
            throw new ServiceException(ServiceError.SHOPPING_BLACK_LIST);
        }
    }

    /**
     * 是否有黑名单记录
     * @param uid
     * @param ip
     * @param userBlacklistArray
     * @return
     */
    private boolean hasBlack(int uid, long ip, UserBlacklist[] userBlacklistArray) {
        boolean hasBlack = false;
        if (ArrayUtils.isEmpty(userBlacklistArray)) {
            return false;
        }
        for (UserBlacklist userBlack : userBlacklistArray) {
            if (userBlack.getUid() == uid && userBlack.getStatus() > 0) {
                //用户有黑名单记录
                hasBlack = true;
                break;
            }
            if (ip > 0 && userBlack.getStatus() == 2 && userBlack.getIp() == ip) {
                //ip有黑名单记录
                hasBlack = true;
                break;
            }
        }
        return hasBlack;
    }


    private void checkRedisBackList(OrderCreationContext context) {
        Order order = context.getOrder();
        int uid = order.getUid();
        if (orderRedis.existBacklist(uid)) {
            logger.info("uid {} is in redis backlist", uid);
            throw new ServiceException(ServiceError.SHOPPING_USER_BLACK_LIST);
        }
    }

    /**
     * 库存验证
     */
    private void checkStorage(OrderCreationContext context) {
        Order order = context.getOrder();
        List<OrderGoods> goodsList = order.getGoodsList();
        if (CollectionUtils.isEmpty(goodsList)) {
            //在订单确认页面，再次下单，sku可能已经售罄
            logger.error("checkStorage failed in shopping_cart_submit, SHOPPING_ORDER_GOODS_IS_EMPTY , uid {}, order code {}, order {} ",
                    order.getUid(), order.getOrderCode(), order);
            throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_STORAGE_NUM_NOT_SUPPORT);
        }
        //x件y元 会进行分组
        Map<Integer, Integer> skuBuyNumberMap = new HashMap<>();
        for (OrderGoods orderGoods : goodsList) {
            Integer totalBuyNumber = skuBuyNumberMap.get(orderGoods.getProduct_sku());
            if (totalBuyNumber == null) {
                totalBuyNumber = 0;
            }
            totalBuyNumber = totalBuyNumber + orderGoods.getBuy_number();

            skuBuyNumberMap.put(orderGoods.getProduct_sku(), totalBuyNumber);
        }

        ArrayList<Integer> skuList = new ArrayList<>(skuBuyNumberMap.keySet());
        //批量查询商品库存
        StorageBo[] storageBos = externalDegradeService.queryStorageBySkuIds(skuList);

        for (Integer sku : skuList) {
            Integer totalBuyNumber = skuBuyNumberMap.get(sku);
            boolean throwEx = false;
            if (storageBos == null) {
                throwEx = true;
            } else {
                StorageBo tmpStorageBo = null;
                for (StorageBo storageBo : storageBos) {
                    if (storageBo.getErpSkuId().intValue() == sku.intValue()) {
                        tmpStorageBo = storageBo;
                        break;
                    }
                }
                //没有查询到库存或者库存数量不满足
                if (tmpStorageBo == null || tmpStorageBo.getStorageNum() < totalBuyNumber) {
                    throwEx = true;
                }
            }
            //
            if (throwEx) {
                //hrow new Exception($productName . ',库存不足.');
                throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_STORAGE_NUM_NOT_SUPPORT);
            }
        }
    }

    /**
     * 限购
     */
    private void checkGoodsBuyLimit(Integer uid, List<OrderGoods> orderGoodsList) {
        Map<Integer,OrderGoods> sknAndBuyLimitMap = new HashMap<>();
        Map<Integer, Integer> sknAndBuyNumberMap = new HashMap<>();
        for (OrderGoods orderGoods : orderGoodsList) {
            //有数量限制的产品才需要查询
            if (orderGoods.getBuyLimit() != null && orderGoods.getBuyLimit() > 0) {
                Integer buyNumber = orderGoods.getBuy_number();
                Integer productSkn = orderGoods.getProduct_skn();
                if (sknAndBuyNumberMap.containsKey(productSkn)) {
                    buyNumber += sknAndBuyNumberMap.get(productSkn);
                }

                sknAndBuyNumberMap.put(productSkn, buyNumber);

                sknAndBuyLimitMap.put(productSkn, orderGoods);
            }
        }
        //没有限购的产品
        if (sknAndBuyNumberMap.isEmpty()) {
            return;
        }
        //获取用户下单产品已经购物的数量
        Map<Integer, Integer> alreadyBuySknMap = queryBuyNumberByUidAndSkns(uid, new ArrayList<>(sknAndBuyNumberMap.keySet()));

        if (MapUtils.isEmpty(alreadyBuySknMap)) {
            alreadyBuySknMap = new HashMap<>();
        }

        Set<Integer> skns = sknAndBuyNumberMap.keySet();
        for (Integer skn : skns) {
            OrderGoods orderGoods = sknAndBuyLimitMap.get(skn);
            int buyNumber = sknAndBuyNumberMap.get(skn);
            int alreadyBuyNum = alreadyBuySknMap.get(skn) == null ? 0 : alreadyBuySknMap.get(skn);
            if ((alreadyBuyNum + buyNumber) > orderGoods.getBuyLimit()) {
                //throw new Exception($val['product_name'] . '(超过了限购数量).');
                // throw new BusinessCheckException(500, orderGoods.getProduct_name() + "(超过了限购数量).", ServiceError.SHOPPING_BUSCHECK_PRODUCT_EXCEED_BUYLIMIT);
                logger.info("uid {} buy skn {} ,the buy number {} over skn buy limit {}", uid, orderGoods.getProduct_skn(), (alreadyBuyNum + buyNumber), orderGoods.getBuyLimit());
                throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_PRODUCT_EXCEED_BUYLIMIT);
            }
        }

    }

    /**
     * dao层的list转化为map
     * @param uid
     * @param skns
     * @return
     */
    private Map<Integer, Integer> queryBuyNumberByUidAndSkns(int uid, List<Integer> skns) {
        List<Map<String, Object>> alreadyBuySknMap = ordersGoodsMapper.selectNumByUidAndSkn(uid, skns);
        if (CollectionUtils.isEmpty(alreadyBuySknMap)) {
            return new HashMap();
        }
        Map<Integer, Integer> resultMap = new HashMap<>();
        for (Map<String, Object> map : alreadyBuySknMap) {
            Integer skn = null;
            Integer buyNumber = null;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if ("product_skn".equals(entry.getKey())) {
                    skn = ((Long) entry.getValue()).intValue();
                } else if ("buy_number".equals(entry.getKey())) {
                    buyNumber = ((java.math.BigDecimal) entry.getValue()).intValue();
                }
            }
            resultMap.put(skn, buyNumber);
        }
        return resultMap;
    }

    /**
     * YOHO币验证
     */
    private void checkYohoCoin(Order order) {
        if (order.getUseYohoCoin() > 0) {
            OrderYohoCoin orderYohoCoin = externalService.queryUsableYohoCoin(order.getUid());
            if (order.getUseYohoCoin() > 0 && (order.getUseYohoCoin() > orderYohoCoin.ratedYohoCoin())) {
                //throw new Exception('超出YOHO币使用数量.');
                logger.info("user {} request use yoho coin {} > real yoho coin {}", order.getUid(), order.getUseYohoCoin(), orderYohoCoin.ratedYohoCoin());
                throw new ServiceException(ServiceError.SHOPPING_YOHO_EXCEED_REAL_AMOUT);
            }
        }
    }

    private void checkLimitcodeStorage(Order order) {
        //限制
        if (Constants.isLimitCodeChargeType(order.getChargeType())) {

            List<ShoppingItem> items = order.getShoppingItems();
            logger.info("order use limit code,check items {} storage", items);

            if (CollectionUtils.isNotEmpty(items)) {
                //只有一项
                ShoppingItem item = items.get(0);
                int sku = item.getSku();
                int buyNumber = Math.abs(item.getBuyNumber());
                String skuKey = LIMIT_CODE_SKU_KEY + sku;
                boolean hasKey = redisTemplate.hasKey(skuKey);
                if (!hasKey) {
                    //判断key是否存在,不存在,则从product服务获取
                    int storageNum = externalService.queryStorageNumBySkuId(sku);
                    valueOperations.setIfAbsent(skuKey, String.valueOf(storageNum));
                    redisTemplate.longExpire(skuKey, 1, TimeUnit.MINUTES);
                    logger.info("cache sku {} ,storage num {},expire after 1 minutes", sku, storageNum);
                }
                Long remainStorageNum = valueOperations.increment(skuKey, -buyNumber);

                logger.info("limit code sku {},buyNumber {},remain storage num {}", sku, buyNumber, remainStorageNum);

                if (remainStorageNum < 0) {
                    logger.info("limit code sku storage num is not enough,sku {},order is {}", sku, order);
                    throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_STORAGE_NUM_NOT_SUPPORT);
                }
            }

        }
    }

    /**
     * 校验零元订单
     * @param order
     */
    private void checkZeroOrder(Order order) {
        //商品金额为0,不可能
        if (order.getOrderAmount() == 0) {
            logger.warn("order amount is zero,order is {}", order);
            throw new ServiceException(ServiceError.SHOPPING_ORDER_AMOUNT_ERROR);
        }
    }

    private void checkKeyFieldNotNull(Order order) {
        if (CollectionUtils.isEmpty(order.getGoodsList())) {
            // throw new Exception('订单商品不嫩为空.');
            logger.error("create local order failed in shopping_cart_submit, SHOPPING_ORDER_GOODS_IS_EMPTY , uid {}, order code {}, order {} ",
                    order.getUid(), order.getOrderCode(), order);

            throw new ServiceException(ServiceError.SHOPPING_ORDER_GOODS_IS_EMPTY);
        }
        //  ############### 订单数据项 ##################
        for (String key : OrderConfig.LOCAL_ORDER_MUST_HAVE_DATA_FIELDS) {
            // throw new Exception('缺少' . $key . '字段.');

            try {
                MyAssert.isNull(MyStringUtils.generateGetterOrSetter("get", key), order, ServiceError.SHOPPING_BUSCHECK_LOCAL_ORDER_MISSING_FIELDS);
            } catch (Exception ex) {
                logger.error("create local order failed in shopping_cart_submit, LOCAL_ORDER_MUST_HAVE_DATA_FIELDS , uid {}, order code {}, order {} ",
                        order.getUid(), order.getOrderCode(), order, ex);

                throw ex;
            }
        }

        //  ############### 快递信息数据项 ##################
        OrderReceiver receiver = order.getReceiver();
        for (String key : OrderConfig.LOCAL_ORDER_RECEIVER_MUST_DATA_FIELDS) {
            //throw new Exception('缺少' . $key . '字段.');

            try {
                MyAssert.isNull(MyStringUtils.generateGetterOrSetter("get", key), receiver, ServiceError.SHOPPING_BUSCHECK_LOCAL_ORDER_MISSING_FIELDS);
            } catch (Exception ex) {
                logger.error("create local order failed in shopping_cart_submit, LOCAL_ORDER_RECEIVER_MUST_DATA_FIELDS , uid {}, order code {}, order {} ",
                        order.getUid(), order.getOrderCode(), order, ex);

                throw ex;
            }
        }
    }


    private void injectDefaultValue(Order order) {
        //2.设置默认值
        ReflectTool.injectDefaultValue(order, OrderConfig.LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP);
        //2.2设置订单接收人默认信息
        ReflectTool.injectDefaultValue(order.getReceiver(), OrderConfig.LOCAL_ORDER_RECEIVER_DEFAULT_REQUEST_DATA_MAP);
    }


    private void doCreate(OrderCreationContext context) {

        checkKeyFieldNotNull(context.getOrder());

        injectDefaultValue(context.getOrder());

        updateOrderToPayIfZeroOrder(context.getOrder());

        /**
         * 1.减库存
         * 2.使用红包
         * 3.使用yoho币
         * 4.使用优惠券
         * 5.使用优惠码
         * 6.使用限购码
         * 7.入库
         * 7.1订单表 orders
         * 7.2订单商品表 orders_goods
         * 7.3优惠码使用表 orders_coupons
         * 7.4订单明细表 order_promotion_info
         * 7.5限购商品记录表 order_ext_attribute
         * 7.6收获订单表 order_default_preferences
         * 7.7清空购物车中的商品 shopping_cart_items
         * 7.8shopping_key下单使用表 shopping_tag
         */
        compensatableOrderService.create(context);

        //发送mq消息
        mqErpOrderService.create(context);

    }


    /**
     * 零元订单,更新订单状态为已支付
     *
     * @param order
     */
    private void updateOrderToPayIfZeroOrder(Order order) {
        if (isZeroOrder(order)) {
            logger.info("order last order amount is 0,order code is {},attribute is {},use yoho coin is {},use red envelopes is {},shipping cost is {}",
                    order.getOrderCode(),
                    order.getAttribute(),
                    order.getUseYohoCoin(),
                    order.getUseRedEnvelopes(),
                    order.getShippingCost());

            //已支付
            order.setPaymentStatus("Y");
            order.setPayment(0);
            order.setBankCode("");
            //更新订单状态
            int status = 1;
            if (order.getAttribute() != null && order.getAttribute() == 3) {
                //如果是虚拟订单变成已完成
                status = 6;
            }
            order.setStatus(status);
        }
    }


    /**
     * @param order
     * @return
     */
    private boolean isZeroOrder(Order order) {
        return (order.getLastOrderAmount() == 0 && (order.getUseYohoCoin() > 0 || order.getUseRedEnvelopes() > 0 || order.getPromotionCodeChargeResult().getDiscountAmount() > 0));
    }


    private void postCreate(String userAgent, OrderCreationContext context) {
        //订单缓存event
        publishOrderSubmitEvent(context);

        //联盟推送
        publishUnionPushEvent(userAgent, context);
    }


    private OrderCreationContext createOrderCreationContext(ShoppingSubmitRequest request, ChargeContext chargeContext) {

        logger.info("[createOrderCreationContext] create order by charge total result in shopping_cart_submit, request {} , \n charge total \n{} \n ",
                request, chargeContext.getChargeTotal());

        Order order = orderBuildeService.build(request, chargeContext);

        OrderCreationContext context = new OrderCreationContext();
        context.setUserInfo(chargeContext.getUserInfo());
        context.setOrder(order);
        context.setPromotionInfoList(chargeContext.getChargeTotal().getUsePromotionInfoList());
        return context;
    }


    /**
     * 根据uid获取一个订单号,获取5次失败后不再获取
     *
     * @param uid
     * @return
     */
    private Long getNextOrderCode(Integer uid) {
        MyAssert.isTrue(uid < 1, ServiceError.SHOPPING_UID_IS_NULL);
        Long orderCode = null;
        OrderCodeQueue orderCodeQueue = new OrderCodeQueue();
        orderCodeQueue.setUid(uid);
        for (int i = 0; i < 5; i++) {

            orderCodeQueueDAO.insertUid(orderCodeQueue);
            if (orderCodeQueue.getId() < 1) {
                continue;
            }
            orderCode = orderCodeListDAO.selectOrderCodeById(orderCodeQueue.getId());
            if (orderCode > 1) {
                break;
            }
        }
        //获取订单号失败！
        MyAssert.isTrue(orderCode < 1, ServiceError.SHOPPING_CART_GET_ORDERCODE_ERROR);
        int twoDigitsYear = MyStringUtils.getYear() - 2000;
        return orderCode < 10000000 ? new Long(twoDigitsYear + "0" + orderCode) : new Long(twoDigitsYear + "" + orderCode);
    }

    private ChargeParam newSubmitChargeParam(final ShoppingSubmitRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setUid(request.getUid());
        chargeParam.setCartType(request.getCart_type());
        chargeParam.parseProductSkuListParameterAndSetupChargeType(request.getCart_type(), request.getProduct_sku_list());
        chargeParam.setUseYohoCoin(request.getUse_yoho_coin());
        chargeParam.setCouponCode(request.getCoupon_code());
        chargeParam.setPaymentType(request.getPayment_type());
        chargeParam.setShippingManner(request.getDelivery_way());
        chargeParam.setUseRedEnvelopes(request.getUse_red_envelopes());
        chargeParam.setShoppingTag(request.getShopping_cart_tag());

        String clientType = request.getClient_type();
        chargeParam.setClientType(clientType);

        //算费结束推送使用
        String userAgent = request.getUser_agent();
        chargeParam.setUserAgent(userAgent);

        chargeParam.setNeedCalcShippingCost(true);
        chargeParam.setUserAgent(request.getUser_agent());

        //优惠码
        chargeParam.setPromotionCode(request.getPromotion_code());

        return chargeParam;
    }


    /**
     * 返还提交订单返回值
     * @param context
     * @return
     */
    public ShoppingSubmitResponse getSubmitResponseForm(OrderCreationContext context) {
        ShoppingSubmitResponse response = new ShoppingSubmitResponse();
        response.setOrder_code(context.getOrder().getOrderCode());
        //订单支付状态 数据库中使用Y(已支付)，N（未支付）；返回给APP，分别为1，0
        response.setPayment_status("Y".equals(context.getOrder().getPaymentStatus()) ? 1 : 0);
        response.setOrder_amount(context.getOrder().getLastOrderAmount());

        if (context.getOrder().getCreateTime() != null) {
            response.setCreate_time(DateUtil.formatDate(context.getOrder().getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            response.setPay_expire(DateUtil.formatDate(context.getOrder().getCreateTime() + 7200, "yyyy-MM-dd HH:mm:ss"));
        }

        return response;
    }

    /**
     * 订单缓存
     *
     * @param context
     */
    private void publishOrderSubmitEvent(OrderCreationContext context) {
        OrderSubmitEvent event = new OrderSubmitEvent();
        event.setOrder(context.getOrder());
        event.setPromotionInfoList(context.getPromotionInfoList());
        publisher.publishEvent(event);
    }

    private void publishUnionPushEvent(String userAgent, OrderCreationContext orderCreationContext) {
        //订单推送
        UnionContext unionContext = new UnionContext();
        unionContext.setOrderCreationContext(orderCreationContext);

        UnionPushOrderEvent event = new UnionPushOrderEvent();
        event.setUserAgent(userAgent);
        event.setUnionContext(unionContext);
        publisher.publishEvent(event);
    }
}
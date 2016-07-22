package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.GoodsBo;
import com.yoho.product.model.GoodsImagesBo;
import com.yoho.product.model.ProductBo;
import com.yoho.product.request.BaseRequest;
import com.yoho.product.request.BatchBaseRequest;
import com.yoho.service.model.order.OrderStatusDesc;
import com.yoho.service.model.order.constants.InvoiceType;
import com.yoho.service.model.order.constants.YohoodType;
import com.yoho.service.model.order.model.HistoryOrderBO;
import com.yoho.service.model.order.model.PackageBO;
import com.yoho.service.model.order.model.PaymentBO;
import com.yoho.service.model.order.model.SimpleGoodsBO;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.*;
import com.yoho.service.model.order.response.shopping.PromotionFormula;
import com.yoho.service.model.promotion.OrderPromotion;
import com.yoho.service.model.request.AreaReqBO;
import com.yoho.service.model.response.AreaRspBo;
import com.yoho.yhorder.common.cache.redis.OrderRedis;
import com.yoho.yhorder.common.cache.redis.UserOrderCache;
import com.yoho.yhorder.common.utils.*;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.domain.DeliveryAddress;
import com.yoho.yhorder.dal.domain.OrdersProcessStatus;
import com.yoho.yhorder.dal.domain.RefundNumber;
import com.yoho.yhorder.dal.domain.RefundNumberStatistics;
import com.yoho.yhorder.dal.model.*;
import com.yoho.yhorder.invoice.helper.MapUtil;
import com.yoho.yhorder.invoice.service.InvoiceService;
import com.yoho.yhorder.order.config.ServerURL;
import com.yoho.yhorder.order.event.OrderGoodsCommentEvent;
import com.yoho.yhorder.order.event.OrderSplitEvent;
import com.yoho.yhorder.order.event.TicketIssueEvent;
import com.yoho.yhorder.order.payment.alipay.AntHbfqPayService;
import com.yoho.yhorder.order.restapi.bean.ResponseBean;
import com.yoho.yhorder.order.service.*;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;


/**
 * YohoOrderServiceImpl 订单业务类
 *
 * @author zhangyonghui
 * @date 2015/11/4
 */
@Service
public class YohoOrderServiceImpl implements IYohoOrderService {

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private IRefundGoodsMapper refundGoodsMapper;

    @Autowired
    private IRefundGoodsDao refundGoodsDao;


    @Autowired
    private IRefundGoodsListDao refundGoodsListDao;

    @Autowired
    private IChangeGoodsMapper changeGoodsMapper;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;


    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    @Qualifier("mqErpService")
    private IErpService mqErpService;

    @Autowired
    private YohoodTicketInfoMapper yohoodTicketInfoMapper;

    @Autowired
    private YohoodTicketsMapper yohoodTicketsMapper;

    @Autowired
    private IOrderPromotionService orderPromotionService;

    @Autowired
    private IOrderMqService orderMqService;

    @Autowired
    private IExpressMapper expressMapper;

    @Autowired
    private IWaybillInfoDao waybillInfoDao;

    @Autowired
    private IPaymentDAO paymentDAO;

    @Autowired
    private IHistoryOrderDao historyOrderDao;

    @Autowired
    private IOrdersDeliveryAddressRepository ordersDeliveryAddressService;

    @Autowired
    private IOrdersProcessStatusService ordersStatusService;

    @Autowired
    private IOrdersYohoCoinDAO ordersYohoCoinDAO;

    @Autowired
    private AntHbfqPayService antHbfqPayService;

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Autowired
    private IYohoodSeatDAO yohoodSeatDAO;

    /**
     * 15 天
     */
    public static long orderExchangeLimitTime = 15;

    /**
     * 7天
     */
    public static long orderRefundLimitTime = 7;

    /**
     * 15 天的秒数
     */
    public static int SECS_OF_15_DAY = 86400 * 15;

    @Autowired
    private IOrderGoodsService orderGoodsService;

    @Autowired
    private ILogisticsInfoDao logisticsInfoDao;

    @Autowired
    private IOrderPayDAO orderPayDAO;

    @Autowired
    private IOrdersPrePayDao ordersPrePayDao;

    @Autowired
    private IOrderExtAttributeDAO orderExtAttributeDAO;

    @Autowired
    private IOrdersPayBankDAO orderPayBankDAO;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${erp.order.close.sync.type:RPC}")
    private String erpOrderCloseSyncType;

    @Autowired
    private OrderRedis orderRedis;

    @Autowired
    private UserOrderCache userOrderCache;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    /**
     * @see IYohoOrderService#findOrderCodeByCreateTimeBetween(OrdersStatRequest)
     */
    public OrdersStatResponse findOrderCodeByCreateTimeBetween(OrdersStatRequest request) {
        OrdersStatResponse response = new OrdersStatResponse();
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getLimit() == null || request.getLimit() < 1) {
            request.setLimit(100);
        }
        response.setPage(request.getPage());
        response.setLimit(request.getLimit());
        if (request.getStartTime() == null || request.getEndTime() == null) {
            response.setList(Collections.emptyList());
            return response;
        }
        int total = ordersMapper.selectCountByCreateTimeBetween(request.getStartTime(), request.getEndTime());
        if (total == 0) {
            response.setList(Collections.emptyList());
        } else {
            List<Long> orderCodes = ordersMapper.selectOrderCodeByCreateTimeBetween(request.getStartTime(), request.getEndTime(), (request.getPage() - 1) * request.getLimit(), request.getLimit());
            response.setList(orderCodes);
        }
        response.setPageTotal((int) Math.ceil((double) total / (double) request.getLimit()));
        return response;
    }

    /**
     * 获取待处理订单总数
     *
     * @param uid
     * @return
     * @see IYohoOrderService#findPendingOrderCountByUid(Integer)
     */
    @Override
    public int findPendingOrderCountByUid(Integer uid) {
        if (uid == null) {
            return 0;
        }
        return ordersMapper.selectCountByUidAndPaymentStatusAndPaymentTypeAndIsCancel(uid, Constants.NO, Orders.PAYMENT_TYPE_ONLINE, Constants.NO);
    }

    /**
     * 取消订单后，不赠送有货币
     *
     * @param orderCode
     * @param yohoCoinNum
     */
    @SuppressWarnings("unused")
    private void cacelDeliverYohoCoin(Long orderCode, int yohoCoinNum, Integer uid) {
        if (yohoCoinNum <= 0) {
            logger.info("Method cacelDeliverYohoCoin; The deliverYohoCoin is 0 of order. orderCode is {}", orderCode);
            return;
        }
        OrdersYohoCoin record = new OrdersYohoCoin();
        record.setOrderCode(orderCode);
        record.setStatus(OrdersYohoCoin.STATUS_CACEL_DELIVER);    // 订单取消，则不需要再赠送有货币

    }

    /**
     * 7天后赠送有货币
     */
    private void sendDeliverYohoCoin(Long orderCode, Integer orderId, Integer uid) {
        logger.info("Method sendDeliverYohoCoin; orderCode is {}. orderId is {}, uid is {}", orderCode, orderId, uid);

        if (null == orderCode || null == orderId || null == uid) {
            logger.warn("Method sendDeliverYohoCoin param is invalid; orderCode is {}. orderId is {}, uid is {}", orderCode, orderId, uid);
            return;
        }

        List<OrdersGoods> ordersGoodsList = ordersGoodsMapper.selectOrderGoodsByOrderId(Lists.newArrayList(orderId));
        if (CollectionUtils.isEmpty(ordersGoodsList)) {
            logger.warn("selectOrderGoodsByOrderId is empty. orderId is {}", orderId);
            return;
        }
        int yohoCoinNum = 0;
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            yohoCoinNum += ordersGoods.getGetYohoCoin();
        }

        OrdersYohoCoin record = new OrdersYohoCoin();
        record.setCreateTime((int) (System.currentTimeMillis() / 1000));
        record.setOrderCode(orderCode);
        record.setUid(uid);
        record.setStatus(OrdersYohoCoin.STATUS_NEED_DELIVER);    // 默认是0  需要赠送

        record.setYohoCoinNum(yohoCoinNum);
        // 7天后赠送有货币
        ordersYohoCoinDAO.insert(record);
    }

    private void throwServiceException(String message) {
        ServiceException serviceException = new ServiceException(ServiceError.ORDER_SERVICE_ERROR);
        serviceException.setParams(message);
        throw serviceException;
    }


    @Override
    public void confirmOrderByCode(Long orderCode) {
        logger.info("Confirm order by order code[{}].", orderCode);
        //(1)验证请求参数
        if (orderCode == null) {
            logger.warn("Confirm order fail, order code is empty");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        //(2)根据订单号查询订单
        Orders orders = ordersMapper.selectByOrderCode(orderCode.toString());
        if (orders == null) {
            logger.warn("Confirm order fail, can not find order by order code {}.", orderCode);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }


        //尚未发货，无法确认
        if (orders.getStatus() < 4) {
            logger.warn("Confirm order fail, the order {} is not ship.", orderCode);
            throw new ServiceException(ServiceError.ORDER_CONFIRM_FAILED_BY_STATUS_NOT_SHIPPED);
        }

        //确认收货的物流信息
        addOneLogistics(orders.getOrderCode(), "0", "", "已经签收", Byte.valueOf("0"), (int) (System.currentTimeMillis() / 1000));
        //首单满199元返200元优惠券
        sendCoupon(orders);
        //修改订单状态
        int updateTime = (int) (System.currentTimeMillis() / 1000);
        if ((updateTime - orders.getCreateTime()) > SECS_OF_15_DAY || orders.getStatus() == 6) {
            updateTime = 0;
        }
        orders.setStatus((byte) 6);
        orders.setUpdateTime(updateTime);
        ordersMapper.updateByPrimaryKeySelective(orders);
        // 确认订单后，清除order的待付款、待发货、待收货缓存
        userOrderCache.clearOrderCountCache(orders.getUid());
        sendConfirmOrderMessageToMQ(orders);
        //更新确认收货时间
        updateConfirmTime(orders);
        // 发一个延迟队列，7天后赠送有货币
        sendDeliverYohoCoin(orderCode, orders.getId(), orders.getUid());
        ordersStatusService.save(orders, 6);
        logger.info("Confirm order by order code[{}] success.", orderCode);
    }

    /**
     * 更新确认收货时间
     *
     * @param orders
     */
    private void updateConfirmTime(Orders orders) {
        int time = (int) (System.currentTimeMillis() / 1000);
        OrdersMeta a = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), OrdersMeta.TIMES);
        JSONObject js = new JSONObject();
        if (a == null) {
            OrdersMeta record = new OrdersMeta();
            record.setUid(orders.getUid());
            record.setOrderCode(orders.getOrderCode());
            record.setOrdersId(orders.getId());
            record.setMetaKey(OrdersMeta.TIMES);
            js.put("pay_finish_time", time);
            js.put("confirm_time", time);
            record.setMetaValue(js.toString());
            ordersMetaDAO.insert(record);
        } else {
            JSONObject son = JSONObject.parseObject(a.getMetaValue());
            if (son.getInteger("pay_finish_time") == null) {
                son.put("pay_finish_time", time);
            }
            if (son.getInteger("confirm_time") == null) {
                son.put("confirm_time", time);
            }
            a.setMetaValue(son.toString());
            ordersMetaDAO.updateByPrimaryKey(a);
        }
    }

    private void sendConfirmOrderMessageToMQ(Orders orders) {
        JSONObject message = new JSONObject();
        message.put("orderCode", orders.getOrderCode());
        message.put("status", 700);
        Map<String, Object> map = new HashMap<>();
        map.put("orderCode", orders.getOrderCode());
        map.put("uid", orders.getUid());
        mqErpService.confirmOrder(message, map);
    }

    @Override
    public OrderSuccessResponse success(OrderSuccessRequest orderSuccessRequest) {
        return null;
    }

    /**
     * 首单满199元返200元优惠券
     *
     * @param orders
     */
    private void sendCoupon(Orders orders) {
        if (orders.getAmount().doubleValue() >= 199) {
            int completedOrdersCount = ordersMapper.selectCountByUidAndStatus(orders.getUid(), Collections.singletonList(Integer.valueOf(6)));
            if (completedOrdersCount == 0) {
                // CouponSend couponSend = new CouponSend();
                // couponSend.setEventCode("SEND_ORDER_CONFIRM_COUPON");
                // couponSend.setEventType(1);
                // couponSend.setUid(orders.getUid());
                // couponSend.setOrderCode(orders.getOrderCode());
                // couponSend.setCreateTime((int) (System.currentTimeMillis() /
                // 1000));
                // couponSendDao.insert(couponSend); 5-15改为mq延时队列处理请求
                JSONObject request = new JSONObject();
                request.put("eventCode", "SEND_ORDER_CONFIRM_COUPON");
                request.put("eventType", 1);
                request.put("orderCode", orders.getOrderCode());
                request.put("createTime", System.currentTimeMillis() / 1000);
                request.put("uid", orders.getUid());
                orderMqService.sendOrderConfirmCoupon(request);
            } else {
                logger.info("can not send coupon for orders[{}],because of user[{}] completed orders count is {}", orders.getOrderCode(), orders.getUid(), completedOrdersCount);
            }
        } else {
            logger.info("can not send coupon for orders[{}],because of amount is {}", roundPrice(orders.getAmount()));
        }
    }

    /**
     * 添加确认收货的物流信息
     *
     * @param orderCode
     * @param waybillCode
     * @param acceptAddress
     * @param acceptRemark
     * @param logisticsType
     * @param ccreateTime
     */
    private void addOneLogistics(Long orderCode, String waybillCode, String acceptAddress, String acceptRemark, Byte logisticsType, int ccreateTime) {
        //state==1 的最多只有一条记录
        Byte state = Byte.valueOf("1");
        List<LogisticsInfo> logisticsInfos = logisticsInfoDao.selectByOrderCodeAndState(orderCode, state);
        if (logisticsInfos.isEmpty()) {
            LogisticsInfo logisticsInfo = new LogisticsInfo();
            logisticsInfo.setOrderCode(orderCode);
            logisticsInfo.setWaybillCode(waybillCode);
            logisticsInfo.setAcceptAddress(acceptAddress);
            logisticsInfo.setAcceptRemark(acceptRemark);
            logisticsInfo.setLogisticsType(logisticsType);
            logisticsInfo.setState(state);
            logisticsInfo.setCreateTime(ccreateTime);
            logisticsInfo.setDealWithCost(Boolean.FALSE);
            logisticsInfoDao.insert(logisticsInfo);
        } else {
            for (LogisticsInfo logisticsInfo : logisticsInfos) {
                logisticsInfo.setOrderCode(orderCode);
                logisticsInfo.setWaybillCode(waybillCode);
                logisticsInfo.setAcceptAddress(acceptAddress);
                logisticsInfo.setAcceptRemark(acceptRemark);
                logisticsInfo.setLogisticsType(logisticsType);
                logisticsInfo.setState(state);
                logisticsInfo.setCreateTime(ccreateTime);
                logisticsInfoDao.updateByPrimaryKey(logisticsInfo);
            }
        }
    }

    @Override
    public void deleteOrderByCode(Long orderCode) {
        logger.info("Delete order by order code[{}].", orderCode);
        //(1)验证请求参数
        if (orderCode == null) {
            logger.warn("order code is empty");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        //(2)根据订单号查询订单
        Orders orders = ordersMapper.selectByOrderCode(orderCode.toString());
        if (orders == null) {
            logger.warn("Can not find order by order code[{}] from db.", orderCode);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        //(3)删除订单
        orders.setOrdersStatus(Orders.ORDERS_STATUS_DELETE);
        ordersMapper.updateByPrimaryKeySelective(orders);
        logger.info("Delete order by order code[{}] success.", orderCode);
    }

    @Override
    public TicketsQr getQrByOrderCode(Long orderCode, Integer uid) {
        //验证请求参数
        if (orderCode == null) {
            logger.warn("GetQrByOrderCode fail, request orderCode is empty");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        //根据订单号查询订单
        Orders orders = ordersMapper.selectByOrderCode(String.valueOf(orderCode));
        if (orders == null) {
            logger.warn("GetQrByOrderCode fail, order {} is null", orderCode);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        //验证订单可以展示二维码
        validateOrderCanShowQrcode(orders);
        //封装返回数据
        TicketsQr qr = new TicketsQr();
        // 根据订单ID获取电子票的商品名称
        qr.setTitle(getQrTitle(orders));
        // 根据订单号获取所有的票
        qr.setTicks(getQrTickets(orders));
        return qr;
    }

    /**
     * 验证订单可以展示二维码
     *
     * @param orders
     */
    private void validateOrderCanShowQrcode(Orders orders) {
        if (orders.getStatus() < 4) {
            logger.info("can not show qrcode, order {} has not sent.", orders.getOrderCode());
            throwServiceException("该订单尚未发货，无法查看二维码");
        }
        if (Constants.NO.equals(orders.getPaymentStatus())) {
            logger.info("can not show qrcode, order {} has not paid.", orders.getOrderCode());
            throwServiceException("该订单尚未付款，无法查看二维码");
        }
        if (Constants.YES.equals(orders.getIsCancel())) {
            logger.info("can not show qrcode, order {} is cancel", orders.getOrderCode());
            throwServiceException("该订单已取消，无法查看二维码");
        }
        if (!Orders.ATTRIBUTE_VIRTUAL.equals(orders.getAttribute())) {
            logger.info("can not show qrcode, order {} is not a virtual order.", orders.getOrderCode());
            throwServiceException("该订单不是虚拟物品订单，无法查看二维码");
        }
    }

    /**
     * 根据订单ID获取电子票的商品名称
     *
     * @param orders
     * @return
     */
    private String getQrTitle(Orders orders) {
        List<OrdersGoods> ordersGoodses = ordersGoodsMapper.selectOrderGoodsByOrderId(Collections.singletonList(orders.getId()));
        // 电子票中OrdersGoods数量只可能为0个或1个
        if (ordersGoodses.isEmpty()) {
            return null;
        }
        Integer productId = ordersGoodses.get(0).getProductId();
        BaseRequest<Integer> request = new BaseRequest<>();
        request.setParam(productId);
        ProductBo productBo = serviceCaller.call("product.queryProductBasicInfo", request, ProductBo.class);
        if (productBo == null) {
            return null;
        } else {
            return productBo.getProductName();
        }
    }

    /**
     * 根据订单号获取所有的票
     *
     * @param orders
     * @return
     */
    private List<TicketsQr.Tick> getQrTickets(Orders orders) {
        // 获取yohood门票类型
        OrdersMeta virtualInfoMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), "virtual_info");
        if (virtualInfoMeta == null) {
            logger.warn("getQrTickets fail, yohood ticket type is empty.");
            throw new ServiceException(ServiceError.ORDER_TICKET_TYPE_IS_EMPTY);
        }
        String ticketType = "";
        JSONObject json = JSONObject.parseObject(virtualInfoMeta.getMetaValue());
        if (json != null && StringUtils.isNotBlank(json.getString("ticket_type"))) {
            ticketType = json.getString("ticket_type");
        }
        List<YohoodTickets> yohoodTicketses = yohoodTicketsMapper.selectByOrderCode(orders.getOrderCode());
        if (CollectionUtils.isEmpty(yohoodTicketses)) {
            logger.warn("getQrTickets fail, yohood ticket is empty.");
            throw new ServiceException(ServiceError.ORDER_TICKET_IS_EMPTY);
        }
        List<Long> ticketCodes = new ArrayList<>();
        for (YohoodTickets tickets : yohoodTicketses) {
            Long ticketCode = tickets.getTicketCode();
            ticketCodes.add(ticketCode);
        }
        List<YohoodTicketInfo> yohoodTicketInfos = yohoodTicketInfoMapper.selectByTicketCodes(ticketCodes);
        Map<Long, YohoodTicketInfo> yohoodTicketInfoMap = MapUtil.transformMap(yohoodTicketInfos, new MapUtil.Function<YohoodTicketInfo, Long>() {
            @Override
            public Long apply(YohoodTicketInfo input) {
                return input.getTicketCode();
            }
        });
        Map<Long, YohoodSeat> yohoodSeatMap = new HashMap<>();
        if (YohoodType.PACKAGE_TICKET.equals(ticketType)) {
            List<YohoodSeat> yohoodSeats = yohoodSeatDAO.selectByTicketCodes(ticketCodes);
            yohoodSeatMap = MapUtil.transformMap(yohoodSeats, new MapUtil.Function<YohoodSeat, Long>() {
                @Override
                public Long apply(YohoodSeat input) {
                    return input.getTicketCode();
                }
            });
        }
        List<TicketsQr.Tick> ticks = new ArrayList<>(yohoodTicketses.size());
        for (YohoodTickets tickets : yohoodTicketses) {
            TicketsQr.Tick tick = new TicketsQr.Tick();
            tick.setTicketCode(String.valueOf(tickets.getTicketCode()));
            tick.setPasskit(Constants.YES);
            tick.setQrImage(yohoodTicketInfoMap.get(tickets.getTicketCode()).getTicketUrl());
            tick.setTicketType(ticketType);
            tick.setEntranceTime(DateUtil.format(tickets.getBeginDate(), DateUtil.MM_dd_HH_mm));
            if (YohoodType.PACKAGE_TICKET.equals(ticketType)) {
                YohoodSeat yohoodSeat = yohoodSeatMap.get(tickets.getTicketCode());
                String seatNo = yohoodSeat.getArea() + yohoodSeat.getRowNo() + "排" + yohoodSeat.getColumnNo() + "座";
                tick.setSeatNo(seatNo);
            }
            ticks.add(tick);
        }
        return ticks;
    }

    @Override
    public void updateOrdersPaymentByCode(Long orderCode, Byte payment) {
        logger.info("UpdateOrdersPaymentByCode, order code is {} and payment is {}.", orderCode, payment);
        //(1)验证请求参数
        if (orderCode == null) {
            logger.warn("UpdateOrdersPaymentByCode fail, order code is empty");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        if (payment == null) {
            logger.warn("UpdateOrdersPaymentByCode fail, payment is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        //(2)根据订单号查询订单
        Orders orders = ordersMapper.selectByOrderCode(orderCode.toString());
        if (orders == null) {
            logger.warn("UpdateOrdersPaymentByCode fail, the order {} is null", orderCode);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }

        // 判断订单支付状态
        if (!isOrderPayable(orders)) {
            logger.warn("UpdateOrdersPaymentByCode fail, order status is not payable, orderCode: {}, status: {}, paymentStatus: {}, isCancel: {}",
                    orderCode, orders.getStatus(), orders.getPaymentStatus(), orders.getIsCancel());
            throw new ServiceException(ServiceError.ORDER_PAY_NOT_ALLOW);
        }

        //(3)修改支付方式
        orders.setPayment(payment);
        ordersMapper.updateByPrimaryKeySelective(orders);
        logger.info("UpdateOrdersPaymentByCode success, order code is {} and payment is {}.", orderCode, payment);
    }

    @Override
    public void updateOrderPaymentStatusById(Integer id, Byte payment, String paymentStatus, String bankCode) {
        logger.info("UpdateOrderPaymentStatusById, id is {} payment is {} payment status is {} bank code is {}.", id, payment, paymentStatus, bankCode);
        //(1)验证请求参数
        if (id == null) {
            logger.warn("UpdateOrderPaymentStatusById fail, id is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (payment == null) {
            logger.warn("UpdateOrderPaymentStatusById fail, payment is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (paymentStatus == null) {
            logger.warn("UpdateOrderPaymentStatusById fail, payment status is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (bankCode == null) {
            logger.warn("UpdateOrderPaymentStatusById fail, bank code is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        //(2)根据id查询订单
        Orders orders = ordersMapper.selectByPrimaryKey(id);
        if (orders == null) {
            logger.warn("UpdateOrderPaymentStatusById fail, can not find order by id {}.", id);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        //(3)修改支付方式
        orders.setPayment(payment);
        orders.setPaymentStatus(paymentStatus);
        orders.setBankCode(bankCode);
        ordersMapper.updateByPrimaryKeySelective(orders);

        // 发送发放电子门票事件
        sendTicketIssueEvent(orders);

        //异步处理jit拆单
        publishOrderSplitEvent(orders);

        logger.info("UpdateOrderPaymentStatusById success, id is {} payment is {} payment status is {} bank code is {}.", id, payment, paymentStatus, bankCode);
    }


    @Override
    public void updateOrderStatusById(Integer id, Byte status, Integer updateTime) {
        logger.info("UpdateOrderStatusById, id is {} status is {} updateTime is {}.", id, status, updateTime);
        //(1)验证请求参数
        if (id == null) {
            logger.warn("UpdateOrderStatusById fail, id is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (status == null) {
            logger.warn("UpdateOrderStatusById fail, status is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (updateTime == null) {
            logger.warn("UpdateOrderStatusById fail, update time is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        //(2)根据id查询订单
        Orders orders = ordersMapper.selectByPrimaryKey(id);
        if (orders == null) {
            logger.warn("UpdateOrderStatusById fail, can not find order by id {}.", id);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        OrdersMeta virtualInfoMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), "virtual_info");
        String virtualType = "";
        if (virtualInfoMeta != null) {
            JSONObject json = JSONObject.parseObject(virtualInfoMeta.getMetaValue());
            if (json != null && StringUtils.isNotBlank(json.getString("virtual_type")) && "3".equals(json.getString("virtual_type")) && status == 1) {
                status = 6;
                virtualType = json.getString("virtual_type");
            }
        }
        //(3)修改订单状态
        orders.setUpdateTime(updateTime);
        orders.setStatus(status);
        ordersMapper.updateByPrimaryKeySelective(orders);
        if (StringUtils.isNotBlank(virtualType) && "3".equals(virtualType) && status == 6) {
            // 插入待评价记录
            OrderGoodsCommentEvent orderGoodsCommentEvent = new OrderGoodsCommentEvent();
            orderGoodsCommentEvent.setOrders(orders);
            publisher.publishEvent(orderGoodsCommentEvent);

        }
        logger.info("UpdateOrderStatusById success, id is {} status is {} updateTime is {}.", id, status, updateTime);
    }

    /**
     * 发送发放电子门票事件
     */
    private void sendTicketIssueEvent(Orders orders) {
        OrdersMeta virtualInfoMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), "virtual_info");
        if (virtualInfoMeta != null) {
            JSONObject json = JSONObject.parseObject(virtualInfoMeta.getMetaValue());
            if (json != null && StringUtils.isNotBlank(json.getString("virtual_type")) && "3".equals(json.getString("virtual_type"))) {
                // 发放电子门票
                TicketIssueEvent ticketIssueEvent = new TicketIssueEvent();
                ticketIssueEvent.setOrders(orders);
                publisher.publishEvent(ticketIssueEvent);
            }
        }
    }

    @Override
    public void paySuccess(Integer id, Byte payment, String bankCode) {
        logger.info("PaySuccess, id is {} payment is {} bankCode is {}.", id, payment, bankCode);
        if (id == null) {
            logger.warn("PaySuccess fail, request id is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        Orders orders = ordersMapper.selectByPrimaryKey(id);
        if (orders == null) {
            logger.warn("PaySuccess fail, can not find order by id {}.", id);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        // 如果该订单已经支付，则不重新支付
        if (Constants.YES.equals(orders.getPaymentStatus())) {
            logger.info("PaySuccess success, the order {} has been paid, you do not need to pay.", orders.getOrderCode());
            return;
        }
        orders.setPayment(payment);
        orders.setPaymentStatus(Constants.YES);
        orders.setBankCode(bankCode);
        orders.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        if (Orders.ATTRIBUTE_VIRTUAL.equals(orders.getAttribute())) {
            orders.setStatus((byte) 6);
        } else {
            orders.setStatus((byte) 1);
        }
        ordersMapper.updateByPrimaryKeySelective(orders);

        // 发送发放电子门票和插入待评价记录事件
        sendTicketEvent(orders);

        //订单已支付
        orderRedis.removeUserUnPayOrder(orders.getUid(), orders.getOrderCode());

        // 支付成功后，清除order的待付款、待发货、待收货缓存
        userOrderCache.clearOrderCountCache(orders.getUid());

        //支付成功后，更新支付完成时间
        updatePayFinishedTime(orders);

        //支付成功后,多jit订单进行拆分,异步处理
        publishOrderSplitEvent(orders);

        logger.info("PaySuccess success, the order {} has been paid", orders.getOrderCode());
    }

    /**
     * 发送发放电子门票和插入待评价记录事件
     */
    private void sendTicketEvent(Orders orders) {
        OrdersMeta virtualInfoMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), "virtual_info");
        if (virtualInfoMeta != null) {
            JSONObject json = JSONObject.parseObject(virtualInfoMeta.getMetaValue());
            if (json != null && StringUtils.isNotBlank(json.getString("virtual_type")) && "3".equals(json.getString("virtual_type"))) {
                // 发放电子门票
                TicketIssueEvent ticketIssueEvent = new TicketIssueEvent();
                ticketIssueEvent.setOrders(orders);
                publisher.publishEvent(ticketIssueEvent);
                // 插入待评价记录
                OrderGoodsCommentEvent orderGoodsCommentEvent = new OrderGoodsCommentEvent();
                orderGoodsCommentEvent.setOrders(orders);
                publisher.publishEvent(orderGoodsCommentEvent);
            }
        }
    }

    /**
     * 更新支付时间
     *
     * @param orders
     */
    private void updatePayFinishedTime(Orders orders) {
        int time = (int) (System.currentTimeMillis() / 1000);
        OrdersMeta a = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), OrdersMeta.TIMES);
        JSONObject js = new JSONObject();
        if (a == null) {
            OrdersMeta record = new OrdersMeta();
            record.setUid(orders.getUid());
            record.setOrderCode(orders.getOrderCode());
            record.setOrdersId(orders.getId());
            record.setMetaKey(OrdersMeta.TIMES);
            js.put("pay_finish_time", time);
            record.setMetaValue(js.toString());
            ordersMetaDAO.insert(record);
        }
    }

    @Override
    public OrdersStatusStatistics getOrdersStatusStatisticsByUid(Integer uid) {
        //(1)验证请求参数
        if (uid == null) {
            logger.warn("GetOrdersStatusStatisticsByUid fail, request uid is empty.");
            throw new ServiceException(ServiceError.ORDER_UID_IS_EMPTY);
        }
        OrdersStatusStatistics ordersStatusStatistics = userOrderCache.getOrdersStatusStatisticsByUid(uid.intValue());
        if (ordersStatusStatistics == null) {
            int waitPayNum = ordersMapper.selectCountByUidAndStatusAndPaymentStatusAndIsCancel(uid, Arrays.asList(new Integer[]{0}), "N", "N");
            int waitCargoNum = ordersMapper.selectCountByUidAndStatusAndPaymentStatusAndIsCancel(uid, Arrays.asList(new Integer[]{1, 2, 3}), null, "N");
            int sendCargoNum = ordersMapper.selectCountByUidAndStatusAndPaymentStatusAndIsCancel(uid, Arrays.asList(new Integer[]{4, 5}), null, "N");
            int refundGoodsCount = refundGoodsMapper.selectCountByUidAndStatusLessThan(uid, (byte) 40);
            ordersStatusStatistics = new OrdersStatusStatistics();
            ordersStatusStatistics.setPendingPaymentCount(waitPayNum);
            ordersStatusStatistics.setDueOutGoodsCount(waitCargoNum);
            ordersStatusStatistics.setDueInGoodsCount(sendCargoNum);
            ordersStatusStatistics.setRefundGoodsCount(refundGoodsCount);
            userOrderCache.cacheOrdersStatusStatisticsByUid(uid.intValue(), ordersStatusStatistics);
            return ordersStatusStatistics;
        } else {
            return ordersStatusStatistics;
        }
    }

    @Override
    public int getOrdersCountByUidAndStatus(Integer uid, List<Integer> status) {
        //(1)验证请求参数
        if (uid == null) {
            logger.warn("GetOrdersCountByUidAndStatus fail, request uid is empty.");
            throw new ServiceException(ServiceError.ORDER_UID_IS_EMPTY);
        }
        if (status == null || status.isEmpty()) {
            status = null;
        }
        return ordersMapper.selectCountByUidAndStatus(uid, status);
    }


    /**
     * 获取订单列表总数
     *
     * @param request
     * @return
     */
    public int getOrderListCount(OrderListRequest request) {
        // 解析请求参数
        parseOrderListRequest(request);
        // 如果 是 1：全部订单* 2：待付款 * 3：待发货 * 4：待收货，则先从缓存中取，取不到再到数据库中查。
        Integer count = userOrderCache.getOrderListCount(Integer.parseInt(request.getUid()), request.getType());
        if (count == null) {
            if (request.getStatus() != null) {
                count = ordersMapper.selectCountByUidAndStatusAndPaymentStatusAndIsCancel(Integer.valueOf(request.getUid()), Arrays.asList(request.getStatus()), request.getPaymentStatus(), request.getIsCancel());
            } else {
                count = ordersMapper.selectCountByUidAndStatusAndPaymentStatusAndIsCancel(Integer.valueOf(request.getUid()), null, request.getPaymentStatus(), request.getIsCancel());
            }
            userOrderCache.cacheOrderListCount(Integer.parseInt(request.getUid()), request.getType(), count);
            return count;
        } else {
            return count;
        }
    }

    /**
     * 解析请求参数
     *
     * @param request
     */
    private void parseOrderListRequest(OrderListRequest request) {
        // 验证请求参数
        if (StringUtils.isEmpty(request.getUid())) {
            logger.warn("GetOrderList fail, request uid is empty.");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (request.getType() == null) {
            logger.warn("GetOrderList fail, request type is empty.");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        // 清空私有参数
        request.setStatus(null);
        request.setIsCancel(null);
        request.setPaymentStatus(null);
        // 设置分页参数
        request.setPage((request.getPage() != null && request.getPage() > 0) ? request.getPage() - 1 : 0);
        request.setLimit((request.getLimit() != null && request.getLimit() > 0) ? request.getLimit() : 15);
        /*
         * 1：全部订单
         * 2：待付款
         * 3：待发货
         * 4：待收货
         * 5：待评论  成功订单
         * 7：失败 取消 订单
         */
        switch (request.getType()) {
            case 1:
                break;
            case 2:
                request.setStatus(new Integer[]{0});
                request.setIsCancel("N");
                request.setPaymentStatus("N");
                break;
            case 3:
                request.setStatus(new Integer[]{1, 2, 3});
                request.setIsCancel("N");
                break;
            case 4:
                request.setStatus(new Integer[]{4, 5});
                request.setIsCancel("N");
                break;
            case 5:
                request.setStatus(new Integer[]{6});
                request.setIsCancel("N");
                break;
            case 7:
                request.setIsCancel("Y");
                break;
        }
    }


    /**
     * 获取订单列表
     *
     * @param request
     * @return
     */
    public List<Orders> getOrderList(OrderListRequest request) {
        logger.info("GetOrderList start, request uid is {} type is {} page is {} limit is {}", request.getUid(), request.getType(), request.getPage(), request.getLimit());
        parseOrderListRequest(request);
        // 分页查询订单列表
        List<Orders> orderList = findOrdersByRequest(request);
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }
        // 查询订单商品信息
        List<OrdersGoods> orderGoodsList = findOrdersGoodsByOrders(orderList);
        if (CollectionUtils.isEmpty(orderGoodsList)) {
            return orderList;
        }
        List<Long> orderCodeList = new ArrayList<>();
        for (Orders orders : orderList) {
            orders.setIsComment("N");
            orderCodeList.add(orders.getOrderCode());
        }
        //根据订单编号查找退货商品的数量
        List<RefundNumberStatistics> refundNumberStatisticses = refundGoodsListDao.selectRefundNumberStatisticsByOrderCodes(orderCodeList);
        List<ProductBo> productBos = findProductBoByOrderGoodses(orderGoodsList);
        for (OrdersGoods ordersGoods : orderGoodsList) {
            // 从商品中拷贝数据
            copyOrdersGoodsPropertiesFromProduct(ordersGoods, productBos);
            // 从退货信息中拷贝数据
            ordersGoods.setRefundNum(getRefundNum(ordersGoods, refundNumberStatisticses));
        }
        //遍历订单与订单商品的对应关系
        setOrderListGoods(orderList, orderGoodsList);


        //
        List<OrderPay> userConfirmPaidList = orderPayDAO.selectByOrderCodes(orderCodeList);
        setupUserConfirmPaid(orderList, userConfirmPaidList);
        List attrList = orderExtAttributeDAO.selectByUidAndOrderCodes(Integer.valueOf(request.getUid()), orderCodeList);
        setupUseLimitCodeFlag(orderList, attrList);
        for (Orders orders : orderList) {
            if (orders.getStatus() < 4) {
                if (Constants.YES.equals(orders.getPaymentStatus())) {
                    orders.setStatus((byte) 1);
                } else {
                    orders.setStatus((byte) 0);
                }
            }
        }

        // 设置yohood门票
        setVirtualInfo(orderList);

        return buildConfirmTimeAndPayTime(orderList);
    }

    /**
     * 设置yohood门票
     */
    private void setVirtualInfo(List<Orders> orderList) {
        List<Integer> ordersIds = new ArrayList<>();
        for (Orders order : orderList) {
            ordersIds.add(order.getId());
        }
        List<OrdersMeta> virtualInfoMetas = ordersMetaDAO.selectByOrdersIdsAndMetaKey(ordersIds, "virtual_info");
        Map<Integer, OrdersMeta> virtualInfoMetaMap = MapUtil.transformMap(virtualInfoMetas, new MapUtil.Function<OrdersMeta, Integer>() {
            @Override
            public Integer apply(OrdersMeta input) {
                return input.getOrdersId();
            }
        });
        for (Orders orders : orderList) {
            OrdersMeta virtualInfoMeta = virtualInfoMetaMap.get(orders.getId());
            if (virtualInfoMeta == null) {
                continue;
            }
            JSONObject json = JSONObject.parseObject(virtualInfoMeta.getMetaValue());
            if (json != null && StringUtils.isNotBlank(json.getString("virtual_type"))) {
                orders.setVirtualType(json.getString("virtual_type"));
            }
        }
    }

    private List<Orders> buildConfirmTimeAndPayTime(List<Orders> orders) {
        if (orders == null || orders.size() == 0) {
            return orders;
        }
        List<Integer> ordersIds = new ArrayList<Integer>();
        for (Orders order : orders) {
            ordersIds.add(order.getId());
        }
        List<OrdersMeta> listMeta = ordersMetaDAO.selectByOrdersIdsAndMetaKey(ordersIds, "times");
        if (listMeta == null || listMeta.size() == 0) {
            return orders;
        }
        for (Orders order : orders) {
            for (OrdersMeta meta : listMeta) {
                //id相同,再匹配
                if (meta.getOrdersId() == order.getId()) {
                    JSONObject json = JSONObject.parseObject(meta.getMetaValue());
                    if (json.getInteger("pay_finish_time") != null) {
                        order.setPay_finish_time(String.valueOf(json.getInteger("pay_finish_time")));
                    }
                    if (json.getInteger("confirm_time") != null) {
                        order.setPay_finish_time(String.valueOf(json.getInteger("confirm_time")));
                    }
                    break;
                }
            }
        }
        return orders;
    }

    /**
     * 获取历史订单总数
     *
     * @param request
     * @return
     */
    @Override
    public int getHistoryOrderCount(OrderListRequest request) {
        // 验证请求参数
        if (StringUtils.isEmpty(request.getUid())) {
            logger.warn("getHistoryOrderCount fail, request uid is empty.");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }

        return historyOrderDao.selectCountByUid(Integer.valueOf(request.getUid()));
    }

    /**
     * 获取历史订单列表
     *
     * @param request
     * @return
     */
    @Override
    public List<HistoryOrderBO> getHistoryOrderList(OrderListRequest request) {
        // 验证请求参数
        if (StringUtils.isEmpty(request.getUid())) {
            logger.warn("getHistoryOrderList fail, request uid is empty.");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }

        // 设置分页参数
        request.setPage((request.getPage() != null && request.getPage() > 0) ? request.getPage() - 1 : 0);
        request.setLimit((request.getLimit() != null && request.getLimit() > 0) ? request.getLimit() : 10);

        List<HistoryOrderBO> historyOrderBOs = new ArrayList<>();
        List<HistoryOrder> historyOrders = historyOrderDao.selectByUid(Integer.valueOf(request.getUid()), request.getPage() * request.getLimit(), request.getLimit());
        for (HistoryOrder historyOrder : historyOrders) {
            HistoryOrderBO historyOrderBO = new HistoryOrderBO();
            BeanUtils.copyProperties(historyOrder, historyOrderBO);
            historyOrderBOs.add(historyOrderBO);
        }

        return historyOrderBOs;
    }

    private void copyOrdersGoodsPropertiesFromProduct(OrdersGoods ordersGoods, List<ProductBo> productBos) {
        ProductBo productBo = findProductBoByProductId(ordersGoods.getProductId(), productBos);
        if (productBo != null) {
            ordersGoods.setBrand(productBo.getBrand());
            ordersGoods.setCnAlphabet(productBo.getCnAlphabet());
            ordersGoods.setProductName(productBo.getProductName());
            ordersGoods.setProductSkn(productBo.getErpProductId());
            ordersGoods.setExpectArrivalTime(productBo.getExpectArrivalTime() == null ? "" : productBo.getExpectArrivalTime() + "月");
            GoodsBo goodsBo = findGoodsBoByGoodsId(ordersGoods.getGoodsId(), productBo.getGoodsList());
            if (goodsBo != null) {
                ordersGoods.setGoodsName(goodsBo.getGoodsName());
                ordersGoods.setGoodsImg(goodsBo.getGoodsImagesList());
            } else {
                logger.warn("can not find goods[{}] from product[{}]", ordersGoods.getGoodsId(), productBo.getId());
            }
        } else {
            logger.warn("can not find product[{}]", ordersGoods.getId());
        }
    }

    /**
     * 查询订单商品信息
     *
     * @param orderList
     * @return
     */
    private List<OrdersGoods> findOrdersGoodsByOrders(List<Orders> orderList) {
        List<Integer> orderIdList = new ArrayList<>();
        for (Orders orders : orderList) {
            orderIdList.add(orders.getId());
        }
        List<OrdersGoods> orderGoodsList = orderGoodsService.selectOrderGoodsByOrderId(orderIdList);
        for (OrdersGoods ordersGoods : orderGoodsList) {
            for (Orders orders : orderList) {
                if (orders.getId().equals(ordersGoods.getOrderId())) {
                    ordersGoods.setOrderCode(orders.getOrderCode());
                }
            }
        }
        return orderGoodsList;
    }

    private List<Orders> findOrdersByRequest(OrderListRequest request) {
        List<Orders> orderList;
        if (request.getStatus() == null) {
            orderList = ordersMapper.selectByUidAndStatusAndPaymentStatusAndIsCancel(Integer.valueOf(request.getUid()), null, request.getPaymentStatus(), request.getIsCancel(), request.getPage() * request.getLimit(), request.getLimit());
        } else {
            orderList = ordersMapper.selectByUidAndStatusAndPaymentStatusAndIsCancel(Integer.valueOf(request.getUid()), Arrays.asList(request.getStatus()), request.getPaymentStatus(), request.getIsCancel(), request.getPage() * request.getLimit(), request.getLimit());
        }
        return orderList;
    }

    private int getRefundNum(OrdersGoods ordersGoods, List<RefundNumberStatistics> refundNumBatch) {
        for (RefundNumberStatistics refundNum : refundNumBatch) {
            if (refundNum.getOrderCode().equals(ordersGoods.getOrderCode())
                    && refundNum.getProductSku().equals(ordersGoods.getErpSkuId())
                    && refundNum.getLastPrice().equals(ordersGoods.getGoodsPrice())) {
                return refundNum.getNumber();
            }
        }
        return 0;
    }

    /**
     * 将分页中所有的订单商品根据订单号区分
     *
     * @param orderList       分页的订单list
     * @param ordersGoodsList 分页中所有的订单商品list
     */
    private void setOrderListGoods(List<Orders> orderList, List<OrdersGoods> ordersGoodsList) {
        BigDecimal orderGoodsTotalAmount = new BigDecimal(0);
        if (CollectionUtils.isEmpty(orderList) || CollectionUtils.isEmpty(ordersGoodsList)) {
            return;
        }
        for (Orders orders : orderList) {
            List<OrdersGoods> ordersGoodsListPart = new ArrayList();
            for (OrdersGoods ordersGoods : ordersGoodsList) {
                if (orders.getId().equals(ordersGoods.getOrderId())) {
                    ordersGoodsListPart.add(ordersGoods);
                    orderGoodsTotalAmount = orderGoodsTotalAmount.add(ordersGoods.getGoodsAmount());
                    // 不存在预售商品的将parentordercode清空
                    if (ordersGoods.getGoodsType().intValue() != 6) {
                        orders.setParentOrderCode(0L);
                    }
                }
            }
//            设置所有商品总价
            orders.setGoodsTotalAmount(orderGoodsTotalAmount);
            orders.setOrdersGoodsList(ordersGoodsListPart);
        }

    }

    /**
     * 用户确认已经完成
     *
     * @param ordersList
     * @param paidList
     */
    private void setupUserConfirmPaid(List<Orders> ordersList, List<OrderPay> paidList) {
        if (CollectionUtils.isNotEmpty(paidList)) {
            for (Orders order : ordersList) {
                for (OrderPay orderPay : paidList) {
                    if (orderPay.getOrderCode().doubleValue() == order.getOrderCode().doubleValue()) {
                        order.setUser_confirm_paid("Y");
                        break;
                    }
                }
            }
        }
    }

    private void setupUseLimitCodeFlag(List<Orders> ordersList, List<OrderExtAttribute> attributeList) {
        if (CollectionUtils.isNotEmpty(attributeList)) {
            for (Orders order : ordersList) {
                for (OrderExtAttribute attribute : attributeList) {
                    if (attribute.getOrderCode() == order.getOrderCode().doubleValue()) {
                        order.setUse_limit_code("Y");
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Orders getOrderByCode(String orderCode) {
        return ordersMapper.selectByOrderCode(orderCode);
    }


    @Autowired
    private InvoiceService invoiceService;

    @Override
    public OrderInfoResponse getOrderDetail(int uid, long orderCode) {
        logger.info("handler order detail, orderCode is {}, uid is {}", orderCode, uid);
        //根据订单编号和用户编号查询订单信息
        Orders orders = ordersMapper.selectByUidAndOrderCode(uid, orderCode);
        if (orders == null) {
            logger.info("handler order detail fail, orderCode is {}, uid is {} can not find order from db.", orderCode, uid);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        //根据订单信息查找订单商品集合
        List<OrdersGoods> ordersGoodsList = orderGoodsService.selectOrderGoodsByOrder(orders);
        //根据订单编号和订单商品集合查找换货商品的数量
        List<Map<String, ?>> changeNumBatch = findChangeNumByOrderCodeAndOrdersGoodses(orderCode, ordersGoodsList);
        //根据订单编号和订单商品集合查找退货商品的数量
        List<RefundNumberStatistics> refundNumBatch = findRefundNumberStatisticsByOrderCodeAndOrdersGoodses(orders.getOrderCode(), ordersGoodsList);

        //根据订单编号和订单商品集合查找  纯退货的商品数量--不包含换货生成的退货单
        List<RefundNumberStatistics> refundNumPure = findPureRefundNumberStatisticsByOrderCode(orders.getOrderCode());
        //封装方法返回结果
        OrderInfoResponse orderInfoResponse = new OrderInfoResponse();
        // 设置能否修改收货人地址
        setOrderDetailCanUpdateDeliveryAddress(orderInfoResponse, orders);
        //设置订单信息
        setOrdersInfo(orderInfoResponse, orders);
        //设置物流信息
        setExpressInfo(orderInfoResponse, orders);
        orderInfoResponse.setCan_comment("Y");
        // 设置是否有关联订单
        if (orders.getParentOrderCode() != null && orders.getParentOrderCode() > 0) {
            orderInfoResponse.setRelateOrderCode("Y");
        } else {
            orderInfoResponse.setRelateOrderCode("N");
        }
        // 设置yohood门票
        OrdersMeta virtualInfoMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), "virtual_info");
        if (virtualInfoMeta != null) {
            JSONObject json = JSONObject.parseObject(virtualInfoMeta.getMetaValue());
            if (json != null && StringUtils.isNotBlank(json.getString("virtual_type"))) {
                orderInfoResponse.setVirtualType(json.getString("virtual_type"));
            }
        }

        //根据订单商品集合查找产品订单
        List<ProductBo> productBoList = findProductBoByOrderGoodses(ordersGoodsList);
        //设置订单商品信息
        setOrdersGoods(orderInfoResponse, orders, ordersGoodsList, changeNumBatch, refundNumBatch, productBoList, refundNumPure);
        //生成订单地址信息中的，省市信息
        orderInfoResponse.setArea(fetchAreaForOrderDetail(orders.getAreaCode()));
        //设置是否可以退货的商品
        setIsSupportRefundAndExchange(orderInfoResponse, orders, ordersGoodsList, refundNumBatch);
        //设置快递送货方式
        setTheCourier(orderInfoResponse, orders);
        setExpressCompany(orderInfoResponse, orders);
        setPromotionFormulas(orderInfoResponse, orders, ordersGoodsList);
        //从order_pay表查询用户是否确认支付完成信息
        if (isFinishPaidByUser(orders)) {
            orderInfoResponse.setUser_confirm_paid("Y");
        }
        /**
         * 为了不影响老版本的再次购物功能,只能这么矬了
         * 限购码和虚拟商品 不能再次购物
         */
        if (haveLimitCode(orders) || orders.getAttribute() == Constants.ATTRIBUTE_VIRTUAL) {
            orderInfoResponse.setUse_limit_code("Y");
        }
        // orderInfoResponse.setYohoGiveCoin(orders.getDeliverYohoCoin()); 遍历订单商品的时候，已经根据商品列表算出总得返有货币数
        // 设置订单包裹
        setOrderInfoPackageList(orders, orderInfoResponse);
        //设置支付时间和确认收货时间
        findMetaTimes(orderInfoResponse, orders.getId());
        //发票
        InvoiceBo invoice = getInvoiceIfPresent(orders);
        if (invoice != null) {
            orderInfoResponse.setInvoice(invoice);
        }
        logger.info("handler order detail success, orderCode is {}, uid is {}, response is {}.", orderCode, uid, orderInfoResponse);
        return orderInfoResponse;
    }

    private InvoiceBo getInvoiceIfPresent(Orders orders) {
        InvoiceBo invoice = invoiceService.queryByOrderId(orders.getId());
        if (invoice == null) {//电子发票版本的记录中没有

            boolean isInvoice = StringUtils.isNotBlank(orders.getIsInvoice()) && orders.getIsInvoice().equals("Y");
            if (isInvoice) {
                invoice = new InvoiceBo();
                //默认的就是纸质发票
                invoice.setType(InvoiceType.page.getIntVal());
                if (StringUtils.isNotBlank(orders.getInvoicesType())){
                    try {
                        invoice.setContentValue(ShoppingConfig.INVOICES_TYPE_MAP.get(Integer.valueOf(orders.getInvoicesType())));
                    }catch (Exception e){
                        logger.error("fuck, getInvoiceIfPresent orders.getInvoicesType {} is not a digit",orders.getInvoicesType());
                    }
                    if (StringUtils.isNotBlank(orders.getMobile())){
                        invoice.setMobilePhone(orders.getMobile());
                    } else {
                        invoice.setMobilePhone(orders.getPhone());
                    }
                    //发票抬头
                    invoice.setTitle(orders.getInvoicesPayable());
                }
            }
        }
        return invoice;
    }

    /**
     * 查找纯退货商品数量,不包含换货生成的退货单
     *
     * @param orderCode
     * @return
     */
    private List<RefundNumberStatistics> findPureRefundNumberStatisticsByOrderCode(Long orderCode) {
        List<RefundGoods> refundGoods = refundGoodsDao.selectByOrderCode(orderCode);
        if (refundGoods == null || refundGoods.size() == 0) {
            return null;
        }
        List<Integer> numList = new ArrayList<Integer>();
        for (RefundGoods goods : refundGoods) {
            //不包含换货生成的退货
            if (goods.getChangePurchaseId().intValue() == 0) {
                numList.add(goods.getId());
            }
        }
        if (numList.size() == 0) {
            return null;
        }
        return refundGoodsListDao.selectPureRefundNumberStatistics(orderCode, numList);
    }

    private void findMetaTimes(OrderInfoResponse orderInfoResponse, int id) {
        OrdersMeta meta = ordersMetaDAO.selectByOrdersIdAndMetaKey(id, "times");
        if (meta == null || meta.getMetaValue() == null) {
            orderInfoResponse.setPay_finish_time("0");
            orderInfoResponse.setConfirm_time("0");
        } else {
            JSONObject json = JSONObject.parseObject(meta.getMetaValue());
            if (json.getInteger("pay_finish_time") == null) {
                orderInfoResponse.setPay_finish_time("0");
            } else {
                orderInfoResponse.setPay_finish_time(String.valueOf(json.getInteger("pay_finish_time")));
            }
            if (json.getInteger("confirm_time") == null) {
                orderInfoResponse.setPay_finish_time("0");
            } else {
                orderInfoResponse.setConfirm_time(String.valueOf(json.getInteger("confirm_time")));
            }
        }
    }

    private void setOrderDetailCanUpdateDeliveryAddress(OrderInfoResponse orderInfoResponse, Orders orders) {

        // 验证订单未支付但是已经取消
        if ("Y".equals(orders.getIsCancel())) {
            logger.info("GetOrderDetail can not support update delivery address, the order {} has been cancelled", orders.getOrderCode());
            orderInfoResponse.setCanUpdateDeliveryAddress("N");
        }
        // 验证订单是否被删除
        else if (orders.getOrdersStatus() != 1) {
            logger.info("GetOrderDetail can not support update delivery address, the order {} has been removed", orders.getOrderCode());
            orderInfoResponse.setCanUpdateDeliveryAddress("N");
        } else {
            OrdersProcessStatus ordersProcessStatus = ordersStatusService.select(orders);
            if (ordersProcessStatus.getValue() >= 3) {
                logger.info("GetOrderDetail can not support update delivery address, the order {} has been delivered", orders.getOrderCode());
                orderInfoResponse.setCanUpdateDeliveryAddress("N");
                return;
            }
            DeliveryAddress deliveryAddress = ordersDeliveryAddressService.select(orders);
            // 验证收货地址是否已经修改一次
            if (deliveryAddress.getDeliveryAddressUpdateTimes() != null && deliveryAddress.getDeliveryAddressUpdateTimes() == 1) {
                logger.info("GetOrderDetail can not support update delivery address, the order {} delivery address can modify only 1 times", orders.getOrderCode());
                orderInfoResponse.setCanUpdateDeliveryAddress("N");
                return;
            }
            List<Orders> otherSubOrderses = getOtherSubOrderses(orders);
            List<OrdersProcessStatus> subOrdersProcessStatuses = ordersStatusService.select(otherSubOrderses);
            for (OrdersProcessStatus subOrdersProcessStatus : subOrdersProcessStatuses) {
                if (subOrdersProcessStatus.getValue() >= 3) {
                    logger.info("GetOrderDetail can not support update delivery address, the order {} has been delivered, JIT split order or advance goods", orders.getOrderCode());
                    orderInfoResponse.setCanUpdateDeliveryAddress("N");
                    return;
                }
            }
            // 开启修改收货地址入口
            orderInfoResponse.setCanUpdateDeliveryAddress("Y");
        }
    }

    private List<Orders> getOtherSubOrderses(Orders orders) {
        if (orders.getParentOrderCode() != null && orders.getParentOrderCode() > 0) {
            return ordersMapper.selectByParentOrderCode(orders.getParentOrderCode().toString());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 设置订单包裹
     *
     * @param orders
     * @param orderInfoResponse
     */
    private void setOrderInfoPackageList(Orders orders, OrderInfoResponse orderInfoResponse) {
        List<PackageBO> packageList = new ArrayList<>();
        for (OrdersGoodsResponse ordersGoodsResponse : orderInfoResponse.getOrder_goods()) {
            PackageBO orderPackageResponse = getOrderPackageResponse(ordersGoodsResponse.getSupplierId(), packageList);
            SimpleGoodsBO goodsBO = new SimpleGoodsBO();
            goodsBO.setBuyNumber(ordersGoodsResponse.getBuy_number());
            if (StringUtils.isNotEmpty(ordersGoodsResponse.getProduct_id())) {
                goodsBO.setProductId(Integer.valueOf(ordersGoodsResponse.getProduct_id()));
            }
            goodsBO.setProductName(ordersGoodsResponse.getProduct_name());
            goodsBO.setProductSkn(ordersGoodsResponse.getProduct_skn());
            goodsBO.setProductSku(ordersGoodsResponse.getProduct_sku());
            goodsBO.setProductSkc(ordersGoodsResponse.getProduct_skc());
            goodsBO.setGoodsType(ordersGoodsResponse.getGoods_type());
            goodsBO.setGoodsImages(ordersGoodsResponse.getGoods_image());
            orderPackageResponse.getGoodsList().add(goodsBO);
        }
        packageList.sort(new Comparator<PackageBO>() {
            @Override
            public int compare(PackageBO o1, PackageBO o2) {
                return Integer.valueOf(o1.getSupplierId()).compareTo(Integer.valueOf(o2.getSupplierId()));
            }
        });
        if (packageList.size() > 1) {
            orderInfoResponse.setIsMultiPackage(Constants.YES);
            orderInfoResponse.setPackageList(packageList);
            double orderShippingCost = orders.getShippingCost().doubleValue();
            List<Double> packageShippingCosts = splitPackageShippingCost(orderShippingCost, packageList.size());
            for (int i = 0; i < packageList.size(); i++) {
                PackageBO packageResponse = packageList.get(i);
                double shoppingCost = packageShippingCosts.get(i);
                packageResponse.setShoppingCost(BigDecimal.valueOf(shoppingCost).setScale(2).toString());
                packageResponse.setShoppingOrigCost(Constants.EMPTY_STRING + (int) orderShippingCost);
                packageResponse.setShoppingCutCost(Constants.EMPTY_STRING + (int) (orderShippingCost - shoppingCost));
            }
        } else {
            orderInfoResponse.setIsMultiPackage(Constants.NO);
        }
    }

    /**
     * 拆分包裹及其运费
     */
    private static List<Double> splitPackageShippingCost(double shippingCost, int size) {
        if (size == 0) {
            return Collections.emptyList();
        }
        if (size == 1) {
            return Collections.singletonList(shippingCost);
        }
        double[] packageShippingCosts = OrderPackageUtils.caclPackageShippingCost(shippingCost, size);
        List<Double> costs = new ArrayList<>(packageShippingCosts.length);
        for (double packageShippingCost : packageShippingCosts) {
            costs.add(packageShippingCost);
        }
        return costs;
    }

    private PackageBO getOrderPackageResponse(String supplierId, List<PackageBO> packageList) {
        for (PackageBO orderPackage : packageList) {
            if (StringUtils.equalsIgnoreCase(supplierId, orderPackage.getSupplierId())) {
                return orderPackage;
            }
        }
        PackageBO orderPackage = new PackageBO();
        orderPackage.setSupplierId(supplierId);
        orderPackage.setGoodsList(new ArrayList<>());
        packageList.add(orderPackage);
        return orderPackage;
    }

    /**
     * 根据订单编号和订单商品集合批量查询换货商品对象集合
     *
     * @return
     */
    private List<Map<String, ?>> findChangeNumByOrderCodeAndOrdersGoodses(long orderCode, List<OrdersGoods> ordersGoodsList) {
        if (ordersGoodsList.isEmpty()) {
            logger.info("return empty list, becouse param ordersGoodsList is empty");
            return Collections.emptyList();
        }

        List<Map<String, String>> changeParamsList = new ArrayList<>();

        //根据订单商品集合封装批量查询条件
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            Map<String, String> params = new HashMap<>();
            params.put("sourceOrderCode", String.valueOf(orderCode));
            params.put("productSku", String.valueOf(ordersGoods.getErpSkuId()));
            params.put("goodsType", String.valueOf(ordersGoods.getGoodsType()));
            changeParamsList.add(params);
        }
        //查找换货商品及数量
        return changeGoodsMapper.selectChangeGoodsNumBatch(changeParamsList);
    }

    /**
     * 获取订单中换货商品的数量
     *
     * @param orderCode      订单code
     * @param ordersGoods    订单商品
     * @param changeNumBatch 换货商品集合
     * @return orderCode订单中换货商品数量
     */
    private int getChangeNum(String orderCode, OrdersGoods ordersGoods, List<Map<String, ?>> changeNumBatch) {
        String erpSkuId = String.valueOf(ordersGoods.getErpSkuId());
        String goodsType = String.valueOf(ordersGoods.getGoodsType());
        for (Map<String, ?> changeNum : changeNumBatch) {
            //如果换货商品订单原订单编号和orderCode不是同一个订单，则continue
            if (!StringUtils.equals(String.valueOf(changeNum.get("sourceOrderCode")), orderCode)) {
                continue;
            }

            //如果商品的sku和订单商品中的sku不是同一款，则continue
            if (!StringUtils.equals(String.valueOf(changeNum.get("productSku")), erpSkuId)) {
                continue;
            }

            //如果商品类型和订单商品中的商品类型不相同，则continue
            if (!StringUtils.equals(String.valueOf(changeNum.get("goodsType")), goodsType)) {
                continue;
            }

            return Integer.parseInt(String.valueOf(changeNum.get("num")));
        }

        logger.info("orderCode {} , productSku {} , goodsType {} , exchange record not found", orderCode, erpSkuId, goodsType);
        return 0;
    }

    /**
     * 根据订单编号和订单商品集合批量查询退货商品对象集合
     *
     * @param orderCode
     * @param ordersGoodsList
     * @return
     */
    private List<RefundNumberStatistics> findRefundNumberStatisticsByOrderCodeAndOrdersGoodses(Long orderCode, List<OrdersGoods> ordersGoodsList) {
        if (ordersGoodsList.isEmpty()) {
            logger.info("return empty list, becouse param ordersGoodsList is empty");
            return Collections.emptyList();
        }

        //根据订单商品集合封装批量查询条件
        List<RefundNumber> refundNumberList = new ArrayList<>();
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            RefundNumber params = new RefundNumber();
            params.setProductSku(ordersGoods.getErpSkuId());
            params.setLastPrice(ordersGoods.getGoodsPrice());
            refundNumberList.add(params);
        }

        //查找退货商品及数量
        return refundGoodsListDao.selectRefundNumberStatistics(orderCode, refundNumberList);
    }

    private int getRefundNum(Long orderCode, OrdersGoods ordersGoods, List<RefundNumberStatistics> refundNumBatch) {
        for (RefundNumberStatistics refundNum : refundNumBatch) {
            if (refundNum.getOrderCode().equals(orderCode)
                    && refundNum.getProductSku().equals(ordersGoods.getErpSkuId())
                    && refundNum.getLastPrice().equals(ordersGoods.getGoodsPrice())) {
                return refundNum.getNumber();
            }
        }
        return 0;
    }

    private void setOrdersInfo(OrderInfoResponse orderInfoResponse, Orders orders) {
        if (orders.getStatus() < 4) {
            if (Constants.YES.equals(orders.getPaymentStatus())) {
                orders.setStatus((byte) 1);
            } else {
                orders.setStatus((byte) 0);
            }
        }
        OrderStatusDesc orderStatusDesc = OrderStatusDesc.valueOf(orders.getPaymentType(), orders.getStatus());
        orderInfoResponse.setOrderId(String.valueOf(orders.getId()));
        orderInfoResponse.setOrder_code(String.valueOf(orders.getOrderCode()));
        orderInfoResponse.setCreate_time(String.valueOf(orders.getCreateTime()));
        orderInfoResponse.setAddress(orders.getAddress());
        orderInfoResponse.setArea_code(String.valueOf(orders.getAreaCode()));
        orderInfoResponse.setUser_name(orders.getUserName());
        orderInfoResponse.setMobile(orders.getMobile());
        orderInfoResponse.setPhone(PrivacyUtils.mobile(orders.getPhone()));
        orderInfoResponse.setPayment(String.valueOf(orders.getPayment()));
        orderInfoResponse.setPayment_status(orders.getPaymentStatus());
        orderInfoResponse.setPayment_type(String.valueOf(orders.getPaymentType()));
        orderInfoResponse.setStatus(String.valueOf(orders.getStatus()));
        orderInfoResponse.setStatus_str("Y".equals(orders.getIsCancel()) ? "已取消" : orderStatusDesc.getStatusDesc());
        orderInfoResponse.setIs_cancel(orders.getIsCancel());
        orderInfoResponse.setAttribute(String.valueOf(orders.getAttribute()));
        //实付金额
        orderInfoResponse.setAmount("¥" + roundPrice(orders.getAmount()));
        orderInfoResponse.setPayment_amount(roundPrice(orders.getAmount()));
        orderInfoResponse.setParent_order_code(String.valueOf(orders.getParentOrderCode()));
        orderInfoResponse.setOrderType(String.valueOf(orders.getOrderType()));
        //支付方式
        Payment payment = paymentDAO.selectByPrimaryKey(orders.getPayment().shortValue());
        String paymentName = "";
        if (Objects.nonNull(payment)) {
            paymentName = payment.getPayName();
        }
        orderInfoResponse.setPaymentName(paymentName);
        orderInfoResponse.setBankName(BankUtils.findBankNameByCode(orders.getBankCode()));
        orderInfoResponse.setRemark(orders.getRemark());
        orderInfoResponse.setRefundStatus(orders.getRefundStatus().toString());
        orderInfoResponse.setExchangeStatus(orders.getExchangeStatus().toString());
    }

    /**
     * 设置物流信息
     *
     * @param orderInfoResponse
     * @param orders
     */
    private void setExpressInfo(OrderInfoResponse orderInfoResponse, Orders orders) {

        //已经被取消的订单，不需要物流信息
        if (!"Y".equals(orders.getIsCancel())) {
            //已发货
            if (orders.getStatus() >= 4) {
                //获取发货时间
                Integer expressTime = getExpressTime(orders);
                if (expressTime != null) {
                    if (CalendarUtils.getTimeDiff(expressTime, CalendarUtils.getSystemSeconds()) < CalendarUtils.getTimes(90)) {
                        orderInfoResponse.setExpress_number(orders.getExpressNumber());
                    } else {
                        logger.info("order {} express has > 90 days.", orders.getOrderCode());
                    }
                } else {
                    logger.info("order {} can not find express time.", orders.getOrderCode());
                }
            }
            //未发货
            else {
                List<Map<String, Object>> express_detail = new ArrayList<>();
                List<LogisticsInfo> logisticsInfos = logisticsInfoDao.selectByOrderCode(orders.getOrderCode());
                if (logisticsInfos.size() > 0) {
                    // 最新一条记录
                    LogisticsInfo logisticsInfo = logisticsInfos.get(logisticsInfos.size() - 1);
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("acceptTime", CalendarUtils.parsefomatSeconds(logisticsInfo.getCreateTime(), CalendarUtils.LONG_FORMAT_LINE));
                    if (StringUtils.isEmpty(logisticsInfo.getAcceptAddress())) {
                        tmp.put("accept_address", logisticsInfo.getAcceptRemark());
                    } else {
                        tmp.put("accept_address", new StringBuffer(logisticsInfo.getAcceptAddress()).append(" ").append(logisticsInfo.getAcceptRemark()).toString());
                    }
                    tmp.put("order_code", logisticsInfo.getOrderCode());
                    express_detail.add(tmp);
                }
                orderInfoResponse.setExpress_detail(express_detail);
            }
        }
    }

    private Integer getExpressTime(Orders orders) {
        if (orders.getExpressId() == 23) {
            return getExpressTimeFromExress(orders.getOrderCode());
        } else if (orders.getExpressId() == 29) {
            return getExpressTimeFromWaybillInfo(orders.getOrderCode(), (byte) 3);
        } else {
            return getExpressTimeFromWaybillInfo(orders.getOrderCode(), orders.getExpressId());
        }
    }

    private Integer getExpressTimeFromExress(Long orderCode) {
        //根据订单号查询express运单表
        List<Express> expressList = expressMapper.selectByOrderCode(String.valueOf(orderCode));
        if (CollectionUtils.isNotEmpty(expressList)) {
            Collections.reverse(expressList);
            return expressList.get(0).getCreateTime();
        } else {
            logger.info("order {} express is empty.", orderCode);
            return null;
        }
    }

    private Integer getExpressTimeFromWaybillInfo(Long orderCode, Byte logisticsType) {
        List<WaybillInfo> waybillInfos = waybillInfoDao.selectByOrderCodeAndLogisticsType(orderCode, logisticsType);
        if (CollectionUtils.isNotEmpty(waybillInfos)) {
            Collections.reverse(waybillInfos);
            return waybillInfos.get(0).getCreateTime();
        } else {
            logger.info("order {} WaybillInfo is empty. logisticsType {}", orderCode, logisticsType);
            return null;
        }
    }

    /**
     * 设置快递方式
     *
     * @param orderInfoResponse
     * @param orders
     */
    private void setTheCourier(OrderInfoResponse orderInfoResponse, Orders orders) {
        String rTime = StringUtils.trimToEmpty(orders.getReceiptTime());
        // 【数据库混合了数字与汉字】这里过滤数字的
        switch (rTime) {
            case "1":
                rTime = "只工作日送货（双休日、节假日不用送）";
                break;
            case "2":
                rTime = "工作日、双休日和节假日均送货";
                break;
            case "3":
                rTime = "只双休日、节假日送货（工作时间不送货）";
                break;
        }
        orderInfoResponse.setDeliveryTime(rTime);
    }

    /**
     * 设置物流公司
     *
     * @param orderInfoResponse
     * @param orders
     */
    private void setExpressCompany(OrderInfoResponse orderInfoResponse, Orders orders) {
        if (orders.getExpressId() == null || orders.getExpressId() <= 0) {
            Map<String, Object> info = new HashMap<>();
            info.put("caption", Constants.EMPTY_STRING);
            info.put("url", Constants.EMPTY_STRING);
            info.put("logo", Constants.EMPTY_STRING);
            info.put("is_support", Constants.EMPTY_STRING);
            orderInfoResponse.setExpress_company(info);
        } else {
            orderInfoResponse.setExpress_company(ExpressUtils.getExpressCompany(orders.getExpressId().toString()));
        }
    }


    private void setPromotionFormulas(OrderInfoResponse orderInfoResponse, Orders orders, List<OrdersGoods> ordersGoodsList) {
        //订单商品总额
        BigDecimal goodsAmount = new BigDecimal(0);
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            goodsAmount = goodsAmount.add(ordersGoods.getGoodsAmount());
        }
        OrderPromotion orderPromotion = getOrderPromotion(orders, goodsAmount);
        List<PromotionFormula> promotionFormulas = new ArrayList<>();
        //商品总金额
        orderInfoResponse.setGoods_total_amount("¥" + orderPromotion.getOrderAmount());
        {
            PromotionFormula promotionFormula = new PromotionFormula();
            promotionFormula.setPromotion("商品总金额");
            promotionFormula.setPromotion_amount(orderInfoResponse.getGoods_total_amount());
            promotionFormulas.add(promotionFormula);
        }
        //活动金额
        orderInfoResponse.setPromotion_amount("-¥" + roundPrice(Double.parseDouble(orderPromotion.getPromotionAmount()) + Double.parseDouble(orderPromotion.getVipCutdownAmount())));
        if (!StringUtils.equals("-¥0.00", orderInfoResponse.getPromotion_amount())) {
            PromotionFormula promotionFormula = new PromotionFormula();
            promotionFormula.setPromotion("活动金额");
            promotionFormula.setPromotion_amount(orderInfoResponse.getPromotion_amount());
            promotionFormulas.add(promotionFormula);
        }
        //运费
        orderInfoResponse.setShipping_cost("¥" + orders.getShippingCost());
        {
            PromotionFormula promotionFormula = new PromotionFormula();
            if (orderPromotion.getShoppingOrigCost() > 0) {
                int shoppingOrigCost = (int) orderPromotion.getShoppingOrigCost();
                int shoppingCost = (int) orderPromotion.getShoppingCost();
                promotionFormula.setPromotion("运费（原价" + shoppingOrigCost + "元，优惠" + (shoppingOrigCost - shoppingCost) + "元）");
            } else {
                promotionFormula.setPromotion("运费");
            }
            promotionFormula.setPromotion_amount(orderInfoResponse.getShipping_cost());
            promotionFormulas.add(promotionFormula);
        }
        //优惠券
        orderInfoResponse.setCoupons_amount("-¥" + orderPromotion.getCouponsAmount());
        if (!StringUtils.equals("-¥0.00", orderInfoResponse.getCoupons_amount())) {
            PromotionFormula promotionFormula = new PromotionFormula();
            promotionFormula.setPromotion("优惠券");
            promotionFormula.setPromotion_amount(orderInfoResponse.getCoupons_amount());
            promotionFormulas.add(promotionFormula);
        }
        //优惠码
        if (orderPromotion.getPromotionCodeDiscountAmount() > 0) {
            String discountAmountStr = "-¥" + roundPrice(orderPromotion.getPromotionCodeDiscountAmount());
            orderInfoResponse.setPromo_code_amount(discountAmountStr);
            PromotionFormula promotionFormula = new PromotionFormula();
            promotionFormula.setPromotion("优惠码");
            promotionFormula.setPromotion_amount(discountAmountStr);
            promotionFormulas.add(promotionFormula);
        }

        //红包
        if (orderPromotion.getUseRedEnvelopes() > 0) {
            String discountAmountStr = "-¥" + roundPrice(orderPromotion.getUseRedEnvelopes());
            PromotionFormula promotionFormula = new PromotionFormula();
            promotionFormula.setPromotion("红包");
            promotionFormula.setPromotion_amount(discountAmountStr);
            promotionFormulas.add(promotionFormula);
        }

        //YOHO币
        orderInfoResponse.setYoho_coin_num("-¥" + orderPromotion.getUseYohoCoin());
        if (!StringUtils.equals("-¥0.00", orderInfoResponse.getYoho_coin_num())) {
            PromotionFormula promotionFormula = new PromotionFormula();
            promotionFormula.setPromotion("YOHO币");
            promotionFormula.setPromotion_amount(orderInfoResponse.getYoho_coin_num());
            promotionFormulas.add(promotionFormula);
        }
        orderInfoResponse.setPromotionFormulas(promotionFormulas);
    }


    /**
     * 退货统计信息
     *
     * @param refundNumberStatisticsList
     * @return
     */
    private MultiKeyMap refundNumberStatisticsList2Map(List<RefundNumberStatistics> refundNumberStatisticsList) {
        MultiKeyMap refundMap = new MultiKeyMap();
        if (refundNumberStatisticsList == null || refundNumberStatisticsList.size() == 0) {
            return refundMap;
        }
        for (RefundNumberStatistics refundNumberStatistics : refundNumberStatisticsList) {
            if (refundMap.containsKey(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice())) {
                //应该走不到这，group by出来的查询结果
                logger.warn("refund num 2 map  orderCode:{} sku:{} price:{} ", refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice());
                Integer num = (Integer) refundMap.get(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice());
                refundMap.put(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice(), num + refundNumberStatistics.getNumber());
            } else {
                refundMap.put(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice(), refundNumberStatistics.getNumber());
            }
        }
        return refundMap;
    }

    private void setIsSupportRefundAndExchange(OrderInfoResponse orderInfoResponse, Orders orders, List<OrdersGoods> ordersGoodsList,
                                               List<RefundNumberStatistics> refundNumBatch) {
        //该订单是否由可以退货的商品
        boolean refundChange = false;
        int availableRefundNum = 0;
        //TODO 统计错误修改
        MultiKeyMap refundMap = refundNumberStatisticsList2Map(refundNumBatch);
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            if (refundMap.containsKey(orders.getOrderCode(), ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice())) {
                int alreadyRefundNum = (Integer) refundMap.get(orders.getOrderCode(), ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice());
                if (ordersGoods.getNum() > alreadyRefundNum) {
                    refundChange = true;
                    break;
                } else {
                    refundMap.put(orders.getOrderCode(), ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice(), alreadyRefundNum - ordersGoods.getNum());
                }
            } else {  //该sku没退货
                refundChange = true;
                break;
            }
        }

//        for (OrdersGoods ordersGoods : ordersGoodsList) {
//            int refundgoodsNum = getRefundNum(orders.getOrderCode(), ordersGoods, refundNumBatch);
//            if (ordersGoods.getNum() > refundgoodsNum) {
//                refundChange = true;
//            }
//        }

        String is_support_exchange = "N";
        String is_support_refund = "N";
        if (refundChange) {
            long time = new Date().getTime() / 1000 - orders.getUpdateTime();
            if (orders.getStatus() >= 6
                    && time < 86400 * orderExchangeLimitTime
                    && orders.getAttribute() != 3
                    && StringUtils.equals(orders.getIsCancel(), "N")) {
                //如果时间小于15天则可以换货
                is_support_exchange = "Y";
            } else {
                logger.info("the orders {} not support exchange, because  status[{}] >= 6 and time[{}] < 15days[{}] and attribute[{}] != 3 and isCancel[{}] == N.", orders.getOrderCode(), orders.getStatus(), time, 86400 * orderExchangeLimitTime, orders.getAttribute(), orders.getIsCancel());
            }
            if (orders.getStatus() >= 6
                    && time < 86400 * orderRefundLimitTime
                    && orders.getAttribute() != 3
                    && StringUtils.equals(orders.getIsCancel(), "N")) {
                //如果时间小于7天则可以换货
                is_support_refund = "Y";
            } else {
                logger.info("the orders {} not support refund, because status[{}] >= 6 and time[{}] < 7days[{}] and attribute[{}] != 3 and isCancel[{}] == N.", orders.getOrderCode(), orders.getStatus(), time, 86400 * orderRefundLimitTime, orders.getAttribute(), orders.getIsCancel());
            }
        } else {
            logger.info("the orders {} not support refund change.", orders.getOrderCode());
        }
        orderInfoResponse.setIs_support_exchange(is_support_exchange);
        orderInfoResponse.setIs_support_refund(is_support_refund);
    }

    private OrderPromotion getOrderPromotion(Orders orders, BigDecimal goodsAmount) {
        OrderPromotion orderPromotion = new OrderPromotion();
        //获取当前订单的优惠
        OrderPromotionInfoBo orderPromotionInfoBo = orderPromotionService.selectByOrderCode(orders.getOrderCode());
        logger.info("order promotion info, uid {}, id {}, order code {}, order promotion {}",
                orderPromotionInfoBo.getUid(),
                orderPromotionInfoBo.getId(),
                orderPromotionInfoBo.getOrderCode(),
                orderPromotionInfoBo.getOrderPromotion()
        );
        if (orderPromotionInfoBo != null && StringUtils.isNotEmpty(orderPromotionInfoBo.getOrderPromotion())) {
            JSONObject jsonObject = JSON.parseObject(orderPromotionInfoBo.getOrderPromotion());
            orderPromotion.setCouponsAmount(roundPrice(jsonObject.getDoubleValue("coupons_amount")));
            orderPromotion.setOrderAmount(roundPrice(jsonObject.getDoubleValue("order_amount")));
            orderPromotion.setPromotionAmount(roundPrice(jsonObject.getDoubleValue("promotion_amount")));
            orderPromotion.setVipCutdownAmount(roundPrice(jsonObject.getDoubleValue("vip_cutdown_amount")));
            orderPromotion.setShoppingCost(jsonObject.getDoubleValue("shipping_cost"));
            orderPromotion.setShoppingOrigCost(jsonObject.getDoubleValue("shopping_orig_cost"));
            //优惠码减免金额
            String promotionCodeKey = "promotioncode_discount_amount";
            if (jsonObject.containsKey(promotionCodeKey)) {
                orderPromotion.setPromotionCodeDiscountAmount(jsonObject.getDoubleValue(promotionCodeKey));
            }

            //红包
            String redEnvelopesKey = "use_red_envelopes";
            if (jsonObject.containsKey(redEnvelopesKey)) {
                orderPromotion.setUseRedEnvelopes(jsonObject.getDoubleValue(redEnvelopesKey));
            }

        } else {
            orderPromotion.setCouponsAmount("0.00");
            orderPromotion.setOrderAmount(roundPrice(goodsAmount));
            orderPromotion.setPromotionAmount("0.00");
            orderPromotion.setVipCutdownAmount("0.00");
        }
        //TODO 12月份后的稀释了（单位为分），12月份前没有稀释（单位为元）
        //(int) (new SimpleDateFormat("yyyyMMdd").parse("20151201").getTime() / 1000) == 1448899200
        int yohoCoinDiluteTime = 1448899200;
        double useYohoCoin = orders.getYohoCoinNum();
        if (orders.getCreateTime() >= yohoCoinDiluteTime) {
            orderPromotion.setUseYohoCoin(roundPrice(useYohoCoin / 100));
        } else {
            orderPromotion.setUseYohoCoin(roundPrice(useYohoCoin));
        }
        logger.info("order promotion info, promotionAmount {}, orderAmount {}, couponsAmount {}, vipCutdownAmount {}, useYohoCoin {}, source promotion {},PromotionCodeDiscountAmount {}",
                orderPromotion.getPromotionAmount(),
                orderPromotion.getOrderAmount(),
                orderPromotion.getCouponsAmount(),
                orderPromotion.getVipCutdownAmount(),
                orderPromotion.getUseYohoCoin(),
                orderPromotionInfoBo.getOrderPromotion(),
                orderPromotion.getPromotionCodeDiscountAmount());
        return orderPromotion;
    }

    private String roundPrice(double price) {
        return roundPrice(BigDecimal.valueOf(price));
    }

    private String roundPrice(BigDecimal price) {
        DecimalFormat df = new DecimalFormat();
        String style = "0.00";
        df.applyPattern(style);
        return df.format(price.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
    }


    /**
     * 换货统计信息
     *
     * @param changeNumBatch
     * @return
     */
    private MultiKeyMap changeNumStatisticsList2Map(List<Map<String, ?>> changeNumBatch) {
        MultiKeyMap refundMap = new MultiKeyMap();
        String sourceOrderCodeKey;
        String productSkuKey;
        String goodsTypeKey;
        for (Map<String, ?> changeNum : changeNumBatch) {
            sourceOrderCodeKey = String.valueOf(changeNum.get("sourceOrderCode"));
            productSkuKey = String.valueOf(changeNum.get("productSku"));
            goodsTypeKey = String.valueOf(changeNum.get("goodsType"));
            if (refundMap.containsKey(sourceOrderCodeKey, productSkuKey, goodsTypeKey)) {
                Integer num = (Integer) refundMap.get(sourceOrderCodeKey, productSkuKey, goodsTypeKey);
                refundMap.put(sourceOrderCodeKey, productSkuKey, goodsTypeKey, num + Integer.parseInt(String.valueOf(changeNum.get("num"))));
            } else {
                refundMap.put(sourceOrderCodeKey, productSkuKey, goodsTypeKey, Integer.parseInt(String.valueOf(changeNum.get("num"))));
            }
        }
        return refundMap;
    }


    /**
     * 设置订单商品
     */
    private void setOrdersGoods(OrderInfoResponse orderInfoResponse,
                                Orders orders,
                                List<OrdersGoods> ordersGoodsList,
                                List<Map<String, ?>> changeNumBatch,
                                List<RefundNumberStatistics> refundNumBatch,
                                List<ProductBo> productBoList,
                                List<RefundNumberStatistics> refundNumPure) {
        List<OrdersGoodsResponse> _ordersGoodsList = new ArrayList<>();
        //定义订单返还总yoho币
        Integer yohoGiveCoinTotal = 0;

        MultiKeyMap refundMap = refundNumberStatisticsList2Map(refundNumPure);
        MultiKeyMap exchangeMap = changeNumStatisticsList2Map(changeNumBatch);

        for (OrdersGoods ordersGoods : ordersGoodsList) {
            //根据订单商品中productId从商品对象集合中获取对象
            ProductBo productBo = findProductBoByProductId(ordersGoods.getProductId(), productBoList);

            if (Objects.isNull(productBo)) {
                continue;
            }

            //获取订单中换货商品的数量
//            int exchangeGoodsNu = getChangeNum(String.valueOf(orders.getOrderCode()), ordersGoods, changeNumBatch);
            int exchangeGoodsNum = 0;
            String sourceOrderCodeKey = String.valueOf(orders.getOrderCode());
            String productSkuKey = String.valueOf(ordersGoods.getErpSkuId());
            String goodsTypeKey = String.valueOf(ordersGoods.getGoodsType());
            if (exchangeMap.containsKey(sourceOrderCodeKey, productSkuKey, goodsTypeKey)) {
                int alreadyExchangeNum = (Integer) exchangeMap.get(sourceOrderCodeKey, productSkuKey, goodsTypeKey);
                if (ordersGoods.getNum() > alreadyExchangeNum) {
                    exchangeGoodsNum = alreadyExchangeNum;
                    exchangeMap.put(sourceOrderCodeKey, productSkuKey, goodsTypeKey, 0);
                } else {
                    exchangeGoodsNum = ordersGoods.getNum();
                    exchangeMap.put(sourceOrderCodeKey, productSkuKey, goodsTypeKey, alreadyExchangeNum - exchangeGoodsNum);
                }
            }


            //获取订单中退货商品的数量
//            int refundgoodsNum = getRefundNum(orders.getOrderCode(), ordersGoods, refundNumBatch);
            //记录退货数量
            int refundgoodsNum = 0;
            if (refundMap.containsKey(orders.getOrderCode(), ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice())) {
                int alreadyRefundNum = (Integer) refundMap.get(orders.getOrderCode(), ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice());
                if (ordersGoods.getNum() > alreadyRefundNum) {
                    refundgoodsNum = alreadyRefundNum;
                    refundMap.put(orders.getOrderCode(), ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice(), 0);
                } else {
                    refundgoodsNum = ordersGoods.getNum();
                    refundMap.put(orders.getOrderCode(), ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice(), alreadyRefundNum - refundgoodsNum);
                }
            }

            //定义商品状态，根据退换货信息进行设置
            String goodsStatus;
            int status_count = 0;
            if (exchangeGoodsNum > 0) {
                status_count = status_count + 1;
            }
            if (refundgoodsNum > 0) {  //TODO 部分退货
                status_count = status_count + 2;
            }
            switch (status_count) {
                case 1:
                    goodsStatus = "已换货";
                    break;
                case 2:
                    goodsStatus = "已退货";
                    break;
                case 3:
                    goodsStatus = "已退换";
                    break;
                default:
                    goodsStatus = "";
                    break;
            }

            //封装订单商品返回对象
            OrdersGoodsResponse temp = new OrdersGoodsResponse();
            temp.setProduct_id(String.valueOf(productBo.getId()));
            temp.setProduct_skn(String.valueOf(productBo.getErpProductId()));
            temp.setProduct_sku(String.valueOf(ordersGoods.getErpSkuId()));
            temp.setSize_name(ordersGoods.getSizeName());
            temp.setColor_name(ordersGoods.getColorName());
            temp.setBuy_number(String.valueOf(ordersGoods.getNum()));
            temp.setProduct_name(productBo.getProductName());
            temp.setGoods_price(roundPrice(ordersGoods.getGoodsPrice()));
            temp.setGoods_amount(roundPrice(ordersGoods.getGoodsAmount()));
            temp.setGoods_image("");
            temp.setGoods_type(OrderGoodsTypeUtils.getOrderGoodsTypeMap(ordersGoods.getGoodsType().toString()).get("en").toString());
            temp.setGoods_status(goodsStatus);
            temp.setExpect_arrival_time(6 == ordersGoods.getGoodsType() && null != productBo.getExpectArrivalTime()
                    ? String.valueOf(productBo.getExpectArrivalTime()) + "月" : "");
            temp.setGoods_id(ordersGoods.getGoodsId());
            temp.setCn_alphabet(productBo.getCnAlphabet());
            temp.setSupplierId(String.valueOf(ordersGoods.getSupplierId()));
            //商品回返有货币
            Integer yohoGiveCoin = ordersGoods.getGetYohoCoin();
            if (Objects.nonNull(yohoGiveCoin)) {
                yohoGiveCoinTotal += yohoGiveCoin;
            }
            temp.setYoho_give_coin(yohoGiveCoin);

            GoodsBo goodsBo = findGoodsBoByGoodsId(ordersGoods.getGoodsId(), productBo.getGoodsList());
            //设置商品图片地址
            temp.setGoods_image(getGoodsImageUrl(ordersGoods.getGoodsId(), goodsBo));

            _ordersGoodsList.add(temp);
        }
        orderInfoResponse.setYohoGiveCoin(yohoGiveCoinTotal);
        orderInfoResponse.setOrder_goods(_ordersGoodsList);
    }

    /**
     * 获取商品图片地址
     *
     * @param goodsId
     * @param goodsBo
     * @return
     */
    private String getGoodsImageUrl(Integer goodsId, GoodsBo goodsBo) {
        //商品默认图片地址
        String defaultGoodsImageUrl = null;
        //商品图片地址
        String goodsImageUrl = "";
        if (Objects.isNull(goodsBo)) {
            logger.info("getGoodsImageUrl return empty, becouse params [goods] is empty");
            return goodsImageUrl;
        }

        for (GoodsImagesBo goodsImagesBo : goodsBo.getGoodsImagesList()) {
            if (!goodsId.equals(goodsImagesBo.getGoodsId())) {
                continue;
            }

            //判断当前商品图片是否为默认图片
            if ("Y".equals(goodsImagesBo.getIsDefault())) {
                defaultGoodsImageUrl = goodsImagesBo.getImageUrl();
                break;
            }

            goodsImageUrl = goodsImagesBo.getImageUrl();
            break;
        }

        //如果有默认图片，则返回默认图片
        if (StringUtils.isNotEmpty(defaultGoodsImageUrl)) {
            goodsImageUrl = defaultGoodsImageUrl;
        }

        return goodsImageUrl;
    }

    private List<ProductBo> findProductBoByOrderGoodses(List<OrdersGoods> orderGoodsList) {
        List<Integer> productIds = new ArrayList<>();
        for (OrdersGoods ordersGoods : orderGoodsList) {
            productIds.add(ordersGoods.getProductId());
        }
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }
        BatchBaseRequest request = new BatchBaseRequest<>();
        request.setParams(productIds);
        ProductBo[] productBoList = serviceCaller.call("product.queryOrderListByProductIds", request, ProductBo[].class);
        if (productBoList == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(productBoList);
        }
    }

    private ProductBo findProductBoByProductId(Integer id, List<ProductBo> productBos) {
        for (ProductBo productBo : productBos) {
            if (productBo.getId().equals(id)) {
                return productBo;
            }
        }
        return null;
    }

    private GoodsBo findGoodsBoByGoodsId(Integer id, List<GoodsBo> goodsBos) {
        for (GoodsBo goodsBo : goodsBos) {
            if (goodsBo.getId().equals(id)) {
                return goodsBo;
            }
        }
        return null;
    }


    /**
     * @param code
     * @return
     */
    private String fetchAreaForOrderDetail(Integer code) {

        String area = "";

        if (code != null && code > 0) {
            AreaReqBO areaReqBO = new AreaReqBO();
            areaReqBO.setCode(Integer.valueOf(code));
            try {
                AreaRspBo areaData = serviceCaller.call(ServerURL.USERS_GET_AREA_INFO_BY_CODE, areaReqBO, AreaRspBo.class);
                while (areaData != null) {
                    area = areaData.getCaption() + " " + area;
                    areaData = areaData.getParent();
                }
            } catch (Exception e) {
                //非关键信息，捕获所有异常
                logger.debug("process order info for order detail failed, code {}", code);
            }
        }
        return area;
    }


    @Override
    public int getNewUserOrderCountForPromotionCode(int uid) {
        return ordersMapper.selectNewUserOrderCountForPromotionCode(uid);
    }

    public boolean isFinishPaidByUser(Orders order) {
        int count = orderPayDAO.selectCountByOrderCode(order.getOrderCode());
        return count > 0;
    }


    private boolean haveLimitCode(Orders orders) {
        //现在只有限购码才会使用这个表
        String attributeStr = orderExtAttributeDAO.selectExtAttributeByOrderCodeAndUid(orders.getOrderCode(), orders.getUid());
        return StringUtils.isNotEmpty(attributeStr);
    }


    /**
     * 根据订单号获取物流详情信息
     *
     * @param orderCode
     * @return
     */
    public ResponseBean findLogisticsDetail(int uid, long orderCode) {
        logger.info("FindLogisticsDetail {}, start.", orderCode);
        ResponseBean responseBean = new ResponseBean();
        //根据订单号查询订单信息
        Orders orders = ordersMapper.selectByUidAndOrderCode(uid, orderCode);
        if (orders == null || orders.getStatus() == null) {
            logger.warn("FindLogisticsDetail fail, can not find order {}.", orderCode);
            responseBean.setCode(ServiceError.ORDER_NO_DELIVERY_ORDER.getMappingGatewayError().getKey().toString());
            responseBean.setMessage(ServiceError.ORDER_NO_DELIVERY_ORDER.getMappingGatewayError().getValue());
            return responseBean;
        }
        //已发货
        if (orders.getStatus() >= 4) {
            if (orders.getExpressId() == null || orders.getExpressId() == 0) {
                logger.warn("FindLogisticsDetail fail, the order {} has no expressId.", orderCode);
                responseBean.setCode(ServiceError.ORDER_NO_LOGISTICS_ORDER.getMappingGatewayError().getKey().toString());
                responseBean.setMessage(ServiceError.ORDER_NO_LOGISTICS_ORDER.getMappingGatewayError().getValue());
                return responseBean;
            }
            String expressId = orders.getExpressId().toString();
            if (!ExpressUtils.containsKey(expressId)) {
                logger.warn("FindLogisticsDetail fail, the order {} can not find express company by id {}.", orderCode, expressId);
                Map<String, Object> data = ExpressUtils.createEmptyExpress();
                data.put("express_number", orders.getExpressNumber());
                responseBean.setData(data);
                responseBean.setCode(ServiceError.ORDER_NOT_FIND_LOGISTICS_COMPANY.getMappingGatewayError().getKey().toString());
                responseBean.setMessage(ServiceError.ORDER_NOT_FIND_LOGISTICS_COMPANY.getMappingGatewayError().getValue());
                return responseBean;
            }
            //根据订单信息中的物流编号获取快递公司信息
            Map<String, Object> data = ExpressUtils.getExpressCompany(expressId);
            //获取快递路由
            List<Map<String, Object>> expressDetailList = getExpressDetail(orders);
            //获取物流调拨信息
            List<Map<String, Object>> logisticsInfos = getLogisticsInfos(orders.getOrderCode());
            removeRepeatExpressRecord(expressDetailList, logisticsInfos);
            expressDetailList.addAll(logisticsInfos);
            sortExpressDetailList(expressDetailList);
            data.put("express_detail", expressDetailList);
            data.put("is_support", CollectionUtils.isNotEmpty(expressDetailList) ? "1" : "3");
            data.put("express_number", orders.getExpressNumber());
            responseBean.setCode(ServiceError.ORDER_LOGISTICS_INFORMATION.getMappingGatewayError().getKey().toString());
            responseBean.setMessage(ServiceError.ORDER_LOGISTICS_INFORMATION.getMappingGatewayError().getValue());
            responseBean.setData(data);
            logger.info("FindLogisticsDetail {}, response is {}.", orderCode, JSONObject.toJSONString(data));
            return responseBean;
        }
        // 未发货
        else {
            Map<String, Object> data = new HashedMap();
            List<Map<String, Object>> expressDetailList = getLogisticsInfos(orders.getOrderCode());
            sortExpressDetailList(expressDetailList);
            data.put("express_detail", expressDetailList);
            data.put("is_support", CollectionUtils.isNotEmpty(expressDetailList) ? "1" : "3");
            responseBean.setCode(ServiceError.ORDER_LOGISTICS_INFORMATION.getMappingGatewayError().getKey().toString());
            responseBean.setMessage(ServiceError.ORDER_LOGISTICS_INFORMATION.getMappingGatewayError().getValue());
            responseBean.setData(data);
            logger.info("FindLogisticsDetail {}, response is {}.", orderCode, JSONObject.toJSONString(data));
            return responseBean;
        }
    }

    /**
     * 根据uid和productId检查是否已经购买过
     *
     * @param checkHasBuyingRequest
     * @return false=没有购买过  true=购买过
     */
    @Override
    public boolean checkHasBuying(CheckHasBuyingRequest checkHasBuyingRequest) {
        if (Objects.isNull(checkHasBuyingRequest.getUid())) {
            logger.warn("CheckHasBuying fail, don't log in first");
            throw new ServiceException(ServiceError.ORDER_LOGIN_FIRST);
        }

        if (Objects.isNull(checkHasBuyingRequest.getProductId())) {
            logger.warn("CheckHasBuying fail, productId does not exist");
            throw new ServiceException(ServiceError.ORDER_PRODUCT_ID_EMPTY);
        }

        List<Integer> orderIds = ordersGoodsMapper.selectByProductId(checkHasBuyingRequest.getUid(), checkHasBuyingRequest.getProductId());
        if (CollectionUtils.isEmpty(orderIds)) {
            logger.info("uid {}, productId {}, get order id is empty", checkHasBuyingRequest.getUid(), checkHasBuyingRequest.getProductId());
            return false;
        }

        List<Orders> ordersList = ordersMapper.selectByPrimaryKeys(orderIds);
        if (CollectionUtils.isEmpty(ordersList)) {
            logger.info("orderIds {}, get orders is empty", orderIds);
            return false;
        }

        for (Orders orders : ordersList) {
            //判断订单状态是否为4=已发货/6=交易成功
            if (orders.getStatus() == 4 || orders.getStatus() == 6) {
                return true;
            }
        }

        return false;
    }

    private List<Map<String, Object>> getExpressDetail(Orders orders) {
        if (orders.getExpressId() == 23) {
            return getExpressDetailFromExpress(orders.getOrderCode());
        } else if (orders.getExpressId() == 29) {
            return getExpressDetailFromWaybillInfo(orders.getOrderCode(), (byte) 3);
        } else {
            return getExpressDetailFromWaybillInfo(orders.getOrderCode(), orders.getExpressId());
        }
    }

    /**
     * 顺丰物流,根据订单编号查询物流信息
     */
    private List<Map<String, Object>> getExpressDetailFromExpress(Long orderCode) {
        List<Map<String, Object>> expressDetail = new ArrayList<>();
        List<Express> expressList = expressMapper.selectByOrderCode(orderCode.toString());
        if (CollectionUtils.isNotEmpty(expressList)) {
            for (Express express : expressList) {
                express.setAcceptRemark(express.getAcceptRemark().replaceAll("null", ""));
                expressDetail.add(new HashMap<String, Object>() {
                    {
                        put("acceptTime", CalendarUtils.parsefomatSeconds(express.getAcceptTime(), CalendarUtils.LONG_FORMAT_LINE));
                        put("accept_address", new StringBuffer(express.getAcceptAddress()).append(" ").append(express.getAcceptRemark()).toString());
                        put("express_id", "23");
                        put("express_number", express.getExpressNum());
                        put("order_code", express.getOrderCode());
                    }
                });
            }
        }

        return expressDetail;
    }

    /**
     * 根据订单编号和快递编号获取快递路由
     */
    private List<Map<String, Object>> getExpressDetailFromWaybillInfo(Long orderCode, Byte expressId) {
        List<Map<String, Object>> expressDetail = new ArrayList<>();
        List<WaybillInfo> waybillInfoList = waybillInfoDao.selectByOrderCodeAndLogisticsType(orderCode, expressId);
        if (CollectionUtils.isNotEmpty(waybillInfoList)) {
            for (WaybillInfo waybillInfo : waybillInfoList) {
                expressDetail.add(new HashMap<String, Object>() {
                    {
                        put("acceptTime", CalendarUtils.parsefomatSeconds(waybillInfo.getCreateTime(), CalendarUtils.LONG_FORMAT_LINE));
                        put("accept_address", waybillInfo.getAddressInfo());
                        put("express_id", waybillInfo.getLogisticsType());
                        put("express_number", waybillInfo.getWaybillCode());
                        put("order_code", waybillInfo.getOrderCode());
                    }
                });
            }
        }
        return expressDetail;
    }

    /**
     * 获取物流调拨信息
     *
     * @param orderCode
     * @return
     */
    private List<Map<String, Object>> getLogisticsInfos(Long orderCode) {
        List<Map<String, Object>> express_detail = new ArrayList<>();
        List<LogisticsInfo> logisticsInfos = logisticsInfoDao.selectByOrderCodeAndWaybillCode(orderCode, "0");
        for (LogisticsInfo logisticsInfo : logisticsInfos) {
            express_detail.add(new HashMap<String, Object>() {
                {
                    put("acceptTime", CalendarUtils.parsefomatSeconds(logisticsInfo.getCreateTime(), CalendarUtils.LONG_FORMAT_LINE));
                    if (StringUtils.isEmpty(logisticsInfo.getAcceptAddress())) {
                        put("accept_address", logisticsInfo.getAcceptRemark());
                    } else {
                        put("accept_address", new StringBuffer(logisticsInfo.getAcceptAddress()).append(" ").append(logisticsInfo.getAcceptRemark()).toString());
                    }
                    put("order_code", logisticsInfo.getOrderCode());
                }
            });
        }
        return express_detail;
    }

    /**
     * 删除重复的物流信息
     *
     * @param expressDetailList
     * @param logisticsInfos
     */
    private void removeRepeatExpressRecord(List<Map<String, Object>> expressDetailList, List<Map<String, Object>> logisticsInfos) {
        Iterator<Map<String, Object>> logisticsInfoIterator = logisticsInfos.iterator();
        while (logisticsInfoIterator.hasNext()) {
            Map<String, Object> logisticsInfo = logisticsInfoIterator.next();
            if (isRepeatExpressRecord(logisticsInfo, expressDetailList)) {
                logisticsInfoIterator.remove();
            }
        }
    }

    private boolean isRepeatExpressRecord(Map<String, Object> logisticsInfo, List<Map<String, Object>> expressDetailList) {
        for (Map<String, Object> expressDetail : expressDetailList) {
            if (ObjectUtils.equals(logisticsInfo.get("acceptTime"), expressDetail.get("acceptTime"))
                    && ObjectUtils.equals(logisticsInfo.get("accept_address"), expressDetail.get("accept_address"))) {
                return true;
            }
        }
        return false;
    }

    private void sortExpressDetailList(List<Map<String, Object>> expressDetailList) {
        Collections.sort(expressDetailList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return -1 * ObjectUtils.compare((String) o1.get("acceptTime"), (String) o2.get("acceptTime"));
            }
        });
    }


    @Override
    public int getOrderCountByUid(Integer uid) {
        //(1)验证请求参数
        if (uid == null) {
            logger.warn("uid is empty");
            throw new ServiceException(ServiceError.ORDER_UID_IS_EMPTY);
        }

        return ordersMapper.selectCountByUid(uid);
    }

    @Override
    public PaymentBO getPaymentById(int id) {

        Payment payment = paymentDAO.selectByPrimaryKey((short) id);
        PaymentBO paymentBO = new PaymentBO();
        BeanUtils.copyProperties(payment, paymentBO);
        paymentBO.setId((int) payment.getId());
        paymentBO.setPayOrder((int) payment.getPayOrder());
        return paymentBO;
    }

    @Override
    public List<PaymentBO> getPaymentList() {

        List<Payment> payments = paymentDAO.selectByStatus();
        if (payments == null)
            return null;

        ArrayList<PaymentBO> list = new ArrayList<>();
        for (Payment payment : payments) {
            PaymentBO paymentBO = new PaymentBO();
            BeanUtils.copyProperties(payment, paymentBO);
            paymentBO.setId((int) payment.getId());
            paymentBO.setPayOrder((int) payment.getPayOrder());
            list.add(paymentBO);

        }

        return list;
    }

    @Override
    public OrderPayBankBO getOrderPayBank(long orderCode) {
        logger.info("Get order pay bank: {}", orderCode);
        OrderPayBankBO orderPayBankResponse = new OrderPayBankBO();
        OrdersPayBank orderPayBank = orderPayBankDAO.selectByOrderCode(orderCode);
        if (orderPayBank == null) {
            logger.warn("order not exist, orderCode: {}", orderCode);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        BeanUtils.copyProperties(orderPayBank, orderPayBankResponse);
        logger.info("Get order pay bank: orderCode {}, payment {}, bankCode {}", orderCode, orderPayBankResponse.getPayment(), orderPayBankResponse.getBankCode());
        return orderPayBankResponse;
    }

    @Override
    public void addOrderPayBank(long orderCode, byte payment, String bankCode) {
        logger.info("Add order pay bank: {}", orderCode);

        OrdersPayBank record = new OrdersPayBank();
        record.setOrderCode(orderCode);
        record.setPayment(payment);
        record.setBankCode(bankCode);

        //直接insert会导致多条记录，没有意义。改为set逻辑，避免select多条异常
        int count = orderPayBankDAO.updateByOrderCodeSelective(record);
        logger.info("Update order pay bank, orderCode: {}, update num: {}", orderCode, count);

        if (count == 0) {
            orderPayBankDAO.insert(record);
            logger.info("Add order pay bank records, orderCode: {}", orderCode);
        }

    }

    @Override
    public void modifyOrderPayBank(long orderCode, byte payment, String bankCode) {
        logger.info("Update order pay bank: {}", orderCode);

        OrdersPayBank record = new OrdersPayBank();
        record.setOrderCode(orderCode);
        record.setPayment(payment);
        record.setBankCode(bankCode);

        int count = orderPayBankDAO.updateByOrderCodeSelective(record);
        logger.info("Update order pay bank: order {}, count {}", orderCode, count);
    }


    /**
     * 异步处理拆单
     *
     * @param parentOrder
     */
    private void publishOrderSplitEvent(Orders parentOrder) {
        OrderSplitEvent event = new OrderSplitEvent();
        event.setUid(parentOrder.getUid());
        event.setOrderCode(parentOrder.getOrderCode());
        publisher.publishEvent(event);
    }

    /**
     * 保存更新预支付方式信息
     *
     * @param request
     */
    public void saveOrUpdatePrePayment(PrePaymentRequest request) {
        validateSaveOrUpdatePrePaymentRequest(request);
        logger.info("SaveOrUpdatePrePayment for order {}, request uid is {} payment is {}.", request.getOrderCode(), request.getUid(), request.getPayment());
        int now = (int) (System.currentTimeMillis() / 1000);
//        OrdersPrePay ordersPrePay = ordersPrePayDao.selectByPrimaryKey(request.getOrderCode());
//        if (ordersPrePay == null) {
//            ordersPrePay = new OrdersPrePay();
//            ordersPrePay.setOrderCode(request.getOrderCode());
//            ordersPrePay.setUid(request.getUid());
//            ordersPrePay.setPayment(request.getPayment());
//            ordersPrePay.setCreateTime(now);
//            ordersPrePay.setUpdateTime(now);
//            ordersPrePayDao.insert(ordersPrePay);
//        } else {
//            ordersPrePay.setPayment(request.getPayment());
//            ordersPrePay.setUpdateTime(now);
//            ordersPrePayDao.updateByPrimaryKeySelective(ordersPrePay);
//        }
        // 实际发生过并发时上述逻辑可能引发主键冲突，改为INSERT ... ON DUPLICATE KEY UPDATE
        OrdersPrePay ordersPrePay = new OrdersPrePay();
        ordersPrePay.setOrderCode(request.getOrderCode());
        ordersPrePay.setUid(request.getUid());
        ordersPrePay.setPayment(request.getPayment());
        ordersPrePay.setCreateTime(now);
        ordersPrePay.setUpdateTime(now);
        ordersPrePayDao.insertOnDuplicateUpdate(ordersPrePay);

        logger.info("SaveOrUpdatePrePayment for order {} success.", request.getOrderCode());
    }

    /**
     * 获取订单花呗分期详情
     *
     * @param orderCode
     * @return
     */
    @Override
    public List<AntHbfqBO> getAntHbfqDetail(long orderCode) {
        logger.info("getAntHbfqDetail for order: {}", orderCode);
        //验证请求参数
        if (orderCode < 1) {
            logger.error("getAntHbfqDetail fail, order code is empty.");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }

        //根据订单号查询订单
        Orders orderData = ordersMapper.selectByOrderCode(String.valueOf(orderCode));
        if (orderData == null) {
            logger.error("getAntHbfqDetail fail, can not find order {}.", orderCode);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }

        List<AntHbfqBO> hbfqBOList = antHbfqPayService.getAntHbfqDetail(orderData.getAmount().doubleValue());
        logger.info("hbfq detail: {}", hbfqBOList);
        return hbfqBOList;
    }

    private void validateSaveOrUpdatePrePaymentRequest(PrePaymentRequest request) {
        if (request == null) {
            logger.warn("SaveOrUpdatePrePayment fail, request is null.");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (request.getOrderCode() == null || request.getOrderCode() < 1) {
            logger.warn("SaveOrUpdatePrePayment fail, request order code is empty.");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        if (request.getPayment() == null || request.getPayment() < 1) {
            logger.warn("SaveOrUpdatePrePayment fail, request payment is empty.");
            throw new ServiceException(ServiceError.ORDER_PAYMENT_IS_EMPTY);
        }
        if (request.getUid() == null || request.getUid() < 1) {
            logger.warn("SaveOrUpdatePrePayment fail, request uid is empty.");
            throw new ServiceException(ServiceError.ORDER_UID_IS_EMPTY);
        }
    }

    /**
     * 判断订单状态是否可支付
     *
     * @param orderData
     * @return
     */
    private boolean isOrderPayable(Orders orderData) {
        if (orderData == null) {
            return false;
        }

        if (orderData.getStatus() != 0 || "Y".equals(orderData.getPaymentStatus())) {
            return false;
        }

        if ("Y".equals(orderData.getIsCancel())) {
            return false;
        }

        return true;
    }

}

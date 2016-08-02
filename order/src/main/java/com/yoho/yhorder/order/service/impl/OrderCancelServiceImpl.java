/*
 * Copyright (C), 2016-2016, yoho
 * FileName: OrderCancelServiceImpl.java
 * Author:   god_liu
 * Date:     2016年5月31日 下午1:47:10
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.ProductPriceBo;
import com.yoho.product.request.BatchBaseRequest;
import com.yoho.service.model.order.constants.InvoiceType;
import com.yoho.service.model.order.constants.OrderStatus;
import com.yoho.service.model.order.constants.OrdersMateKey;
import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;
import com.yoho.service.model.order.request.OrderCancelRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.order.utils.ClientTypeUtils;
import com.yoho.service.model.promotion.request.CouponsLogReq;
import com.yoho.service.model.promotion.request.LimitCodeReq;
import com.yoho.service.model.promotion.request.PromotionCodeReq;
import com.yoho.service.model.request.RedEnvelopesReqBO;
import com.yoho.service.model.request.YohoCoinReqBO;
import com.yoho.service.model.response.RedEnvelopesCancelRspBO;
import com.yoho.service.model.response.YohoCurrencyRspBO;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.cache.redis.OrderRedis;
import com.yoho.yhorder.common.cache.redis.UserOrderCache;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.model.*;
import com.yoho.yhorder.invoice.service.InvoiceService;
import com.yoho.yhorder.order.config.OrderConstant;
import com.yoho.yhorder.order.payment.service.PaymentService;
import com.yoho.yhorder.order.service.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * 取消订单---专用Service实现类
 *
 * @author maelk-liu
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
@Service
public class OrderCancelServiceImpl implements IOrderCancelService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Logger orderCloseLog = LoggerFactory.getLogger("orderCloseLog");

    @Autowired
    private IOrdersMapper ordersMapper;


    @Autowired
    private OrdersCouponsMapper ordersCouponsMapper;


    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    private IOrderMqService orderMqService;

    @Autowired
    private OrderPromotionInfoMapper orderPromotionInfoMapper;

    @Autowired
    private OrderCancelMapper orderCancelMapper;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    @Qualifier("mqErpService")
    private IErpService mqErpService;

    @Autowired
    private IOrderGoodsService orderGoodsService;

    @Autowired
    private IExpressCompanyDao expressCompanyDao;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IOrdersYohoCoinDAO ordersYohoCoinDAO;


    @Autowired
    private IOrdersPrePayDao ordersPrePayDao;

    @Autowired
    private IOrderExtAttributeDAO orderExtAttributeDAO;

    @Autowired
    private OrderRedis orderRedis;

    @Autowired
    private UserOrderCache userOrderCache;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Autowired
    private IOrdersDeliveryAddressRepository ordersDeliveryAddressService;

    @Autowired
    private IOrdersProcessStatusService ordersStatusService;

    /**
     * 用户取消
     */
    public void cancelByUser(OrderCancelRequest request) {
        orderCloseLog.info("CancelByUser {} start.", request.getOrderCode());
        Orders orders = findOrdersByOrderCode(request.getOrderCode());
        //订单已取消直接返回
        if (Constants.YES.equals(orders.getIsCancel())) {
            orderCloseLog.warn("CancelByUser {} fail, because of order has closed.", request.getOrderCode());
            return;
        }
        validateActiveCancel(orders);
        cancelOrderToDB(orders, 900);
        refund(orders);
        cancelOrderToErp(request.getOrderCode(), 900);
        orderRedis.cancelUserOrder(orders.getUid(), orders.getOrderCode(), 900);
        userOrderCache.clearOrderCountCache(orders.getUid());
        //取消订单原因
        insertCancelReson(orders, request);
        orderCloseLog.info("CancelByUser {} success.", request.getOrderCode());
    }

    /**
     * 自动取消 不需要将unpay缓存删除
     */
    public void cancelBySystemAuto(Long orderCode) {
        orderCloseLog.info("CancelByAuto {} start.", orderCode);
        Orders orders = findOrdersByOrderCode(orderCode);
        //订单已取消直接返回
        if (Constants.YES.equals(orders.getIsCancel())) {
            orderCloseLog.warn("CancelByAuto {}  fail, because of order has closed.", orderCode);
            return;
        }
        validateActiveCancel(orders);
        cancelOrderToDB(orders, 906);
        refund(orders);
        cancelOrderToErp(orderCode, 906);
        orderRedis.cancelUserOrder(orders.getUid(), orders.getOrderCode(), 906);
        userOrderCache.clearOrderCountCache(orders.getUid());
        buildSendCancelToWEchat(orders);
        orderCloseLog.info("CancelByAuto {} success, the order {} has closed.", orderCode);
    }

    /**
     * 客服取消
     */
    public void cancelByCS(Long orderCode) {
        orderCloseLog.info("CancelByCS {} start.", orderCode);
        Orders orders = findOrdersByOrderCode(orderCode);
        //订单已取消直接返回
        if (Constants.YES.equals(orders.getIsCancel())) {
            orderCloseLog.warn("CancelByCS {}  fail, because of order has closed.", orderCode);
            return;
        }
        //取消订单
        cancelOrderToDB(orders, 901);
        refund(orders);
        orderRedis.cancelUserOrder(orders.getUid(), orders.getOrderCode(), 901);
        userOrderCache.clearOrderCountCache(orders.getUid());
        buildSendCancelToWEchat(orders);
        orderCloseLog.info("CancelByCS {} success, the order {} has closed.", orderCode);
    }

    /**
     * ERP自动取消
     */
    public void cancelByErpAuto(Long orderCode) {
        orderCloseLog.info("CancelByErpAuto {} start.", orderCode);
        Orders orders = findOrdersByOrderCode(orderCode);
        //订单已取消直接返回
        if (Constants.YES.equals(orders.getIsCancel())) {
            orderCloseLog.warn("CancelByErpAuto {} fail, because of order has closed.", orderCode);
            return;
        }
        //取消订单
        cancelOrderToDB(orders, 906);
        refund(orders);
        orderRedis.cancelUserOrder(orders.getUid(), orders.getOrderCode(), 906);
        userOrderCache.clearOrderCountCache(orders.getUid());
        buildSendCancelToWEchat(orders);
        orderCloseLog.info("CancelByErpAuto {} success, the order has closed.", orderCode);
    }


    private void refund(Orders orders) {
        orderCloseLog.info("refund for order {}.", orders.getOrderCode());
        // 换货订单,不返回
        if (orders.getOrderType() != null && orders.getOrderType().intValue() == 7) {
            orderCloseLog.info("refund for order {} fail, the order is change order.", orders.getOrderCode());
            return;
        }
        Orders parentOrder = findParentOrders(orders);
        // 如果是JIT子订单
        if (parentOrder != null && Constants.YES.equals(parentOrder.getIsJit())) {
            refundYohoCoin(orders);
            //子订单,需要判断父订单下的所有子订单是否 全部取消,才能 返红包,退有货币,优惠券,优惠码
            refundByParentOrders(parentOrder);
        } else {
            // 返还YOHO币
            refundYohoCoin(orders);
            // 返还红包
            refundRedEnvelopes(orders);
            //返还优惠券
            refundOrderCouponUse(orders);
            //返还优惠码
            refundPromotionCode(orders);
            //退还限购码
            returnLimitCodeIfHavaLimitCode(orders);
        }
        orderCloseLog.info("refund for order {} success.", orders.getOrderCode());
    }

    private Orders findParentOrders(Orders orders) {
        if (orders.getParentOrderCode() != null && orders.getParentOrderCode() > 0) {
            orderCloseLog.info("find parent orders for {}.", orders.getOrderCode());
            Orders parentOrder = ordersMapper.selectByOrderCode(orders.getParentOrderCode().toString());
            if (parentOrder == null) {
                orderCloseLog.info("can not find parent orders for {}.", orders.getOrderCode());
            }
            return parentOrder;
        } else {
            return null;
        }
    }

    private void refundByParentOrders(Orders parentOrder) {
        List<Orders> subOrders = ordersMapper.selectByParentOrderCode(parentOrder.getOrderCode().toString());
        for (Orders order : subOrders) {
            if (Constants.NO.equals(order.getIsCancel())) {
                return;
            }
        }
        // 返还红包
        refundRedEnvelopes(parentOrder);
        //返还优惠券
        refundOrderCouponUse(parentOrder);
        //返还优惠码
        refundPromotionCode(parentOrder);
        //退还限购码
        returnLimitCodeIfHavaLimitCode(parentOrder);
    }


    private void cancelOrderToErp(Long orderCode, int type) {
        JSONObject data = new JSONObject();
        data.put("orderCode", orderCode);
        data.put("status", type);
        mqErpService.cancelOrder(data);
    }

    /**
     * 主动取消和自动取消时参数校验
     *
     * @param orders
     */
    private void validateActiveCancel(Orders orders) {
        //验证订单支付情况，已支付订单不能取消。
        if (Constants.YES.equals(orders.getPaymentStatus())) {
            orderCloseLog.warn("CloseOrderByCode fail, because of order {} has paid.", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_ORDER_HAS_PAID);
        }
        //已支付的订单不能取消
        if (orders.getStatus() > 3) {
            //>3是已发货
            orderCloseLog.warn("CloseOrderByCode fail, because of order:{} has shipped,status is{}", orders.getOrderCode(), orders.getStatus());
            throw new ServiceException(ServiceError.ORDER_CANCEL_FAILED_BY_STATUS_SHIPPED);
        }
        //修复支付成功订单可以取消的问题：
        //      订单支付状态尚未变更，用户迅速取消，此时再强制远程到Alipay检查支付是否已成功，
        //      如果已支付成功，则不允许取消订单(当前只考虑支付宝远程查询一次支付状态，其余支付方式不考虑)
        //默认是没支付
        boolean isPaid = queryPayStatusResult(orders);
        if (isPaid) {
            orderCloseLog.warn("CloseOrderByCode fail, order {} is TRADE_SUCCESS, payment type: {}.", orders.getOrderCode(), orders.getPayment());
            throw new ServiceException(ServiceError.ORDER_ORDER_HAS_PAID);
        }
        //取消訂單时进行时间间隔判断
        if (!checkPrePayTime(orders.getOrderCode())) {
            orderCloseLog.warn("CloseOrderByCode fail, prePayTime is less than setTime. order: {} ", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_CANCEL_FAILED_NOT_FIT_TIME);
        }
    }

    /**
     * 自动取消订单,推微信
     * 限制：同一个uid,一天只能推一笔取消订单的消息
     *
     * @param orders
     */
    private void buildSendCancelToWEchat(Orders orders) {
        orderCloseLog.info("begin buildSendCancelToWEchat,uid {},orderCode {}", orders.getUid(), orders.getOrderCode());
        //调user服务
        String openId = null;
        try {
            openId = serviceCaller.call("users.getWechatOpenId", String.valueOf(orders.getUid()), String.class);
            orderCloseLog.info("begin call service users.getWechatOpenId,uid {}", orders.getUid());
        } catch (Exception e) {
            orderCloseLog.info("call service users.getWechatOpenId fail", orders.getUid());
            return;
        }
        if (openId == null) {
            orderCloseLog.info("this user has no relationship to Yoho-Wechat-portal,uid:{}", orders.getUid());
            return;
        }
        String redisResult = orderRedis.getCancelOrderWechatResult(String.valueOf(orders.getUid()));
        //说明是首次
        if (redisResult == null) {
            JSONObject obj = new JSONObject();
            obj.put("cancel_time", DateUtil.getDateFormat(new Date(), "yyyy-MM-dd"));
            obj.put("order_code", orders.getOrderCode());
            orderCloseLog.info("first set cancel wechat redis,uid:{},orderCode:{}", orders.getUid(), orders.getOrderCode());
            orderRedis.setCancelOrderWechatResult(String.valueOf(orders.getUid()), obj.toString());
        } else {
            if ("fail".equals(redisResult)) {
                orderCloseLog.info("redis service is not available,orderCode:{},uid:{}", orders.getOrderCode(), orders.getUid());
                return;
            }
            //判断result中存储的时间是不是当天,如果不是,说明(redis并未失效)需要先清理redis,再设置
            //如果是，记录同一个id一天只能推一次消息
            JSONObject result = JSON.parseObject(redisResult);
            if (DateUtil.getDateFormat(new Date(), "yyyy-MM-dd").equals(result.getString("cancel_time"))) {
                orderCloseLog.info("the same user can only send cancelMessage to wechat at one day,uid is {}", orders.getUid());
                return;
            } else {
                JSONObject obj = new JSONObject();
                obj.put("cancel_time", DateUtil.getDateFormat(new Date(), "yyyy-MM-dd"));
                obj.put("order_code", orders.getOrderCode());
                orderRedis.setCancelOrderWechatResult(String.valueOf(orders.getUid()), obj.toString());
            }
        }
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        //类型为取消
        obj.put("type", "order_cancle");
        obj.put("urlParam", "order_code=" + orders.getOrderCode());
        obj.put("openid", openId);
        //订单金额
        data.put("orderProductPrice", orders.getAmount().setScale(2).doubleValue() + "元");
        List<OrdersGoods> ordersGoodsList = orderGoodsService.selectOrderGoodsByOrder(orders);
        //商品详情
        data.put("orderProductName", buildGoodsNames(ordersGoodsList));
        //收货信息
        data.put("orderAddress", orders.getProvince() + orders.getCity() + orders.getDistrict() + orders.getAddress());
        //订单编号
        data.put("orderName", orders.getOrderCode());
        data.put("cancel_time", DateUtil.getDateFormat(new Date(), "yyyy-MM-dd HH:mm"));
        obj.put("data", data);
        orderCloseLog.info("send cancel wechat success,uid:{},orderCode:{}", orders.getUid(), orders.getOrderCode());
        orderMqService.sendWechatPushMessage(obj);
    }

    private static String buildGoodsNames(List<OrdersGoods> list) {
        StringBuffer sb = new StringBuffer();
        if (list == null || list.size() == 0) {
            return "";
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.size() == 1 || i == list.size() - 1) {
                sb.append(list.get(i).getProductName());
            } else {
                sb.append(list.get(i).getProductName()).append(",");
            }
        }
        return sb.toString();
    }

    private boolean queryPayStatusResult(Orders orders) {
        boolean queryPayStatusResult = false;
        orderCloseLog.info("query pay status result for order {}.", orders.getOrderCode());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
            public Boolean call() {
                return paymentService.queryTradeStatus(orders);
            }
        });
        executor.execute(futureTask);
        //在这里可以做别的任何事情
        try {
            //取得结果，同时设置超时执行时间为1秒。同样可以用future.get()，不设置执行超时时间取得结果
            queryPayStatusResult = futureTask.get(1000, TimeUnit.MILLISECONDS);
            orderCloseLog.info("query pay status result for order {} success.", orders.getOrderCode());
        } catch (InterruptedException e) {
            futureTask.cancel(true);
        } catch (ExecutionException e) {
            futureTask.cancel(true);
        } catch (TimeoutException e) {
            futureTask.cancel(true);
            orderCloseLog.info("query pay status result for order {} timeout.", orders.getOrderCode());
            //向支付宝确认超时,当作未支付, modify by jipeng
            queryPayStatusResult = false;
            //throw new ServiceException(ServiceError.ORDER_PAYRESULT_CONFIRM_ING);
        } finally {
            executor.shutdown();
        }
        return queryPayStatusResult;
    }

    /**
     * 给erp调用：
     * 验证订单取消前的支付结果确认,返回:是否能取消   默认:Y
     *
     * @param orderCode
     * @return
     */
    public String validateCancelStatus(Long orderCode) {
        orderCloseLog.info("begin validateCancelStatus for ERP cancel order {}.", orderCode);
        Orders orders = findOrdersByOrderCode(orderCode);
        validateActiveCancel(orders);
        //能走到这,说明都是能正常取消的,否则会在此之前抛出异常
        orderCloseLog.info("leave validateCancelStatus for ERP cancel order {},canCancel:{}", orderCode, "Y");
        return "Y";
    }

    private void insertCancelReson(Orders orders, OrderCancelRequest request) {
        if (StringUtils.isEmpty(request.getReasonId())) {
            return;
        }

        byte cancelReasonId = 0;
        try {
            cancelReasonId = Byte.parseByte(request.getReasonId());
        } catch (NumberFormatException e) {
            logger.warn("parse ReasonId error,reason id is {}", request.getReasonId());
            cancelReasonId = 0;
        }

        if (cancelReasonId <= 0) {
            return;
        }

        orderCloseLog.info("insert cancel reason for order {}.", orders.getOrderCode());
        OrderCancel record = new OrderCancel();
        record.setOrderCode(orders.getOrderCode());
        record.setUid(orders.getUid());
        record.setReasonId(cancelReasonId);
        if (StringUtils.isEmpty(request.getReason())) {
            record.setReason(Constants.EMPTY_STRING);
        } else {
            record.setReason(request.getReason());
        }
        if (ClientTypeUtils.isIphone(request.getClientType())) {
            record.setCanceltype(OrderCancel.CANCEL_TYPE_IPHONE);
        } else if (ClientTypeUtils.isIpad(request.getClientType())) {
            record.setCanceltype(OrderCancel.CANCEL_TYPE_IPAD);
        } else if (ClientTypeUtils.isAndroid(request.getClientType())) {
            record.setCanceltype(OrderCancel.CANCEL_TYPE_ANDROID);
        } else if (ClientTypeUtils.isH5(request.getClientType())) {
            record.setCanceltype(OrderCancel.CANCEL_TYPE_H5);
        } else if (ClientTypeUtils.isPC(request.getClientType())) {
            record.setCanceltype(OrderCancel.CANCEL_TYPE_PC);
        } else if (ClientTypeUtils.isYoho(request.getClientType())) {
            record.setCanceltype(OrderCancel.CANCEL_TYPE_YOHO);
        } else {
            record.setCanceltype(OrderCancel.CANCEL_TYPE_YOHO);
            logger.info("insert cancel reason for order {}, unknown client type {}", orders.getOrderCode(), request.getClientType());
        }
        record.setCreateTime((int) (System.currentTimeMillis() / 1000));
        orderCancelMapper.insert(record);
        orderCloseLog.info("insert cancel reason for order {} success.", orders.getOrderCode());
    }

    /**
     * 判断预支付与取消下单时的时间间隔是否大于阈值
     *
     * @param orderCode
     * @return
     */
    private boolean checkPrePayTime(Long orderCode) {
        logger.info("begin ordersPrePayDAO.checkPrePayTime,orderCode:{} ", orderCode);
        OrdersPrePay orderPrePay = ordersPrePayDao.selectByPrimaryKey(orderCode);
        if (orderPrePay == null) return true;
        int payTime = (int) (System.currentTimeMillis() / 1000) - orderPrePay.getUpdateTime();
        logger.info("end ordersPrePayDAO.checkPrePayTime,orderCode:{} ,payTime:{}", orderCode, payTime);
        return payTime > OrderConstant.PRE_PAY_Time;
    }


    /**
     * 同步erp系统过来的mq消息，更新前台订单/取消订单等
     *
     * @param orderCode
     * @param status
     */
    public void updateOrderStatus(Long orderCode, int status, int express_id, String express_number) {
        logger.info("erp order status sync start,orderCode:{} status:{}", orderCode, status);
        // 取消订单 900:用户取消,901:客服取消,902:备货中取消,903:配货中取消,904:发货后取消,905:运输中取消,906:系统自动取消
        if (status == 901 || status == 902 || status == 903 || status == 904 || status == 905) {
            cancelByCS(orderCode);
            logger.info("erp order status sync success,orderCode:{} status:{}", orderCode, status);
        }
        // 系统自动取消
        else if (status == 906) {
            cancelByErpAuto(orderCode);
            logger.info("erp order status sync success,orderCode:{} status:{}", orderCode, status);
        }
        // 订单已审核
        else if (status == 100) {
            Orders orders = findOrdersByOrderCode(orderCode);
            ordersStatusService.save(orders, 2);
            logger.info("erp order status sync success,orderCode:{} status:{}", orderCode, status);
        }
        // 订单已备货
        else if (status == 200) {
            Orders orders = findOrdersByOrderCode(orderCode);
            ordersStatusService.save(orders, 3);
            logger.info("erp order status sync success,orderCode:{} status:{}", orderCode, status);
        }
        // 订单已交寄
        else if (status == 600) {
            Orders orders = findOrdersByOrderCode(orderCode);
            orders.setStatus((byte) 4);
            orders.setExpressId((byte) express_id);
            orders.setExpressNumber(express_number);
            ordersMapper.updateByPrimaryKeySelective(orders);
            userOrderCache.clearOrderCountCache(orders.getUid());
            buildSendPreReceiveToWEchat(orders, express_id, express_number);
            ordersStatusService.save(orders, 4);
            //开发票
            buildInvoice(orders);
            logger.info("erp order status sync success,orderCode:{} status:{}", orderCode, status);
        }
        // 订单已妥投
        else if (status == 700) {
            Orders orders = findOrdersByOrderCode(orderCode);
            orders.setStatus((byte) 6);
            ordersMapper.updateByPrimaryKeySelective(orders);
            userOrderCache.clearOrderCountCache(orders.getUid());
            // 发一个延迟队列，7天后赠送有货币
            sendDeliverYohoCoin(orderCode, orders.getId(), orders.getUid());
            ordersStatusService.save(orders, 6);
            //货到付款需要更新支付时间以及确认收货时间
            updateConfirmTime(orders);
            logger.info("erp order status sync success,orderCode:{} status:{}", orderCode, status);
        } else {
            logger.warn("erp order status sync fail,the order {} got an unknown status {}", orderCode, status);
        }
    }

    private void buildInvoice(Orders orders) {
        logger.info("begin buildInvoice,orderCode:{} ", orders.getOrderCode());
        OrdersMeta meta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), OrdersMateKey.ELECTRONIC_INVOICE);
        if (null == meta || null == meta.getMetaValue()) {
            logger.info("this order need not buildInvoice,orderCode:{}", orders.getOrderCode());
            return;
        }
        InvoiceBo bo = JSON.parseObject(meta.getMetaValue(), InvoiceBo.class);
        //没开过发票,防止重复开发票
        if (bo.getInvoiceNum() != null) {
            logger.info("this order has builded eleInvoice,not duplicate buid invoice orderCode:{}", orders.getOrderCode());
            return;
        }
        //电子发票
        if (bo.getType() == InvoiceType.electronic.getIntVal()) {
            OrderInvoiceBo orderInvoice = new OrderInvoiceBo();
            orderInvoice.setAmount(orders.getAmount().setScale(2).doubleValue());
            orderInvoice.setOrderId(orders.getId());
            if (orders.getShippingCost() != null){
                orderInvoice.setShippingCost(orders.getShippingCost().doubleValue());
            }
            orderInvoice.setGoodsItemList(buildGoodsItems(orders));
            orderInvoice.setOrderStatus(OrderStatus.normal);
            InvoiceBo oib = invoiceService.issueInvoice(orderInvoice);
            // json.put("orderInvoice", JSONObject.toJSONString(orderInvoice));
            // json.put("invoiceBo", JSONObject.toJSONString(oib));
        }
        //meta.setMetaValue(json.toString());
        //ordersMetaDAO.updateByPrimaryKey(meta);
    }

    private List<GoodsItemBo> buildGoodsItems(Orders orders) {
        List<OrdersGoods> ordersGoodsList = orderGoodsService.selectOrderGoodsByOrder(orders);
        if (ordersGoodsList == null || ordersGoodsList.size() == 0) {
            return null;
        }
        List<GoodsItemBo> goodsItemList = new ArrayList<GoodsItemBo>();
        GoodsItemBo item;
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            item = new GoodsItemBo();
            item.setBuyNumber(ordersGoods.getNum());
            item.setPayPrice(ordersGoods.getGoodsPrice().setScale(2).doubleValue());
            item.setPrductName(ordersGoods.getProductName());
            item.setSalePrice(ordersGoods.getSalesPrice());
            item.setSkn(ordersGoods.getProductSkn());
            goodsItemList.add(item);
        }
        List<GoodsItemBo> mergeList = new ArrayList<GoodsItemBo>();
        //indexMap,key为skn,value为list下标,记录不重复的skn,并标记goodsItemList的下标
        //numsMap 计数map,统计skn下的所有数量,做计算
        Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
        Map<Integer, Integer> numsMap = new HashMap<Integer, Integer>();
        List<Integer> skns = new ArrayList<Integer>();
        for (int i = 0; i < goodsItemList.size(); i++) {
            if (indexMap.get(goodsItemList.get(i).getSkn()) == null) {
                indexMap.put(goodsItemList.get(i).getSkn(), i);
                numsMap.put(goodsItemList.get(i).getSkn(), goodsItemList.get(i).getBuyNumber());
                skns.add(goodsItemList.get(i).getSkn());
            } else {
                //累加skn下的数量
                numsMap.put(goodsItemList.get(i).getSkn(), goodsItemList.get(i).getBuyNumber() + numsMap.get(goodsItemList.get(i).getSkn()));
            }
        }
        //调商品服务,获得市场价
        BatchBaseRequest<Integer> request = new BatchBaseRequest<Integer>();
        request.setParams(skns);
        ProductPriceBo[] priceBos = serviceCaller.call("product.queryProductPriceBySkns", request, ProductPriceBo[].class);
        if (priceBos == null || priceBos.length == 0) {
            logger.warn("call service product.queryProductPriceBySkns null,skns {}", skns);
            throw new ServiceException(ServiceError.ORDER_INVOICE_QUERY_SKNS_MARKETPRICE_NULL);
        } else {
            //市场价的校验，决定了发票的成功
            for (int i = 0; i < priceBos.length; i++) {
                if (priceBos[i].getMarketPrice() == null || priceBos[i].getMarketPrice() == 0) {
                    logger.warn("call service product.queryProductPriceBySkns marketprice is 0, priceBos {}", priceBos);
                    throw new ServiceException(ServiceError.ORDER_INVOICE_QUERY_SKNS_MARKETPRICE_NULL);
                }
            }
        }
        for (Integer i : indexMap.values()) {
            GoodsItemBo gb = goodsItemList.get(i);
            gb.setBuyNumber(numsMap.get(gb.getSkn()));
            setMarkketPrice(new ArrayList<ProductPriceBo>(Arrays.asList(priceBos)), gb);
            mergeList.add(gb);
        }
        return mergeList;
    }

    /**
     * 设置市场价格
     *
     * @param priceBos
     * @param gb
     */
    private void setMarkketPrice(List<ProductPriceBo> priceBos, GoodsItemBo gb) {
        for (ProductPriceBo price : priceBos) {

            if (price.getProductSkn().equals(gb.getSkn())) {
                if (price.getMarketPrice() != null) {
                    gb.setMarketPrice(Double.valueOf(Integer.toString(price.getMarketPrice())));
                }
                break;
            }
        }
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

    private void buildSendPreReceiveToWEchat(Orders orders, int expressId, String ExpressNum) {
        //调user服务
        String openId = null;
        try {
            openId = serviceCaller.call("users.getWechatOpenId", String.valueOf(orders.getUid()), String.class);
        } catch (Exception e) {
            orderCloseLog.info("call service users.getWechatOpenId fail", orders.getUid());
            return;
        }
        if (openId == null) {
            orderCloseLog.info("this user has no relationship to Yoho-Wechat-portal,uid:{}", orders.getUid());
            return;
        }
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        ExpressCompany expressCompany = expressCompanyDao.selectByPrimaryKey(expressId);
        //类型为待收货
        obj.put("type", "pre_receive");
        obj.put("urlParam", "order_code=" + orders.getOrderCode());
        obj.put("openid", openId);
        data.put("keyword2", expressCompany.getCompanyName());
        data.put("keyword3", ExpressNum);
        //订单编号
        data.put("keyword1", orders.getOrderCode());
        obj.put("data", data);
        orderMqService.sendWechatPushMessage(obj);
    }

    private Orders findOrdersByOrderCode(Long orderCode) {
        // 验证请求参数
        if (orderCode == null) {
            orderCloseLog.warn("sync erp order status Service fail, because of order code is empty.");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        // 根据订单号查询订单
        Orders orders = ordersMapper.selectByOrderCode(orderCode.toString());
        if (orders == null) {
            orderCloseLog.warn("sync erp order status Service fail, because of can not find order {}.", orderCode);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        return orders;
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

    private void cancelOrderToDB(Orders orders, int status) {
        orders.setIsCancel(Constants.YES);
        switch (status) {
            //用户取消
            case 900:
                orders.setCancelType(Orders.CANCEL_TYPE_USER);
                break;
            //客服取消
            case 901:
                orders.setCancelType(Orders.CANCEL_TYPE_CS);
                break;
            //备货中取消
            case 902:
                orders.setCancelType(Orders.CANCEL_TYPE_PREPARING);
                break;
            //配货中取消
            case 903:
                orders.setCancelType(Orders.CANCEL_TYPE_DISTRIBUTION);
                break;
            //发货后取消
            case 904:
                orders.setCancelType(Orders.CANCEL_TYPE_POST_SHIP);
                break;
            //运输中取消
            case 905:
                orders.setCancelType(Orders.CANCEL_TYPE_IN_TRANSIT);
                break;
            //系统自动取消
            case 906:
                orders.setCancelType(Orders.CANCEL_TYPE_SYSTEM_AUTO);
                orderCloseLog.info("the order has auto closed,orderCode is {},delayTime is {}", orders.getOrderCode(), (int) (System.currentTimeMillis() / 1000) - orders.getCreateTime() - 7200);
                break;
            default:
                orderCloseLog.info("unknown status {} for order {}.", status, orders.getOrderCode());
        }
        ordersMapper.updateByPrimaryKeySelective(orders);
        //判断传入的order_code是子订单号还是父订单号,只有父订单才能更新父订单下的所有子订单的状态 is_cancel为"Y"
        if (orders.getParentOrderCode() == null || orders.getParentOrderCode() == 0) {
            ordersMapper.updateSubOrderCancelStatusByParentCode(orders.getOrderCode(), (int) orders.getCancelType());
        }
    }

    private void refundRedEnvelopes(Orders orders) {
        OrderPromotionInfo orderPromotionInfo = orderPromotionInfoMapper.selectByOrderCode(orders.getOrderCode());
        if (null != orderPromotionInfo && orderPromotionInfo.getOrderPromotion() != null) {
            JSONObject jobj = JSONObject.parseObject(orderPromotionInfo.getOrderPromotion());
            double useRedEnvelopes = jobj.getDoubleValue("use_red_envelopes");
            if (useRedEnvelopes > 0) {
                RedEnvelopesReqBO red = new RedEnvelopesReqBO();
                red.setType(3);
                red.setRemark("取消订单加红包");
                red.setUid(orders.getUid());
                red.setAmount(useRedEnvelopes);
                red.setFounder(String.valueOf(orders.getUid()));
                red.setOrderCode(orders.getOrderCode());
                red.setActiveId(2);
                serviceCaller.call("users.cancelReturnRedenvelopes", red, RedEnvelopesCancelRspBO.class);
                orderCloseLog.info("call users.cancelReturnRedenvelopes service,param is {}", red);
            }
        }
    }

    private void refundYohoCoin(Orders orders) {
        if (orders.getYohoCoinNum() != null && orders.getYohoCoinNum() > 0) {
            YohoCoinReqBO yohoCoinReqBO = new YohoCoinReqBO();
            yohoCoinReqBO.setNum(orders.getYohoCoinNum());
            //判断是子订单号还是父订单号
            if (orders.getParentOrderCode() == null || orders.getParentOrderCode() == 0) {
                //父订单
                yohoCoinReqBO.setOrder_code(orders.getOrderCode());
                orderCloseLog.info("parent order return yohoCoin,so the orderCode:{},num:{}", orders.getOrderCode(), orders.getYohoCoinNum());
            } else {
                //传子订单，需要找到父订单
                yohoCoinReqBO.setOrder_code(orders.getParentOrderCode());
                orderCloseLog.info("sub order return yohoCoin,so the orderCode:{}, parentOrderCode:{},num:{}", orders.getOrderCode(), orders.getParentOrderCode(), orders.getYohoCoinNum());
            }
            yohoCoinReqBO.setType(2);
            yohoCoinReqBO.setUid(orders.getUid());
            yohoCoinReqBO.setPid(0);
            orderCloseLog.info("begin call users.refundYohoCoin service,param is : {} ", yohoCoinReqBO);
            //调用远程服务
            YohoCurrencyRspBO res = serviceCaller.call("users.refundYohoCoin", yohoCoinReqBO, YohoCurrencyRspBO.class);
            orderCloseLog.info("call users.refundYohoCoin service success,result is : {} ", res);
        }
    }

    /**
     * 返回优惠券
     *
     * @param orders
     */
    private void refundOrderCouponUse(Orders orders) {
        OrdersCoupons ordersCoupons = ordersCouponsMapper.selectByOrderId(orders.getId());
        if (ordersCoupons == null) {
            orderCloseLog.info("user {} not use coupons in order {}.", orders.getUid(), orders.getId());
            return;
        }
        CouponsLogReq couponsLogReq = new CouponsLogReq();
        couponsLogReq.setUid(orders.getUid());
        couponsLogReq.setOrderCode(orders.getOrderCode());
        boolean result = serviceCaller.call("promotion.cancelOrderCouponUse", couponsLogReq, Boolean.class);
        if (result) {
            orderCloseLog.info("cancelOrderCouponUse success by order code {}, uid {}.", orders.getOrderCode(), orders.getUid());
        } else {
            orderCloseLog.info("cancelOrderCouponUse fail by order code {}, uid {}.", orders.getOrderCode(), orders.getUid());
        }
    }

    /**
     * 返回优惠码
     *
     * @param orders
     */
    private void refundPromotionCode(Orders orders) {
        // 发货之前取消订单，可以继续使用优惠码
        if (orders.getStatus() >= 4) {
            return;
        }
        PromotionCodeReq req = new PromotionCodeReq();
        req.setUid(orders.getUid());
        req.setOrderCode(String.valueOf(orders.getOrderCode()));
        serviceCaller.call("promotion.updatePromotionCodeHistory", req, Boolean.class);
        orderCloseLog.info("refundPromotionCode success by order code {}, uid {}.", orders.getOrderCode(), orders.getUid());
    }

    /**
     * 回退限购码
     *
     * @param orders
     */
    private void returnLimitCodeIfHavaLimitCode(Orders orders) {
        String attributeStr = orderExtAttributeDAO.selectExtAttributeByOrderCodeAndUid(orders.getOrderCode(), orders.getUid());
        orderCloseLog.info("order code {} has ext attribute {}", orders.getOrderCode(), attributeStr);
        List<ShoppingItem> items = stringToShoppingItemList(attributeStr);
        if (CollectionUtils.isNotEmpty(items)) {
            ShoppingItem item = items.get(0);
            //回退
            LimitCodeReq req = new LimitCodeReq();
            req.setUid(orders.getUid());
            req.setOrderCode(String.valueOf(orders.getOrderCode()));
            req.setLimitCode(item.getLimitCode());
            req.setProductSkn(String.valueOf(item.getSkn()));
            req.setLimitProductCode(item.getLimitProductCode());
            orderCloseLog.info("call promotion.cancelLimitCodeUseRecord to return limit code,order code is {},request is {}", orders.getOrderCode(), req);
            Boolean success = serviceCaller.call("promotion.cancelLimitCodeUseRecord", req, Boolean.class);
            if (success) {
                orderCloseLog.info("return limit code success,order code is {}", orders.getOrderCode());
            } else {
                orderCloseLog.warn("CloseOrderByCode fail, because of can not returnLimitCodeIfHavaLimitCode by order code[{}], uid [{}]", orders.getOrderCode(), orders.getUid());
                throw new ServiceException(ServiceError.ORDER_CANCEL_ORDERS_LIMITCODE_FAIL);

            }
        }
    }

    /**
     * str 类似 [{"type":"limitcode","limitcode":"xxxxx","limitproductcode":"xxxxx","skn":1,"sku":1,"buy_number":1}]
     *
     * @param str
     * @return
     */
    private List<ShoppingItem> stringToShoppingItemList(String str) throws ServiceException {
        List<ShoppingItem> items = new ArrayList<>();
        if (StringUtils.isEmpty(str)) {
            return items;
        }
        try {
            JSONObject attributeJSON = JSON.parseObject(str);
            JSONArray array = attributeJSON.getJSONArray("product_sku_list");
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject ele = array.getJSONObject(i);
                    ShoppingItem item = new ShoppingItem();
                    String type = ele.getString("type");
                    item.setType(type);
                    item.setSku(ele.getIntValue("sku"));
                    item.setSkn(ele.getIntValue("skn"));
                    item.setBuyNumber(ele.getIntValue("buyNumber"));
                    item.setLimitProductCode(ele.getString("limitProductCode"));
                    item.setLimitCode(ele.getString("limitCode"));
                    items.add(item);
                }
            }
        } catch (Exception ex) {
            logger.warn("parse {} to ShoppingItem error", str, ex);
        }
        return items;
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
    
    /**
     * 验证订单取消前的支付结果确认,没有支付payment_status=N,已经支付没有支付payment_status=Y
     */
    public String getOrdersOnlinePaymentStatus(Orders orders) {
    	if(!Orders.PAYMENT_TYPE_ONLINE.equals(orders.getPaymentType())){
    		throw new ServiceException(ServiceError.ACTIVITY_NOT_CONTAIN_PRODUCT); 
    	}
        //验证订单支付情况，已支付订单不能取消。
        if (Constants.YES.equals(orders.getPaymentStatus())) {
            return  Constants.YES;
        }
        
        boolean isPaid = queryPayStatusResult(orders);
        if (isPaid) {
        	return  Constants.YES;
        }
        //取消訂單时进行时间间隔判断
        if (!checkPrePayTime(orders.getOrderCode())) {
        	logger.info("its difficult to enter this branch orderCode:{}", orders.getOrderCode());
        }
        return orders.getPaymentStatus();
    }

}

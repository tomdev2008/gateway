package com.yoho.yhorder.shopping.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.restbean.ResponseBean;
import com.yoho.core.common.utils.LocalIp;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.service.model.inbox.request.InboxReqBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.common.cache.redis.OrderRedis;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.dal.IOrderPromotionSnapshotDao;
import com.yoho.yhorder.dal.model.OrderPromotionSnapshot;
import com.yoho.yhorder.dal.model.YHPushCenterTempalte;
import com.yoho.yhorder.shopping.cache.YHPushCenterTempalteCacheService;
import com.yoho.yhorder.shopping.model.Order;
import com.yoho.yhorder.shopping.model.OrderGoods;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.service.IShoppingMqService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wujiexiang on 16/4/22.
 */
@Component
public class OrderSubmitHandler {

    private final static Logger logger = LoggerFactory.getLogger("orderEventLog");


    //大数据采集日志
    private final Logger collectLog = LoggerFactory.getLogger("collectLog");

    public final static String YHGW_WAITPAYNUM_PRE = "yh:gw:waitPayNum:";

    @Resource(name="yhRedisTemplate")
    private YHRedisTemplate<String, String> redisTemplate;

    @Autowired
    private OrderRedis orderRedis;

    @Autowired
    private IShoppingMqService shoppingMqService;

    @Autowired
    private IOrderPromotionSnapshotDao orderPromotionSnapshotDao;
    
    @Value("${erp.order.autoCancel.switch.key:N}")
    private String autoCancelType;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private YHPushCenterTempalteCacheService yhPushCenterTempalteCacheService;

    /**
     * @param event
     */
    @Async
    @EventListener
    public void handleOrderSubmitEvent(OrderSubmitEvent event) {
        Order order = event.getOrder();

        logger.info("begin to handle OrderSubmitEvent,uid is {},order code is {}",
                order.getUid(), order.getOrderCode());

        //记录订单使用的促销,待退换货需求在记录
        //addOrderPromotionSnapshot(order.getOrderCode(), event.getPromotionInfoList());

        splitMultiPackageIfNecessary(order);

        //货到付款
        cacheOrderToRedis(order);
 
        sendAutoCancelMessage(order.getOrderCode(),order.getPaymentType(),order.getPaymentStatus(),order.getOrderType());

        clearWaitPayNumCache(order.getUid());

        pushYohoCoinMessageWhenUseYohoCoin(order);

        //打印大数据采集日志
        printCollectLog(order);

        logger.info("end to handle OrderSubmitEvent");
    }

    private void sendAutoCancelMessage(Long orderCode,int paymentType,String paymentStatus,int orderType){
        /*if("N".equals(autoCancelType)){
            return;
        }*/
        //换货订单,直接返回
        if(orderType==7){
           return; 
        }
        //在线支付且未支付的订单，2小时后自动取消
        if(paymentType==1&&paymentStatus.equals("N")){
           JSONObject json = new  JSONObject();
           json.put("orderCode", orderCode);
           shoppingMqService.autoCancelOrder(json);
        }
    }

    private void addOrderPromotionSnapshot(long orderCode, List<PromotionInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            logger.info("order code {} did not use any promotion",orderCode);
            return;
        }

        List<OrderPromotionSnapshot> snapshotList = new ArrayList<>();
        for (PromotionInfo info : list) {
            OrderPromotionSnapshot snapshot = new OrderPromotionSnapshot();
            snapshot.setOrderCode(orderCode);
            snapshot.setPromotionId(info.getId());
            snapshot.setPromotionType(info.getPromotionType());
            snapshot.setActionParam(info.getActionParam());
            snapshot.setConditionParam(JSON.toJSONString(info.getCondition()));
            snapshot.setLimitParam(info.getLimitParam());
            snapshot.setRejectParam(StringUtils.join(info.getReject_param()));
            snapshot.setPriority(info.getPriority());
            snapshot.setStartTime(info.getStartTime());
            snapshot.setEndTime(info.getEndTime());
            snapshotList.add(snapshot);
        }
        try {
            orderPromotionSnapshotDao.insertOrderPromotionSnapshot(snapshotList);
            logger.error("add order promotion to database success, order code {},order promotion snapshotList {}.",
                    orderCode, snapshotList);
        } catch (Exception ex) {
            logger.error("exception happen when add order promotion to database, order code {},order promotion snapshotList {}.",
                    orderCode, snapshotList, ex);
        }
    }

    private void splitMultiPackageIfNecessary(Order order) {
        logger.info("order code {},payment status {},last order amount {},status {},is multi package {}",
                order.getOrderCode(),
                order.getPaymentStatus(),
                order.getLastOrderAmount(),
                order.getStatus(),
                order.getIsMultiPackage());

        if (shouldBeSplit(order)) {
            logger.info("order need to split immediately,uid {},order code {}", order.getUid(), order.getOrderCode());
            try {
                Integer subOrderNum = externalService.splitMultiPackage(order.getUid(), order.getOrderCode());
                logger.info("order split success,sub order num {}", subOrderNum);
            } catch (Exception ex) {
                logger.error("split mutili package error,uid is {},order code {}", order.getUid(), order.getOrderCode(), ex);
            }
        }
    }

    private boolean shouldBeSplit(Order order) {
        return "Y".equals(order.getPaymentStatus()) && "Y".equals(order.getIsMultiPackage());
    }

    /**
     * 缓存订单数据
     *
     * @param order
     */
    private void cacheOrderToRedis(Order order) {
        orderRedis.cacheUserOrder(order.getUid(), order.getOrderCode(), order.getAmount(), order.getPaymentType());

        logger.info("cache order info to redis success,uid is {},order code is {},amount is {},paymentType is {}",
                order.getUid(), order.getOrderCode(), order.getAmount(), order.getPaymentType());
    }

    private void clearWaitPayNumCache(int uid) {
        //清除待支付订单数量缓存
        String key = YHGW_WAITPAYNUM_PRE + uid;
        logger.info("delete redis key,key is {}", key);
        try {
            redisTemplate.delete(key);
            logger.info("delete redis key success");
        } catch (Exception ex) {
            logger.warn("delete redis key,key is {}", key, ex);
        }
    }

    private void pushYohoCoinMessageWhenUseYohoCoin(Order order) {
        double useYohoCoin = YHMath.mul(order.getYohoCoinNum(), order.getYohoCoinRatio());
        int yohoCoinNum = (int) useYohoCoin;
        if (yohoCoinNum > 0) {
            pushUseCoinMessage(order.getUid(), order.getOrderCode(), yohoCoinNum);
        }
    }

    private void pushUseCoinMessage(int uid, long orderCode, int yohoCoinNum) {
        logger.info("push usecoin message for uid {}, order {},yoho coin num is {}", uid, orderCode, yohoCoinNum);
        try {
            YHPushCenterTempalte tempalte = yhPushCenterTempalteCacheService.getUseCoinTemplate();
            InboxReqBO reqBO = new InboxReqBO();
            reqBO.setUid(uid);
            reqBO.setContent(tempalte.getContent().replace("{num}",String.valueOf(yohoCoinNum)).replace("{order_code}",String.valueOf(orderCode)));
            reqBO.setTitle(tempalte.getTitle());
            reqBO.setType("1");
            reqBO.setVerifyKey(0);
            logger.info("push usecoin message request is {}", reqBO);
            ResponseBean responseBean = externalService.saveInbox(reqBO);
            logger.info("push usecoin message success, response is {}", responseBean);
        } catch (Exception e) {
            logger.warn("push use coin message faild!,uid {},order {},yoho coin num is {}", uid, orderCode, yohoCoinNum, e);
        }
    }

    private void printCollectLog(Order order) {
        try {
            JSONObject jsonLog = new JSONObject();
            //客户端ip
            jsonLog.put("ip", order.getClientIP());
            jsonLog.put("order_code", String.valueOf(order.getOrderCode()));
            jsonLog.put("order_amount", String.valueOf(order.getOrderAmount()));
            jsonLog.put("last_order_amount", String.valueOf(order.getLastOrderAmount()));
            jsonLog.put("skn", getSkns(order.getGoodsList()));
            jsonLog.put("skn_num", getSknAndBuyNumbers(order.getGoodsList()));
            jsonLog.put("pay_mode", String.valueOf(order.getPaymentType()));
            jsonLog.put("uid", String.valueOf(order.getUid()));
            jsonLog.put("order_time", DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            jsonLog.put("collect_ip", LocalIp.getLocalIp());
            jsonLog.put("user_agent", getUserAgentForCollectLog(order.getClientType()));
            jsonLog.put("order_type", String.valueOf(order.getOrderType()));
            jsonLog.put("service_key", "order_log");
            collectLog.info("{}", jsonLog);
        } catch (Exception e) {
            logger.warn("printCollectLog error ,order {},", order, e);
        }
    }


    private String getSkns(List<OrderGoods> orderGoodsList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < orderGoodsList.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(orderGoodsList.get(i).getProduct_skn());
        }
        return builder.toString();
    }

    private String getSknAndBuyNumbers(List<OrderGoods> orderGoodsList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < orderGoodsList.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(orderGoodsList.get(i).getProduct_skn()).append(":").append(orderGoodsList.get(i).getBuy_number());
        }
        return builder.toString();
    }

    private String getUserAgentForCollectLog(String clientType) {
        if ("iphone".equals(clientType)) {
            return "2";
        } else if ("android".equals(clientType)) {
            return "2";
        } else if ("ipad".equals(clientType)) {
            return "2";
        } else if ("h5".equals(clientType)) {
            return "3";
        } else if ("wechat".equals(clientType)) {
            return "3";
        } else {
            return "1";
        }
    }
}

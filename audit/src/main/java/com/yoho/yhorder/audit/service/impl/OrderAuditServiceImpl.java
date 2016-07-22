package com.yoho.yhorder.audit.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.error.ServiceError;
import com.yoho.service.model.order.model.audit.AuditGoods;
import com.yoho.service.model.order.request.AuditRequest;
import com.yoho.yhorder.common.cache.redis.BigDataRedis;
import com.yoho.yhorder.common.cache.redis.OrderRedis;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.common.utils.OrderYmlUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.yoho.error.exception.ServiceException;
import com.yoho.yhorder.audit.service.IOrderAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wujiexiang on 16/3/24.
 */
@Service
public class OrderAuditServiceImpl implements IOrderAuditService {

    private Logger logger = LoggerFactory.getLogger("orderAuditLog");

    public final static String SUPPORT_STR = "Y";

    public final static String NOT_SUPPORT_STR = "N";

    @Autowired
    private BigDataRedis bigDataRedis;

    @Autowired
    private OrderRedis orderRedis;

    /**
     * 1.货到付款拒收两次
     * 2.品牌及单品限制
     * 3.同一UID24小时内使用货到付款累计订单总额超过5000元
     *
     * @param auditRequest
     * @return Pair key 是否支持N/Y,value:提示信息
     */
    public Pair<String, String> auditCodPay(AuditRequest auditRequest) {
        int uid = auditRequest.getUid();
        List<AuditGoods> goodsList = auditRequest.getGoodsList();
        double amount = auditRequest.getAmount();

        if (uid < 1) {
            throw new ServiceException(ServiceError.SHOPPING_UID_IS_NULL);
        }

        try {
            //1.货到付款拒收两次 从大数据的redis中获取
            boolean hasRejectedRecord = hasRejectedOrderRecordsLastTwoTimes(uid);
            if (hasRejectedRecord) {
                logger.warn("uid {} has reject order record", uid);
                return Pair.of(NOT_SUPPORT_STR, OrderYmlUtils.getCashOnDeliveryMessage("R1"));
            }
            //2.原有的限量商品、预售商品、化妆品
            //3.品牌及单品限制

            boolean notAllowCodPay = notAllowCodPay(goodsList);
            if (notAllowCodPay) {
                return Pair.of(NOT_SUPPORT_STR, OrderYmlUtils.getCashOnDeliveryMessage("R2"));
            }
            //3.同一UID24小时内使用货到付款累计订单总额超过5000元
            double orderAmountTotal = sumCodPayOrderAmountPre24Hours(uid);
            if (orderAmountTotal > Constants.CODPAY_ORDER_AMOUNT_THRESHOLD) {
                logger.info("uid {} cod order summary amount is {} pre 24h", uid, orderAmountTotal);
                return Pair.of(NOT_SUPPORT_STR, OrderYmlUtils.getCashOnDeliveryMessage("R4"));
            }

            //4.订单金额不能超过5000元
            if (amount > Constants.CODPAY_ORDER_AMOUNT_THRESHOLD) {
                logger.info("uid {},current amount is {}", uid, amount);
                return Pair.of(NOT_SUPPORT_STR, OrderYmlUtils.getCashOnDeliveryMessage("R5"));
            }

        } catch (Exception e) {
            //吃掉所以异常,不影响下单
            logger.warn("audit cod pay error,uid is {},goodsList is {},amount is {}", uid, goodsList, amount, e);
        }
        return Pair.of(SUPPORT_STR, OrderYmlUtils.getCashOnDeliveryMessage("R0"));
    }

    /**
     * 最近两次是否有拒收记录
     *
     * @param uid
     * @return
     */
    private boolean hasRejectedOrderRecordsLastTwoTimes(int uid) {
        boolean hasRecord = bigDataRedis.hasRejectedOrderRecord(uid);
        return hasRecord;
    }

    /**
     * 商品是否货到付款限制
     *
     * @param goodsList
     * @return
     */
    private boolean notAllowCodPay(List<AuditGoods> goodsList) {

        boolean notAllow = false;
        if (CollectionUtils.isNotEmpty(goodsList)) {
            AuditGoods goods = findCannotCodPayGoods(goodsList);
            if (goods != null) {
                logger.info("audit goods not allow to use codpay,goods is {}", goods);
                notAllow = true;
            }
        }
        return notAllow;
    }

    private AuditGoods findCannotCodPayGoods(List<AuditGoods> goodsList) {
        Assert.notEmpty(goodsList, "goodsList must not be empty");
        for (AuditGoods goods : goodsList) {

            //不能货到付款
            if ("N".equals(goods.getCan_cod_pay())) {
                return goods;
            }

            //化妆品不能货到付款
            if (Constants.MUST_ONLINE_PAYMENT_MISORT_LIST.contains(goods.getMiddle_sort_id())) {
                return goods;
            }
            if (Constants.IS_LIMIT_PRODUCT_STR.equals(goods.getIs_limited())) {
                return goods;
            }
            //如果是限量
            if (goods.getBuy_limit() > 0) {
                return goods;
            }

            //预售
            if (Constants.IS_ADVANCE_PRODUCT_STR.equals(goods.getIs_advance())) {
                return goods;
            }

            if (Constants.IS_JIT_PRODUCT_STR.equals(goods.getIs_jit())) {
                return goods;
            }
        }

        return null;
    }

    /**
     * 同一UID24小时内使用货到付款累计订单总额
     *
     * @param uid
     * @return
     */
    private double sumCodPayOrderAmountPre24Hours(int uid) {
        /**
         * redis 采取hash结果
         * 获取规则,先根据uid获取所有货到付款的订单field,然后拼接field ->key->判断key是否已经失效,失效从hash中移除
         */
        double orderAmountSummary = 0;
        Map<String, String> orderMap = orderRedis.getUserCodPayOrderMap(uid);
        logger.info("uid {} cod pay order of redis,orders are {}", uid, orderMap);
        if (MapUtils.isEmpty(orderMap)) {
            return orderAmountSummary;
        }

        List<String> expiredKeys = new ArrayList<>();

        String jsonStr = null;
        Set<String> fields = orderMap.keySet();
        try {

            for (String fieldName : fields) {
                jsonStr = orderMap.get(fieldName);
                JSONObject json = JSONObject.parseObject(jsonStr);
                Long createTimes = json.getLong("cacheAt");
                Double amount = json.getDoubleValue("orderAmount");
                if (orderRedis.expire(createTimes)) {
                    expiredKeys.add(fieldName);
                } else {
                    orderAmountSummary = YHMath.add(orderAmountSummary, amount);
                }
            }
        } catch (Exception ex) {
            logger.error("parse json string error, json string is {}", jsonStr, ex);
        }

        if (CollectionUtils.isNotEmpty(expiredKeys)) {
            logger.info("uid {} has expire codpay order,keys ares {}", uid, expiredKeys);
            orderRedis.removeUserExpiredCodPayFields(uid, expiredKeys.toArray(new String[expiredKeys.size()]));
        }


        return orderAmountSummary;
    }

    @Override
    public int removeBackList(int uid) {
        logger.info("remove uid {} from redis backlist", uid);
        boolean success = orderRedis.removeBackList(uid);
        return success ? 1 : 0;
    }
}

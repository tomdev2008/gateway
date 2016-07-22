package com.yoho.yhorder.common.cache.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.redis.YHHashOperations;
import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.yhorder.common.constants.ErpOrderStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by wujiexiang on 16/3/28.
 */
@Component
public class OrderRedis {

    private Logger logger = LoggerFactory.getLogger("orderAuditLog");

    @Autowired
    YHHashOperations<String, String, String> hashOperations;

    @Autowired
    YHValueOperations<String, String> valueOperations;

    @Resource(name = "yhRedisTemplate")
    YHRedisTemplate<String, String> redisTemplate;

    /**
     * 货到付款订单的缓存前缀,后缀为uid
     */
    public final static String USER_CODPAY_ORDER_LIST_PREFIX_KEY = "yh:order:codpay:uid:";

    /**
     * 未支付订单的缓存前缀,后缀为uid
     */
    public final static String USER_UNPAY_ORDER_LIST_PREFIX_KEY = "yh:order:unpay:uid:";

    /**
     * 用户黑名单key
     */
    public final static String USER_BLACKLIST_PREFIX_KEY = "yh:order:backlist:uid:";
    
    /**
     * 取消订单推送消息给微信,单个用户一天只能推一次
     */
    public final static String CANCEL_ORDER_SEND_WECHAT_KEY="yh:order:sendWechatCancelOrder:uid:";

    /**
     * 用户电子票
     */
    public final static String USER_ETICKET_SKU_PREFIX_KEY = "yh:order:ticket:uid:";


    //禁止货到付款时长,单位为分,默认为24h
    @Value("${order.codpay.lock.duration:1440}")
    private int codPayLockDuration;

    //货到付款黑名单,锁定时长
    // private int codPayBackListLockDuration  = codPayLockDuration * 2;

    /**
     * 数据过期时间(24h)
     */
    //private long EXPIRE_MILLSECONDS = codPayLockDuration  * 60 * 1000L;

    /**
     * 自动取消订单个数为6个,禁止用户下单
     */
    public final static int LOCK_USER_UNPAY_ORDER_NUM = 6;
    
    /**
     * 获得redis中uid对应发送记录的值
     * @param uid
     */
    public String getCancelOrderWechatResult(String uid){
        String redisKey;
        String result=null;
        redisKey = CANCEL_ORDER_SEND_WECHAT_KEY+uid;
        try {
            //首次获取可能为Null,排除redis不可用情形，业务判断处理
            result =valueOperations.get(redisKey);
        } catch (Exception e) {
            logger.warn("get cancelOrderToWechat redis result fail,key is : {}",redisKey,e);
            return "fail";
        }
        return result;
    }
    
    /**
     * 设置发送微信记录
     * @param uid
     */
    public void setCancelOrderWechatResult(String uid,String result){
        String redisKey = CANCEL_ORDER_SEND_WECHAT_KEY+uid;
        try {
            valueOperations.getAndSet(redisKey, result);
            //失效时间为当前时间到当天24点所剩的分钟数
            redisTemplate.longExpire(redisKey, getMinutesLeft(), TimeUnit.MINUTES);
        } catch (Exception e) {
            try {
                redisTemplate.delete(redisKey);
                logger.info("seldom face ,rediskey is",redisKey);
            } catch (Exception e1) {
                logger.warn("fatal warn: redis key is {}",redisKey);
            }
            logger.info("set cancelOrderToWechat redis result,key is :"+redisKey+" fail {}",e);
        }
    }
    
    private static int getMinutesLeft(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long x=cal.getTimeInMillis()-System.currentTimeMillis();
        return (int) x/60000;
    }
    
    /**
     * 删除已经失效的fields
     *
     * @param uid
     * @param fields
     */
    public void removeUserExpiredCodPayFields(int uid, String... fields) {
        deleteHashFields(USER_CODPAY_ORDER_LIST_PREFIX_KEY + uid, fields);
    }

    /**
     * 缓存订单,根据支付类型缓存到不同的key中
     *
     * @param uid
     * @param orderCode
     * @param amount
     * @param paymentType
     */
    public void cacheUserOrder(int uid, long orderCode, double amount, int paymentType) {
        logger.info("cache uid {} order to redis,order code is {},amount is {},paymentType is {},cache duration is {}m", uid, orderCode, amount, paymentType,codPayLockDuration);

        if (paymentType == 2) {
            //货到付款
            cacheUserCodPayOrderAmount(uid, orderCode, amount, codPayLockDuration);
        } else {
            //在线支付
            cacheUserUnPayOrderAmount(uid, orderCode, amount, codPayLockDuration);

        }

        lockUserIfNecessary(uid);


        logger.info("cache order to redis success,uid is {}, order code is {}\n", uid, orderCode);
    }

    private void cacheUserOrderAmount(int uid, long orderCode, double amount, String cacheKey, int minute) {
        try {
            JSONObject json = new JSONObject();
            json.put("cacheAt", System.currentTimeMillis());
            json.put("orderAmount", amount);
            json.put("autoCancel","N");

            hashOperations.putIfAbsent(cacheKey, String.valueOf(orderCode), json.toJSONString());

            redisTemplate.longExpire(cacheKey, minute, TimeUnit.MINUTES);

        } catch (Exception ex) {
            logger.warn("call redis hash put error,uid is {},orderCode is {},amount is {}", uid, orderCode, amount, ex);
        }
    }

    private void cacheUserCodPayOrderAmount(int uid, long orderCode, double amount, int mius) {
        cacheUserOrderAmount(uid, orderCode, amount, USER_CODPAY_ORDER_LIST_PREFIX_KEY + uid, mius);
    }

    private void cacheUserUnPayOrderAmount(int uid, long orderCode, double amount, int mius) {
        cacheUserOrderAmount(uid, orderCode, amount, USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid, mius);
    }

    private void deleteHashFields(String key, String... fields) {
        if (ArrayUtils.isEmpty(fields)) {
            return;
        }
        try {
            hashOperations.delete(key, fields);
        } catch (Exception ex) {
            logger.warn("call redis hash delete error,key is {},fields are {}", key, fields, ex);
        }
    }

    private String getHashField(String key, String fieldKey) {
        try {
            return hashOperations.get(key, fieldKey);
        } catch (Exception ex) {
            logger.warn("call redis hash get error,key is {},fieldKey is {}", key, fieldKey, ex);
        }
        return null;
    }

    private void putHashField(String key, String fieldKey, String fieldValue) {
        try {
            hashOperations.put(key, fieldKey, fieldValue);
        } catch (Exception ex) {
            logger.warn("call redis hash put error,key is {},fieldKey is {},fieldValue is {}", key, fieldKey, fieldValue, ex);
        }
    }

    /**
     * 取消用户订单
     *
     * @param uid
     * @param orderCode
     * @param cancleCode 900:用户取消,901:客服取消,902:备货中取消,903:配货中取消,904:发货后取消,905:运输中取消,906:自动取消
     */
    public void cancelUserOrder(int uid, long orderCode, int cancleCode) {
        logger.info("cancle uid {} order {} cache from redis by {}", uid, orderCode, cancleCode);

        if (cancleCode < ErpOrderStatus.CANCLE_BY_USER || cancleCode > ErpOrderStatus.AUTO_CANCLE_BY_SYSTEM) {
            logger.warn("order {} was cancel by incorrect [{}]", orderCode, cancleCode);
            return;
        }

        /**
         * 自动取消,当次数达到6次,用户加入黑名单
         */
        if (cancleCode == ErpOrderStatus.AUTO_CANCLE_BY_SYSTEM) {
            logger.info("order {} was canceld automatically", orderCode);
            autoCancelOrder(uid,orderCode);
            lockUserIfNecessary(uid);
        } else {
            removeUserOrder(uid, orderCode);
        }

        removeSkuBuyNumberByUid(uid);

        logger.info("cancel order success,uid is {},order is {}\n", uid, orderCode);
    }


    private void removeUserOrder(int uid, long orderCode) {

        logger.info("remove order cache from redis,uid {},order code {}", uid, orderCode);
        String redisKey = USER_CODPAY_ORDER_LIST_PREFIX_KEY + uid;
        logger.info("remove uid {} order {} cache from redis,redis key is {}", uid, orderCode,redisKey);

        deleteHashFields(redisKey, String.valueOf(orderCode));


        redisKey = USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid;
        logger.info("remove uid {} order {} cache from redis,redis key is {}", uid, orderCode,redisKey);

        deleteHashFields(USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid, String.valueOf(orderCode));

        logger.info("remove order cache from redis success,uid {},order code {}\n", uid, orderCode);

        //removeETicketCache(uid);
    }

    /**
     * 删除未支付订单的缓存,比如订单支付了
     *
     * @param uid
     * @param orderCode
     */
    public void removeUserUnPayOrder(int uid, long orderCode) {

        String key = USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid;
        logger.info("remove uid {} unpaid order {} cache from redis,key is {}", uid, orderCode,key);
        deleteHashFields(key, String.valueOf(orderCode));
    }

    private Map<String, String> getAllFieldsMap(String key) {
        try {
            return hashOperations.entries(key);
        } catch (Exception ex) {
            logger.warn("call redis hash entries error,key is {}", key, ex);
            return null;
        }
    }

    public Map<String, String> getUserCodPayOrderMap(int uid) {
        return getAllFieldsMap(USER_CODPAY_ORDER_LIST_PREFIX_KEY + uid);
    }

    public Map<String, String> getUserUnPayOrderMap(int uid) {
        return getAllFieldsMap(USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid);
    }

    /**
     * 自动取消订单
     * @param uid
     * @param orderCode
     */
    private void autoCancelOrder(int uid, long orderCode) {
        String key = USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid;
        String fieldKey = String.valueOf(orderCode);
        String fieldValue = this.getHashField(key, fieldKey);
        logger.info("auto cancel order info is {} from redis,key is {},fieldKey is {}", fieldValue, key, fieldKey);
        if (StringUtils.isNotEmpty(fieldValue)) {
            JSONObject json = null;
            try {
                json = JSON.parseObject(fieldValue);
            } catch (Exception ex) {
                logger.warn("parse text to jsonObject error,text is {}", fieldValue, ex);
            }
            if (json != null) {
                json.put("autoCancel", "Y");
                putHashField(key, fieldKey, json.toJSONString());
            }
        } else {
            logger.warn("redis unpay order hash have no order record,uid is {},order code is {}", uid, orderCode);
        }
    }


    /**
     * 用户自动取消订单累计6次,禁止用户48小时下单
     * @param uid
     */
    private void lockUserIfNecessary(int uid) {
        Map<String, String> unPayOrderMap = this.getUserUnPayOrderMap(uid);
        logger.info("try to lock user,uid {},unpaid order redis records are {}", uid, unPayOrderMap);
        if (MapUtils.isEmpty(unPayOrderMap)) {
            return;
        }

        int autoCancleOrderNum = 0;
        List<String> expiredKeys = new ArrayList<>();
        Set<String> fields = unPayOrderMap.keySet();
        for (String fieldName : fields) {
            JSONObject json = JSONObject.parseObject(unPayOrderMap.get(fieldName));
            Long createTimes = json.getLong("cacheAt");
            if (expire(createTimes)) {
                expiredKeys.add(fieldName);
            } else if (isAutoCancledOrder(json.getString("autoCancel"))) {
                autoCancleOrderNum++;
            }
        }
        if (CollectionUtils.isNotEmpty(expiredKeys)) {
            logger.info("uid {} has expire keys,keys ares {}", uid, expiredKeys);
            deleteHashFields(USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid, expiredKeys.toArray(new String[expiredKeys.size()]));
        }

        logger.info("uid {},auto cancel order num is {}", uid, autoCancleOrderNum);
        if (autoCancleOrderNum >= LOCK_USER_UNPAY_ORDER_NUM) {
            String backListKey = USER_BLACKLIST_PREFIX_KEY + uid;
            try {
                int lockTime = 2 * codPayLockDuration;
                valueOperations.setIfAbsent(backListKey, "1");
                redisTemplate.longExpire(backListKey, lockTime, TimeUnit.MINUTES);
                logger.info("uid {} was pushed to redis backlist,lock time is {}m", uid, lockTime);
            } catch (Exception ex) {
                logger.warn("call redis set error,key is {}", backListKey, ex);
            }
        }
    }

    /**
     *
     * @param autoCancel
     * @return
     */
    private boolean isAutoCancledOrder(String autoCancel) {
        return "Y".equals(autoCancel);
    }


    public boolean expire(long time) {
        return (System.currentTimeMillis() - time) > codPayLockDuration  * 60 * 1000L;
    }

    public boolean existBacklist(int uid) {
        try {
            return redisTemplate.hasKey(USER_BLACKLIST_PREFIX_KEY + uid);
        } catch (Exception ex) {
            logger.warn("call redis hasKey error,key is {}", USER_BLACKLIST_PREFIX_KEY + uid, ex);
            return false;
        }
    }

    private boolean deleteKey(List<String> keys) {
        try {
            redisTemplate.delete(keys);
            return true;
        } catch (Exception ex) {
            logger.warn("call redis delete error,keys are {}", keys, ex);
            return false;
        }
    }

    /**
     * 解除下单黑名单
     * @param uid
     */
    public boolean removeBackList(int uid) {
        List<String> keys = new ArrayList<>();
        keys.add(USER_BLACKLIST_PREFIX_KEY + uid);
        keys.add(USER_UNPAY_ORDER_LIST_PREFIX_KEY + uid);
        return deleteKey(keys);
    }

    /**
     *
     * @param uid
     * @param sku
     * @param buyNumber
     */
    public void incrementSkuBuyNumber(int uid, int sku, int buyNumber) {

        //TODO 不同步
        logger.info("cache user ticket sku,uid is {},sku is {},buy number is {}", uid, sku, buyNumber);

        String key = USER_ETICKET_SKU_PREFIX_KEY + uid;

        String hashKey = String.valueOf(sku);

        try {
            hashOperations.putIfAbsent(key, hashKey, String.valueOf("0"));
            redisTemplate.longExpire(key, 300, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.info("call redis hash putIfAbsent error,key is {},hash key is {}", key, hashKey, ex);
            return;
        }

        try {
            hashOperations.increment(key, hashKey, buyNumber);
            logger.info("increment success,key {},hash key {},num {}", key, hashKey, buyNumber);
        } catch (Exception ex) {
            logger.info("call redis hash increment error,key is {},hash key is {},increment is {}", key, hashKey, buyNumber, ex);
        }
    }

    /**
     * sku的购买数量
     * @param uid
     * @param sku
     * @return
     */
    public int getSkuBuyNumberIfExceptionReturnMaxInteger(int uid, int sku) {
        String key = USER_ETICKET_SKU_PREFIX_KEY + uid;
        String fieldKey = String.valueOf(sku);

        //不能捕获异常,涉及校验

        String fieldValue = null;

        try {
            fieldValue =  hashOperations.get(key, fieldKey);
        } catch (Exception ex) {
            logger.warn("call redis hash get error,key is {},fieldKey is {}", key, fieldKey, ex);
            return Integer.MAX_VALUE;
        }

        if (fieldValue != null) {
            try {
                return Integer.parseInt(fieldValue);
            } catch (NumberFormatException ex) {
                logger.info("parse {} to int error", fieldValue, ex);
                return Integer.MAX_VALUE;
            }
        }
        return 0;
    }

    public boolean existSkuByNumber(int uid, int sku) {
        String key = USER_ETICKET_SKU_PREFIX_KEY + uid;
        String fieldValue = this.getHashField(key, String.valueOf(sku));
        return fieldValue != null ? true : false;
    }

    /**
     * 删除用户电子票的缓存
     * @param uid
     */
    public void removeSkuBuyNumberByUid(int uid) {
        List<String> keys = new ArrayList<>();
        keys.add(USER_ETICKET_SKU_PREFIX_KEY + uid);
        deleteKey(keys);
        logger.info("remove ticket cache,keys are {}", keys);
    }

}
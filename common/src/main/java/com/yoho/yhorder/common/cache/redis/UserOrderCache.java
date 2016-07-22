package com.yoho.yhorder.common.cache.redis;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.service.model.order.response.OrdersStatusStatistics;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshijie on 16/4/21.
 */
@Component
public class UserOrderCache {
    private final static Logger logger = LoggerFactory.getLogger(UserOrderCache.class);

    private static String getOrderListCountKey(int uid, int type) {
        return "yh:order:userOrder:getOrderListCount:" + type + ":" + uid;
    }

    private static String getOrdersStatusStatisticsKey(int uid) {
        return "yh:order:userOrder:getOrdersStatusStatistics:" + uid;
    }

    // 缓存时间
    public final static long EXPIRE_TIME = 60;

    @Resource(name = "yhRedisTemplate")
    private YHRedisTemplate<String, String> redisTemplate;

    @Autowired
    private YHValueOperations<String, String> valueOperations;


    public Integer getOrderListCount(int uid, int type) {
        String key = getOrderListCountKey(uid, type);
        try {
            String value = valueOperations.get(key);
            if (StringUtils.isNotEmpty(value)) {
                return Integer.valueOf(value);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.warn("getOrderListCount from redis error: key is {}, error message is {}", key, e.getMessage());
            return null;
        }
    }

    public void cacheOrderListCount(int uid, int type, int count) {
        String key = getOrderListCountKey(uid, type);
        cache(key, String.valueOf(count), EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public OrdersStatusStatistics getOrdersStatusStatisticsByUid(int uid) {
        String key = getOrdersStatusStatisticsKey(uid);
        try {
            String value = valueOperations.get(key);
            if (StringUtils.isNotEmpty(value)) {
                return JSONObject.parseObject(value, OrdersStatusStatistics.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.warn("getOrdersStatusStatistics from redis error: key is {}, error message is {}", key, e.getMessage());
            return null;
        }
    }

    public void cacheOrdersStatusStatisticsByUid(int uid, OrdersStatusStatistics ordersStatusStatistics) {
        if (ordersStatusStatistics == null) {
            return;
        }
        String key = getOrdersStatusStatisticsKey(uid);
        cache(key, JSONObject.toJSONString(ordersStatusStatistics), EXPIRE_TIME, TimeUnit.SECONDS);
    }


    /**
     * 根据uid清除待支付、待发货、已发货的缓存。
     *
     * @param uid
     */
    public void clearOrderCountCache(Integer uid) {
        if (uid == null) {
            return;
        }
        clearCache(uid,
                // 订单状态统计
                getOrdersStatusStatisticsKey(uid),
                // 全部订单
                getOrderListCountKey(uid, 1),
                // 待付款
                getOrderListCountKey(uid, 2),
                // 待发货
                getOrderListCountKey(uid, 3),
                // 待收货
                getOrderListCountKey(uid, 4),
                // 成功订单
                getOrderListCountKey(uid, 5),
                // 取消订单
                getOrderListCountKey(uid, 7));
    }

    /**
     * 设置缓存
     */
    private void cache(String key, String value, long expire, TimeUnit timeUnit) {
        try {
            valueOperations.setIfAbsent(key, value);
            redisTemplate.longExpire(key, expire, timeUnit);
        } catch (Exception e) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e1) {

            }
            logger.warn("setValueToRedis error: key is {}, value is {}, expire is {}, error message is {}", key, value, expire, e.getMessage());
        }
    }

    /**
     * 清除缓存
     *
     * @param key
     */
    private void clearCache(Integer uid, String... key) {
        if (ArrayUtils.isEmpty(key)) {
            return;
        }
        try {
            redisTemplate.delete(Arrays.asList(key));
            logger.info("ClearCache success for user {} keys {}", uid, Arrays.asList(key));
        } catch (Exception ex) {
            logger.info("ClearCache fail for user {} keys {}", uid, Arrays.asList(key));
        }
    }

}

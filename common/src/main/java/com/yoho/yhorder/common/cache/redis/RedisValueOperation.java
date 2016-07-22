package com.yoho.yhorder.common.cache.redis;

import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.yhorder.common.utils.BeanTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by wujiexiang on 16/6/29.
 */
@Component
public class RedisValueOperation {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    YHValueOperations<String, String> valueOperations;

    @Resource(name = "yhRedisTemplate")
    YHRedisTemplate<String, String> redisTemplate;

    private void put(String key, String value, long expireTime, boolean overwrite) {
        try {
            if (overwrite) {
                valueOperations.set(key, value, expireTime, TimeUnit.SECONDS);
            } else {
                valueOperations.setIfAbsent(key, value);
                redisTemplate.longExpire(key, expireTime, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            logger.warn("call redis  put error,key is {},value is {},expire time is {}", key, value, expireTime, ex);
        }
    }

    public void put(CacheEnum cacheEnum, String postKey, String value) {
        this.put(cacheEnum, postKey, value, true);
    }

    public void put(CacheEnum cacheEnum, String postKey, String value, boolean overwrite) {
        String key = cacheEnum.getCacheKey(postKey);
        this.put(key, value, cacheEnum.getExpireTime(), overwrite);
    }

    private String get(String key) {
        try {
            return valueOperations.get(key);
        } catch (Exception ex) {
            logger.warn("call redis get error,key is {}", key, ex);
            return null;
        }
    }

    public <T> T get(CacheEnum cacheEnum, String postKey, Class<T> clazz) {
        String key = cacheEnum.getCacheKey(postKey);
        if (!hasKey(key)) {
            return null;
        }
        try {
            String value = this.get(key);
            if (value == null) {
                return null;
            }
            logger.info("get redis value operations. value is {}", value);
            return BeanTool.string2Value(value, clazz);
        } catch (Exception e) {
            logger.warn("get redis value operation failed. key is {}", key, e);
        }
        return null;
    }


    private boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception ex) {
            logger.warn("call redis hasKey error,key is {}", key, ex);
            return false;
        }
    }

    public boolean hasKey(CacheEnum cacheEnum, String postKey) {
        String key = cacheEnum.getCacheKey(postKey);
        return this.hasKey(key);
    }



}

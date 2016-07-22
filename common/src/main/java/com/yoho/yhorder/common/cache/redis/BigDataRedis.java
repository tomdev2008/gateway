package com.yoho.yhorder.common.cache.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by wujiexiang on 16/3/24.
 * 大数据的redis缓存
 */
@Component
public class BigDataRedis {

    private Logger logger = LoggerFactory.getLogger(BigDataRedis.class);

    private final static String USER_REJECT_ORDER_KEY = "ERP_BLACKLIST";

    @Resource(name = "bigdata-redisTemplate")
    private HashOperations<String, String, String> hashOperations;


    /**
     * @param uid
     * @return
     */
    public boolean hasRejectedOrderRecord(int uid) {
        try {
            return hashOperations.hasKey(USER_REJECT_ORDER_KEY, String.valueOf(uid));
        } catch (Exception ex) {
            logger.warn("call bigdata redis hasKey error,uid is {}", uid, ex);
            return false;
        }
    }
}

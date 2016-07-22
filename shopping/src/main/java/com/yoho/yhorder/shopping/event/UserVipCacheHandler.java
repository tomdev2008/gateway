package com.yoho.yhorder.shopping.event;

import com.yoho.yhorder.common.cache.redis.CacheEnum;
import com.yoho.yhorder.common.cache.redis.RedisValueOperation;
import com.yoho.yhorder.shopping.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by wujiexiang on 16/6/29.
 */
@Component
public class UserVipCacheHandler {

    private final static Logger logger = LoggerFactory.getLogger(UserVipCacheHandler.class);

    @Autowired
    private RedisValueOperation redisValueOperation;


    /**
     * cart item delete event
     *
     * @param event
     */
    @Async
    @EventListener
    public void handleUserVipCacheEvent(UserVipCacheEvent event) {

        UserInfo userInfo = event.getUserInfo();

        int userLevel = userInfo.getUserLevel();

        logger.info("begin to handle UserVipCacheEvent,uid:{},vip level:{}",
                userInfo.getUid(), userLevel);

        redisValueOperation.put(CacheEnum.USER_VIP,String.valueOf(userInfo.getUid()), String.valueOf(userLevel));

        logger.info("end to handle UserVipCacheEvent");
    }
}

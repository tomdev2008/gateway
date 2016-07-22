package com.yoho.yhorder.order.service.impl;

import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.yhorder.common.utils.cache.KeyGenerator;
import com.yoho.yhorder.dal.IOrderCodeListDAO;
import com.yoho.yhorder.dal.IOrderCodeQueueDAO;
import com.yoho.yhorder.order.service.IOrderCodeService;
import com.yoho.yhorder.common.utils.cache.ExpireTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by yoho on 2016/7/4.
 */
@Service
public class OrderCodeServiceImpl implements IOrderCodeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IOrderCodeListDAO orderCodeListDAO;

    @Autowired
    private IOrderCodeQueueDAO orderCodeQueueDAO;

    @Resource(name = "yhRedisTemplate")
    private YHRedisTemplate<String, String> redisTemplate;

    @Autowired
    private YHValueOperations<String, String> valueOperations;

    @Override
    public int findUidByOrderCode(long orderCode) {
        if (orderCode <= 100) {
            logger.info("find uid by order code fail, order code is empty.");
            return 0;
        }
        Integer uid = findUidByOrderCodeFromRedis(orderCode);
        if (uid != null) {
            logger.info("find uid by order code {} success from redis, uid is {}.", orderCode, uid);
            return uid.intValue();
        }
        long actOrderCode = Long.parseLong(String.valueOf(orderCode).substring(2));
        Integer id = orderCodeListDAO.selectIdByOrderCode(actOrderCode);
        if (id == null) {
            logger.info("find uid by order code {} fail, find nothing from order code list.", orderCode);
            return 0;
        }
        uid = orderCodeQueueDAO.selectUidById(id);
        if (uid == null) {
            logger.info("find uid by order code {} fail, find nothing from order code queue by id {}.", orderCode);
            return 0;
        }
        logger.info("find uid by order code {} success, uid is {}.", orderCode, uid);
        cacheUidToRedisByOrderCode(orderCode, uid);
        return uid.intValue();

    }

    private Integer findUidByOrderCodeFromRedis(long orderCode) {
        String key = KeyGenerator.generateOrderCodeUidKey(orderCode);
        try {
            String value = valueOperations.get(key);
            if (StringUtils.hasText(value)) {
                return Integer.valueOf(value);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.warn("find uid by order code {} fail from redis, key is {}.", orderCode, key);
            return null;
        }
    }

    private void cacheUidToRedisByOrderCode(long orderCode, Integer uid) {
        String key = KeyGenerator.generateOrderCodeUidKey(orderCode);
        try {
            logger.info("cache uid to redis by order code {}, key is {}.", orderCode, key);
            valueOperations.setIfAbsent(key, uid.toString());
            redisTemplate.longExpire(key, ExpireTime.ORDER_CODE_UID, TimeUnit.SECONDS);
            logger.info("cache uid to redis by order code {} success, key is {} uid is {}.", orderCode, key, uid);
        } catch (Exception e) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e1) {

            }
            logger.warn("cache uid to redis by order code {} fail, key is {} uid is {}.", orderCode, key, uid, e);
        }
    }
}

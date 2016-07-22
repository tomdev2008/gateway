package com.yoho.yhorder.shopping.union.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.yhorder.shopping.union.UnionService;
import com.yoho.yhorder.shopping.utils.MyStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by JXWU on 2015/12/8.
 */
public abstract class AbstractPushService implements UnionService {

    private final Logger logger = LoggerFactory.getLogger("unionPushLog");

    @Resource(name = "union-redisTemplate")
    private ValueOperations<String, String> valueOperations;

    @Resource(name = "union-redisTemplate")
    private ListOperations<String, String> listOperations;

    @Resource(name = "union-redisTemplate")
    private RedisTemplate redis;

    protected void push(String idfa, JSONObject eventJSON) {
        JSONObject makeData = new JSONObject();
        JSONObject accountJSON = new JSONObject();
        accountJSON.put("an", "com.yoho.buy");
        accountJSON.put("cn", "cn");
        accountJSON.put("ln", "zh");
        makeData.put("account", accountJSON);
        makeData.put("site_type", "aios");
        JSONObject idJSON = new JSONObject();
        idJSON.put("idfa", idfa);
        makeData.put("id", idJSON);
        JSONArray events = new JSONArray();
        events.add(eventJSON);
        makeData.put("events", events);
        makeData.put("version", "s2s_v1.0.0");

        String makeDataStr = makeData.toJSONString();
        JSONObject data = new JSONObject();
        data.put("url", "http://widget.cn.criteo.com/m/event");
        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("data", makeDataStr);
        data.put("params", paramsJSON);
        data.put("mode", "get");
        String key = MyStringUtils.getMd5(data.toJSONString());
        //String redisDB = "app";
        boolean haskey = redis.hasKey(key);
        logger.debug("redis has key {} {}", key, haskey);
        if (!haskey) {
            logger.info("push redis,data is {}", data);
            listOperations.leftPush("post_queue", data.toJSONString());
            //TODO
            //设置key的值为86400
            valueOperations.set(key, "86400");
            //缓存1s
            redis.expire(key, 1, TimeUnit.SECONDS);
        }
    }
}
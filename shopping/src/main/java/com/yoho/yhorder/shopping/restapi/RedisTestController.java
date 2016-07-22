package com.yoho.yhorder.shopping.restapi;

import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.core.rest.annotation.ServiceDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

/**
 * 内部测试redis
 * Created by fruwei on 2016/5/9.
 */
@Controller
@RequestMapping(value = "/shopping")
public class RedisTestController {
    @Autowired
    private YHRedisTemplate<String,String> yhRedisTemplate;

    @Autowired
    private  YHValueOperations<String,String> yhValueOperations;

    @RequestMapping("/redis_get")
    @ResponseBody
    @ServiceDesc("redis_get")
    public  String get(@RequestParam String key){
        String val=null;
        val=yhValueOperations.get(key);

        return val;

    }

    @RequestMapping("/redis_set")
    @ResponseBody
    @ServiceDesc("redis_set")
    public  String set(@RequestParam String key,@RequestParam String val,
                       @RequestParam(required = false,defaultValue = "60") int timeout){

        yhValueOperations.set(key,val,timeout, TimeUnit.SECONDS);

        return "ok";

    }

}

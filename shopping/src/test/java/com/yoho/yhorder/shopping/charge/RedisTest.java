package com.yoho.yhorder.shopping.charge;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.redis.YHHashOperations;
import com.yoho.core.redis.YHListOperations;
import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.yhorder.common.cache.redis.OrderRedis;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by wujiexiang on 16/3/28.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class RedisTest {

    @Autowired
    YHHashOperations<String, String, String> hashOperations;

    @Autowired
    YHValueOperations<String, String> valueOperations;

    @Resource(name = "yhRedisTemplate")
    YHRedisTemplate<String, String> redisTemplate;

    static String userCodPayOrderListKey = "yh:order:codpay:uid:8040155";

    @Resource(name = "union-redisTemplate")
    private ListOperations<String, String> listOperations;

    @Autowired
    private OrderRedis orderRedis;


    @Test
    public void test() {

        String codPayOrderKey = "yh:order:cod:order:";
        for (int i = 1; i < 100; i++) {
            String key = codPayOrderKey + i;
            String value = String.valueOf(i * 100);
            System.out.println(key + ";" + value);
            valueOperations.set(key, value);
            redisTemplate.longExpire(key, 3600, TimeUnit.SECONDS);
            hashOperations.put(userCodPayOrderListKey, key, value);
        }

    }

    @Test
    public void testUserBacklist()
    {
        orderRedis.existBacklist(123456);
    }

    @Test
    public void testcacheUserOrder() {
        int uid = 8039835;
        orderRedis.cacheUserOrder(uid, 1611430015, 3, 1);

        orderRedis.cacheUserOrder(uid, 1619149901, 3, 1);

        orderRedis.cacheUserOrder(uid, 1608564096, 3, 1);

        orderRedis.cacheUserOrder(uid, 1619810705, 3, 1);
        orderRedis.cacheUserOrder(uid, 1615324163, 3, 1);
        orderRedis.cacheUserOrder(uid, 1610313620, 3, 1);

    }

    @Test
    public void testCancelOrder()
    {
        int uid = 123456;
        int orderCode = 100000;
        for(int i = 0;i < 6;i++)
        {
            orderRedis.cancelUserOrder(123456, orderCode + i,906);
        }

        boolean back = orderRedis.existBacklist(uid);

        Assert.assertTrue(back);
    }

    @Test
    public void autoCancelOrder()
    {
        int uid = 8039835;
        orderRedis.cancelUserOrder(uid, 1611430015,906);

        orderRedis.cancelUserOrder(uid, 1619149901, 906);

        orderRedis.cancelUserOrder(uid, 1608564096, 906);

        orderRedis.cancelUserOrder(uid, 1619810705, 906);
        orderRedis.cancelUserOrder(uid, 1615324163, 906);
        orderRedis.cancelUserOrder(uid, 1610313620, 906);
    }

    @Test
    public void testUnion()
    {
        JSONObject dataJSON = new JSONObject();
        dataJSON.put("order_code", 160168015);
        dataJSON.put("unionid", 2919);
        dataJSON.put("union_name", "YOHO有货IOS");

        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("data", dataJSON.toJSONString());

//        StringBuilder builder = new StringBuilder();
//        builder.append("{").append("\"url\":").append("\"http://portal.admin.yohobuy.com/api/orderunion/updateunion\"");
//        builder.append(",").append("\"params\":").append("\"").append(paramsJSON).append("\"");
//        builder.append(",").append("\"mode\":\"post\"").append("}");

        String url="http://portal.admin.yohobuy.com/api/orderunion/updateunion";



        JSONObject postJSON = new JSONObject();
        postJSON.put("url", url);

        postJSON.put("params", paramsJSON);
        postJSON.put("mode", "post");

//        System.out.println( );
//
//
//        String tt = postJSON.toJSONString();


        listOperations.leftPush("post_queue",postJSON.toJSONString());

       // listOperations.leftPush("post_queue","{\"url\":\"http:\\/\\/portal.admin.yohobuy.com\\/api\\/orderunion\\/updateunion\",\"params\":{\"data\":\"{\\\"order_code\\\":160196149,\\\"unionid\\\":2919,\\\"union_name\\\":\\\"YOHO\\\\u6709\\\\u8d27IOS\\\"}\"},\"mode\":\"post\"}");
    }




    @Test
    public void hgetall()
    {

        Map<String, String> orderMap = hashOperations.entries(userCodPayOrderListKey);

        System.out.println("***************************");

        System.out.println(orderMap);

        System.out.println("***************************");
        double orderAmountSummary = 0;
        if (MapUtils.isNotEmpty(orderMap)) {
            List<String> expiredKeys = new ArrayList<>();
            Set<String> fields = orderMap.keySet();
            for (String fieldName : fields) {
                JSONObject json = JSONObject.parseObject(orderMap.get(fieldName));
                Long createTimes = json.getLong("cacheAt");
                Double amount = json.getDoubleValue("orderAmount");

                System.out.println("json="+json);

                if (expire(createTimes)) {
                    expiredKeys.add(fieldName);
                } else {
                    orderAmountSummary = YHMath.add(orderAmountSummary, amount);
                }

            }

            System.out.println("orderAmountSummary="+orderAmountSummary);

            if (CollectionUtils.isNotEmpty(expiredKeys)) {
                hashOperations.delete(userCodPayOrderListKey, expiredKeys.toArray(new String[expiredKeys.size()]));
            }
        }
    }

    private boolean expire(long time) {
        return (System.currentTimeMillis() - time) > 24*3600*1000L;
    }



}

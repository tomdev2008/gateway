package com.yoho.yhorder.shopping.charge;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.yoho.yhorder.dal.IOrderExtAttributeDAO;
import com.yoho.yhorder.dal.model.OrderExtAttribute;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2016/2/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class OrderTest {

    @Resource(name = "union-redisTemplate")
    private ListOperations<String, String> listOperations;

    @Test
    public void test()throws Exception
    {
        JSONObject dataJSON = new JSONObject();
        dataJSON.put("order_code", 1111);
        dataJSON.put("unionid", 129);
        dataJSON.put("union_name", "有货IOS");

        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("data", dataJSON);

        JSONObject postJSON = new JSONObject();
        postJSON.put("url", "http://portal.admin.yohobuy.com/api/orderunion/updateunion");
        postJSON.put("params", paramsJSON);
        postJSON.put("mode", "post");

        //
        //logger.debug("updateUnion->post->json {}", postJSON);



//        StringBuilder builder = new StringBuilder();
//
//        postJSON.writeJSONString(builder);

        listOperations.leftPush("post_queue", postJSON.toJSONString());
    }

    @Test
    public void test2()throws Exception
    {
        Map<String,Object> dataJSON = new HashMap<>();
        dataJSON.put("order_code", 1111);
        dataJSON.put("unionid", 129);
        dataJSON.put("union_name", "1111");

        Map<String,Object> paramsJSON = new HashMap<>();
        paramsJSON.put("data", dataJSON);

        Map<String,Object> postJSON = new HashMap<>();
        postJSON.put("url", "http://portal.admin.yohobuy.com/api/orderunion/updateunion");
        postJSON.put("params", paramsJSON);
        postJSON.put("mode", "post");

        //
        //logger.debug("updateUnion->post->json {}", postJSON);



//        StringBuilder builder = new StringBuilder();
//
//        postJSON.writeJSONString(builder);

       // JSONSerializer.

        //listOperations.leftPush("post_queue", postJSON.to);
    }
}

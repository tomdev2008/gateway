package com.yoho.error.test;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yoho.error.ServiceError;
import com.yoho.error.internel.ConfigLoader;

/**
 * Created by chang@yoho.cn on 2015/11/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/spring*.xml")
public class TestConfigLoader {

    @Resource
    private ConfigLoader configLoader;



    /**
     *
     300000001:
     message: "request error"
     gwError: 22222:消息不能为空
     */
    @Test
    public void test_gw_error_find()
    {

        Pair<Integer,String> error =  ServiceError.RESOURCES_REQUEST_ERROR.getMappingGatewayError();

        System.out.println(error);

        Assert.assertTrue(22222 == error.getLeft());

        Assert.assertTrue("消息不能为空".equals(error.getRight()));

    }

    /**
     *
     300000001:
     message: "request error"
     gwError: 22222:消息不能为空
     */
    @Test
    public void test_gw_error_not_find()
    {

        Pair<Integer,String> error =  ServiceError.RESOURCES_CONTENT_CODE_IS_EMPTY.getMappingGatewayError();

        System.out.println(error);

        Assert.assertTrue(998 == error.getLeft());

    }



}

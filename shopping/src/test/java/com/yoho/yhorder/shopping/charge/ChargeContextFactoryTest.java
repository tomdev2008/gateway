package com.yoho.yhorder.shopping.charge;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by JXWU on 2016/1/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "file:src/test/resources/META-INF/spring/spring-test-context.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class ChargeContextFactoryTest {

    private int uid;
    private String shoppingKey;

    @Resource
    ChargeContextFactory chargeContextFactory;

    @org.junit.Before
    public void setup()
    {
        uid = 0;
        shoppingKey = null;
    }
}

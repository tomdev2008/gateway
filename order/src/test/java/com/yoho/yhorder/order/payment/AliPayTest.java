package com.yoho.yhorder.order.payment;

import com.yoho.yhorder.order.payment.alipay.AliPayQuerier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by JXWU on 2016/2/2.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "file:src/test/resources/META-INF/spring/spring-test-context.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class AliPayTest {

    @Resource
    AliPayQuerier aliPayQuerier;

    @Test
    public void test()
    {
        String tradeNo = "1609872878";
        aliPayQuerier.query(tradeNo);
    }
}

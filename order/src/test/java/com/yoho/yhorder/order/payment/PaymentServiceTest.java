package com.yoho.yhorder.order.payment;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by JXWU on 2016/2/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "file:src/test/resources/META-INF/spring/spring-test-context.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class PaymentServiceTest {

    @Resource
    private YhProducerTemplate producerTemplate;


    @Test
    public void testSendMQ()
    {
        String orderCode ="160154671";
        double amount = 419;
        JSONObject statusData = new JSONObject();
        statusData.put("paymentCode", "");
        statusData.put("bankCode", "");
        statusData.put("bankName", "");
        statusData.put("amount", amount);
        statusData.put("payment", "");
        statusData.put("payOrderCode", "");
        statusData.put("tradeNo", "");
        statusData.put("bankBillNo", "");
        JSONObject data = new JSONObject();
        data.put("orderCode", orderCode);
        data.put("status", 200);
        data.put("statusData", statusData);


        try {
            producerTemplate.send("order.payment", data);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

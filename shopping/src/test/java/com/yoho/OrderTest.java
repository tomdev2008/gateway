package com.yoho;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by fruwei on 2016/5/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class OrderTest {

    @Test
    public  void testPaySuccessForSplitOrder(){


        //wait for split event hanle
        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

package com.yoho;

import com.yoho.yhorder.dal.model.UnionUsers;
import com.yoho.yhorder.shopping.model.Order;
import com.yoho.yhorder.shopping.model.OrderCreationContext;
import com.yoho.yhorder.shopping.model.UserInfo;
import com.yoho.yhorder.shopping.union.UnionContext;
import com.yoho.yhorder.shopping.union.impl.DefaultUnionServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by fruwei on 2016/5/27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class UninMqTest {
    @Autowired
    DefaultUnionServiceImpl defaultUnionService;


    @Test
    public void testMq(){
        String userAgent="frw_pc_test";
        int channel=1;
        Order order=new Order();
        order.setOrderCode(111111111L);
        order.setAmount(111.0);
        order.setUid(1);
        order.setUnionData("{'client_id':1010}");
        order.setCreateTime((int)(System.currentTimeMillis()));

        UnionUsers unionUsers=new UnionUsers();
        unionUsers.setId(111);
        unionUsers.setUserName("frw");


        OrderCreationContext orderCreationContext=new OrderCreationContext();
        orderCreationContext.setOrder(order);

        UnionContext unionContext=new UnionContext();
        unionContext.setOrderCreationContext(orderCreationContext);


        defaultUnionService.run(userAgent, unionContext);
    }
}

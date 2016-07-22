package com.yoho.yhorder.shopping.cart;

import com.alibaba.fastjson.JSON;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.response.shopping.ShoppingAddResponse;
import com.yoho.service.model.order.response.shopping.ShoppingCartMergeRequestBO;
import com.yoho.service.model.order.response.shopping.ShoppingCartMergeResponseBO;
import com.yoho.service.model.order.response.shopping.ShoppingQueryResponse;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.shopping.restapi.ShoppingCartController;
import com.yoho.yhorder.shopping.service.IShoppingCartService;
import com.yoho.yhorder.shopping.service.impl.ShoppingCartAddService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by fruwei on 2016/7/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class ShoppingCartTest {

    Logger log = LoggerFactory.getLogger(ShoppingCartTest.class);
    @Autowired
    ShoppingCartController cartController;

    @Autowired
    IShoppingCartService shoppingCartService;
    @Autowired
    ShoppingCartAddService shoppingCartAddService;

    @Test
    public void testCart() {

        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setUid(8050560);
//        request.setShopping_key("964b540393a989c5be0ad406cef7f936");
        ShoppingQueryResponse response = cartController.query(request);

        log.info("resp :#################################\n{}\n#########################################", JSON.toJSONString(response));

    }


    @Test
    public void testAdd() {

        ShoppingCartRequest req = new ShoppingCartRequest();
        req.setUid(8050560);
//        req.setShopping_key("be65ceb321d1339442ac98192b05f40d");
        req.setBuy_number(2);
        req.setSelected("Y");
        req.setProduct_sku(1029799);

        ShoppingAddResponse rep = shoppingCartAddService.add(req);
        log.info("resp :################\n{}\n###################", JSON.toJSONString(rep));
    }


    @Test
    public void testRemove() {

        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setUid(8050560);
//        request.setShopping_key("964b540393a989c5be0ad406cef7f936");
        ShoppingQueryResponse response = cartController.query(request);

        log.info("resp :#################################\n{}#########################################\n", JSON.toJSONString(response));

    }


    @Test
    public void testAddCustom() {

        ShoppingCartRequest req = new ShoppingCartRequest();
//        req.setUid(8050560);
//        req.setShopping_key("be65ceb321d1339442ac98192b05f40d");
        req.setBuy_number(2);
        req.setSelected("Y");
        req.setProduct_sku(1029799);

        ShoppingAddResponse rep = shoppingCartAddService.add(req);
        log.info("resp :################\n{}\n###################", JSON.toJSONString(rep));
    }

    @Test
    public void testMerge() throws InterruptedException {

        ShoppingCartRequest req = new ShoppingCartRequest();
//        req.setUid(8050560);
//        req.setShopping_key("be65ceb321d1339442ac98192b05f40d");
        req.setBuy_number(2);
        req.setSelected("Y");
        req.setProduct_sku(690271);

        ShoppingAddResponse rep = shoppingCartAddService.add(req);
        log.info("resp :################\n{}\n###################", JSON.toJSONString(rep));

        String shoppingKey = rep.getShopping_key();

        Thread.sleep(60000);

        ShoppingCartMergeRequestBO mergerReq = new ShoppingCartMergeRequestBO();
        mergerReq.setShopping_key(shoppingKey);
        mergerReq.setUid(8050560);
        ShoppingCartMergeResponseBO megreRsp = cartController.mergeCart(mergerReq);

        log.info("megreRsp :################\n{}\n###################", JSON.toJSONString(megreRsp));


    }


}

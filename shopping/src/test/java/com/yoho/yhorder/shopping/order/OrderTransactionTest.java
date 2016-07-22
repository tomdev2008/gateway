package com.yoho.yhorder.shopping.order;

import com.yoho.core.redis.YHValueOperations;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.service.IShoppingCartService;
import com.yoho.yhorder.shopping.service.impl.ShoppingCartAddService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wujiexiang on 16/5/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class OrderTransactionTest {

    @Autowired
    IShoppingCartService shoppingCartService;
    @Autowired
    ShoppingCartAddService shoppingCartAddService;

    @Autowired
    YHValueOperations<String, String> valueOperations;

    @Autowired
    private IShoppingCartItemsDAO shoppingCartItemsDAO;

    int uid = 8040155;

    @Test
    public void testAddAndSubmit() {

        //添加到购物车商品

        ShoppingCartRequest req = new ShoppingCartRequest();
        req.setUid(uid);
        req.setBuy_number(1);
        req.setSelected("Y");
        req.setProduct_sku(131874);
        shoppingCartAddService.add(req);


        ShoppingSubmitRequest request = new ShoppingSubmitRequest();
        request.setUid(uid);
        request.setPayment_type(2);
        request.setAddress_id(27);
        request.setUse_yoho_coin(10.00);
        shoppingCartService.submit(request);

        try {
            Thread.sleep(2000000);
        } catch (InterruptedException e) {

        }


    }

    @Test
    public void stubRPC() {
        valueOperations.set("yh:order:stub:rpc:" + uid, "1", 1, TimeUnit.SECONDS);

        //valueOperations.set("yh:order:stub:db:" + uid, "1", 1, TimeUnit.SECONDS);
    }


    @Test
    public void stubDB() {

        valueOperations.set("yh:order:stub:db:" + uid, "1", 1, TimeUnit.HOURS);
    }

    @Test
    public void testDBTransactional() {
        shoppingCartItemsDAO.insertShoppingCartGoods(uid, 1, 1, 1, 1, 1, "1", 1,"");

        System.out.println("*************************************************");
        try {
            Thread.sleep(10000000);
        }catch (Exception ex){}

        List<ShoppingCartItems> itemses = shoppingCartItemsDAO.selectByCartId(1,0);
    }

}

package com.yoho.yhorder.shopping.order;

import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.request.ShoppingComputeRequest;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.service.model.order.response.shopping.ShoppingSubmitResponse;
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
 * Created by fruwei on 2016/5/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class PaymentTest {
    Logger log = LoggerFactory.getLogger("FRW_TEST");
    @Autowired
    IShoppingCartService shoppingCartService;
    @Autowired
    ShoppingCartAddService shoppingCartAddService;


    public static final int uid = 8041886;

    @Test
    public void addShoppingCart() {


        //jit  1029799,936190
        addGoods(1029799, 3);   //luna limited 简约小高领毛衣
        addGoods(936190, 3);     //   less 横机针织纯色长袖套头衫 686.00


        //normal
        addGoods(1017470, 3);  //AliyaStore 【SHUT UP】嘻哈平沿棒球帽   99
//        addGoods(861014,1);
//        addGoods(877449,1);

        //   doSubmit();
    }

    @Test
    public void doPayment() {

        ShoppingSubmitRequest req = new ShoppingSubmitRequest();
        req.setUid(uid);
        req.setCoupon_code("p1dbcfbea");
        req.setPayment_type(1);
        req.setPayment_id(15);
        req.setDelivery_way(2);  //加急
        req.setDelivery_time(1);
        req.setUse_yoho_coin(2728.89);
        req.setCart_type("ordinary");
        req.setClient_type("pc");
        req.setAddress_id(2386832);
        req.setShopping_key("227a23642483b950bd607108b97f9ac7");

        ShoppingSubmitResponse rsp = shoppingCartService.submit(req);




        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }



    @Test
    public void testJit0Payment() {

        addGoods(1029799, 1);   //LUNA LIMITED 简约小高领毛衣  140
        addGoods(936190, 1);     //   less 横机针织纯色长袖套头衫 686.00
        //normal
        addGoods(1017470, 1);   //AliyaStore 【SHUT UP】嘻哈平沿棒球帽   99

//        if(true)
//            return ;
        ShoppingSubmitRequest req = new ShoppingSubmitRequest();
        req.setUid(uid);
        req.setCoupon_code("p1dbcfbea");
        req.setPayment_type(1);
        req.setPayment_id(15);
        req.setDelivery_way(2);  //加急
        req.setDelivery_time(1);
        req.setUse_yoho_coin(875.99);
        req.setCart_type("ordinary");
        req.setClient_type("pc");
        req.setAddress_id(2386832);
        req.setShopping_key("227a23642483b950bd607108b97f9ac7");

        ShoppingSubmitResponse rsp = shoppingCartService.submit(req);




        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testNormalPayment() {

        addGoods(1017470, 1);  //帽子   99
        addGoods(1017470, 1);  //帽子   99
        addGoods(1017470, 1);  //帽子   99
        addGoods(1017470, 1);  //帽子   99
        addGoods(1017470, 1);  //帽子   99
        addGoods(1017470, 1);  //帽子   99
        addGoods(1017470, 1);  //帽子   99
        addGoods(609751, 1);    //蝴蝶结  60
        addGoods(609751, 1);    //蝴蝶结  60

        ShoppingSubmitRequest req = new ShoppingSubmitRequest();
        req.setUid(uid);
        req.setCoupon_code("p1dbcfbea");
        req.setPayment_type(1);
        req.setPayment_id(15);
        req.setDelivery_way(2);
        req.setDelivery_time(1);
        req.setUse_yoho_coin(752.99);
        req.setClient_type("ordinary");
        req.setAddress_id(2386832);
        req.setShopping_key("227a23642483b950bd607108b97f9ac7");

        ShoppingSubmitResponse rsp = shoppingCartService.submit(req);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void addGoods(int sku, int buyNum) {
        //添加到购物车商品
        ShoppingCartRequest req = new ShoppingCartRequest();
        req.setUid(uid);
        req.setBuy_number(buyNum);
        req.setSelected("Y");
        req.setProduct_sku(sku);
        try {
            shoppingCartAddService.add(req);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }


    }


    @Test
    public void doCompute() {
        ShoppingComputeRequest req = new ShoppingComputeRequest();
        req.setUse_yoho_coin(930.00);
        req.setUid(uid);
        req.setDelivery_way(2);
        shoppingCartService.compute(req);
    }

    @Test
    public void doSubmit() {
        ShoppingSubmitRequest req = new ShoppingSubmitRequest();
        req.setUid(uid);
        req.setPayment_type(1);
        req.setPayment_id(15);
        req.setDelivery_way(2);
        req.setDelivery_time(1);
        req.setUse_yoho_coin(199.00);
        req.setClient_type("ordinary");
        req.setCoupon_code("p1dbcfbea");
        req.setAddress_id(2386832);
        req.setShopping_key("227a23642483b950bd607108b97f9ac7");

        ShoppingSubmitResponse rsp = shoppingCartService.submit(req);
    }


    @Test
    public void testUnion() {

        addGoods(1017470, 1);  //帽子   99
        addGoods(1017470, 1);  //帽子   99
        addGoods(609751, 1);    //蝴蝶结  60

        ShoppingSubmitRequest req = new ShoppingSubmitRequest();
        req.setUid(uid);
        req.setPayment_type(1);
        req.setPayment_id(15);
        req.setDelivery_way(2);
        req.setDelivery_time(1);
        req.setUse_yoho_coin(107.11);
        req.setClient_type("ordinary");
        req.setAddress_id(2386832);
        req.setShopping_key("227a23642483b950bd607108b97f9ac7");
        req.setUser_agent("YH_Mall_iPhone_Pre/3.8.1.1511270001(Model/iPhone 4S;OS/iOS8.4.1;Scale/2.00;Channel/1010;Resolution/320*480;Udid/5b2891c56eaed2b2423868b510d1646c436ef760;sid/f945612fb921b95c58bfd47457cf9cbc;ts/1450334406;uid/;ifa/5FED334D-5E8F-4083-AAC4-0CC7FBBEC87E)");
        req.setQhy_union("{\"channel_code\":\"578789\",\"client_id\":\"1010\"}");

        ShoppingSubmitResponse rsp = shoppingCartService.submit(req);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

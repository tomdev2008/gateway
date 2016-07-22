package com.yoho.yhorder.shopping.charge;

import com.alibaba.fastjson.JSON;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.model.OrderYohoCoin;
import com.yoho.yhorder.shopping.model.UserInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by JXWU on 2016/1/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "file:src/test/resources/META-INF/spring/spring-test-context.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class PromotionCodeChargeTest {

    List<ShoppingCartItems> itemsList = null;

    ChargeParam chargeParam = null;

    List<ChargeGoods> chargeGoodseList = null;

    UserInfo userInfo = null;

    @Autowired
    ChargerService chargerService;

    @Test
    public void makeShoppingCartItems()
    {
        itemsList = new ArrayList<>();
        ShoppingCartItems items = new ShoppingCartItems();
        items.setId(1);
        items.setSelected("Y");
        items.setShoppingCartId(1);
        items.setNum(1);
        items.setProductSkn(1);
        items.setSkuId(1);

        items.getExtMap().put("vip_price", 0);
        double salesPrice = 98.78;
        double marketPrice = 100;
        items.getExtMap().put("sales_price", salesPrice * 1.0);
        items.getExtMap().put("real_price", salesPrice * 1.0);
        items.getExtMap().put("market_price", marketPrice * 1.0);
        items.getExtMap().put("last_vip_price", salesPrice * 1.0);
        items.getExtMap().put("last_price", salesPrice * 1.0);

        items.getExtMap().put("goods_images", "");
        items.getExtMap().put("color_name", "");
        items.getExtMap().put("product_id", 1);
        items.getExtMap().put("product_name", "test");
        items.getExtMap().put("selected", "Y");
        items.getExtMap().put("promotion_id", 0);
        items.getExtMap().put("color_id", 0);
        items.getExtMap().put("goods_id", 0);
        items.getExtMap().put("size_name", "test");
        items.getExtMap().put("size_id", 1);
        items.getExtMap().put("storage_number", 100);

        items.getExtMap().put("yoho_coin_num", 0);
        items.getExtMap().put("get_yoho_coin", 0);

        items.getExtMap().put("real_vip_price", new Integer(0));//vip 价格
        items.getExtMap().put("vip_discount_money", new Integer(0));//vip 金额
        items.getExtMap().put("vip_discount_type", 0);//VIP折扣类型
        items.getExtMap().put("vip_discount", 0);//VIP折扣

        items.getExtMap().put("vip1_price", "0.00");//VIP1
        items.getExtMap().put("vip2_price", "0.00");//VIP2
        items.getExtMap().put("vip3_price", "0.00");//VIP3

        items.getExtMap().put("brand_id", 1);
        items.getExtMap().put("is_limited", 0);
        items.getExtMap().put("max_sort_id", 0);
        items.getExtMap().put("middle_sort_id", 0);
        items.getExtMap().put("small_sort_id", 0);
        items.getExtMap().put("is_special", "N");//是否特殊商品

        items.getExtMap().put("promotion_flag",0);//促销ID
        items.getExtMap().put("buy_number", 2);
        items.getExtMap().put("is_advance", "N");//是否预售
        items.getExtMap().put("is_outlets", "N");//outlets
        items.getExtMap().put("attribute", 0);//商品属性
        items.getExtMap().put("uid", 1);
        items.getExtMap().put("shopping_cart_id", 1);
        items.getExtMap().put("product_skn", 1);
        items.getExtMap().put("product_skc", 1);
        items.getExtMap().put("product_sku", 1);

        items.getExtMap().put("expect_arrival_time", "");

        items.getExtMap().put("activities_id", 0);

        items.getExtMap().put("is_jit", "N");//JIT
        items.getExtMap().put("shop_id", 0);//店铺
        items.getExtMap().put("supplier_id", 0);//供应商
        items.getExtMap().put("shopping_cart_goods_id", 1);

        items.getExtMap().put("buy_limit", 0);//商品限购数量

        itemsList.add(items);

        Assert.assertEquals(1, itemsList.size());
    }

    @Test
    public void makeChargeParam()
    {
        chargeParam = new ChargeParam();
        chargeParam.setUid(1);
    }

    @Test
    public void makePromotionCodeRequest(){
//        PromotionCodeParam request = new PromotionCodeParam();
//        request.setPromotionCode("t001");
//        request.setDiscountType("2");
//        request.setAmountAtLeast(100);
//        request.setCountAtLeast(2);
//        request.setDiscount(0.77f);
//        request.setDiscountAtMost(100);
//        chargeParam.setPromotionCodeParam(request);
//        chargeParam.setNeedCalcShippingCost(true);
//        Assert.assertEquals("t001",chargeParam.getPromotionCodeParam().getPromotionCode());
    }

    @Test
    public void makeChargeGoodsList()
    {
        chargeGoodseList = new LinkedList<>();
        for (ShoppingCartItems shoppingCartGoods : itemsList) {
            chargeGoodseList.add(new ChargeGoods(shoppingCartGoods));
        }
    }

    @Test
    public void makeUserInfo()
    {
        userInfo = new UserInfo();
        userInfo.setUserLevel(0);
        userInfo.setMonthOrderCount(0);
        userInfo.setRedEnvelopes(0);
        OrderYohoCoin yohoCoin = new OrderYohoCoin();
        yohoCoin.setCoinUnit("fen");
        yohoCoin.setYohoCoinNum(0);
        userInfo.setOrderYohoCoin(yohoCoin);
    }

    @Before
    public void setup()
    {
        makeChargeParam();
        makePromotionCodeRequest();
        makeShoppingCartItems();
        makeChargeGoodsList();
        makeUserInfo();
    }

    @Test
    public void testPromotionCodeCharge()
    {


        ChargeContext chargeContext = new ChargeContext();
        chargeContext.setChargeGoodsList(chargeGoodseList);
        chargeContext.setChargeParam(chargeParam);
        chargeContext.setUserInfo(userInfo);
        chargerService.charge(chargeContext);

        System.out.println(JSON.toJSONString(chargeContext.getChargeResult()));
    }

}

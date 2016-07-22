package com.yoho.yhorder.shopping.promotion.test;

import com.yoho.service.model.order.model.promotion.PromotionCondition;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.response.shopping.ShoppingGoods;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.promotion.service.CartPromotionService;
import com.yoho.yhorder.shopping.charge.promotion.service.PromotionInfoRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 *  test x 件 y 元
 *
 * Created by chunhua.zhang@yoho.cn on 2015/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/spring*.xml"})
public class TestSpecialAmount {



    @Autowired
    @InjectMocks
    private CartPromotionService cartPromotionService;


    @Mock
    private PromotionInfoRepository repository;


    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);


    }

    /**
     * 满599， brand = 517 ，减少100
     *
     */
    @Test
    public void test_fit() throws IOException {

        //prepare
        List<PromotionInfo> promotionInfos = new LinkedList<>();

        PromotionInfo info = new PromotionInfo();

        String conJSOn = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("promotion_flag.json"));
        PromotionCondition condition = new PromotionCondition(conJSOn);
        info.setCondition(condition);
        info.setId("6324");
        info.setTitle("2件1000块");
        info.setFitChannel("1,2,3");
        info.setPromotionType("SpecifiedAmount");
        info.setActionParam(" {\"specified_amount_list\":\"2:1000\"}");
        info.setRejectParam("6392");
        promotionInfos.add(info);

        when(repository.getActivePromotionsByFitChannel()).thenReturn(promotionInfos);


        ChargeContext chargeContext = new ChargeContext();


        //总计
        ChargeTotal chargeTotal = new ChargeTotal();
        chargeContext.setChargeTotal(chargeTotal);

        List<ChargeGoods> mainGoods = new LinkedList<>();

        ChargeGoods chargeGoods = new ChargeGoods();
        ShoppingGoods shoppingGoods = new ShoppingGoods();

        shoppingGoods.setSelected("Y");
        shoppingGoods.setProduct_sku("sku001");
        shoppingGoods.setGoods_type("衣服");
        shoppingGoods.setBrand_id("517");
        shoppingGoods.setBuy_number("7");
        shoppingGoods.setSales_price(2000d);
        shoppingGoods.setReal_price(2000d);
        shoppingGoods.setPromotion_flag("102");
        chargeGoods.setShoppingGoods(shoppingGoods);
        mainGoods.add(chargeGoods);

        chargeContext.setMainGoods(mainGoods);


        this.cartPromotionService.caculatePromotins(chargeContext);


        System.out.println(chargeContext.getMainGoods());

        double total = 0.0d;
        int buy  = 0  ;
        for(ChargeGoods goods : chargeContext.getMainGoods()){
            total = total + goods.getShoppingGoods().getReal_price() * Integer.parseInt(goods.getShoppingGoods().getBuy_number());
            buy = buy + Integer.parseInt(goods.getShoppingGoods().getBuy_number());
        }

        assertEquals(buy,  7);
        assertEquals(total,  5000, 0.01d);
        assertEquals(500,  chargeContext.getMainGoods().get(1).getShoppingGoods().getReal_price(), 0.01d);


        // cutdown = 2000 * 7  - （3000 + 2000）= 9000
        assertEquals(9000, chargeContext.getChargeTotal().getDiscountAmount(), 0.01d);



        assertEquals(1, chargeContext.getChargeTotal().getPromotionInfoList().size());

    }




}

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
 *  test cash reduce
 *
 * Created by chunhua.zhang@yoho.cn on 2015/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/spring*.xml"})
public class Test_Promotion_flag_match {



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
     *  amount < 1699
     *
     */
    @Test
    public void test_promotion_flag_match_but_amount_not_match() throws IOException {

        //prepare
        List<PromotionInfo> promotionInfos = new LinkedList<>();

        PromotionInfo info = new PromotionInfo();

        String conJSOn = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("promotion_flag.json"));
        PromotionCondition condition = new PromotionCondition(conJSOn);
        info.setCondition(condition);
        info.setId("6324");
        info.setTitle("满600减100");
        info.setFitChannel("1,2,3");
        info.setPromotionType("Cashreduce");
        info.setActionParam("{\"reduce\":\"100\"}");
        info.setRejectParam("6392");
        promotionInfos.add(info);

        when(repository.getActivePromotionsByFitChannel()).thenReturn(promotionInfos);


        ChargeContext chargeContext = new ChargeContext();


        //总计
        ChargeTotal chargeTotal = new ChargeTotal();
        chargeTotal.setDiscountAmount(100);
        chargeContext.setChargeTotal(chargeTotal);

        List<ChargeGoods> mainGoods = new LinkedList<>();

        ChargeGoods chargeGoods = new ChargeGoods();
        ShoppingGoods shoppingGoods = new ShoppingGoods();

        shoppingGoods.setSelected("Y");
        shoppingGoods.setProduct_sku("sku001");
        shoppingGoods.setGoods_type("衣服");
        shoppingGoods.setBrand_id("517");
        shoppingGoods.setBuy_number("1");
        shoppingGoods.setReal_price(213d);
        shoppingGoods.setPromotion_flag("102");
        chargeGoods.setShoppingGoods(shoppingGoods);
        mainGoods.add(chargeGoods);

        chargeContext.setMainGoods(mainGoods);


        int totalMatch =  this.cartPromotionService.caculatePromotins(chargeContext);


        assertEquals(totalMatch,0);

    }


    /**
     * 测试promotion flag + amount 都满足促销条件
     * @throws IOException
     */
    @Test
    public void test_promotion_flag_match() throws IOException {

        //prepare
        List<PromotionInfo> promotionInfos = new LinkedList<>();

        PromotionInfo info = new PromotionInfo();

        String conJSOn = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("promotion_flag.json"));
        PromotionCondition condition = new PromotionCondition(conJSOn);
        info.setCondition(condition);
        info.setId("6324");
        info.setTitle("满600减100");
        info.setFitChannel("1,2,3");
        info.setPromotionType("Cashreduce");
        info.setActionParam("{\"reduce\":\"100\"}");
        info.setRejectParam("6392");
        promotionInfos.add(info);

        when(repository.getActivePromotionsByFitChannel()).thenReturn(promotionInfos);


        ChargeContext chargeContext = new ChargeContext();


        //总计
        ChargeTotal chargeTotal = new ChargeTotal();
        chargeTotal.setDiscountAmount(100);
        chargeContext.setChargeTotal(chargeTotal);

        List<ChargeGoods> mainGoods = new LinkedList<>();

        ChargeGoods chargeGoods = new ChargeGoods();
        ShoppingGoods shoppingGoods = new ShoppingGoods();

        shoppingGoods.setSelected("Y");
        shoppingGoods.setProduct_sku("sku001");
        shoppingGoods.setGoods_type("衣服");
        shoppingGoods.setBrand_id("517");
        shoppingGoods.setBuy_number("1");
        shoppingGoods.setReal_price(2103d);
        shoppingGoods.setPromotion_flag("102");
        chargeGoods.setShoppingGoods(shoppingGoods);
        mainGoods.add(chargeGoods);

        chargeContext.setMainGoods(mainGoods);


        int totalMatch =  this.cartPromotionService.caculatePromotins(chargeContext);


        assertEquals(totalMatch,1);

    }


    /**
     * amount > 1699 but promotion_flag not fit
     * @throws IOException
     */
    @Test
    public void test_promotion_flag_Not_match_but_amount_match () throws IOException {

        //prepare
        List<PromotionInfo> promotionInfos = new LinkedList<>();

        PromotionInfo info = new PromotionInfo();

        String conJSOn = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("promotion_flag.json"));
        PromotionCondition condition = new PromotionCondition(conJSOn);
        info.setCondition(condition);
        info.setId("6324");
        info.setTitle("满600减100");
        info.setFitChannel("1,2,3");
        info.setPromotionType("Cashreduce");
        info.setActionParam("{\"reduce\":\"100\"}");
        info.setRejectParam("6392");
        promotionInfos.add(info);

        when(repository.getActivePromotionsByFitChannel()).thenReturn(promotionInfos);


        ChargeContext chargeContext = new ChargeContext();


        //总计
        ChargeTotal chargeTotal = new ChargeTotal();
        chargeTotal.setDiscountAmount(100);
        chargeContext.setChargeTotal(chargeTotal);

        List<ChargeGoods> mainGoods = new LinkedList<>();

        ChargeGoods chargeGoods = new ChargeGoods();
        ShoppingGoods shoppingGoods = new ShoppingGoods();

        shoppingGoods.setSelected("Y");
        shoppingGoods.setProduct_sku("sku001");
        shoppingGoods.setGoods_type("衣服");
        shoppingGoods.setBrand_id("517");
        shoppingGoods.setBuy_number("1");
        shoppingGoods.setReal_price(2103d);
        shoppingGoods.setPromotion_flag("12");
        chargeGoods.setShoppingGoods(shoppingGoods);
        mainGoods.add(chargeGoods);

        chargeContext.setMainGoods(mainGoods);


        int totalMatch =  this.cartPromotionService.caculatePromotins(chargeContext);


        assertEquals(totalMatch, 0);

    }






}

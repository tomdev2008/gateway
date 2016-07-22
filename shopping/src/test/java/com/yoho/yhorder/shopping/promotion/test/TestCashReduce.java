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

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;

/**
 *  test cash reduce
 *
 * Created by chunhua.zhang@yoho.cn on 2015/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/spring*.xml"})
public class TestCashReduce {



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
    public void testCharge_not_fit() throws IOException {

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
        shoppingGoods.setReal_price(102d);
        shoppingGoods.setPromotion_flag("102");
        chargeGoods.setShoppingGoods(shoppingGoods);
        mainGoods.add(chargeGoods);

        chargeContext.setMainGoods(mainGoods);


        this.cartPromotionService.caculatePromotins(chargeContext);


        assertEquals(chargeContext.getChargeTotal().getPromotionInfoList().size(),0);

    }



    /**
     * 满599， brand = 517 ，减少100
     *
     */
    @Test
    public void testCharge() throws IOException {

        List<PromotionInfo> promotionInfos = new LinkedList<>();

        PromotionInfo info = new PromotionInfo();

        String conJSOn = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("cashreduce.json"));
        PromotionCondition condition = new PromotionCondition(conJSOn);
        info.setCondition(condition);
        info.setId("6324");
        info.setTitle("TUK满599减100");
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
        shoppingGoods.setReal_price(900d);
        chargeGoods.setShoppingGoods(shoppingGoods);
        mainGoods.add(chargeGoods);

        chargeContext.setMainGoods(mainGoods);


        this.cartPromotionService.caculatePromotins(chargeContext);

        Assert.assertTrue(chargeContext.getChargeTotal().getDiscountAmount() == 200);
        Assert.assertTrue(shoppingGoods.getReal_price() == 800);
        Assert.assertTrue(chargeContext.getChargeTotal().getPromotionInfoList().size() == 1);
        Assert.assertTrue(chargeGoods.getFit_promotions().get(0).equals("6324"));
        Assert.assertTrue(chargeContext.getChargeTotal().getPromotionInfoList().get(0).getPromotion_id()==6324);
        System.out.println(chargeContext.getChargeTotal().getPromotionInfoList());

    }

}

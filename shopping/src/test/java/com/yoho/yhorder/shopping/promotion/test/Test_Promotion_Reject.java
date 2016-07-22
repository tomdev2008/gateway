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
 * 测试促销互斥的情况
 *
 * Created by chunhua.zhang@yoho.cn on 2015/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/spring*.xml"})
public class Test_Promotion_Reject {



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
    public void test_promotion_reject() throws IOException {

        //prepare
        List<PromotionInfo> promotionInfos = new LinkedList<>();

        PromotionInfo info = new PromotionInfo();

        String conJSOn = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("promotion_flag.json"));
        PromotionCondition condition = new PromotionCondition(conJSOn);
        info.setCondition(condition);
        info.setId("6324");
        info.setTitle("满1699减500");
        info.setFitChannel("1,2,3");
        info.setPromotionType("Cashreduce");
        info.setActionParam("{\"reduce\":\"500\"}");
        info.setRejectParam(" [\"6325\",\"6325\",\"6325\",\"6325\"]");


        PromotionInfo info_665 = new PromotionInfo();
        String conJSON_665 = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("promotion_flag_665.json"));
        PromotionCondition condition_665 = new PromotionCondition(conJSON_665);
        info_665.setCondition(condition_665);
        info_665.setId("6325");
        info_665.setTitle("满600减100");
        info_665.setFitChannel("1,2,3");
        info_665.setPromotionType("Cashreduce");
        info_665.setActionParam("{\"reduce\":\"100\"}");
        info_665.setRejectParam(" [\"6324\",\"6324\",\"6324\",\"6324\"]");


        promotionInfos.add(info);
        promotionInfos.add(info_665);

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
        shoppingGoods.setReal_price(2356d);
        shoppingGoods.setPromotion_flag("102");
        chargeGoods.setShoppingGoods(shoppingGoods);
        mainGoods.add(chargeGoods);

        chargeContext.setMainGoods(mainGoods);


        int totalMatch =  this.cartPromotionService.caculatePromotins(chargeContext);


        assertEquals(1, chargeContext.getChargeTotal().getPromotionInfoList().size());
        assertEquals(6324, chargeContext.getChargeTotal().getPromotionInfoList().get(0).getPromotion_id());

        assertEquals(1, totalMatch);

    }









}

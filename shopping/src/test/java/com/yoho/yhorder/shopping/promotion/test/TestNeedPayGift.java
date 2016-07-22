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
import static org.junit.Assert.*;
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

import static org.mockito.Mockito.when;

/**
 * test cash reduce
 * <p/>
 * Created by chunhua.zhang@yoho.cn on 2015/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/spring*.xml"})
public class TestNeedPayGift {


    @Autowired
    @InjectMocks
    private CartPromotionService cartPromotionService;


    @Mock
    private PromotionInfoRepository repository;


    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        List<PromotionInfo> promotionInfos = new LinkedList<>();

        PromotionInfo info = new PromotionInfo();

        String conJSOn = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("need_pay_gift.json"));
        PromotionCondition condition = new PromotionCondition(conJSOn);
        info.setCondition(condition);
        info.setId("6323");
        info.setTitle("Bduck加价购");
        info.setFitChannel("1,2,3");
        info.setPromotionType("Needpaygift");
        info.setActionParam("{\"add_cost\":\"60\",\"goods_list\":\"51064920\",\"num\":\"1\"}");
        info.setRejectParam("6392");
        promotionInfos.add(info);

        when(repository.getActivePromotionsByFitChannel()).thenReturn(promotionInfos);
    }


    /**
     * 满599， brand = 517 ，减少100
     */
    @Test
    public void testCharge() {


        ChargeContext chargeContext = new ChargeContext();


        //总计
        ChargeTotal chargeTotal = new ChargeTotal();
        chargeTotal.setDiscountAmount(100);
        chargeContext.setChargeTotal(chargeTotal);

        List<ChargeGoods> mainGoodsGiftList = new LinkedList<>();

        //add gift goods
        ChargeGoods mainGoodGift = new ChargeGoods();
        ShoppingGoods shoppingGoods = new ShoppingGoods();
        shoppingGoods.setSelected("Y");
        shoppingGoods.setProduct_sku("sku001");
        shoppingGoods.setGoods_type("衣服");
        shoppingGoods.setBrand_id("1236");
        shoppingGoods.setBuy_number("1");
        shoppingGoods.setReal_price(90d);
        shoppingGoods.setSales_price(90d);
        shoppingGoods.setPromotion_id("6233");
        mainGoodGift.setShoppingGoods(shoppingGoods);
        mainGoodsGiftList.add(mainGoodGift);
        chargeContext.setMainGoodsPriceGift(mainGoodsGiftList);

        // add mainGoods
        List<ChargeGoods> mainGoods = new LinkedList<>();
        ChargeGoods mainchargeGoods = new ChargeGoods();
        ShoppingGoods mainshoppingGoods = new ShoppingGoods();

        mainshoppingGoods.setSelected("Y");
        mainshoppingGoods.setProduct_sku("sku001");
        mainshoppingGoods.setGoods_type("衣服");
        mainshoppingGoods.setBrand_id("246");
        mainshoppingGoods.setBuy_number("1");
        mainshoppingGoods.setReal_price(900d);
        mainchargeGoods.setShoppingGoods(mainshoppingGoods);
        mainGoods.add(mainchargeGoods);

        chargeContext.setMainGoods(mainGoods);



        this.cartPromotionService.caculatePromotins(chargeContext);


        // 100 + (setSales_price-90 - add-cost-60) = 130
        assertEquals(chargeContext.getChargeTotal().getDiscountAmount(), 130d, 0.1d);
        assertEquals(shoppingGoods.getReal_price().doubleValue(), 90d, 0.1d);
        assertEquals(chargeContext.getChargeTotal().getPromotionInfoList().size() ,  1);
        assertEquals(mainchargeGoods.getFit_promotions().get(0), "6323");
         System.out.println(chargeContext.getChargeTotal().getPromotionInfoList());

    }

}

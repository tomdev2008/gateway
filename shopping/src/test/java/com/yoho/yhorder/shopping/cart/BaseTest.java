package com.yoho.yhorder.shopping.cart;

import com.alibaba.fastjson.JSON;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.caculator.PromotionCharge;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.promotion.service.CartPromotionService;
import com.yoho.yhorder.shopping.charge.promotion.service.PromotionInfoRepository;
import com.yoho.yhorder.shopping.utils.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by wujiexiang on 16/3/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public abstract class BaseTest {

    List<PromotionInfo> promotionInfos = new LinkedList<>();


    List<ShoppingCartItems> itemsList = new ArrayList<>();

    @Autowired
    @InjectMocks
    private ChargerService chargerService;

    @Autowired
    @InjectMocks
    PromotionCharge promotionCharge;


    @Autowired
    @InjectMocks
    CartPromotionService service;

    @Mock
    ChargeContextFactory chargeContextFactory;

    @Mock
    private PromotionInfoRepository repository;




    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        addShoppingCartItems();


        when(repository.getActivePromotionsByFitChannel()).thenReturn(promotionInfos);
    }

    public void addPromotionInfo(PromotionInfo info)
    {
        promotionInfos.add(info);
    }


    public void  addShoppingCartItems() throws Exception
    {

        String json = IOUtils.toString(  this.getClass().getClassLoader().getResourceAsStream("shopping_cart_items.json"));

        List<ShoppingCartItems> itemsList = JSON.parseArray(json, ShoppingCartItems.class);

        this.itemsList = itemsList;


    }


    /**
     * 普通商品算费参数对象
     *
     * @param request
     * @return
     */
    protected ChargeParam newOrdinaryChargeParam(final ShoppingCartRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setShoppingKey(request.getShopping_key());
        chargeParam.setUid(request.getUid());
        chargeParam.setCartType(Constants.ORDINARY_CART_TYPE);
        chargeParam.setChargeType(Constants.ORDINARY_CHARGE_TYPE);
        chargeParam.setSaleChannel(request.getSale_channel());
        chargeParam.setNeedCalcShippingCost(false);
        chargeParam.setNeedQueryYohoCoin(false);
        chargeParam.setNeedQueryRedEnvelopes(false);
        chargeParam.setUserAgent(request.getUser_agent());
        return chargeParam;
    }


    protected List<ChargeGoods> transToChargeGoods() {
        //2.1转化bean
        List<ChargeGoods> chargeGoodseList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(itemsList)) {
            for (ShoppingCartItems shoppingCartGoods : itemsList) {
                chargeGoodseList.add(new ChargeGoods(shoppingCartGoods));
            }
        }

        return chargeGoodseList;
    }
}

package com.yoho.yhorder.shopping.cart;

import com.alibaba.fastjson.JSON;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.response.shopping.ShoppingQueryResponse;
import com.yoho.service.model.vip.VipInfo;
import com.yoho.yhorder.dal.IShoppingCartDAO;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.model.UserInfo;
import com.yoho.yhorder.shopping.service.impl.ShoppingCartQueryService;
import junit.framework.Assert;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by wujiexiang on 16/3/15.
 */
public class ShoppingCartQueryServiceTest  extends  BaseTest{

    @Autowired
    @InjectMocks
    ShoppingCartQueryService shoppingCartQueryService;



    @Test
    public void test()throws Exception
    {
        ShoppingCartRequest request = createShoppingCartRequest();

        when(chargeContextFactory.build(false,null)).thenReturn(createChargeContext(request));

        ShoppingQueryResponse response = shoppingCartQueryService.query(request);


        Assert.assertEquals(1,response.getOrdinary_cart_data().getGoods_list().size());

        System.out.println(response);
    }


    private ChargeContext createChargeContext(ShoppingCartRequest request)
    {
        ChargeContext chargeContext = new ChargeContext();

        ChargeParam chargeParam = newOrdinaryChargeParam(request);

        //1.设置请求参数
        chargeContext.setChargeParam(chargeParam);

        List<ChargeGoods> chargeGoodseList = transToChargeGoods();

        chargeContext.setChargeGoodsList(chargeGoodseList);

        //3.设置用户信息
        chargeContext.setUserInfo(this.getUserInfo(chargeParam));

        return chargeContext;

    }

    public ShoppingCartRequest  createShoppingCartRequest()
    {
        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setUid(8040155);
        return request;
    }


    private UserInfo getUserInfo(ChargeParam chargeParam) {
        int uid = chargeParam.getUid();
        UserInfo userInfo = new UserInfo();
        //访客
        if (uid <= 0) {
            return userInfo;
        }
        userInfo.setUserLevel(1);
        //当月订单数量
        userInfo.setMonthOrderCount(0);

        return userInfo;

    }


}

package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.promotion.service.CartPromotionService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 促销计算
 *
 * @author CaoQi
 * @Time 2015/11/24
 */
@Service
public class PromotionCharge {


    @Autowired
    private CartPromotionService service;


    /**
     * do charge
     *
     * @param chargeContext
     */
    public void charge(ChargeContext chargeContext) {

//        if (chargeContext.getChargeParam().isPreSaleCart()) {
//            //预售购物车不能进行促销
//            return;
//        }
        this.service.caculatePromotins(chargeContext);
    }
}

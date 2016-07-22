package com.yoho.yhorder.shopping.charge.promotion.impl;

import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.promotion.AbstractPromotion;
import org.springframework.stereotype.Service;

/**
 * Vipfixeddiscount 类型的算费
 *
 * @author CaoQi
 * @Time 2015/12/1
 */
@Service
public class Vipfixeddiscount extends AbstractPromotion {

    public void doCompute(ChargeContext chargeContext, PromotionInfo promotionInfo) {

    }
}

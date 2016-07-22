package com.yoho.yhorder.shopping.service.impl;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.ShoppingComputeRequest;
import com.yoho.service.model.order.response.shopping.ShoppingPromotionCodeResponse;
import com.yoho.service.model.promotion.PromotionCodeBo;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.ChargeContextFactory;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.PromotionCodeChargeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by JXWU on 2016/1/15.
 */
@Service
public class ShoppingPromotionCodeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ChargeContextFactory changeContextFactory;

    @Autowired
    private ChargerService chargerService;

    public ShoppingPromotionCodeResponse usePromotionCode(ShoppingComputeRequest request) {
        //1.算费
        //1.1新建算费参数
        ChargeParam chargeParam = newChargeParam(request);
        //1.2新建普通商品算费context，context中包括需要算费的sku、用户信息等
        ChargeContext chargeContext = changeContextFactory.build(false, chargeParam);
        //1.3算费，算费结果在context对象的ChargeTotal中。
        chargerService.charge(chargeContext);

        PromotionCodeChargeResult promotionCodeChargeResult = chargeContext.getChargeTotal().getPromotionCodeChargeResult();

        PromotionCodeBo promotionCodeBo = promotionCodeChargeResult.getPromotionCodeBo();

        if (!promotionCodeChargeResult.isValid() || promotionCodeBo == null) {
            //不满足优惠条件
            logger.info("[{}] promotionCode {} not meeting condition", chargeParam.getUid(), promotionCodeChargeResult);
            ServiceException exception = new ServiceException(ServiceError.SHOPPING_PROMOTIONCODE_NOTMEETING_CONDITION);
            exception.setParams(new String[]{String.valueOf(promotionCodeChargeResult.getAmountAtLeast()), String.valueOf(promotionCodeChargeResult.getCountAtLeast())});
            throw exception;
        }

        return convertBoToVo(promotionCodeBo);
    }

    private ShoppingPromotionCodeResponse convertBoToVo(PromotionCodeBo codeBo) {
        ShoppingPromotionCodeResponse codeVo = new ShoppingPromotionCodeResponse();
        codeVo.setId(codeBo.getId());
        codeVo.setCode(codeBo.getCode());
        codeVo.setName(codeBo.getName());
        codeVo.setLimitTimes(codeBo.getLimitTimes());
        codeVo.setDiscountType(codeBo.getDiscountType());
        codeVo.setAmountAtLeast(codeBo.getAmountAtLeast());
        codeVo.setCountAtLeast(codeBo.getCountAtLeast());
        codeVo.setDiscount(codeBo.getDiscount());
        codeVo.setDiscountAtMost(codeBo.getDiscountAtMost());
        codeVo.setStatus(codeBo.getStatus());

        return codeVo;
    }


    /**
     * 新建一个购物车payment订单
     *
     * @return
     */
    private ChargeParam newChargeParam(final ShoppingComputeRequest request) {
        ChargeParam chargeParam = new ChargeParam();
        chargeParam.setPromotionCode(request.getPromotion_code());
        chargeParam.setUid(request.getUid());
        chargeParam.setCartType(request.getCart_type());
        chargeParam.setNeedCalcShippingCost(false);//不需要计算运费
        chargeParam.setNeedQueryYohoCoin(false);//不需要查询yoho币
        chargeParam.setNeedQueryRedEnvelopes(false);//不需要查询红包
        chargeParam.setNeedAuditCodPay(false);//不需要计算货到付款
        chargeParam.setUserAgent(request.getUser_agent());
        return chargeParam;
    }
}

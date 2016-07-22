package com.yoho.yhorder.shopping.compensatable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import com.yoho.service.model.promotion.request.PromotionCodeReq;
import com.yoho.yhorder.shopping.charge.model.PromotionCodeChargeResult;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wujiexiang on 16/5/18.
 * 优惠码
 */
@Component
public class PromotionCodeService {

    private final Logger orderSubmitLog = LoggerFactory.getLogger("orderSubmitLog");

    private final Logger orderCompensatelogger = LoggerFactory.getLogger("orderCompensateLog");

    @Autowired
    protected ServiceCaller serviceCaller;

    /**
     * 增加优惠码的使用记录，yhb_promotion.user_promotion_code_history
     *
     * @param uid
     * @param orderCode
     * @param promotionCodeChargeResult
     */
    @TxCompensatable(value = PromotionCodeService.class)
    public void useOrderPromotionCode(@TxCompensateArgs("uid") int uid, @TxCompensateArgs("orderCode") long orderCode,
                                      @TxCompensateArgs("promotionCodeChargeResult") PromotionCodeChargeResult promotionCodeChargeResult) {
        orderSubmitLog.info("order {} request to use promotion code,uid is {},promotionCodeChargeResult is {}", orderCode, uid, promotionCodeChargeResult);
        if (canUsePromotionCode(promotionCodeChargeResult)) {
            PromotionCodeReq req = new PromotionCodeReq();
            req.setUid(uid);
            req.setOrderCode(String.valueOf(orderCode));
            req.setPromotionCode(promotionCodeChargeResult.getPromotionCode());
            boolean result = serviceCaller.call(ShoppingConfig.PROMOTION_ADD_PROMOTIONCODEHISTORY_REST_URL, req, Boolean.class);
            orderSubmitLog.info("add promotion code history,result is {},uid {},order code {},promotion code charge result{}",
                    result,
                    uid,
                    orderCode,
                    promotionCodeChargeResult);
        } else {
            orderSubmitLog.info("order can not use invalid promotion code,uid is {},order code is {}", uid, orderCode);
        }
    }


    public void compensate(String message) {
        orderCompensatelogger.info("PromotionCodeService begin to compensate: {}", message);
        int uid = 0;
        long orderCode = 0;
        PromotionCodeChargeResult promotionCodeChargeResult = null;
        try {
            JSONObject json = JSON.parseObject(message);
            uid = json.getIntValue("uid");
            orderCode = json.getLongValue("orderCode");
            promotionCodeChargeResult = json.getObject("promotionCodeChargeResult", PromotionCodeChargeResult.class);
        } catch (Exception ex) {
            orderCompensatelogger.warn("parse message to json error,message is {}", message, ex);
        }

        compensatePromotionCode(uid, orderCode, promotionCodeChargeResult);

        orderCompensatelogger.info("PromotionCodeService compensate end,uid is {},orderCode is {}", uid, orderCode);


    }

    private void compensatePromotionCode(int uid, long orderCode, PromotionCodeChargeResult promotionCodeChargeResult) {
        orderCompensatelogger.info("compensate promotion code,uid is {},orderCode is {},promotionCodeChargeResult is {}", uid, orderCode, promotionCodeChargeResult);
        if (uid > 0 && orderCode > 0 && canUsePromotionCode(promotionCodeChargeResult)) {
            PromotionCodeReq req = new PromotionCodeReq();
            req.setUid(uid);
            req.setOrderCode(String.valueOf(orderCode));
            Boolean result = serviceCaller.call("promotion.updatePromotionCodeHistory", req, Boolean.class);
            orderCompensatelogger.info("refundPromotionCode success by order code {}, uid {} ,result is {}.", orderCode, uid, result);
        }
    }

    public boolean canUsePromotionCode(PromotionCodeChargeResult promotionCodeChargeResult) {
        return promotionCodeChargeResult != null && promotionCodeChargeResult.isValid() && promotionCodeChargeResult.getDiscountAmount() > 0;
    }
}

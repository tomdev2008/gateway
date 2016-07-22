package com.yoho.yhorder.shopping.compensatable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import com.yoho.service.model.response.RedenvelopesResponseBO;
import com.yoho.yhorder.shopping.service.ExternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Created by wujiexiang on 16/5/17.
 * 红包
 */
@Component
public class RedEnvelopeService {

    private final Logger orderSubmitLog = LoggerFactory.getLogger("orderSubmitLog");

    private final Logger orderCompensateLog = LoggerFactory.getLogger("orderCompensateLog");

    @Autowired
    private ExternalService externalService;

    @TxCompensatable(value = RedEnvelopeService.class)
    public void useRedEnvelopes(@TxCompensateArgs("uid") int uid, @TxCompensateArgs("orderCode") long orderCode,
                                @TxCompensateArgs("useRedEnvelopes") double useRedEnvelopes) {

        orderSubmitLog.info("order {} request to use RedEnvelopes,uid is {},RedEnvelopes is {}", orderCode, uid, useRedEnvelopes);
        if (useRedEnvelopes > 0) {
            useRedEnvelopes = YHMath.mul(useRedEnvelopes, -1);
            BigDecimal afterValue = externalService.useRedenvelopes(uid, orderCode, useRedEnvelopes, 2, "" + orderCode, "用户下单", 2);
            orderSubmitLog.info("after order {} use RedEnvelopes,uid {} has RedEnvelopes {}", orderCode, uid, afterValue);
        }
    }


    /**
     * 补偿红包
     *
     * @param message
     */
    public void compensate(String message) {
        orderCompensateLog.info("RedEnvelopeService begin to compensate : {}", message);
        int uid = 0;
        long orderCode = 0;
        double useRedEnvelopes = 0;
        try {
            JSONObject json = JSON.parseObject(message);
            uid = json.getIntValue("uid");
            orderCode = json.getLongValue("orderCode");
            useRedEnvelopes = json.getIntValue("useRedEnvelopes");
        } catch (Exception ex) {
            orderCompensateLog.warn("parse message to json error,message is {}", message, ex);
        }
        compensateRedEnvelope(uid, orderCode, useRedEnvelopes);
        orderCompensateLog.info("RedEnvelopeService compensate end,uid is {},orderCode is {}", uid, orderCode);
    }


    private void compensateRedEnvelope(int uid, long orderCode, double useRedEnvelopes) {
        orderCompensateLog.info("compensate RedEnvelopes,uid is {},orderCode is {},useRedEnvelopes is {}", uid, orderCode, useRedEnvelopes);
        if (uid > 0 && orderCode > 0 & useRedEnvelopes > 0) {
            RedenvelopesResponseBO[] array = externalService.queryOrderRedenvelopes(uid, orderCode);
            double realRefundRedEnvelopes = 0;
            if (array != null) {
                for (RedenvelopesResponseBO responseBO : array) {
                    realRefundRedEnvelopes = YHMath.add(responseBO.getAmount(), realRefundRedEnvelopes);
                }
            }
            //下单使用红包,记录为负数
            if (Math.abs(realRefundRedEnvelopes) == useRedEnvelopes) {
                externalService.refundRedEnvelopes(uid, orderCode, useRedEnvelopes);
            } else {
                orderCompensateLog.warn("can not compensate red envelope,real refund RedEnvelopes is {},order use RedEnvelopes is {},uid is {},order code is {}",
                        realRefundRedEnvelopes,
                        useRedEnvelopes,
                        uid,
                        orderCode);
            }

        }
    }

}

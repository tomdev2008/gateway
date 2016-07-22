package com.yoho.yhorder.shopping.compensatable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import com.yoho.service.model.response.YohoCurrencyRspBO;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wujiexiang on 16/5/18.
 * yoho币
 */
@Component
public class YohoCoinService {

    private final Logger orderSubmitLog = LoggerFactory.getLogger("orderSubmitLog");

    private final Logger orderCompensateLog = LoggerFactory.getLogger("orderCompensateLog");

    @Autowired
    private ExternalService externalService;

    @TxCompensatable(value = YohoCoinService.class)
    public void useYohoCoin(@TxCompensateArgs("uid") int uid, @TxCompensateArgs("orderCode") long orderCode, @TxCompensateArgs("useYohoCoinNum") int useYohoCoinNum) {
        //double useYohoCoin =
        orderSubmitLog.info("order {} request to use yoho coin,uid is {},yoho coin num is {},", orderCode, uid, useYohoCoinNum);
        if (useYohoCoinNum > 0) {
            JSONObject params = new JSONObject();
            params.put("order_code", orderCode);
            YohoCurrencyRspBO rspBO = externalService.useYohoCoin(uid, orderCode, useYohoCoinNum * -1, Constants.ORDER_SUBMIT_USE_YOHO_COIN_TYPE, params.toJSONString());
            orderSubmitLog.info("after order {} use yoho coin,uid {} has yohoCoin {},", orderCode, uid, rspBO);
        }
    }


    /**
     * yoho币补偿方式,由core调用
     *
     * @param message
     */
    public void compensate(String message) {
        orderCompensateLog.info("YohoCoinService begin to compensate,message is {}", message);
        int uid = 0;
        long orderCode = 0;
        int useYohoCoinNum = 0;
        try {
            JSONObject json = JSON.parseObject(message);
            uid = json.getIntValue("uid");
            orderCode = json.getLongValue("orderCode");
            useYohoCoinNum = json.getIntValue("useYohoCoinNum");
        } catch (Exception ex) {
            orderCompensateLog.warn("parse message to json error,message is {}", message, ex);
        }
        compensateYohoCoin(uid, orderCode, useYohoCoinNum);

        orderCompensateLog.info("YohoCoinService compensate end,uid is {},orderCode is {}", uid, orderCode);

    }


    private void compensateYohoCoin(int uid, long orderCode, int useYohoCoinNum) {
        orderCompensateLog.info("compensate yoho coin,uid is {},orderCode is {},useYohoCoinNum is {}", uid, orderCode, useYohoCoinNum);
        if (uid > 0 && orderCode > 0 & useYohoCoinNum > 0) {
            YohoCurrencyRspBO reponse = externalService.refundYohoCoin(uid, orderCode, useYohoCoinNum);
            if (reponse != null) {
                orderCompensateLog.info("compensate yoho coin success,compensate response is {}", reponse);
            } else {
                orderCompensateLog.info("compensate yoho coin fail!!");
            }
        }
    }
}

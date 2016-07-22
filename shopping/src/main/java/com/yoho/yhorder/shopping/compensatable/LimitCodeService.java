package com.yoho.yhorder.shopping.compensatable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import com.yoho.service.model.promotion.request.LimitCodeReq;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.utils.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wujiexiang on 16/5/18.
 * 限购码
 */
@Component
public class LimitCodeService {

    private final Logger orderSubmitLog = LoggerFactory.getLogger("orderSubmitLog");

    private final Logger orderCompensateLog = LoggerFactory.getLogger("orderCompensateLog");

    @Autowired
    private ExternalService externalService;

    @Autowired
    protected ServiceCaller serviceCaller;

    /**
     * 添加限购码使用记录
     *
     * @param uid
     * @param orderCode
     * @param chargeType
     * @param items
     */
    @TxCompensatable(value = LimitCodeService.class)
    public void addLimitCodeUseRecord(@TxCompensateArgs("uid") int uid, @TxCompensateArgs("orderCode") long orderCode,
                                      @TxCompensateArgs("chargeType") String chargeType, @TxCompensateArgs("items") List<ShoppingItem> items) {
        orderSubmitLog.info("order {} request to use limit code,uid is {},chargeType is {},items are {}", orderCode, uid, chargeType, items);
        if (Constants.isLimitCodeChargeType(chargeType)) {
            ShoppingItem item = items.get(0);
            //使用不成功,抛出异常
            externalService.addLimitCodeUseRecord(uid, item.getLimitCode(), item.getSkn(), orderCode);
            orderSubmitLog.info("order use limit code success,order code is {}", orderCode);
        } else {
            orderSubmitLog.info("order chargeType is not limitcode,uid is {},order code is {}", uid, orderCode);
        }
    }

    public void compensate(String message) {
        orderCompensateLog.info("LimitCodeService begin to compensate : {}", message);

        int uid = 0;
        long orderCode = 0;
        List<ShoppingItem> items = null;
        try {
            JSONObject json = JSON.parseObject(message);
            uid = json.getIntValue("uid");
            orderCode = json.getLongValue("orderCode");
            items = JSON.parseArray(json.getString("items"), ShoppingItem.class);
        } catch (Exception ex) {
            orderCompensateLog.warn("parse message to json error,message is {}", message, ex);
        }

        compensateLimitCode(uid, orderCode, items);

        orderCompensateLog.info("LimitCodeService compensate end,uid is {},orderCode is {}", uid, orderCode);
    }

    private void compensateLimitCode(int uid, long orderCode, List<ShoppingItem> items) {
        orderCompensateLog.info("compensate limit code,uid is {},orderCode is {},items is {}", uid, orderCode, items);
        if (CollectionUtils.isNotEmpty(items)) {
            ShoppingItem item = items.get(0);
            //回退
            LimitCodeReq req = new LimitCodeReq();
            req.setUid(uid);
            req.setOrderCode(String.valueOf(orderCode));
            req.setLimitCode(item.getLimitCode());
            req.setProductSkn(String.valueOf(item.getSkn()));
            req.setLimitProductCode(item.getLimitProductCode());
            orderCompensateLog.info("call promotion.cancelLimitCodeUseRecord to return limit code,order code is {},request is {}", orderCode, req);
            Boolean success = serviceCaller.call("promotion.cancelLimitCodeUseRecord", req, Boolean.class);
            if (success) {
                orderCompensateLog.info("return limit code success,order code is {}", orderCode);
            } else {
                orderCompensateLog.warn("CloseOrderByCode fail, because of can not returnLimitCodeIfHavaLimitCode by order code {}, uid {}", orderCode, uid);
            }
        }
    }
}

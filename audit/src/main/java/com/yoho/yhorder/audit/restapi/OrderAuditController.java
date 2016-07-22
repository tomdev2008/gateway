package com.yoho.yhorder.audit.restapi;

import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.request.AuditRequest;
import com.yoho.service.model.order.response.audit.AuditCodPayResponse;
import com.yoho.yhorder.audit.service.IOrderAuditService;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wujiexiang on 16/3/28.
 */
@Controller
@RequestMapping(value = "/audit")
public class OrderAuditController {

    private Logger logger = LoggerFactory.getLogger("orderAuditLog");

    @Autowired
    IOrderAuditService auditService;

    @RequestMapping("/auditCodPay")
    @ResponseBody
    @ServiceDesc(serviceName = "auditCodPay")
    public AuditCodPayResponse auditCodPay(@RequestBody AuditRequest auditRequest) {
        logger.info("enter auditCodPay in controller,request is {}", auditRequest);
        Pair<String, String> pair = auditService.auditCodPay(auditRequest);
        AuditCodPayResponse response = buildAduitResponse(pair);
        logger.info("exit auditCodPay in controller,uid is {},response is {}\n", auditRequest.getUid(),response);
        return response;
    }

    /**
     * 解除下单黑名单
     * @param uid
     * @return
     */
    @RequestMapping("/removeBacklist")
    @ResponseBody
    @ServiceDesc(serviceName = "removeBacklist")
    public int removeBacklist(@RequestBody int uid) {
        logger.info("enter removeBacklist in controller,uid is {}", uid);
        int successNum = auditService.removeBackList(uid);
        logger.info("enter removeBacklist in controller,response is {}\n", successNum);
        return successNum;
    }

    private AuditCodPayResponse buildAduitResponse(Pair<String, String> pair) {
        AuditCodPayResponse response = new AuditCodPayResponse();
        response.setIsSupport(pair.getKey());
        response.setIsSupportMessage(pair.getValue());
        return response;
    }
}

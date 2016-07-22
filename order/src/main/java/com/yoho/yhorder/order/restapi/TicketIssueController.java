package com.yoho.yhorder.order.restapi;

import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.request.IssueTicketRequest;
import com.yoho.yhorder.order.service.ITicketShoppingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wujiexiang on 16/7/6.
 */
@Controller
@RequestMapping("/orderInfo")
@ServiceDesc(serviceName = "order")
public class TicketIssueController {

    private final Logger logger = LoggerFactory.getLogger("ticketLog");

    @Autowired
    private ITicketShoppingService ticketShoppingService;

    @RequestMapping("/issueTicket")
    @ResponseBody
    @ServiceDesc(serviceName = "issueTicket")
    @Database(ForceMaster = true)
    public void issueTicket(@RequestBody IssueTicketRequest request) {

        logger.info("receive ticket issue in controller,request is {}", request);

        ticketShoppingService.issueTicket(request.getUid(), request.getOrderCode());

        logger.info("exit ticket issue in controller");
    }
}

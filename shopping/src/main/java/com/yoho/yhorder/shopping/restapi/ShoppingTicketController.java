package com.yoho.yhorder.shopping.restapi;

import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.request.ShoppingTicketRequest;
import com.yoho.service.model.order.response.shopping.ShoppingSubmitResponse;
import com.yoho.service.model.order.response.shopping.ShoppingTicketQueryResult;
import com.yoho.yhorder.shopping.service.IShoppingTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wangshijie on 16/5/16.
 */
@Controller
@RequestMapping(value = "/shopping")
public class ShoppingTicketController {

	private final Logger logger = LoggerFactory.getLogger("ticketLog");

	@Autowired
	private IShoppingTicketService shoppingTicketService;

	/**
	 * 电子票添加计算
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/addTicketAndQuery")
	@ResponseBody
	@ServiceDesc(serviceName = "addAndQueryTicket")
	@Database(ForceMaster = true)
	public ShoppingTicketQueryResult ticket(@RequestBody ShoppingTicketRequest request) {

		logger.info("receive addTicketAndQuery in controller, request is: {}", request);

		try {
			return shoppingTicketService.addAndQuery(request);
		} catch (Exception ex) {
			logger.error("process addTicketAndQuery failed, request is: {}", request, ex);
			throw ex;
		}
	}

	/**
	 * 电子票提交下单
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping("/submitTicket")
	@ResponseBody
	@ServiceDesc(serviceName = "submitTicket")
	@Database(ForceMaster = true)
	public ShoppingSubmitResponse submitTicket(@RequestBody ShoppingTicketRequest request) {

		logger.info("receive submitTicket in controller, request is: {}", request);

		try {
			return shoppingTicketService.submitTicket(request);
		} catch (Exception ex) {
			logger.error("process submitTicket failed, request is: {}", request, ex);
			throw ex;
		}
	}
}

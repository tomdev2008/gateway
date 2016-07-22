package com.yoho.yhorder.shopping.service;

import com.yoho.service.model.order.request.ShoppingTicketRequest;
import com.yoho.service.model.order.response.shopping.ShoppingSubmitResponse;
import com.yoho.service.model.order.response.shopping.ShoppingTicketQueryResult;

/**
 * 
 * @author wangshijie 2016/5/16
 *
 */
public interface IShoppingTicketService {
	/**
	 * 电子票添加查询接口
	 * 
	 * @param request
	 * @return
	 */
	ShoppingTicketQueryResult addAndQuery(ShoppingTicketRequest request);

	/**
	 * 提交电子票订单接口
	 * 
	 * @param request
	 * @return
	 */
	ShoppingSubmitResponse submitTicket(ShoppingTicketRequest request);
}

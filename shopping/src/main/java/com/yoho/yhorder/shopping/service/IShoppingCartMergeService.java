package com.yoho.yhorder.shopping.service;

import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.ShoppingCartLocalMergeRequestBO;
import com.yoho.service.model.order.response.shopping.ShoppingCartMergeRequestBO;
import com.yoho.service.model.order.response.shopping.ShoppingCartMergeResponseBO;

/**
 * 合并购物车接口
 * @author ping.huang
 *
 */
public interface IShoppingCartMergeService {

	/**
	 * 合并购物车
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	public ShoppingCartMergeResponseBO mergeCart(ShoppingCartMergeRequestBO request) throws ServiceException;

	/**
	 * 合并购物车 by local
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	public ShoppingCartMergeResponseBO  mergeCartByLocal(ShoppingCartLocalMergeRequestBO request) throws ServiceException;
}

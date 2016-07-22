package com.yoho.yhorder.shopping.service;

import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.shopping.*;

/**
 * Created by JXWU on 2015/12/12.
 */
public interface IShoppingCartService {
    /**
     * 添加购物车
     *
     * @param request
     * @return
     */
    ShoppingAddResponse add(ShoppingCartRequest request);


    /**
     * 查询购物车
     *
     * @param request
     * @return
     */
    ShoppingQueryResponse query(ShoppingCartRequest request);


    /**
     * 增加数量
     *
     * @param request
     * @return
     */
    ShoppingAddResponse increase(ShoppingCartRequest request);

    /**
     * 减少数量
     *
     * @param request
     * @return
     */
    ShoppingAddResponse decrease(ShoppingCartRequest request);

    /**
     * 删除购物车商品
     *
     * @param request
     * @return
     */
    ShoppingAddResponse remove(ShoppingCartRequest request);

    /**
     * 更换商品
     *
     * @param request
     * @return
     */
    ShoppingAddResponse swap(ShoppingCartRequest request);

    /**
     * 获取购物车总数
     *
     * @param request
     * @return
     */
    ShoppingCountResponse count(ShoppingCartRequest request);


    /**
     * 选中购物商品
     *
     * @param request
     * @return
     */
    void selected(ShoppingCartRequest request);

    /**
     * //移除商品到收藏夹
     *
     * @param request
     */
    ShoppingAddResponse addfavorite(ShoppingCartRequest request);

    /**
     * 获取购物车支付信息接口
     *
     * @param request
     * @return
     */
    ShoppingPaymentResponse payment(ShoppingCartRequest request);


    /**
     * 算费
     *
     * @param request
     * @return
     */
    ShoppingComputeResponse compute(ShoppingComputeRequest request);

    /**
     * 下单
     * @param request
     * @return
     */
    ShoppingSubmitResponse submit(ShoppingSubmitRequest request);

    /**
     * 使用优惠券
     * @param request
     */
    ShoppingUseCouponResponse useCoupon(ShoppingComputeRequest request);


    /**
     * 使用优惠码
     * @param request
     * @return
     */
    ShoppingPromotionCodeResponse usePromotionCode(ShoppingComputeRequest request);


    /**
     * 将已取消订单中的sku添加到购物车
     *
     * @param request
     * @return
     */
    ShoppingAddResponse readd(ShoppingReAddRequest request);

    /**
     * 先执行老selectd接口，然后执行query接口
     * @param request
     * @return
     */
    ShoppingQueryResponse selectedAndCart(ShoppingCartRequest request);


    /**
     * 先执行删除操作，然后执行query接口
     * @param request
     * @return
     */
    ShoppingQueryResponse removeAndCart(ShoppingCartRequest request);

    /**
     * 先执行移入收藏夹操作，然后执行query接口
     * @param request
     * @return
     */
    ShoppingQueryResponse addfavoriteAndCart(ShoppingCartRequest request);

}

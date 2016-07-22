package com.yoho.yhorder.shopping.service;

import com.yoho.service.model.order.request.ShoppingCouponRequest;
import com.yoho.service.model.order.response.CountBO;
import com.yoho.service.model.order.response.shopping.ShoppingCouponListResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by wujiexiang on 16/4/6.
 */
public interface IShoppingCouponService {

    /**
     * 统计可用的优惠券的张数
     *
     * @param request
     * @return
     */
    CountBO countUsableCoupon(@RequestBody ShoppingCouponRequest request);

    /**
     * 返回优惠券,分别可用和不可用两种
     * @param request
     * @return
     */
    ShoppingCouponListResponse listCoupon(@RequestBody ShoppingCouponRequest request);
}



package com.yoho.yhorder.shopping.restapi;

import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.request.ShoppingCouponRequest;
import com.yoho.service.model.order.response.CountBO;
import com.yoho.service.model.order.response.shopping.ShoppingCouponListResponse;
import com.yoho.yhorder.common.cache.redis.ShoppingCartRedis;
import com.yoho.yhorder.shopping.service.impl.ShoppingCouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wujiexiang on 16/4/6.
 * 购物车中优惠券的使用
 */
@Controller
@RequestMapping(value = "/shopping")
public class ShoppingCouponController {

    private final Logger addPaymentComputeLogger = LoggerFactory.getLogger("addPaymentComputeLog");

    @Autowired
    private ShoppingCouponService couponService;

    @Autowired
    private ShoppingCartRedis shoppingCartRedis;

    /**
     * 统计可用的优惠券的张数
     *
     * @param request
     * @return
     */
    @RequestMapping("/countUsableCoupon")
    @ResponseBody
    @ServiceDesc(serviceName = "countUsableCoupon")
    @Database(ForceMaster = true)
    public CountBO countUsableCoupon(@RequestBody ShoppingCouponRequest request) {

        addPaymentComputeLogger.info("\n\nreceive shopping cart countUsableCoupon in controller, request is: {}", request);

        //清除购物车缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), null);

        try {
            return couponService.countUsableCoupon(request);
        } catch (Exception ex) {
            addPaymentComputeLogger.error("process shopping cart countUsableCoupon failed, request is: {}", request, ex);
            throw ex;
        }
    }

    /**
     * 返回优惠券,分别可用和不可用两种
     *
     * @param request
     * @return
     */
    @RequestMapping("/listCoupon")
    @ResponseBody
    @ServiceDesc(serviceName = "listCoupon")
    @Database(ForceMaster = true)
    public ShoppingCouponListResponse listCoupon(@RequestBody ShoppingCouponRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart listCoupon in controller, request is: {}", request);

        //清除购物车缓存
        shoppingCartRedis.killShoppingQueryResponse(request.getUid(), null);

        try {
            return couponService.listCoupon(request);
        } catch (Exception ex) {
            addPaymentComputeLogger.error("process shopping cart listCoupon failed, request is: {}", request, ex);
            throw ex;
        }

    }
}

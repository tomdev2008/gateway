package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrdersCoupons;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrdersCouponsMapper {
    int insert(OrdersCoupons record);

    /**
     * 根据订单ID获得该订单使用的优惠券
     */
    OrdersCoupons selectByOrderId(Integer orderId);


    List<OrdersCoupons> selectByUidAndCouponsCodes(@Param("uid") Integer uid, @Param("couponsCodes") List<String> couponsCodes);
}
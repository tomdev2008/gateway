package com.yoho.yhorder.order.service;


import com.yoho.service.model.order.request.OrderPromotionInfoReq;
import com.yoho.service.model.order.response.OrderPromotionInfoBo;

import java.util.ArrayList;

public interface IOrderPromotionService {

    /**
     * 查询订单中优惠券信息
     * @param orderPromotionInfoReq
     * @return
     */

    ArrayList<OrderPromotionInfoBo> selectOrdPromotionListByUserInfo(OrderPromotionInfoReq orderPromotionInfoReq);

    /**
     * 根据订单号查询优惠券信息
     * @param orderCode
     * @return
     */
    OrderPromotionInfoBo selectByOrderCode(Long orderCode);

}

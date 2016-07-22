package com.yoho.yhorder.dal;

import com.yoho.service.model.order.request.OrderPromotionInfoReq;
import com.yoho.yhorder.dal.model.OrderPromotionInfo;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;


/*
* create by lijian
* 2015-11-24 09:49:56
* 订单的优惠信息
* */

public interface OrderPromotionInfoMapper {

    int insert(OrderPromotionInfo record);

    ArrayList<OrderPromotionInfo> selectOrdPromotionListByUserInfo(OrderPromotionInfoReq orderPromotionInfoReq);

    OrderPromotionInfo selectByOrderCode(Long orderCode);

    /**
     * 批量插入
     *
     * @param list
     * @return
     */
    int insertByBatch(@Param("list") List<OrderPromotionInfo> list);
}
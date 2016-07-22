package com.yoho.yhorder.order.service;

import com.alibaba.fastjson.JSONArray;
import com.yoho.service.model.order.request.DeliveryAddressRequest;

/**
 * qianjun 2016/6/14
 */
public interface IDeliveryAddressService {

    /**
     * 客服修改收货人地址，批量更新订单表中订单
     */
    void updateBatchDeliveryAddress(JSONArray jsonArray);

    /**
     * 修改订单收货地址
     */
    void updateDeliveryAddress(DeliveryAddressRequest deliveryAddressRequest);
}

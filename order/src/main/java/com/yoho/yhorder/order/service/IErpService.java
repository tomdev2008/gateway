package com.yoho.yhorder.order.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.yhorder.order.model.ERPOrder;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * Created by LUOXC on 2015/11/27.
 */
public interface IErpService {

    /**
     * ERP 同步方式
     */
    String ERP_SERVICE_TYPE_MQ = "MQ";
    String ERP_SERVICE_TYPE_RPC = "RPC";

    /**
     * 在ERP系统上创建换货订单
     * 这个是以前的同步调用的，不用了
     *
     * @param request
     * @return
     */
    JSONObject createChangeOrder(JSONObject request);


    /**
     * 取消订单
     *
     * @param request
     * @return
     */
    void cancelOrder(JSONObject request);

    void confirmOrder(JSONObject request, Map<String, Object> map);

    /**
     * 在erp系统提交退货申请
     *
     * @param request
     * @return
     */
    JSONObject refundGoods(JSONObject request);

    /**
     * 在erp系统提交退货物流数据
     */
    void setRefundExpressData(JSONObject request);

    /**
     * 在erp系统提交换货物流数据
     */
    void updateChangeOrderExpressInfo(Map<String, Object> express);

    /**
     * 修改订单收货地址
     */
    void updateDeliveryAddress(JSONArray request);
}

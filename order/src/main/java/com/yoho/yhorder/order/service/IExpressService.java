package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.model.ExpressCompanyBO;
import com.yoho.service.model.order.model.WaybillInfoBO;

import java.util.List;
import java.util.Map;

/**
 * qianjun
 * 2015/11/27
 */
public interface IExpressService {

    /**
     * 获取物流公司列表
     */
    List<ExpressCompanyBO> getExpressCompanyList(Byte status);

    /**
     * 新退换货物流信息
     */
    Map<String, Object> getNewExpress(Integer id, Integer uid, String type);

    /**
     * 通过物流类型和订单编号获取退货物流信息
     */
    List<WaybillInfoBO> getExpressByTypeAndCode(Byte logisticsType, String waybillCode);

}































/*
 * Copyright (C), 2016-2016, yoho
 * FileName: IOrderCancelService.java
 * Author:   maelk-liu
 * Date:     2016年5月31日 下午1:44:05
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.request.OrderCancelRequest;

/**
 * 取消订单---专用Service接口
 *
 * @author maelk-liu
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface IOrderCancelService {

    /**
     * 根据订单号取消订单
     *
     * @param request
     */
    void cancelByUser(OrderCancelRequest request);

    /**
     * 根据订单号自动取消订单
     *
     * @param orderCode
     */
    void cancelBySystemAuto(Long orderCode);

    /**
     * 同步erp系统过来的mq消息，更新前台订单/取消订单等
     *
     * @param orderCode
     * @param status
     */
    void updateOrderStatus(Long orderCode, int status, int express_id, String express_number);
    
    /**
     * 给erp调用：
     * 验证订单取消前的支付结果确认,返回:是否能取消   默认:Y
     *
     * @param orderCode
     * @return
     */
    String validateCancelStatus(Long orderCode);

}

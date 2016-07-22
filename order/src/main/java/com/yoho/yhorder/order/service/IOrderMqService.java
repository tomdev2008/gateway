/*
 * Copyright (C), 2016-2016, yoho
 * FileName: IOrderMqService.java
 * Author:   god_liu
 * Date:     2016年4月22日 下午2:25:33
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 前台order - mq发送者接口
 * 
 * @author maelk_liu
 */
public interface IOrderMqService {
    
    /**
     * 发送订单确认优惠券
     */
    void sendOrderConfirmCoupon(JSONObject request);
    
    /**
     * 
     * 功能描述: <br>发送微信消息
     * 〈功能详细描述〉
     *
     * @param request
     */
    void sendWechatPushMessage(JSONObject request);
    
    /**
     * 功能描述: <br>取消退换货申请--用户主动发起的
     * 〈功能详细描述〉
     *
     * @param request
     */
    void sendChangeRefundCancelMessage(JSONObject request);
}

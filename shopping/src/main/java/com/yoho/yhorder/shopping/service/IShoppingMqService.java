/*
 * Copyright (C), 2016-2016, yoho
 * FileName: IShoppingMqService.java
 * Author:   maelk_liu
 * Date:     2016年5月5日 下午1:51:12
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.shopping.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 前台shopping - mq发送者接口
 * 
 * @author maelk_liu
 */
public interface IShoppingMqService {
    
    void autoCancelOrder(JSONObject request);

}

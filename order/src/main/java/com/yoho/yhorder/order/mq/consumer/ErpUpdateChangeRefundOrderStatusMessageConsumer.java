/*
 * Copyright (C), 2016-2016, yoho
 * FileName: ErpSyncOrderUpdateRefundStatusMsgConsumer.java
 * Author:   god_liu
 * Date:     2016年4月12日 上午11:34:03
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.mq.consumer;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.message.YhMessageConsumer;
import com.yoho.yhorder.order.service.IChangeGoodsService;
import com.yoho.yhorder.order.service.IRefundService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 退货订单处理 ：根据erp传入mq消息同步更新前台表退货状态
 *
 * @author maelk_liu
 */
@Component
public class ErpUpdateChangeRefundOrderStatusMessageConsumer implements YhMessageConsumer {

    private final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private final String TOPIC = "erp.updateChangeRefundOrderStatus";

    private final String TYPE_CHANGE = "change";
    private final String TYPE_REFUND = "refund";

    @Autowired
    private IRefundService refundService;

    @Autowired
    private IChangeGoodsService changeGoodsService;

    @Override
    public String getMessageTopic() {
        return TOPIC;
    }

    /**
     * @param message
     * @see <a href="http://git.dev.yoho.cn/yoho-documents/api-interfaces/blob/master/%E8%AE%A2%E5%8D%95/%E8%AE%A2%E5%8D%95%E5%89%8D%E5%90%8E%E5%8F%B0%E5%88%86%E7%A6%BB.md">订单前后台分离</a>
     */
    @Override
    @Database(ForceMaster = true)
    public void handleMessage(Object message) {
        logger.info("begin handle erp.updateChangeRefundOrderStatus message, message is {}.", message);
        try {
            JSONObject msg = JSONObject.parseObject(String.valueOf(message));
            String type = StringUtils.trim(msg.getString("type"));
            switch (type) {
                case TYPE_REFUND:
                    refundService.syncRefundStatus(msg.getIntValue("id"), msg.getIntValue("status"), msg.getDoubleValue("real_returned_amount"),
                            msg.getString("is_return_coupon"), msg.getInteger("return_yoho_coin"));
                    break;
                case TYPE_CHANGE:
                    changeGoodsService.syncChangeGoodsStatus(msg.getIntValue("id"), msg.getIntValue("status"));
                    break;
                default:
                    logger.warn("erp.updateChangeRefundOrderStatus fail, unknown type {} message is {}.", type, message);

            }
            logger.info("handle erp.updateChangeRefundOrderStatus message success, message is {}.", message);
        } catch (Exception e) {
            logger.info("handle erp.updateChangeRefundOrderStatus message fail, message is {}.", message, e);
        }

    }

}

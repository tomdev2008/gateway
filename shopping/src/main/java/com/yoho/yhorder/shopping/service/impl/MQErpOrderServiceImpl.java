package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.yhorder.common.model.ERPOrder;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.shopping.model.*;
import com.yoho.yhorder.shopping.service.IOrderCreationService;
import com.yoho.yhorder.shopping.utils.OrderConfig;
import com.yoho.yhorder.shopping.utils.ReflectTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JXWU on 2016/2/17.
 */
@Component("mqErpOrderServiceImpl")
public class MQErpOrderServiceImpl extends ErpOrderServiceImpl implements IOrderCreationService {

    private final Logger logger = LoggerFactory.getLogger("orderSubmitLog");

    @Resource
    private YhProducerTemplate producerTemplate;

    @Override
    public void create(OrderCreationContext context) {

        logger.info("enter create erp order in shopping_cart_submit, uid {}, order code {}, \nuser info \n{}, \norder info \n{}\n ",
                context.getOrder().getUid(), context.getOrder().getOrderCode(),
                context.getUserInfo(), context.getOrder());

        //1.构建订单
        ERPOrder erpOrder = super.bulidOrder(context);

        logger.info("after build  erp order in create erp order of shopping_cart_submit, user id {}, order code {}, \nerp order \n{}\n ",
                context.getOrder().getUid(), context.getOrder().getOrderCode(), erpOrder);

        //2.校验订单
        super.checkOrder(erpOrder);
        //3.如某些字段值为null，则设置默认值
        // ############### 默认值的订单数据项 ##################
        ReflectTool.injectDefaultValue(erpOrder, OrderConfig.ERP_ORDER_DEFAULT_REQUEST_DATA_MAP);

        //入MQ
        sendMQ(erpOrder);

        logger.info("exit create erp order in shopping_cart_submit, user id {}, order code {} ",
                context.getOrder().getUid(), context.getOrder().getOrderCode());

    }

    private void sendMQ(ERPOrder erpOrder) {

        //手机号码
        String mobile = erpOrder.getMobile();
        erpOrder.hideMobile(mobile);
        JSONArray array = new JSONArray();
        array.add(erpOrder);
        logger.info("send create order message to mq in shopping_cart_submit, uid {}, order code {}, \nmessage is \n{}\n ",
                erpOrder.getUid(), erpOrder.getOrder_code(), array);

        try {
            erpOrder.unHideMobile(mobile);

            Map<String,Object> map = new HashMap<>();
            map.put("order_code",erpOrder.getOrder_code());
            map.put("uid",erpOrder.getUid());
            producerTemplate.send(Constants.ORDER_SUBMIT_TOPIC, array, map);
            logger.info("send create order message to mq success in shopping_cart_submit, topic {},uid {}, order code {}",
                    Constants.ORDER_SUBMIT_TOPIC,
                    erpOrder.getUid(), erpOrder.getOrder_code());
        } catch (Exception ex) {
            erpOrder.hideMobile(mobile);
            logger.warn("send create order message to mq fail!,topic {},uid {},message is: \n{}\n",
                    Constants.ORDER_SUBMIT_TOPIC,
                    erpOrder.getUid(), array, ex);
        }
    }
}

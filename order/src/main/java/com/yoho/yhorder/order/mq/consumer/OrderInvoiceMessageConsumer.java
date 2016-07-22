/*
 * Copyright (C), 2016-2016, yoho
 * FileName: OrderInvoiceMessageConsumer.java
 * Author:   god_liu
 * Date:     2016年6月16日 下午2:32:21
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.order.mq.consumer;

import java.util.ArrayList;
import java.util.List;

import com.yoho.yhorder.invoice.webservice.constant.InvoiceSoapErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.message.YhMessageConsumer;
import com.yoho.service.model.order.constants.InvoiceType;
import com.yoho.service.model.order.constants.OrderStatus;
import com.yoho.service.model.order.constants.OrdersMateKey;
import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;
import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.IOrdersMetaDAO;
import com.yoho.yhorder.dal.model.OrdersMeta;
import com.yoho.yhorder.invoice.service.InvoiceService;
import com.yoho.yhorder.order.service.IOrderCancelService;
import com.yoho.yhorder.order.service.IRefundService;

/**
 * 顾客拒收以及客服取消订单  导致发票冲红以及开具新的发票
 *
 * @author maelk_liu
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
@Component
public class OrderInvoiceMessageConsumer implements YhMessageConsumer {

    private final Logger logger = LoggerFactory.getLogger("mqConsumerLog");

    private final String TOPIC = "order.invoice";
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private IOrdersMapper ordersMapper;
    
    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;
    
    @Override
    public String getMessageTopic() {
        return TOPIC;
    }
    /*{

        "orderCode" : 1061003682, --原始订单号
        "refoundAmount" : 245.45, --浮点，保留两位
        "type":1   -- 1.拒收 2.撤销 3.客服退货
        "goodsItemList": --退货商品清单
        [
        {
            "skn" : 234566, 
            "prductName" : "sdfd", --商品名称
            "salePrice" : 23.33, --下单支付时的销售价，浮点，保留两位，一定要是下单保存的，没有就不用赋值
            "buyNumber" :  2  --该skn(234566)退货的数量   
        },
        {
            "skn" : 23453336, 
            "prductName" : "sdfd22", --商品名称
            "salePrice" : 23.34, --下单支付时的销售价，浮点，保留两位，一定要是下单保存的，没有就不用赋值
            "buyNumber" :  1  --该skn(234566)退货的数量   
        }
        ]
        }*/

    /**
     * @param message
     */
    @Override
    public void handleMessage(Object message) {
        logger.info("begin handle order.invoice message, message is {}.", message);
        try {
            JSONObject msg = JSONObject.parseObject(String.valueOf(message));
            String orderCode = msg.getString("orderCode");
            double amount = msg.getDouble("refoundAmount")==null?0:msg.getDouble("refoundAmount");
            JSONArray array = msg.getJSONArray("goodsItemList");
            int type=msg.getIntValue("type");
            Orders orders = ordersMapper.selectByOrderCode(orderCode);
            if(orders==null){
                logger.warn("this orders not exist !orderCode:{} ", orderCode);
                return;
            }
            redbuildInvoice(orders.getId(),orders.getOrderCode(),amount,array,type);
            logger.info("handle order.invoice message success, message is {}.", message);
        } catch (Exception e) {
            logger.info("handle order.invoice message fail, message is {}.", message, e);
        }
    }
    
    private List<GoodsItemBo> buildGoodsItemBoListByArray(JSONArray array){
        List<GoodsItemBo> list = new ArrayList<GoodsItemBo>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject jo = (JSONObject) array.get(i);
            GoodsItemBo gb = new GoodsItemBo(); 
            gb.setBuyNumber(jo.getInteger("buyNumber"));
            //gb.setPrductName(jo.getString("prductName"));
            //gb.setSalePrice(jo.getDouble("salePrice"));
            gb.setSkn(jo.getInteger("skn"));
            list.add(gb);
        }
        return list;
    }
    
    private void redbuildInvoice(int id,long orderCode,double amount,JSONArray array,int type){
        logger.info("begin build-red-Invoice,orderCode:{} ", orderCode);
        OrdersMeta meta = ordersMetaDAO.selectByOrdersIdAndMetaKey(id, OrdersMateKey.ELECTRONIC_INVOICE);
        //meta没数据,说明该笔订单不需要开电子发票
        if(null==meta||null==meta.getMetaValue()){
            logger.info("this order need not buildInvoice,orderCode:{}",  orderCode);
            return;
        }
        InvoiceBo bo = JSON.parseObject(meta.getMetaValue(), InvoiceBo.class);
        //电子发票
        if(bo.getType()==InvoiceType.electronic.getIntVal()){
            OrderInvoiceBo orderInvoice= new OrderInvoiceBo();
            orderInvoice.setRefoundAmount(amount);
            orderInvoice.setOrderId(id);
            orderInvoice.setGoodsItemList(buildGoodsItemBoListByArray(array));
            //设置类别为退货
            orderInvoice.setOrderStatus(OrderStatus.refund);
            switch (type) {
                case 1:
                    //拒收
                    invoiceService.redInvoice(orderInvoice);
                    break;
                case 2:
                    //撤销
                    invoiceService.redInvoice(orderInvoice);
                    break;
                case 3:
                    //客服操作的退货
                    logger.info("{}.redbuildInvoice begin red Invoice, orderInvoice {}",getClass().getSimpleName(),orderInvoice);
                    //冲红
                    InvoiceBo invoiceBo = invoiceService.redInvoice((OrderInvoiceBo)orderInvoice.clone());
                    logger.info("{}.redbuildInvoice end red Invoice, invoiceBo {}",getClass().getSimpleName(), invoiceBo);
                    //开新发票
                    if (invoiceBo != null && InvoiceSoapErrorCode.SUCCESS_KP.equalsIgnoreCase(invoiceBo.getReturnCode())){
                        invoiceBo = invoiceService.issueInvoice(orderInvoice);
                        logger.info("{}.redbuildInvoice do blue Invoice, invoiceBo {}",getClass().getSimpleName(), invoiceBo);
                    }
                    break;  
                default:
                    break;
            }
        }
    }

}

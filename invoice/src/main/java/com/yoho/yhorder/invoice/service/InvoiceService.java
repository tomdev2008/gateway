package com.yoho.yhorder.invoice.service;

import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;

import java.util.List;

/**
 * Created by chenchao on 2016/6/14.
 */
public interface InvoiceService {
    /**
     * 新增
     * @param invoiceBo
     * @return
     */
    int add(InvoiceBo invoiceBo);

    /**
     * 根据订单ID查询
     * @param orderId
     * @return
     */
    InvoiceBo queryByOrderId(int orderId);

    /**
     * 分库分表需要使用uid
     * @param orderId
     * @param uid
     * @return
     */
    InvoiceBo queryByOrderIdNUserid(int orderId, int uid);


    List<InvoiceBo> queryByOrderIds(List<Integer> orderIds);

    /**
     * 在开具发票，冲红时进行修改
     * @return
     */
    int update(InvoiceBo invoiceBo);

    /**
     * 开具发票
     * @return
     */
    InvoiceBo issueInvoice(OrderInvoiceBo orderInvoice);

    /**
     * 发票冲红
     * @return
     */
    InvoiceBo redInvoice(OrderInvoiceBo orderInvoice);

}

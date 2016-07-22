package com.yoho.yhorder.invoice.service.impl;

import com.google.common.collect.Lists;
import com.yoho.service.model.order.model.invoice.InvoiceLogsBo;
import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.common.convert.Convert;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.InvoiceLogsMapper;
import com.yoho.yhorder.dal.model.InvoiceLogs;
import com.yoho.yhorder.invoice.service.InvoiceLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchao on 2016/7/2.
 */
@Service
public class InvoiceLogServiceImpl implements InvoiceLogService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceLogServiceImpl.class);

    @Autowired
    private InvoiceLogsMapper logsMapper;

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private Convert convert;


    @Override
    public List<InvoiceLogsBo> getListByOrderCode(String orderCode) {
        List<InvoiceLogsBo> list = Lists.newArrayList();
        Orders orders = ordersMapper.selectByOrderCode(orderCode);
        if (orders == null) {
            logger.error("in getListByOrderCode ,orders is null ,param orderCode is {}", orderCode);
            InvoiceLogsBo bo = new InvoiceLogsBo();
            bo.setInvoiceInfo("in InvoiceLogServiceImpl.getListByOrderCode ,orders is null");
            list.add(bo);
            return list;
        }
        List<InvoiceLogs> dos = logsMapper.selectByOrderId(orders.getId());
        return convert.convertFromBatch(dos,new ArrayList<InvoiceLogsBo>(),InvoiceLogsBo.class);
    }
}

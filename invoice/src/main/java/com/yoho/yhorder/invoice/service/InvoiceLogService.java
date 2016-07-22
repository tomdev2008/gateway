package com.yoho.yhorder.invoice.service;

import com.yoho.service.model.order.model.invoice.InvoiceLogsBo;

import java.util.List;

/**
 * Created by chenchao on 2016/7/2.
 */
public interface InvoiceLogService {

    List<InvoiceLogsBo> getListByOrderCode(String orderCode);
}

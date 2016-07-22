package com.yoho.yhorder.invoice.rest;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.model.invoice.InvoiceLogsBo;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;
import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.common.request.BaseRequest;
import com.yoho.yhorder.invoice.service.InvoiceLogService;
import com.yoho.yhorder.invoice.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by chenchao on 2016/6/13.
 */
@Controller
@RequestMapping("/invoice")
@ServiceDesc(serviceName = "order")
public class InvoiceController {


    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceLogService invoiceLogService;

    @RequestMapping("/addInvoice")
    @ResponseBody
    public InvoiceBo addInvoice(@RequestBody InvoiceBo bo){
        invoiceService.add(bo);
        return bo;
    }

    @RequestMapping("/queryByOrderId")
    @ResponseBody
    public InvoiceBo queryByOrderId(@RequestBody InvoiceBo bo){
        return invoiceService.queryByOrderId(bo.getOrderId());
    }

    @RequestMapping("/issueInvoice")
    @ResponseBody
    public InvoiceBo issueInvoice(@RequestBody OrderInvoiceBo orderInvoice){
        return invoiceService.issueInvoice(orderInvoice);
    }

    @RequestMapping("/redInvoice")
     @ResponseBody
     public InvoiceBo redInvoice(@RequestBody OrderInvoiceBo bo){

        return invoiceService.redInvoice(bo);
    }


    @RequestMapping("/queryInvoiceLogs")
    @ResponseBody
    public List<InvoiceLogsBo> queryInvoiceLogs(@RequestBody Orders bo){
        if (bo == null || bo.getOrderCode() == null){
            return Lists.newArrayList();
        }
        return invoiceLogService.getListByOrderCode(Long.toString(bo.getOrderCode()));
    }

    @Resource
    private YhProducerTemplate producerTemplate;

    @RequestMapping("/pushInvoiceMqData")
    public void pushInvoiceMqData(@RequestParam String queueName, @RequestParam JSONObject statusData){

        try {
            producerTemplate.send(queueName, statusData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

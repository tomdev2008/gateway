package com.yoho.yhorder.invoice.service.merger;

import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.yhorder.invoice.webservice.xmlbean.FpkjResp;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by chenchao on 2016/6/21.
 */
public final class InvoiceMerger {

    private static Logger logger = LoggerFactory.getLogger(InvoiceMerger.class);
    /**
     * 回填发票
     * @param invoice
     * @param fpkjResp
     */
    public static void mergeFpkjResp(InvoiceBo invoice,FpkjResp fpkjResp){

        //from http invoice
        if (fpkjResp != null){
            invoice.setInvoiceNum(fpkjResp.getInvoiceNum());
            invoice.setInvoiceCode(fpkjResp.getInvoiceCode());
            if (StringUtils.isNotBlank(fpkjResp.getIssueDate())){
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    invoice.setIssueDate(dateFormat.parse(fpkjResp.getIssueDate()).getTime());
                } catch (ParseException e) {
                    logger.error("parse date {}, {}",fpkjResp.getIssueDate(),e);
                }
            }
            invoice.setValidateCode(fpkjResp.getValidateCode());
            invoice.setPdfUrl(fpkjResp.getPdfUrl());
        }

    }

    public static void main(String[] args) {
        String date = "20160628141030";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            System.out.println(dateFormat.parse(date));
            System.out.println(dateFormat.parse(date).getTime());
        } catch (ParseException e) {
            logger.error("parse date  {}",e);
        }
    }
}



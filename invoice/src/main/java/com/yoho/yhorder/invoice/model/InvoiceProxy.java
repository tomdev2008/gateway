package com.yoho.yhorder.invoice.model;

import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.service.model.order.model.invoice.InvoiceAmount;
import com.yoho.yhorder.invoice.webservice.xmlbean.CommonFPKJReq;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Created by chenchao on 2016/7/11.
 */
public class InvoiceProxy {
    private InvoiceAmount invoiceAmount;
    private List<GoodsItemBo> goodsItemList;
    private CommonFPKJReq fpkjReq;

    public InvoiceAmount getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(InvoiceAmount invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public List<GoodsItemBo> getGoodsItemList() {
        return goodsItemList;
    }

    public void setGoodsItemList(List<GoodsItemBo> goodsItemList) {
        this.goodsItemList = goodsItemList;
    }

    public CommonFPKJReq getFpkjReq() {
        return fpkjReq;
    }

    public void setFpkjReq(CommonFPKJReq fpkjReq) {
        this.fpkjReq = fpkjReq;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("invoiceAmount", invoiceAmount)
                .append("goodsItemList", goodsItemList)
                .append("fpkjReq", fpkjReq)
                .toString();
    }
}

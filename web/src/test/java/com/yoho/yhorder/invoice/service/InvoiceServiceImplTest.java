package com.yoho.yhorder.invoice.service;

import com.google.common.collect.Lists;
import com.yoho.service.model.order.constants.InvoiceType;
import com.yoho.service.model.order.constants.OrderStatus;
import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;
import com.yoho.yhorder.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by chenchao on 2016/6/16.
 */
public class InvoiceServiceImplTest extends BaseTest {
    @Autowired
    private InvoiceService invoiceService;

    @Test
    public void testRefundIssueInvoice(){
        OrderInvoiceBo orderInvoice = createRefundInvoice();
        invoiceService.issueInvoice(orderInvoice);
    }

    /**
     * 正常下单
     */
    @Test
    public void testNormalIssueInvoice(){
        OrderInvoiceBo orderInvoice = createNormalInvoice();
        invoiceService.issueInvoice(orderInvoice);
    }

    @Test
    public void testAdd(){
        InvoiceBo invoiceBo = createAddinvoice();
        invoiceService.add(invoiceBo);
    }

    @Test
    public void testQueryOne(){
        int orderId = 5540568;
        InvoiceBo invoiceBo = invoiceService.queryByOrderId(orderId);

        System.out.println("invoiceService.queryByOrderId(orderId) invoiceBo is " + invoiceBo);

        System.out.println("is null " + (invoiceBo == null));
    }

    @Test
    public void redInvoice(){
        int orderId = 999999;
        OrderInvoiceBo invoiceBo = new OrderInvoiceBo();
        invoiceBo.setOrderId(orderId);
        invoiceService.redInvoice(invoiceBo);
    }

    OrderInvoiceBo createRefundInvoice(){
        int orderId = 5541036;
        OrderInvoiceBo orderInvoice = new OrderInvoiceBo();
        orderInvoice.setOrderStatus(OrderStatus.refund);
        orderInvoice.setOrderId(orderId);
        orderInvoice.setRefoundAmount(13.21D);
        GoodsItemBo itemBo = new GoodsItemBo();
        itemBo.setSkn(51148285);
        itemBo.setPrductName("EVD 纯色条纹束袖口衬衫");
        itemBo.setBuyNumber(1);
        List<GoodsItemBo> goodsItemList = Lists.newArrayList();
        goodsItemList.add(itemBo);
        //
        orderInvoice.setGoodsItemList(goodsItemList);
        return orderInvoice;
    }

    OrderInvoiceBo createNormalInvoice(){
        int orderId = 999999;
        OrderInvoiceBo orderInvoice = new OrderInvoiceBo();
        orderInvoice.setOrderStatus(OrderStatus.normal);
        orderInvoice.setOrderId(orderId);
        orderInvoice.setAmount(888.89D);
        GoodsItemBo itemBo = new GoodsItemBo();
        itemBo.setSkn(123);
        itemBo.setPrductName("a");
        itemBo.setMarketPrice(100D);
        itemBo.setBuyNumber(3);
        List<GoodsItemBo> goodsItemList = Lists.newArrayList();
        goodsItemList.add(itemBo);
        //
        itemBo = new GoodsItemBo();
        itemBo.setSkn(125);
        itemBo.setPrductName("b");
        itemBo.setMarketPrice(200);
        itemBo.setBuyNumber(4);
        goodsItemList.add(itemBo);

        orderInvoice.setGoodsItemList(goodsItemList);
        return orderInvoice;
    }

    InvoiceBo createAddinvoice(){
        int orderId = 999999;
        InvoiceBo invoiceBo = new InvoiceBo();
        invoiceBo.setOrderId(orderId);
        invoiceBo.setContent(1);
        //invoiceBo.setContentValue(ShoppingConfig.INVOICE_CONTENT_MAP.get(1));
        invoiceBo.setMobilePhone("18234561212");
        invoiceBo.setTitle("电子发票测试");

        invoiceBo.setType(InvoiceType.electronic.getIntVal());
        OrderInvoiceBo orderInvoice = new OrderInvoiceBo();
        orderInvoice.setOrderId(orderId);
        orderInvoice.setAmount(388.98);
        GoodsItemBo itemBo = new GoodsItemBo();
        itemBo.setSkn(123);
        itemBo.setPrductName("a");
        itemBo.setMarketPrice(100);
        itemBo.setBuyNumber(2);
        List<GoodsItemBo> goodsItemList = Lists.newArrayList();
        goodsItemList.add(itemBo);
        //
        itemBo = new GoodsItemBo();
        itemBo.setSkn(125);
        itemBo.setPrductName("b");
        itemBo.setMarketPrice(200);
        itemBo.setBuyNumber(2);
        goodsItemList.add(itemBo);

        orderInvoice.setGoodsItemList(goodsItemList);
        invoiceBo.setOrderInvoice(orderInvoice);

        return invoiceBo;
    }
}

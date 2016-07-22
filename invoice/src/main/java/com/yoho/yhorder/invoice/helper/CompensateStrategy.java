package com.yoho.yhorder.invoice.helper;

import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.yhorder.invoice.model.InvoiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by chenchao on 2016/7/8.
 */
public abstract class CompensateStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CompensateStrategy.class);

    public abstract InvoiceProxy compensateDifference(InvoiceRegionReq req);


    GoodsItemBo findGoodsItemUseSubtract(List<GoodsItemBo> goodsItemList, double diff, double tax_rate){
        GoodsItemBo goodsItem_subtract = null;
        for(GoodsItemBo goodsItem : goodsItemList){
            //检验，减去误差没变化
            if(isEqualAfterSubtractDiff(goodsItem, diff, tax_rate)){
                goodsItem_subtract = goodsItem;
                break;
            }
        }
        return goodsItem_subtract;
    }

    boolean isEqualAfterSubtractDiff(GoodsItemBo goodsItem, double diff, double tax_rate){
        return DigitHelper.formatDouble(InvoiceCalculator.calculateMultiply(InvoiceCalculator.calculateSubtract(goodsItem.getAmountWithoutTax(), diff).doubleValue(),
                tax_rate).doubleValue(), 2) == goodsItem.getTaxAmount();
    }

    boolean isEqualAfterAddDiff(GoodsItemBo goodsItem, double diff, double tax_rate){
        return DigitHelper.formatDouble(InvoiceCalculator.calculateMultiply(InvoiceCalculator.calculateAdd(goodsItem.getAmountWithoutTax(), diff).doubleValue(),
                tax_rate).doubleValue(), 2) == goodsItem.getTaxAmount();
    }

    static class InvoiceRegionReq{
        /**
         * 所有项目（不含税）总和
         */
        double allItemsTotalAmount;
        /**
         * 所有项目的税额总和
         */
        double allTaxTotalAmount;
        /**
         * 折扣总价（不含税）
         */
        double allItemsDiscountAmount;
        /**
         * 折扣税额
         */
        double allTaxDiscountAmount;
        /**
         * 发票货品项目
         */
        List<GoodsItemBo> goodsItemList;
        /**
         * 差额
         */
        double diff;
        /**
         * 税率
         */
        double tax_rate;
    }
}

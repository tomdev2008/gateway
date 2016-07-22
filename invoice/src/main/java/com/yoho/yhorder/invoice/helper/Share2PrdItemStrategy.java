package com.yoho.yhorder.invoice.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.yhorder.invoice.model.InvoiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by chenchao on 2016/7/8.
 */
public class Share2PrdItemStrategy extends CompensateStrategy{

    private static final Logger logger = LoggerFactory.getLogger(Share2PrdItemStrategy.class);

    public InvoiceProxy compensateDifference(InvoiceRegionReq req){
        List<GoodsItemBo> goodsItemList= req.goodsItemList;
        double diff = req.diff;
        double tax_rate = req.tax_rate;
        return doCompensateDifference(goodsItemList,diff,tax_rate);
    }

    private InvoiceProxy doCompensateDifference(List<GoodsItemBo> goodsItemList, double diff, double tax_rate){
        Map<Integer,GoodsItemBo> map = MapUtil.transformMap(goodsItemList, (GoodsItemBo input) -> input.getSkn());

        //加误差值不影响的条目
        GoodsItemBo goodsItem_plus = null;
        List<GoodsItemBo> goodsItems_plus = Lists.newArrayList();
        for(GoodsItemBo goodsItem : goodsItemList){
            if(isEqualAfterAddDiff(goodsItem, diff, tax_rate)){
                goodsItems_plus.add(goodsItem);
            }
        }
        if (goodsItems_plus.size() == 0){
            logger.error("can't find any item which keep tax after plus diff");
            return null;
        }
        GoodsItemBo goodsItem_subtract = null;
        //每一项都满足加上误差后不变
        if (goodsItems_plus.size() == goodsItemList.size()){
            //这里不会发生移除一个后集合为空，一种商品时没有折扣，不会有误差
            goodsItem_plus = goodsItems_plus.remove(0);
            //从集合中移除
            goodsItemList.remove(goodsItem_plus);

            //寻找减去误差值不影响的条目
            goodsItem_subtract =  findGoodsItemUseSubtract(goodsItemList, diff, tax_rate);

            //没有找到,用刚移除的加不变项目
            if(goodsItem_subtract == null){
                if(isEqualAfterSubtractDiff(goodsItem_plus, diff, tax_rate)){
                    //
                    goodsItem_subtract = goodsItem_plus;
                    goodsItem_plus = goodsItemList.get(0);
                }
            }
        }else{//不是所有的项目都满足加上误差后不变
            //从集合中移除
            goodsItemList.removeAll(goodsItems_plus);

            //寻找减去误差值不影响的条目
            goodsItem_subtract =  findGoodsItemUseSubtract(goodsItemList, diff, tax_rate);
            //匹配到
            if (goodsItem_subtract != null){
                //任意取一个
                goodsItem_plus = goodsItems_plus.get(0);
            }
            //没有匹配到,在加误差不变的集合中寻找
            if (goodsItem_subtract == null){

                goodsItem_subtract =  findGoodsItemUseSubtract(goodsItems_plus, diff, tax_rate);
                //find one
                if (goodsItem_subtract != null){
                    goodsItems_plus.remove(goodsItem_subtract);
                    goodsItem_plus = goodsItems_plus.get(0);
                }
            }
        }
        //at last, still can not find
        if (goodsItem_subtract == null){
            logger.error("can't find any item which keep tax after subtract diff");
            return null;
        }
        //转移误差
        //加不变
        goodsItem_plus.setAmountWithoutTax(DigitHelper.formatDouble(InvoiceCalculator.calculateAdd(goodsItem_plus.getAmountWithoutTax(), diff).doubleValue(), 2));
        if(goodsItem_plus.getSkn() != InvoiceCalculator.discountLine_skn){
            goodsItem_plus.setUnitPrice(InvoiceCalculator.calculateUnitPrice(goodsItem_plus.getAmountWithoutTax(), goodsItem_plus.getBuyNumber()).doubleValue());
        }
        //减不变
        goodsItem_subtract.setAmountWithoutTax(DigitHelper.formatDouble(InvoiceCalculator.calculateSubtract(goodsItem_subtract.getAmountWithoutTax(), diff).doubleValue(), 2));
        if (goodsItem_subtract.getSkn() != InvoiceCalculator.discountLine_skn){
            goodsItem_subtract.setUnitPrice(InvoiceCalculator.calculateUnitPrice(goodsItem_subtract.getAmountWithoutTax(), goodsItem_subtract.getBuyNumber()).doubleValue());
        }
        //替换
        map.replace(goodsItem_plus.getSkn(), goodsItem_plus);
        map.replace(goodsItem_subtract.getSkn(), goodsItem_subtract);
        InvoiceProxy invoiceProxy = new InvoiceProxy();

        return invoiceProxy;
    }
}

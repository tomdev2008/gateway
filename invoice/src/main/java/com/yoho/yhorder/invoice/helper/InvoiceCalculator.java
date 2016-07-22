package com.yoho.yhorder.invoice.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.service.model.order.model.invoice.InvoiceAmount;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchao on 2016/6/18.
 */
public final class InvoiceCalculator {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceCalculator.class);

    public static final int discountLine_skn = -10000;
    /**
     * 单价位数
     */
    public static final int digit_number_single_price = 6;
    /**
     * 计算发票上单价 税额 价税总计 折扣
     * @param goodsItemList
     * @param actualAmount
     * @return
     */
    public static InvoiceAmount calculateBlueInvoiceAmount(final List<GoodsItemBo> goodsItemList, double actualAmount, double tax_rate){
        //单个栏目
        BigDecimal taxAmount;
        double taxAmountDouble;
        double projectAmount;
        BigDecimal unitPrice;
        BigDecimal priceWithoutTax;
        double totalProjectAmount_withTax = 0D;
        //所有栏总和
        //合计项目金额
        double totalProjectAmount = 0D;
        double totalProjectAmount_goods = 0D;

        //商品合计税额（没有算折扣）
        double totalTaxAmount_goods_jq = 0D;
        double totalTaxAmount_goods = 0D;

        for(GoodsItemBo goodsItem : goodsItemList){
            int buyNumber = goodsItem.getBuyNumber()<0 ? - goodsItem.getBuyNumber(): goodsItem.getBuyNumber();
            //项目单价 否 小数点后6位 不含税
            unitPrice = calculateUnitPrice(goodsItem.getMarketPrice(), tax_rate);
            goodsItem.setUnitPrice(unitPrice.doubleValue());

            //项目金额 是 不含税，单位：元（2位小数）
            priceWithoutTax = calculatePriceWithoutTax(unitPrice, buyNumber);
            projectAmount = DigitHelper.formatDouble(priceWithoutTax.doubleValue(), 2);
            goodsItem.setAmountWithoutTax(projectAmount);
            //多位小数
            totalProjectAmount = calculateAdd(totalProjectAmount,projectAmount).doubleValue();
            totalProjectAmount_goods = DigitHelper.formatDouble(totalProjectAmount,2);
            //含税总价
            totalProjectAmount_withTax = calculateAdd(totalProjectAmount_withTax,calculateMultiply(goodsItem.getMarketPrice(), buyNumber).doubleValue()).doubleValue();

            //税额 是 单位：元（2位小数）
            taxAmount = calculateTaxAmount(projectAmount,tax_rate);
            taxAmountDouble = DigitHelper.formatDouble(taxAmount.doubleValue(), 2);
            goodsItem.setTaxAmount(taxAmountDouble);
            //多位小数
            totalTaxAmount_goods_jq = calculateAdd(totalTaxAmount_goods_jq,taxAmountDouble).doubleValue();
            totalTaxAmount_goods = DigitHelper.formatDouble(totalTaxAmount_goods_jq,2);
        }

        logger.debug("normal item calculate without discount, totalProjectAmount {},totalTaxAmount_goods_jq {}, goodsItemList {} ",
                totalProjectAmount, totalTaxAmount_goods_jq, goodsItemList);

        InvoiceAmount invoiceAmount = new InvoiceAmount();
        //合计金额
        double totalProjectAmount_jq = DigitHelper.formatDouble(calculatePrdAmount(actualAmount, tax_rate).doubleValue(),2);
        //合计税额
        double totalTaxAmount =  DigitHelper.formatDouble(calculateSubtract(actualAmount,totalProjectAmount_jq).doubleValue(), 2);
        invoiceAmount.setTotalTaxAmount(totalTaxAmount);

        //存在折扣，总的项目金额（不含税）=实际支付金额（含税）-总的税额
        //totalProjectAmount_jq = DigitHelper.formatDouble(calculateSubtract(actualAmount, totalTaxAmount).doubleValue(),2);
        //必须使用 总金额（含税）-税额
        invoiceAmount.setTotalProjectAmount(totalProjectAmount_jq);
        invoiceAmount.setActualAmount(actualAmount);
        //计算折扣，当存在折扣时，append to list
        GoodsItemBo goodsItem = new GoodsItemBo();
        if(actualAmount > totalProjectAmount_withTax){
            logger.error("this order actualAmount {} bigger than totalProjectAmount_withTax {}", actualAmount, totalProjectAmount_withTax );
            //return invoiceAmount;
        }
        //折扣行的项目金额 和 税额，从总的反推，必须这样（我们只知道实际支付的金额，折扣都是演算出来的）
        if (actualAmount < totalProjectAmount_withTax){
            goodsItem.setSkn(discountLine_skn);
            // 这里计算可能会存在误差，四舍五入
            double totalTaxAmount_discount
                    = DigitHelper.formatDouble(calculateSubtract(totalTaxAmount_goods,totalTaxAmount).doubleValue(),2);
            goodsItem.setTaxAmount(-totalTaxAmount_discount);
            // 这里计算可能会存在误差，四舍五入
            //项目金额 是 不含税，单位：元（2位小数）
            double totalProjectAmount_discount
                    = DigitHelper.formatDouble(calculateSubtract(totalProjectAmount_goods,totalProjectAmount_jq).doubleValue(), 2);
            goodsItem.setAmountWithoutTax(-totalProjectAmount_discount);
            //计算折扣
            double discount = DigitHelper.formatDouble(
                    calculateDiscount(totalProjectAmount_discount, totalProjectAmount_goods).doubleValue(),3);
            goodsItem.setPrductName(buildDiscountMsg(goodsItemList.size(), discount).toString());
            invoiceAmount.setDiscount(discount);
            invoiceAmount.setHasDiscount(true);
            goodsItemList.add(goodsItem);
            //存在误差时，转移误差
            if(needCompensate(totalProjectAmount_discount, totalTaxAmount_discount, tax_rate)){
                double diff = calDiff(totalProjectAmount_discount,totalTaxAmount_discount, tax_rate);
                Map<Integer,GoodsItemBo> map = compensateDifference(new ArrayList(goodsItemList), diff, tax_rate);
                if(map.isEmpty()){
                    logger.error("can not compensate diff, diff {} , goodsItemList {}", diff, goodsItemList);
                }else{
                    goodsItemList.clear();
                    goodsItemList.addAll(map.values());
                    //重新计算折扣率
                    GoodsItemBo goodsItem_disount = map.get(discountLine_skn);
                    if(goodsItem.getAmountWithoutTax() != goodsItem_disount.getAmountWithoutTax()){
                        discount = DigitHelper.formatDouble(
                                calculateDiscount(goodsItem_disount.getAmountWithoutTax(), totalProjectAmount_goods).doubleValue(),3);
                        invoiceAmount.setDiscount(discount);
                    }
                }
            }

        }else{//无折扣
            //单个商品不要算误差,清单里只有一个项目，直接使用合计的数据取代
            if(goodsItemList.size() == 1){
                invoiceAmount = calSingleItemBlueInvoice(goodsItemList.get(0), actualAmount,  tax_rate);
            }else {
                double amount_diff = calculateSubtract(totalProjectAmount_jq, totalProjectAmount_goods).doubleValue();
                double tax_diff = calculateSubtract(totalTaxAmount, totalTaxAmount_goods).doubleValue();
                calNoDiscountBlueInvoice(goodsItemList, amount_diff, tax_diff, tax_rate);
            }
            invoiceAmount.setHasDiscount(false);
        }
        logger.debug("normal item calculate after discount,  goodsItemList {} ", goodsItemList);
        return invoiceAmount;
    }

    private static void calNoDiscountBlueInvoice(List<GoodsItemBo> goodsItemList, double amount_diff, double tax_diff, double tax_rate){
        //从列表中拿出最后一个
        int lastIndex = goodsItemList.size()-1;

        GoodsItemBo goodsItem = goodsItemList.get(lastIndex);
        double amount = DigitHelper.formatDouble(calculateAdd(goodsItem.getAmountWithoutTax(), amount_diff).doubleValue(), 2);
        goodsItem.setAmountWithoutTax(amount);
        //
        double unitPrice_last = calculateUnitPrice(amount, goodsItem.getBuyNumber()).doubleValue();
        goodsItem.setUnitPrice(unitPrice_last);
        //

        double tax = DigitHelper.formatDouble(calculateAdd(goodsItem.getTaxAmount(), tax_diff).doubleValue(), 2);
        goodsItem.setTaxAmount(tax);

        //转移误差
        if(needCompensate(amount, tax, tax_rate)){
            double diff = calDiff(amount, tax, tax_rate);
            Map<Integer,GoodsItemBo> map = compensateDifference(new ArrayList(goodsItemList), diff, tax_rate);
            if(map.isEmpty()){
                logger.error("can not compensate diff in calNoDiscountBlueInvoice, diff {} , goodsItemList {}", diff, goodsItemList);
            }else{
                goodsItemList.clear();
                goodsItemList.addAll(map.values());
            }
        }

    }

    /**
     * 单种（同一个skn）商品，不计算折扣
     * @param goodsItem
     * @param actualAmount
     * @param tax_rate
     * @return
     */
    public static InvoiceAmount calSingleItemBlueInvoice(GoodsItemBo  goodsItem, double actualAmount, double tax_rate){
        InvoiceAmount invoiceAmount = new InvoiceAmount();

        int buyNumber = goodsItem.getBuyNumber()<0 ? - goodsItem.getBuyNumber(): goodsItem.getBuyNumber();
        //项目合计金额，根据用户支付金额获取
        double totalProjectAmount = DigitHelper.formatDouble(calculatePrdAmount(actualAmount, tax_rate).doubleValue(),2);
        invoiceAmount.setTotalProjectAmount(totalProjectAmount);

        //合计税额
        double totalTaxAmount =  DigitHelper.formatDouble(calculateSubtract(actualAmount,totalProjectAmount).doubleValue(), 2);
        invoiceAmount.setTotalTaxAmount(totalTaxAmount);

        //项目金额 是 不含税，单位：元（2位小数）
        double projectAmount = totalProjectAmount;
        goodsItem.setAmountWithoutTax(projectAmount);

        //项目单价 否 小数点后6位 不含税
        BigDecimal unitPrice = calculateUnitPrice(projectAmount, buyNumber);
        goodsItem.setUnitPrice(unitPrice.doubleValue());

        //项目税额 是 单位：元（2位小数）
        double taxAmount = totalTaxAmount;
        goodsItem.setTaxAmount(taxAmount);

        //实际支付，发票的价税合计金额
        invoiceAmount.setActualAmount(actualAmount);
        return invoiceAmount;
    }



    private static boolean needCompensate(double amount, double tax, double tax_rate){
        double act_tax = DigitHelper.formatDouble(calculateMultiply(amount, tax_rate).doubleValue(),2);
        return act_tax != tax;
    }

    /**
     * 计算误差，不区分正负，保留两位小数，应该是0.01，不应该是其他值
     * @param amount
     * @param tax
     * @param tax_rate
     * @return
     */
    private static double calDiff(double amount, double tax, double tax_rate){
        double act_tax = DigitHelper.formatDouble(calculateMultiply(amount, tax_rate).doubleValue(),2);
        return DigitHelper.formatDouble(calculateSubtract(tax, act_tax).doubleValue(),2);
    }


    private static boolean isEqualAfterSubtractDiff(GoodsItemBo goodsItem, double diff, double tax_rate){
        return DigitHelper.formatDouble(calculateMultiply(calculateSubtract(goodsItem.getAmountWithoutTax(), diff).doubleValue(),
                tax_rate).doubleValue(), 2) == goodsItem.getTaxAmount();
    }

    private static boolean isEqualAfterAddDiff(GoodsItemBo goodsItem, double diff, double tax_rate){
        return DigitHelper.formatDouble(calculateMultiply(calculateAdd(goodsItem.getAmountWithoutTax(), diff).doubleValue(),
                tax_rate).doubleValue(), 2) == goodsItem.getTaxAmount();
    }
    /**
     * 补偿误差，将误差值转移到任意一项货品金额上，验证加上误差值后，金额*税率=原来的税额；
     * 另一项减去误差后，保持计算得到的税额与原税额一致；
     * @param goodsItemList
     * @param diff
     */
    private static double minDiff = 0.01;
    private static double minDiff_negate = -0.01;

    /**
     * //?:0或1个, *:0或多个, +:1或多个
     * @param doubleVal
     * @return
     */
    private static boolean isNegate(double doubleVal){
        return  Double.toString(doubleVal).matches("-[0-9]+.*[0-9]*");
    }
    private static Map<Integer,GoodsItemBo> compensateDifference(List<GoodsItemBo> goodsItemList, double diff, double tax_rate){
        return doCompensateDifference(goodsItemList,diff,tax_rate);
        /*
        Map<Integer,GoodsItemBo> map = null;

        double baseMinDiff = minDiff;
        if (isNegate(diff)){
            baseMinDiff = minDiff_negate;
        }
        if (diff == baseMinDiff){
            return doCompensateDifference(goodsItemList,diff,tax_rate);
        }else{
            do{
                if (map != null){
                    goodsItemList = new ArrayList<>(map.values());
                }
                map = doCompensateDifference(goodsItemList, baseMinDiff, tax_rate);
                diff = diff - baseMinDiff;
            }while(diff!=0D);
        }
        return map;
        */
    }

    private static Map<Integer,GoodsItemBo> doCompensateDifference(List<GoodsItemBo> goodsItemList, double diff, double tax_rate){
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
            return Maps.newHashMap();
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
            return Maps.newHashMap();
        }
        //转移误差
        //加不变
        goodsItem_plus.setAmountWithoutTax(DigitHelper.formatDouble(calculateAdd(goodsItem_plus.getAmountWithoutTax(), diff).doubleValue(), 2));
        if(goodsItem_plus.getSkn() != discountLine_skn){
            goodsItem_plus.setUnitPrice(calculateUnitPrice(goodsItem_plus.getAmountWithoutTax(), goodsItem_plus.getBuyNumber()).doubleValue());
        }
        //减不变
        goodsItem_subtract.setAmountWithoutTax(DigitHelper.formatDouble(calculateSubtract(goodsItem_subtract.getAmountWithoutTax(), diff).doubleValue(), 2));
        if (goodsItem_subtract.getSkn() != discountLine_skn){
            goodsItem_subtract.setUnitPrice(calculateUnitPrice(goodsItem_subtract.getAmountWithoutTax(), goodsItem_subtract.getBuyNumber()).doubleValue());
        }
        //替换
        map.replace(goodsItem_plus.getSkn(), goodsItem_plus);
        map.replace(goodsItem_subtract.getSkn(), goodsItem_subtract);
        return map;
    }

    private static GoodsItemBo findGoodsItemUseSubtract(List<GoodsItemBo> goodsItemList, double diff, double tax_rate){
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

    public static void calculateSingleRedInvoiceAmount(GoodsItemBo goodsItem, InvoiceAmount invoiceAmount){
        //单个项目总价
        double projectAmount_Double;
        //单价
        BigDecimal unitPrice;
        double unitPriceDouble;
        projectAmount_Double = invoiceAmount.getTotalProjectAmount();
        goodsItem.setAmountWithoutTax(-projectAmount_Double);
        goodsItem.setTaxAmount(-invoiceAmount.getTotalTaxAmount());
        //单价
        unitPrice = calculateUnitPrice(projectAmount_Double, goodsItem.getBuyNumber());
        unitPriceDouble = unitPrice.doubleValue();
        goodsItem.setUnitPrice(unitPriceDouble);
        goodsItem.setBuyNumber(-goodsItem.getBuyNumber());
    }

    /**
     * 计算冲红发票上单价 税额 价税总计 折扣
     * @param goodsItemList

     * @return
     */
    public static void calculateRedInvoiceAmount(final List<GoodsItemBo> goodsItemList, InvoiceAmount invoiceAmount,
                                                          double tax_rate){

        //单个skn商品，只需要计算项目单价
        if (goodsItemList.size() == 1){
            calculateSingleRedInvoiceAmount(goodsItemList.get(0), invoiceAmount);
            //it's over
            return;
        }
        //单个项目总价
        double projectAmount_Double;
        //单价
        BigDecimal unitPrice;
        double unitPriceDouble;
        //扣除折扣后的实际金额
        BigDecimal projectAmount;
        //单个栏目
        BigDecimal taxAmount;
        double taxAmountDouble;
        //所有栏总和
        //合计项目金额
        double totalProjectAmount_goods = 0D;
        //合计税额
        double totalTaxAmount_goods = 0D;
        //不计算折扣，单项目的总价
        double projectAmountNoDiscnt;
        double discount = new BigDecimal(invoiceAmount.getDiscount()).divide(new BigDecimal(100),5,BigDecimal.ROUND_HALF_UP).doubleValue();
        int count = 1;
        for(GoodsItemBo goodsItem : goodsItemList){
            if (count == goodsItemList.size()){//最后一个用作调账
                projectAmount = calculateSubtract(invoiceAmount.getTotalProjectAmount(),totalProjectAmount_goods);
                projectAmount_Double = DigitHelper.formatDouble(projectAmount.doubleValue(), 2);
                goodsItem.setAmountWithoutTax(-projectAmount_Double);
                //税额 是 单位：元（2位小数）
                taxAmount = calculateSubtract(invoiceAmount.getTotalTaxAmount(), totalTaxAmount_goods);
                taxAmountDouble = DigitHelper.formatDouble(taxAmount.doubleValue(), 2);
                goodsItem.setTaxAmount(-taxAmountDouble);
                //单价
                unitPrice = calculateUnitPrice(projectAmount_Double, goodsItem.getBuyNumber());
                unitPriceDouble = unitPrice.doubleValue();
                goodsItem.setUnitPrice(unitPriceDouble);
                goodsItem.setBuyNumber(-goodsItem.getBuyNumber());
                return;
            }

            //先计算单项目的货品总价（保证项目总价没有误差），再算单价
            //项目金额(没算折扣的) 是 不含税，单位：元（2位小数）
            projectAmountNoDiscnt = goodsItem.getAmountWithoutTax();
            //扣除折扣后的实际金额
            projectAmount = calculateSubtract(projectAmountNoDiscnt, calculateMultiply(projectAmountNoDiscnt, discount).doubleValue());
            projectAmount_Double = DigitHelper.formatDouble(projectAmount.doubleValue(), 2);
            goodsItem.setAmountWithoutTax(-projectAmount_Double);
            totalProjectAmount_goods = calculateAdd(totalProjectAmount_goods,projectAmount_Double).doubleValue();
            //项目单价 否 小数点后6位 不含税
            //单价
            unitPrice = calculateUnitPrice(projectAmount_Double, goodsItem.getBuyNumber());
            unitPriceDouble = unitPrice.doubleValue();
            goodsItem.setUnitPrice(unitPriceDouble);

            //税额 是 单位：元（2位小数）
            //多位小数
            taxAmount = calculateMultiply(projectAmount.doubleValue(), tax_rate);
            taxAmountDouble = DigitHelper.formatDouble(taxAmount.doubleValue(), 2);
            goodsItem.setTaxAmount(-taxAmountDouble);
            totalTaxAmount_goods = calculateAdd(totalTaxAmount_goods,taxAmountDouble).doubleValue();
            goodsItem.setBuyNumber(-goodsItem.getBuyNumber());
            count++;
        }

        //todo 将最后一个拿出来比对，是否存在误差，存在误差时作转移补偿
        //GoodsItemBo goodsItem_last = goodsItemList.get(goodsItemList.size() - 1);



    }
    public static BigDecimal calculateAdd(double one ,double anothor){
        return new BigDecimal(one).add(new BigDecimal(anothor));
    }

    public static BigDecimal calculateSubtract(double max ,double min){
        return new BigDecimal(max).subtract(new BigDecimal(min));
    }

    public static BigDecimal calculateUnitPrice(double totalAmount ,int num){
        return new BigDecimal(totalAmount).divide(new BigDecimal(num),6, BigDecimal.ROUND_HALF_UP);
    }


    public static BigDecimal calculateMultiply(double one ,double another){
        return new BigDecimal(one).multiply(new BigDecimal(another));
    }

    public static BigDecimal calculateDiscount(double totalProjectAmount_discount, double totalProjectAmount_goods){
        return new BigDecimal(totalProjectAmount_discount)
                .divide(new BigDecimal(totalProjectAmount_goods), 5, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));
    }

    /**
     * 不含税的价格
     * @param unitPrice
     * @param buyNumber
     * @return
     */
    public static BigDecimal calculatePriceWithoutTax(BigDecimal unitPrice, int buyNumber){
        return unitPrice.multiply(new BigDecimal(buyNumber));
    }

    /**
     * 计算单价
     * @param marketprice
     * @param taxRate
     * @return
     */
    public static BigDecimal calculateUnitPrice(double marketprice, double taxRate){
        return new BigDecimal(marketprice).divide(new BigDecimal(taxRate).add(new BigDecimal(1)), 6, BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 计算实际项目金额
     * @param actualAmount
     * @param tax_rate
     * @return
     */
    public static BigDecimal calculatePrdAmount(double actualAmount,  double tax_rate){
        return new BigDecimal(actualAmount).divide(new BigDecimal(tax_rate).add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal calculateDiscountAmount(double actualAmount, double totalGoodsPrice, double tax_rate){
        //实际金额（不含税），实际金额（含税）/1+税率
        BigDecimal noTaxActualMount = new BigDecimal(actualAmount).divide(new BigDecimal(1).add(new BigDecimal(tax_rate)), 8, BigDecimal.ROUND_HALF_UP);
        //(货品金额（不含税） - 实际金额（不含税）)
        return new BigDecimal(totalGoodsPrice).subtract(noTaxActualMount);
    }

    /**
     * 计算税额
     * @param price
     * @param taxRate
     * @return
     */
    public static BigDecimal calculateTaxAmount(double price,double taxRate){
        return new BigDecimal(price).multiply(new BigDecimal(taxRate));
    }


    /**
     * 折扣行信息
     * @param row
     * @param discount
     * @return
     */
    public static StringBuilder buildDiscountMsg(int row, double discount){
        return new StringBuilder("折扣行数").append(row).append("(").append(DigitHelper.appendTailZero(discount,3)).append("%)");
    }

    public static void main(String[] args) {

        System.out.println("calculateDiscount : " + calculateDiscount(180.44,940.17));
        System.out.println(new BigDecimal(-180).abs());
        System.out.println("calculateSubtract :" + calculateSubtract(-180.54, 80.54));
        System.out.println("needCompensate :" + needCompensate(286.32, 48.68, 0.17));
        System.out.println("needCompensate [false]:" + needCompensate(286.32, 48.67, 0.17));

        System.out.println("calDiff " + calDiff(286.32, 48.68, 0.17));

        List<GoodsItemBo> goodsItemBoList = Lists.newArrayList();
        GoodsItemBo bo = new GoodsItemBo();
        bo.setMarketPrice(269.0);
        bo.setBuyNumber(1);
        bo.setSkn(51127118);
        bo.setPrductName("GAWS 魔术贴拖鞋-小魔术贴");
        goodsItemBoList.add(bo);


        List<GoodsItemBo> goodsItemBoList_1 = Lists.newArrayList();
        goodsItemBoList_1.addAll(goodsItemBoList);
        List<GoodsItemBo> goodsItemBoList_2 = Lists.newArrayList();
        goodsItemBoList_2.addAll(goodsItemBoList);

        //多个商品有折扣 有误差
        System.out.println("\n\n多个商品有折扣 有误差");
        System.out.println("goodsItemBoList is : " + JSONArray.toJSONString(goodsItemBoList));
        System.out.println("多个商品有折扣： " + calculateBlueInvoiceAmount(goodsItemBoList, 269, 0.17));
        System.out.println("after calculateBlueInvoiceAmount :"+ JSONArray.toJSONString(goodsItemBoList));

        /*
        //多个商品无折扣
        System.out.println("\n\n多个商品无折扣");
        System.out.println("goodsItemBoList is : " + JSONArray.toJSONString(goodsItemBoList_1));
        System.out.println("多个商品无折扣： " + calculateBlueInvoiceAmount(goodsItemBoList_1, 351, 0.17));
        System.out.println("after calculateBlueInvoiceAmount :" + JSONArray.toJSONString(goodsItemBoList_1));

        //单个skn的商品 有折扣
        System.out.println("\n\n1个商品有折扣");
        bo = new GoodsItemBo();
        bo.setMarketPrice(12);
        bo.setBuyNumber(1);
        bo.setSkn(2);
        bo.setPrductName("b");
        System.out.println("1个商品有折扣： " + calSingleItemBlueInvoice(bo, 11, 0.17));
        System.out.println("after calSingleItemBlueInvoice is : " + bo);
        */
    }
}

package com.yoho.yhorder.invoice.helper;

import com.yoho.yhorder.invoice.model.InvoiceProxy;

/**使用合计金额和合计税额
 * 若只是根据四元组【折扣 金额 ，税额 ； 合计 金额， 税额；】做调整误差，可能无法完成转移；
 * 方法一：可以尝试使用，设定一个阈值，达到多少就不再尝试；
 * 方法一不行，使用所有的项进行差值转移；
 * 还是要有阀值，尝试到多少后停止；
 * 不能无限计算（死循环）
 * 否则CPU占比太高，影响订单
 * 稳妥的办法：发票供应商提供运算约束
 * Created by chenchao on 2016/7/8.
 */
public class Share2SummationStrategy extends CompensateStrategy {


    @Override
    public InvoiceProxy compensateDifference(InvoiceRegionReq req) {



        return null;
    }
}

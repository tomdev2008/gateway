package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.service.model.order.model.PackageBO;
import com.yoho.service.model.order.response.shopping.PromotionFormula;
import com.yoho.yhorder.common.utils.OrderPackageUtils;
import com.yoho.yhorder.common.utils.OrderYmlUtils;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.utils.MathUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wujiexiang on 16/4/23.
 * 运费计算
 */
@Component
public class ShippingCostFormulaCharge {

    public void charge(ChargeContext chargeContext) {
        //#不计算运费
        if (!chargeContext.getChargeParam().isNeedCalcShippingCost()) {
            return;
        }
        //运费公式
        setupShippingCostFormula(chargeContext);
    }


    /**
     * @param chargeContext
     */
    private void setupShippingCostFormula(ChargeContext chargeContext) {

        ChargeTotal chargeTotal = chargeContext.getChargeTotal();

        List<PackageBO> packageList = chargeTotal.getPackageList();

        //1.如果有多个包裹并且运费不为0,显示优惠信息
        int packageNum = packageList.size();
        double lastShippingCost = chargeTotal.getLastShippingCost();
        String shippinCostFormula = "运费";
        if (OrderPackageUtils.canSplitSubOrder(packageNum) && lastShippingCost > 0) {
            String template = OrderYmlUtils.getShippingCostShowTemplate();

            shippinCostFormula = String.format(template, new Object[]{MathUtils.formatCurrencyStr(lastShippingCost * packageNum), MathUtils.formatCurrencyStr(lastShippingCost * (packageNum - 1))});
        }
        PromotionFormula formula = new PromotionFormula("+", shippinCostFormula, MathUtils.formatCurrencyStr(chargeTotal.getLastShippingCost()));
        chargeTotal.getPromotionFormulaList().add(formula);
    }
}

package com.yoho.yhorder.common.utils;

import com.yoho.core.common.utils.YHMath;

/**
 * Created by wujiexiang on 16/4/22.
 * 订单包裹
 */
public interface OrderPackageUtils {

    //拆分包裹最小数量
    int SPLIT_PACKAGE_MIN_NUM = 2;

    static boolean canSplitSubOrder(int packageNum) {
        return packageNum >= SPLIT_PACKAGE_MIN_NUM;
    }

    /**
     * 计算包裹运费,包裹运费 = 运费/ 包裹数量,
     * 前面的运费向上取整,超过运费,取0
     * 如运费为5,包裹数量为3,则包裹运费为 2,2,1
     *
     * @param shippingCost
     * @param packageNum
     * @return
     */
    static double[] caclPackageShippingCost(double shippingCost, int packageNum) {
        if (packageNum <= 0) {
            return null;
        }

        double[] packageShippingCostArray = new double[packageNum];
        double avgShippingCost = MathUtils.round((MathUtils.calcAvg(shippingCost, packageNum)),0);

        double usedAmount = 0;
        for (int i = 0; i < packageNum; i++) {
            if (usedAmount < shippingCost) {
                if (i == packageNum - 1) {
                    packageShippingCostArray[i] = Math.max(0, shippingCost - usedAmount);
                } else {
                    packageShippingCostArray[i] = avgShippingCost;
                    usedAmount = YHMath.add(usedAmount, avgShippingCost);
                }

            } else {
                packageShippingCostArray[i] = 0;
            }
        }
        return packageShippingCostArray;
    }

    public static void main(String[] args) {
        double[] array = caclPackageShippingCost(5, 3);
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }


    }
}

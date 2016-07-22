package com.yoho.yhorder.shopping.charge.promotion.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by chunhua.zhang@yoho.cn on 2015/12/15.
 */
public class BigDecimalHelper {


    /**
     *  to double
     * @return double
     */
    public static double round(BigDecimal toRound){

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.DOWN);

        return Double.valueOf(df.format(toRound));
    }

    /**
     *  to double
     * @param bigDecimal
     * @return double
     */
    public static double toDouble(BigDecimal bigDecimal){

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.DOWN);

        return Double.valueOf(df.format(bigDecimal.doubleValue()));
    }


    /**
     *  to double
     * @param bigDecimal
     * @return double
     */
    public static BigDecimal up(BigDecimal bigDecimal){

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);

        return new BigDecimal(df.format(bigDecimal.doubleValue()));
    }

    public static double upDouble(BigDecimal bigDecimal) {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);
        return Double.valueOf(df.format(bigDecimal.doubleValue()));
    }


}

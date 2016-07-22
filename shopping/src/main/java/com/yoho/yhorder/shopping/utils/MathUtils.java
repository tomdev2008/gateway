package com.yoho.yhorder.shopping.utils;

import com.yoho.core.common.utils.YHMath;
import com.yoho.yhorder.shopping.charge.promotion.impl.BigDecimalHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by JXWU on 2015/11/21.
 */
public class MathUtils {

    private MathUtils() {
    }

    /**
     * 获取价格后的后2位小数点
     *
     * @param price
     * @return
     */
    public static double roundPrice(double price) {
        return BigDecimalHelper.round(BigDecimal.valueOf(price));
    }

    /**
     * 获取价格后的后2位小数点
     *
     * @param price
     * @return
     */
    public static BigDecimal roundPrice(BigDecimal price) {
        return BigDecimal.valueOf(BigDecimalHelper.toDouble(price));
    }

    public static String formatCurrencyStr(double d) {
        DecimalFormat df = new DecimalFormat("#####0.00");
        return Constants.CURRENCY_UNIT_STR + df.format(roundPrice(d));
    }

    /**
     * @param d
     * @param decimals
     * @return
     */
    public static double round(double d, int decimals) {
        return roundPrice(d);
    }

    public static double round(double d) {
        return round(d, 2);
    }

    /**
     * @param d
     * @param precision 保留小数点后几位
     * @return
     */
    public static double numberFormat(double d, int precision) {
        BigDecimal bg = new BigDecimal(d);
        return bg.setScale(precision, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    public static double upToDouble(BigDecimal bigDecimal){

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);
        return Double.valueOf(df.format(bigDecimal.doubleValue()));
    }

    public static int upToInt(double d)
    {
        double ceild = Math.ceil(d);
        DecimalFormat df = new DecimalFormat("0");
        df.setRoundingMode(RoundingMode.UP);
        return Integer.valueOf(df.format(ceild));
    }

    public static int downToInt(double v)
    {
        DecimalFormat df = new DecimalFormat("0");
        df.setRoundingMode(RoundingMode.DOWN);
        return Integer.valueOf(df.format(v));
    }

    public static BigDecimal up(BigDecimal bigDecimal){

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);

        return new BigDecimal(df.format(bigDecimal.doubleValue()));
    }


    /**
     * to double
     *
     * @param bigDecimal
     * @return double
     */
    public static double downToDouble(BigDecimal bigDecimal) {

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.DOWN);

        return Double.valueOf(df.format(bigDecimal.doubleValue()));
    }


    /**
     * 小数减法
     */
    public static double minus(double minuend, double subtrahend) {
        return YHMath.sub(minuend,subtrahend) ;
    }

    /**
     * 小数加法
     */
    public static double addition(double minuend, double subtrahend) {
        return YHMath.add(minuend,subtrahend);
    }


    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0 ? b : a;
    }
}

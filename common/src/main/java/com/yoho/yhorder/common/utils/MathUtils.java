package com.yoho.yhorder.common.utils;

import com.yoho.core.common.utils.YHMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by JXWU on 2015/11/21.
 */
public class MathUtils {

    private static Logger logger = LoggerFactory.getLogger(MathUtils.class);

    // 除法运算默认精度
    private static final int DEF_DIV_SCALE = 20;

    private MathUtils() {
    }

    /**
     * 获取价格后的后2位小数点
     *
     * @param price
     * @return
     */
    public static double roundPrice(double price) {
        BigDecimal bg = new BigDecimal(price);
        //直接取小数点最后两位，不足补0
        double priceToUse = bg.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        DecimalFormat df = new DecimalFormat();
        String style = "0.00";// 定义要显示的数字的格式
        df.applyPattern(style);// 将格式应用于格式化器
        return Double.valueOf(df.format(priceToUse));
    }

    public static String formatCurrencyStr(double d) {

        /***
         * number_format($number, $decimals, '.', '');
         * php 四舍五入 保留小数点最后两位，整数特殊,如5,结果为5.00
         */

        return formatCurrencyStr(d, 2);
    }

    public static String formatCurrencyStr(double d, int decimals) {
        BigDecimal bd = new BigDecimal(d);
        DecimalFormat df = new DecimalFormat("###,##0.00");
        return Constants.CURRENCY_UNIT_STR + df.format(bd.setScale(decimals, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    public static double numberFormat(double d) {
        BigDecimal bg = new BigDecimal(d);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double numberFormat(double d, int precision, int version) {
        if (version >= 5) {
            BigDecimal bg = new BigDecimal(d);
            return bg.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return d;
    }

    /**
     * 四舍五入
     *
     * @param d
     * @param decimals
     * @return
     */
    public static double round(double d, int decimals) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(decimals, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 四舍五入
     *
     * @param d
     * @param precision 保留小数点后几位
     * @return
     */
    public static double numberFormat(double d, int precision) {
        BigDecimal bg = new BigDecimal(d);
        return bg.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /**
     * 小数减法
     */
    public static double minus(double minuend, double subtrahend) {
        return (minuend * 100 - subtrahend * 100) / 100;
    }

    /**
     * 小数加法
     */
    public static double addition(double minuend, double subtrahend) {
        return (minuend * 10 + subtrahend * 10) / 10;
    }

    public static void main(String[] args) {
        BigDecimal bg = new BigDecimal(4.56);
        System.out.println(bg.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    public static double calcAvg(double amount, int num) {
        double avg = 0;
        try {
            avg = YHMath.round(YHMath.div(amount, num * 1.0), BigDecimal.ROUND_HALF_UP);
        } catch (IllegalAccessException e) {
            logger.warn("calc shopping cost avg error,amount is {},num is {}", amount, num);
        }

        return avg;
    }

    public static int upToInt(double d) {
        double ceild = Math.ceil(d);
        DecimalFormat df = new DecimalFormat("0");
        df.setRoundingMode(RoundingMode.UP);
        return Integer.valueOf(df.format(ceild));
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

    public static String formatCurrency(double d) {
        DecimalFormat df = new DecimalFormat("#####0.00");
        return df.format(roundPrice(d));
    }

    public static int toInt(BigDecimal bigDecimal) {
        return bigDecimal.intValue();
    }

    public static BigDecimal mul(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.multiply(b2);
    }

    public static int toInt(double value1, double value2) {
        return toInt(mul(value1, value2));
    }

    /**
     * 精确除法
     * @param scale 精度
     */
    public static double div(double value1, double value2, int scale) throws IllegalAccessException {
        if(scale < 0) {
            throw new IllegalAccessException("精确度不能小于0");
        }
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        // return b1.divide(b2, scale).doubleValue();
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_EVEN).setScale(scale, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    /**
     * 精确除法 使用默认精度
     */
    public static double div(double value1, double value2) throws IllegalAccessException {
        return div(value1, value2, DEF_DIV_SCALE);
    }


    /**
     * 向下获取后2位小数点
     *
     * @param d
     * @return
     */
    public static double down(double d) {
        return toDouble(BigDecimal.valueOf(d));
    }

}

package com.yoho.yhorder.invoice.helper;

import java.text.DecimalFormat;

/**
 * Created by chenchao on 2016/6/18.
 */
public final class DigitHelper {

    /**
     * 数字格式化
     * @param doubleVal
     * @param scale
     * @return
     */
    public static double formatDouble(double doubleVal, int scale){
        StringBuilder scaleFormat = new StringBuilder("0.");
        if (scale<=0){
            scale = 2;
        }
        for(int i=0; i<scale; i++){
            scaleFormat.append("0");
        }

        DecimalFormat format=new DecimalFormat(scaleFormat.toString());

        return Double.valueOf(format.format(doubleVal));
    }


    public static String appendTailZero(double val, int howmuch){

        String val_str = Double.toString(val);
        final String seperator = ".";
        if (val_str.contains(seperator)){
            String[] arry = val_str.split("\\"+seperator);
            int len = arry[1].length();
            return val_str+creatZero(howmuch-len);
        }else{
            return val_str+seperator+creatZero(howmuch);
        }

    }

    private static String creatZero(int scale){
        StringBuilder scaleFormat = new StringBuilder();
        for(int i=0; i<scale; i++){
            scaleFormat.append("0");
        }
        return scaleFormat.toString();
    }

    public static void main(String[] args) {
        double val = 168.54999999999998;
        System.out.println(DigitHelper.formatDouble(val,2));

        System.out.println(DigitHelper.formatDouble(45.444,2));

        System.out.println("3位，是否会补0 ： " + DigitHelper.formatDouble(45.44,3));


        System.out.println(DigitHelper.appendTailZero(234D, 3));
        System.out.println(DigitHelper.appendTailZero(234.2D, 3));
        System.out.println(DigitHelper.appendTailZero(234.23D, 3));
        System.out.println(DigitHelper.appendTailZero(234.233D, 3));
        System.out.println(DigitHelper.appendTailZero(234.23333D, 3));
    }
}

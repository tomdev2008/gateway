package com.yoho.yhorder.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JXWU on 2015/11/17.
 */
public class Constants {

    /**
     * 系统货币单位
     */
    public final static String CURRENCY_UNIT_STR = "¥";

    public final static String EMPTY_STRING = "";

    public final static String YES = "Y";

    public final static String NO = "N";

    //是否限量商品
    public final static String IS_LIMIT_PRODUCT_STR = "Y";

    //预售商品
    public final static String IS_ADVANCE_PRODUCT_STR = "Y";

    //jit
    public final static String IS_JIT_PRODUCT_STR = "Y";

    /**
     * 必须在线支付的二级分类
     *
     * @var array
     */
    public static List<String> MUST_ONLINE_PAYMENT_MISORT_LIST = new ArrayList<>();

    static {
        MUST_ONLINE_PAYMENT_MISORT_LIST.add("259");
    }
    
    //货到付款订单阀值
    public final static double CODPAY_ORDER_AMOUNT_THRESHOLD = 5000;


    public final static String ORDER_SUBMIT_TOPIC = "order.submit";


    public final static  int YOHOCOIN_YUAN__CURRENCY_DILUTION_RATIO = 1;


    public final static  int YOHOCOIN_JIAO_CURRENCY_DILUTION_RATIO = 10;


    public final static  int YOHOCOIN_FEN_CURRENCY_DILUTION_RATIO = 100;


    public final static  int YOHOCOIN_CURRENCY_DILUTION_RATIO = YOHOCOIN_FEN_CURRENCY_DILUTION_RATIO;

    /**
     * 订单属性 正常订单
     */
    public final static int ATTRIBUTE_NORMAL = 1;

    /**
     * 虚拟订单,如电子票
     */
    public final static int ATTRIBUTE_VIRTUAL = 3;


    /**
     * 预售订单
     */
    public final static int ATTRIBUTE_PRESALE = 5;


    /**
     * 虚拟订单
     */
    public final static String ATTRIBUTE_VIRTUAL_STR = "3";


    /**
     * 展览票
     */
    public final static String SHOW_TICKET = "1";

    /**
     * 套票
     */
    public final static String SEASON_TICKET = "2";

}

package com.yoho.yhorder.shopping.utils;

/**
 * Created by JXWU on 2015/11/17.
 */
public class Constants {

    //购物车可以购物的sku最大款数
    public final static int MAX_BUY_SKU_NUMBER = 50;

    //单个sku可以购买的最大数量
    public final static int MAX_BUY_ONE_SKU_NUMBER = 50;

    //预售购物车
    public final static String PRESALE_CART_TYPE = "advance";

    //普通购物车
    public final static String ORDINARY_CART_TYPE = "ordinary";

    //预售购物车
    public final static String ADVANCE_CHARGE_TYPE = "advance";

    //普通购物车
    public final static String ORDINARY_CHARGE_TYPE = "ordinary";

    // 虚拟商品购物车
    public final static String TICKET_CHARGE_TYPE = "ticket";

    //限购码
    public final static String LIMITCODE_CHARGE_TYPE = "limitcode";

    //可用优惠券计算
    public final static String LISTCOUPON_CHARGE_TYPE = "listcoupon";

    //使用优惠券计算
    public final static String USECOUPON_CHARGE_TYPE = "usecoupon";

    public final static int ORDER_GOODS_TYPE_ORDINARY = 1;

    public final static String ORDER_GOODS_TYPE_ORDINARY_STR = "ordinary";

    public final static String ORDER_GOODS_TYPE_ORDINARY_STR_CN = "正常商品";

    public final static int ORDER_GOODS_TYPE_GIFT = 2;

    public final static String ORDER_GOODS_TYPE_GIFT_STR = "gift";

    public final static String ORDER_GOODS_TYPE_GIFT_STR_CN = "赠品";

    public final static int ORDER_GOODS_TYPE_PRICE_GIFT = 3;

    public final static String ORDER_GOODS_TYPE_PRICE_GIFT_STR = "price_gift";

    public final static String ORDER_GOODS_TYPE_PRICE_GIFT_STR_CN = "加价购";

    public final static int ORDER_GOODS_TYPE_OUTLET = 4;

    public final static String ORDER_GOODS_TYPE_OUTLET_STR = "outlet";

    public final static String ORDER_GOODS_TYPE_OUTLET_STR_CN = "outlet";

    public final static int ORDER_GOODS_TYPE_FREE = 5;

    public final static String ORDER_GOODS_TYPE_FREE_STR = "free";

    public final static String ORDER_GOODS_TYPE_FREE_STR_CN = "免单";

    public final static int ORDER_GOODS_TYPE_ADVANCE = 6;

    public final static String ORDER_GOODS_TYPE_ADVANCE_STR = "advance";

    public final static String ORDER_GOODS_TYPE_ADVANCE_STR_CN = "预售";

    public final static int ORDER_GOODS_TYPE_TICKET = 7;

    public final static String ORDER_GOODS_TYPE_TICKET_STR = "ticket";

    public final static String ORDER_GOODS_TYPE_TICKET_STR_CN = "电子票";

    //public final static String PRESALE_ORDER = "advance";

    //是否自选sku
    public final static String IS_SELECTED_Y = "Y";

    public final static String IS_ADVANCE_STR = "Y";

    public final static String IS_OUTLETS_STR = "Y";

    public final static String IS_JIT_STR = "Y";

    public final static String IS_SPECIAL_STR = "Y";

    public final static String IS_PRODUCT_LIMIT_STR = "Y";

    public final static String IS_LIMIT_STR = "Y";

    /**
     * 每个月计算VIP的订单数量限制，如果超过这个数量，则不享受VIP的优惠
     */
    public final static int VIP_CHARGE__ORDER_COUNT_LIMIT = 10;


    /**
     * outlet 满1999金额
     */
    public final static int OUTLET_AMOUNT = 1999;

    /**
     * 货到付款的限额
     */
    public final static int MAX_COD_AMOUNT = 10000;

    /**
     * 运费
     */
    public final static int SHIPPING_COST = 10;

    /**
     * 加急费用
     */
    public final static int FAST_SHOPPING_COST = 5;

    /**
     * 免运费
     */
    public final static int FREE_SHIPPING_LIMIT = 499;

    /**
     * 系统货币单位
     */
    public final static String CURRENCY_UNIT_STR = "¥";


    public final static String NULL_STR = "null";


    /**
     * 新客满 $newCustomerFreeShippingLimit 免运费
     *
     * @var int
     */
    public final static int NEW_CUSTOMER_FREE_SHIPPING_LIMIT = 299;


    /**
     * 数据库表orders user_name varchar(10)
     */
    public final static int ORDERS_TABLE_USER_NAME_FIELD_LENGTH = 10;


    /**
     * 数据库表orders remark varchar(255)
     */
    public final static int ORDERS_TABLE_REMARK_FIELD_LENGTH = 255;

    /**
     * 数据库表orders invoices_payable varchar(100)
     */
    public final static int ORDERS_TABLE_INVOICES_PAYABLE_FIELD_LENGTH = 100;

    /**
     * 取消订单的状态
     */
    public final static String IS_CANCELED_ORDER = "Y";

    public final static String ORDER_EXTATTR_KEY_PRODUCT_SKU_LIST = "product_sku_list";

    public final static int ORDER_SUBMIT_USE_YOHO_COIN_TYPE = 9;

    public final static String NEED_INVOICE_CHAR = "Y";

    public final static String NO_NEED_INVOICE_CHAR = "N";

    /**
     * 正常订单
     */
    public final static int ATTRIBUTE_NORMAL = com.yoho.yhorder.common.utils.Constants.ATTRIBUTE_NORMAL;

    /**
     * 预售订单
     */
    public final static int ATTRIBUTE_PRESALE = com.yoho.yhorder.common.utils.Constants.ATTRIBUTE_PRESALE;

    /**
     * 虚拟订单,电子票
     */
    public final static int ATTRIBUTE_VIRTUAL = com.yoho.yhorder.common.utils.Constants.ATTRIBUTE_VIRTUAL;

    /**
     * 虚拟类型,3门票
     */
    public final static String VIRTUAL_TYPE_TICKET = "3";

    /**
     * 展览票
     */
    public final static String SHOW_TICKET = com.yoho.yhorder.common.utils.Constants.SHOW_TICKET;

    /**
     * 套票
     */
    public final static String SEASON_TICKET = com.yoho.yhorder.common.utils.Constants.SEASON_TICKET;

    /**
     * 虚拟订单,电子票
     */
    public final static String ATTRIBUTE_VIRTUAL_STR = com.yoho.yhorder.common.utils.Constants.ATTRIBUTE_VIRTUAL_STR;


    public static boolean isCartChargeType(String chargeType) {
        return Constants.ORDINARY_CHARGE_TYPE.equals(chargeType) || Constants.ADVANCE_CHARGE_TYPE.equals(chargeType);
    }

    //限购码
    public static boolean isLimitCodeChargeType(String chargeType) {
        return Constants.LIMITCODE_CHARGE_TYPE.equals(chargeType);
    }

    //支持的购物车类型
    public static String[] CART_TYPE_ARRAY_SUPPORT = new String[]{ORDINARY_CART_TYPE, PRESALE_CART_TYPE};

}

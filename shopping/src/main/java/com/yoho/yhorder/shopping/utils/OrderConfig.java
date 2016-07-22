package com.yoho.yhorder.shopping.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by JXWU on 2015/12/3.
 */
public abstract class OrderConfig {

    public static Map<String, Object> ERP_ORDER_DEFAULT_REQUEST_DATA_MAP = new HashMap<String, Object>();

    static {
        ERP_ORDER_DEFAULT_REQUEST_DATA_MAP.put("vip_cutdown_amount", new Double(0));
        ERP_ORDER_DEFAULT_REQUEST_DATA_MAP.put("receipt_time_type", new Integer(2));
        ERP_ORDER_DEFAULT_REQUEST_DATA_MAP.put("shipping_cost", new Double(0));
        ERP_ORDER_DEFAULT_REQUEST_DATA_MAP.put("is_jit", "N");
        ERP_ORDER_DEFAULT_REQUEST_DATA_MAP.put("activities_id", new Integer(0));
        ERP_ORDER_DEFAULT_REQUEST_DATA_MAP.put("redenvelopesnum", new Integer(0));
    }

    public static Set<String> ERP_ORDER_MUST_HAVE_DATA_FIELDS = new HashSet<String>();

    static {
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("uid");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("user_level");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("order_code");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("need_invoice");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("invoice_payable");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("invoice_type");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("yoho_coin_num");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("orders_coupons");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("is_print_price");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("is_contact");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("remark");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("payment_type");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("shipping_manner");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("receipt_time");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("order_type");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("order_referer");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("order_amount");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("last_order_amount");
        ERP_ORDER_MUST_HAVE_DATA_FIELDS.add("attribute");
    }

    public static Set<String> ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS = new HashSet<String>();

    static {
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("product_sku");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("buy_number");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("sale_price");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("real_price");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("last_price");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("get_yoho_coin");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("vip_discount");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("real_vip_price");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("vip_discount_money");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("goods_type");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("is_jit");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("shop_id");
        ERP_ORDER_GOODS_MUST_HAVE_DATA_FIELDS.add("supplier_id");
    }

    public static Set<String> ERP_ORDER_RECEIVER_MUST_DATA_FIELDS = new HashSet<String>();

    static {
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("consignee_name");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("phone");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("mobile");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("area_code");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("address");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("zip_code");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("email");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("province");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("city");
        ERP_ORDER_RECEIVER_MUST_DATA_FIELDS.add("district");
    }

    public static Set<String> ERP_ORDER_FIT_PROMOTIONS_MUST_HAVE_DATA_FIELDS = new HashSet<>();

    static {
        ERP_ORDER_FIT_PROMOTIONS_MUST_HAVE_DATA_FIELDS.add("promotion_id");
        ERP_ORDER_FIT_PROMOTIONS_MUST_HAVE_DATA_FIELDS.add("promotion_title");
        ERP_ORDER_FIT_PROMOTIONS_MUST_HAVE_DATA_FIELDS.add("cutdown_amount");
    }


    public static Set<String> LOCAL_ORDER_MUST_HAVE_DATA_FIELDS = new HashSet<>();

    static {
        LOCAL_ORDER_MUST_HAVE_DATA_FIELDS.add("orderCode");
        LOCAL_ORDER_MUST_HAVE_DATA_FIELDS.add("uid");
        LOCAL_ORDER_MUST_HAVE_DATA_FIELDS.add("orderType");
        LOCAL_ORDER_MUST_HAVE_DATA_FIELDS.add("yohoCoinNum");
        LOCAL_ORDER_MUST_HAVE_DATA_FIELDS.add("remark");
        LOCAL_ORDER_MUST_HAVE_DATA_FIELDS.add("attribute");
        LOCAL_ORDER_MUST_HAVE_DATA_FIELDS.add("amount");
    }

    public static Set<String> LOCAL_ORDER_RECEIVER_MUST_DATA_FIELDS = new HashSet<String>();

    static {
        LOCAL_ORDER_RECEIVER_MUST_DATA_FIELDS.add("mobile");
        LOCAL_ORDER_RECEIVER_MUST_DATA_FIELDS.add("address");
    }


    public static Map<String, Object> LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP = new HashMap<String, Object>();

    static {
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("paymentStatus", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("shippingTypeId", 1);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("expressId", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("userName", "");

        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("receivingTime", 2);
        //todo 'receipt_time' => '工作日、双休日和节假日均送货',
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("exceptionStatus", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("isLock", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("isArrive", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("status", 0);

        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("isCancel", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("cancelType", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("exchangeStatus", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("refundStatus", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("arriveTime", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("shipmentTime", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("isPreContact", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("activitiesId", 0);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("needInvoice", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("invoiceType", 0);

        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("invoicePayable", "");

        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("expressNumber", "0");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("paymentType", 1);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("shippingCost", 0d);
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("isPrintPrice", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("bankCode", "");

        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("isNeedRapid", "N");
        LOCAL_ORDER_DEFAULT_REQUEST_DATA_MAP.put("payment", 0);
    }

    public static Map<String, Object> LOCAL_ORDER_RECEIVER_DEFAULT_REQUEST_DATA_MAP = new HashMap<String, Object>();

    static {
        LOCAL_ORDER_RECEIVER_DEFAULT_REQUEST_DATA_MAP.put("areaCode", "0");
        LOCAL_ORDER_RECEIVER_DEFAULT_REQUEST_DATA_MAP.put("phone", "");
        LOCAL_ORDER_RECEIVER_DEFAULT_REQUEST_DATA_MAP.put("zipCode", "0");
    }
}

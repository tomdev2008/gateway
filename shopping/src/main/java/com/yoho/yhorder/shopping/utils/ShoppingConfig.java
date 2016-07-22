package com.yoho.yhorder.shopping.utils;

import com.yoho.service.model.order.constants.InvoiceContentType;
import com.yoho.service.model.order.response.shopping.ShoppingInvoiceType;

import java.util.*;

/**
 * Created by JXWU on 2015/11/25.
 */
public class ShoppingConfig {

    // rest服务地址
    public final static String PRODUCT_QUERY_PRODUCTDETAIL_BYSKU_URL = "product.queryStorageBySkuId";

    public final static String PRODUCT_QUERY_PRODUCTDETAILINFO_URL = "product.queryProductBasicInfo";

    public final static String PRODUCT_QUERY_PRODUCTSHOPCART_BYSKUIDS_REST_URL = "product.queryProductShopCartBySkuIds";

    public final static String PRODUCT_BATCHUPDATE_STORAGEBYSKUID_REST_URL = "product.batchUpdateStorageBySkuId";

    public final static String PRODUCT_QUERY_CATEGORYBYIDS_REST_URL = "product.queryCategoryByIds";

    public final static String PRODUCT_BATCHQUERY_STORAGEBYSKUIDS_REST_URL = "product.queryStorageBySkuIds";

    public final static String PRODUCT_BATCHADDFAVORITE_REST_URL = "product.batchAddFavorite";

    public final static String PRODUCT_BATCHDECREASESTORAGEBYSKUID_REST_URL = "product.batchDecreaseStorageBySkuId";

    public final static String PRODUCT_QUERYSTORAGENUMBYSKUID_REST_URL ="product.queryStorageNumBySkuId";

    public final static String PROMOTION_QUERY_PRODUCTBUYLIMIT_BYSKNIDS_REST_URL = "promotion.queryProdBuyLimitList";

    public final static String USERS_QUERY_USERDEFAULTADRESS_REST_URL = "users.getDefaultAddress";

    public final static String USERS_QUERY_ADDRESS_REST_URL = "users.getAddress";

    public final static String USERS_QUERY_USERYOHO_REST_URL = "users.getYohoCoin";

    public final static String USERS_QUERY_USERYOHO_FILTERLOGID_REST_URL = "users.getYohoCoinFilterLogId";

    public final static String USERS_UPDATE_YOHOCURRENCY_REST_URL = "users.updateYohoCurrency";

    public final static String USERS_QUERY_USER_LEVEL_REST_URL = "users.getVipDetailInfo";

    public final static String USERS_QUERY_VIP_LEVEL_REST_URL = "users.getVipSimpleInfo";

    public final static String USERS_QUERY_USER_REDENVELOPESCOUNT_REST_URL = "users.selectRedenvelopesCount";

    public final static String USERS_CHANGEYOHOCOIN_REST_URL = "users.changeYohoCoin";

    public final static String USERS_USEREDENVELOPES_REST_URL = "users.useRedenvelopes";

    public final static String PROMOTION_QUERY_PROMOTIONPARAM_REST_URL = "promotion.queryParmListByIds";

    public final static String PROMOTION_QUERYPROMOTIONINFO_REST_LIST = "promotion.queryPromotionInfoList";

    public final static String PROMOTION_QUERY_COUPONPRODLIMIT_REST_URL = "promotion.queryCouponProdLimit";

    public final static String PROMOTION_QUERY_CHECKCOUPON_REST_URL = "promotion.queryCheckedCoupon";

    public final static String PROMOTION_USECOUPON_REST_URL = "promotion.useCoupon";

    public final static String PROMOTION_GET_PROMOITONCODE_REST_URL ="promotion.getPromotionCode";

    public final static String PROMOTION_ADD_PROMOTIONCODEHISTORY_REST_URL = "promotion.addPromotionCodeHistory";

    public final static String PROMOTION_CHECKLIMITCODE_REST_URL = "promotion.checkLimitCode";

    public final static String PROMOTION_ADDLIMITCODEUSERECORD_REST_URL = "promotion.addLimitCodeUseRecord";

    public final static String PROMOTION_QUERY_USERNOUSEDCOUPONS_REST_URL = "promotion.queryUserNoUsedCoupons";

    public final static String SNS_ADDCOMMENTRECORDLIST_REST_URL = "sns.addCommentRecordList";

    public final static String MESSAGE_SAVEINBOX_REST_URL = "message.saveInbox";

    public final static String ORDER_AUDIT_CODPAY_REST_URL = "order.auditCodPay";

    public final static Map<Integer, String> INVOICES_TYPE_MAP = new HashMap<>();

    public final static Map<Integer, String> INVOICE_CONTENT_MAP = new HashMap<>();


    static {
        INVOICES_TYPE_MAP.put(7, "服装");
        INVOICES_TYPE_MAP.put(3, "办公用品");
        INVOICES_TYPE_MAP.put(8, "饰品");
        INVOICES_TYPE_MAP.put(4, "洗涤用品");
        INVOICES_TYPE_MAP.put(9, "配件");
        INVOICES_TYPE_MAP.put(5, "化妆品");
        INVOICES_TYPE_MAP.put(1, "图书");
        INVOICES_TYPE_MAP.put(6, "体育用品");
        INVOICES_TYPE_MAP.put(2, "资料");
        INVOICES_TYPE_MAP.put(10, "数码产品");


        /*
        clothing(7,"服装"),
        books(1,"图书"),
        accessories(9,"配件"),
        daily_necessities(11,"日用品"),
        office_supplies(3,"办公用品"),
        sporting_goods(6,"体育用品"),
        digital_products(10,"数码产品");
        */
        INVOICE_CONTENT_MAP.put(InvoiceContentType.clothing.getIntVal(), InvoiceContentType.clothing.getMsg());
        INVOICE_CONTENT_MAP.put(InvoiceContentType.books.getIntVal(), InvoiceContentType.books.getMsg());
        INVOICE_CONTENT_MAP.put(InvoiceContentType.accessories.getIntVal(), InvoiceContentType.accessories.getMsg());
        INVOICE_CONTENT_MAP.put(InvoiceContentType.daily_necessities.getIntVal(), InvoiceContentType.daily_necessities.getMsg());
        INVOICE_CONTENT_MAP.put(InvoiceContentType.office_supplies.getIntVal(), InvoiceContentType.office_supplies.getMsg());
        INVOICE_CONTENT_MAP.put(InvoiceContentType.sporting_goods.getIntVal(), InvoiceContentType.sporting_goods.getMsg());
        INVOICE_CONTENT_MAP.put(InvoiceContentType.digital_products.getIntVal(), InvoiceContentType.digital_products.getMsg());

    }

    public static List<ShoppingInvoiceType> INVOICES_TYPE_LIST = new ArrayList<ShoppingInvoiceType>(10);
    public static List<ShoppingInvoiceType> INVOICE_CONTENT_LIST = new ArrayList<ShoppingInvoiceType>(7);
    static {

        Set<Integer> invoiceTypes = INVOICES_TYPE_MAP.keySet();
        for (Integer invoiceType : invoiceTypes) {
            INVOICES_TYPE_LIST.add(new ShoppingInvoiceType(invoiceType, INVOICES_TYPE_MAP.get(invoiceType)));
        }
        INVOICE_CONTENT_LIST.add(new ShoppingInvoiceType(InvoiceContentType.clothing.getIntVal(), InvoiceContentType.clothing.getMsg()));
        INVOICE_CONTENT_LIST.add(new ShoppingInvoiceType(InvoiceContentType.books.getIntVal(), InvoiceContentType.books.getMsg()));
        INVOICE_CONTENT_LIST.add(new ShoppingInvoiceType(InvoiceContentType.accessories.getIntVal(), InvoiceContentType.accessories.getMsg()));
        INVOICE_CONTENT_LIST.add(new ShoppingInvoiceType(InvoiceContentType.daily_necessities.getIntVal(), InvoiceContentType.daily_necessities.getMsg()));
        INVOICE_CONTENT_LIST.add(new ShoppingInvoiceType(InvoiceContentType.office_supplies.getIntVal(), InvoiceContentType.office_supplies.getMsg()));
        INVOICE_CONTENT_LIST.add(new ShoppingInvoiceType(InvoiceContentType.sporting_goods.getIntVal(), InvoiceContentType.sporting_goods.getMsg()));
        INVOICE_CONTENT_LIST.add(new ShoppingInvoiceType(InvoiceContentType.digital_products.getIntVal(), InvoiceContentType.digital_products.getMsg()));
    }

    /**
     * 必须在线支付的二级分类
     *
     * @var array
     */
    public static List<String> MUST_ONLINE_PAYMENT_MISORT_LIST = new ArrayList<String>();

    static {
        MUST_ONLINE_PAYMENT_MISORT_LIST.add("259");
    }


    public static Map<String, Integer> ORDERGOODS_TYPE_TO_CODE_MAP = new HashMap<String, Integer>();

    static {
        ORDERGOODS_TYPE_TO_CODE_MAP.put("ordinary", Constants.ORDER_GOODS_TYPE_ORDINARY);//正常商品
        ORDERGOODS_TYPE_TO_CODE_MAP.put("gift", Constants.ORDER_GOODS_TYPE_GIFT);//赠品
        ORDERGOODS_TYPE_TO_CODE_MAP.put("price_gift", Constants.ORDER_GOODS_TYPE_PRICE_GIFT);//加价购
        ORDERGOODS_TYPE_TO_CODE_MAP.put("outlet", Constants.ORDER_GOODS_TYPE_OUTLET);//outlet
        ORDERGOODS_TYPE_TO_CODE_MAP.put("free", Constants.ORDER_GOODS_TYPE_FREE);//免单商品
        ORDERGOODS_TYPE_TO_CODE_MAP.put("advance", Constants.ORDER_GOODS_TYPE_ADVANCE);//预售商品
        ORDERGOODS_TYPE_TO_CODE_MAP.put("ticket", Constants.ORDER_GOODS_TYPE_TICKET);//电子商品
    }


    public static Integer getOrderType(String clientType) {
        if ("iphone".equals(clientType)) {
            return 3;
        } else if ("android".equals(clientType)) {
            return 4;
        } else if ("ipad".equals(clientType)) {
            return 20;
        } else if ("h5".equals(clientType)) {
            return 6;
        } else if ("wechat".equals(clientType)) {
            //微信商城
            return 17;
        } else {
            return 1;
        }
    }

    public final static Map<Integer, String> UNION_MAP = new HashMap<Integer, String>();

    static {
        UNION_MAP.put(1001, "http://o.yiqifa.com/adv/yoho.jsp");
        UNION_MAP.put(1010, "http://service.linktech.cn/purchase_cps.php");
        UNION_MAP.put(1009, "http://count.chanet.com.cn/add_action_ec.cgi");
        UNION_MAP.put(2995, "http://o.yiqifa.com/adv/yoho.jsp");
        UNION_MAP.put(3001, "http://union.fanli.com/dingdan/push?shopid=690");
        UNION_MAP.put(3017, "http://www.duomai.com/api/push/yohobuy.php");
        UNION_MAP.put(3019, "http://www.duomai.com/api/push/myohobuy.php");
        UNION_MAP.put(3057, "http://www.duomai.com/api/push/yohobuyroi.php");
    }


    public static String ERP_ORDER_CREATE_URL = "http://portal.admin.yohobuy.com/api/orderform/create";

    //public static String ERP_ORDER_STATUS_URL = "http://portal.admin.yohobuy.com/api/orders/status";


    //shubmit请求
    public static Map<String, Object> SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP = new HashMap<String, Object>();

    static {
        SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP.put("cart_type", Constants.ORDINARY_CART_TYPE);
        SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP.put("address_id", 0);
        SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP.put("use_yoho_coin", 0);
        SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP.put("use_red_envelopes", 0);
        SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP.put("delivery_way", 1);
        SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP.put("delivery_time", 0);
    }
}

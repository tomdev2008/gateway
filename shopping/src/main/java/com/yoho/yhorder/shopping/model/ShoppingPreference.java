package com.yoho.yhorder.shopping.model;

/**
 * Created by JXWU on 2015/11/25.
 */
public class ShoppingPreference {

//    public ShoppingPreference(String onlinePayment, String cod, int shippingManner, String codPaymentSupport, String rapidExpressSupport, String rapidExpressDelivery) {
//        this.onlinePayment = onlinePayment;
//        this.cod = cod;
//        this.shippingManner = shippingManner;
//        this.codPaymentSupport = codPaymentSupport;
//        this.rapidExpressSupport = rapidExpressSupport;
//        this.rapidExpressDelivery = rapidExpressDelivery;
//    }
    //在线支付
    private String onlinePayment;
    //货到付款 Cash on delivery
    private String cod;

    //配送方式 1快递  2加急
    private int shippingManner;

    //是否支持货到付款
    private String codPaymentSupport;

    //是否支持加急快递
    private String rapidExpressSupport;
    //上门换货
    private String rapidExpressDelivery;

    public String getOnlinePayment() {
        return onlinePayment;
    }

    public void setOnlinePayment(String onlinePayment) {
        this.onlinePayment = onlinePayment;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public int getShippingManner() {
        return shippingManner;
    }

    public void setShippingManner(int shippingManner) {
        this.shippingManner = shippingManner;
    }

    public String getCodPaymentSupport() {
        return codPaymentSupport;
    }

    public void setCodPaymentSupport(String codPaymentSupport) {
        this.codPaymentSupport = codPaymentSupport;
    }

    public String getRapidExpressSupport() {
        return rapidExpressSupport;
    }

    public void setRapidExpressSupport(String rapidExpressSupport) {
        this.rapidExpressSupport = rapidExpressSupport;
    }

    public String getRapidExpressDelivery() {
        return rapidExpressDelivery;
    }

    public void setRapidExpressDelivery(String rapidExpressDelivery) {
        this.rapidExpressDelivery = rapidExpressDelivery;
    }
}

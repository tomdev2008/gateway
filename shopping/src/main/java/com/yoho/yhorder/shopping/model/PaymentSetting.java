package com.yoho.yhorder.shopping.model;

import com.yoho.service.model.order.model.DeliveryAddressBO;
import com.yoho.service.model.order.response.shopping.ShoppingInvoice;

/**
 * Created by JXWU on 2015/12/12.
 * 用户订单支付、快递、地址、发票设置
 */
public class PaymentSetting {
    //在线支付 Y N
    private String onlinePayment = "N";

    //货到付款 Cash on delivery
    private String cod = "N";

    //
    private String receivingTime = "0";


    //配送方式 1快递  2加急
    private int shippingManner = 1;

    //是否支持货到付款
    private String codPaymentSupport = "Y";

    //不能支持货到付款的提示语
    private String codPaymentSupportMessage = "";

    //是否支持加急快递
    private String rapidExpressSupport = "Y";
    //上门换货
    private String rapidExpressDelivery = "Y";

    //快递地址
    private DeliveryAddressBO deliveryAddress;

    //发票
    private ShoppingInvoice shoppingInvoice;

    public String getOnlinePayment() {
        return onlinePayment;
    }

    public void setOnlinePayment(String onlinePayment) {
        this.onlinePayment = onlinePayment;
    }

    public String getReceivingTime() {
        return receivingTime;
    }

    public void setReceivingTime(String receivingTime) {
        this.receivingTime = receivingTime;
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

    public DeliveryAddressBO getDeliveryAddress() {
        return deliveryAddress;
    }

    public ShoppingInvoice getShoppingInvoice() {
        return shoppingInvoice;
    }

    public void setShoppingInvoice(ShoppingInvoice shoppingInvoice) {
        this.shoppingInvoice = shoppingInvoice;
    }

    /**
     * @param deliveryAddress
     */
    public void setDeliveryAddress(DeliveryAddressBO deliveryAddress) {
        this.deliveryAddress = deliveryAddress;

    }

    public String getCodPaymentSupportMessage() {
        return codPaymentSupportMessage;
    }

    public void setCodPaymentSupportMessage(String codPaymentSupportMessage) {
        this.codPaymentSupportMessage = codPaymentSupportMessage;
    }

    /**
     * 根据快递地址信息变更加急和快递方式
     */
    public void resetRapidAndShipping() {
        if (this.deliveryAddress != null) {
            setRapidExpressSupport(this.deliveryAddress.getIs_support());
            setRapidExpressDelivery(this.deliveryAddress.getIs_delivery());
            if ("N".equals(this.deliveryAddress.getIs_support())) {
                setShippingManner(1);
            }
        }
    }
}

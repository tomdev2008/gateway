package com.yoho.yhorder.common.model;

import com.yoho.yhorder.common.utils.PrivacyUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2015/11/30.
 */
public class ERPOrder {
    //
    private Double yoho_coin_num;

    private Double redenvelopesnum;

    private Long order_code;

    //用户UID
    private Integer uid;

    //用户级别
    private Integer user_level;

    //订单金额
    private Double order_amount;

    //最终订单金额
    private Double last_order_amount;

    //最终订单金额 = last_order_amount
    private Double amount;

    //订单类型
    private Integer order_type;

    //是否需要发票
    private String need_invoice;

    //发票类型,老版本的内容
    @Deprecated
    private Integer invoice_type;
    /**
     * 发票类型
     */
    private Integer invoice_types;

    //发票抬头
    private String invoice_payable;

    //使用的YOHO币
    private Double use_yoho_coin;

    //分摊到运费的YOHO币
    private Double yohocoin_shipping_cost;

    //优惠券
    private Coupon orders_coupons;

    //优惠券id
    private Integer coupon_id;

    //优惠券code
    private String coupon_code;

    //优惠券amount
    private Double coupon_amount;


    private String coupon_title;

    //支付方式
    private Integer payment_type;

    //
    private Integer payment;

    //运费
    private Double shipping_cost;

    //送货时间
    private Integer receipt_time;

    //送货时间
    private Integer receipt_time_type;

    //订单来源
    private String order_referer;

    //备注
    private String remark;

    //是否打印
    private String is_print_price;

    //是否提前联系
    private String is_contact;

    //是否加快
    private String is_need_rapid;

    /**
     * 订单属性
     * 1、正常订单
     * 2、
     * 3、虚拟订单
     * 4、
     * 5、预售订单
     * 6、
     * 7、特殊订单
     *
     * @var string
     */
    private Integer attribute;

    //活动
    private Integer activities_id;

    //收货人
    private String consignee_name;

    //手机号
    private String phone;

    //电话
    private String mobile;

    //省
    private String province;

    //市
    private String city;

    //区
    private String district;
    //地址
    private String address;
    //编码
    private String zip_code;
    //邮件地址
    private String email;
    //快递方式
    private Integer shipping_manner;
    //地址编码
    private String area_code;
    private String is_jit;

    private Double vip_cutdown_amount;

    private int promo_id;

    /**
     * 优惠码
     */
    private String promo_code;

    /**
     *优惠码折扣
     */
    private double promo_code_discount;

    /**
     *优惠码折扣金额
     */
    private double promo_code_amount;

    private Map<String, Object> receiver_info = new HashMap<String, Object>();


    private List<ERPPromotion> fit_promotions = new ArrayList<ERPPromotion>();

    private List<ERPOrderGoods> goods_list = new ArrayList<ERPOrderGoods>();

    //父订单
    private long parent_order_code = 0;

    //是否需要拆单,Y 需要 ,N 不需要
    private String is_multi_package ="N";

    //子订单数量
    private int sub_order_num = 0;

    public Double getYoho_coin_num() {
        return yoho_coin_num;
    }

    public void setYoho_coin_num(Double yoho_coin_num) {
        this.yoho_coin_num = yoho_coin_num;
    }

    public Double getRedenvelopesnum() {
        return redenvelopesnum;
    }

    public void setRedenvelopesnum(Double redenvelopesnum) {
        this.redenvelopesnum = redenvelopesnum;
    }

    public Long getOrder_code() {
        return order_code;
    }

    public void setOrder_code(Long order_code) {
        this.order_code = order_code;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getUser_level() {
        return user_level;
    }

    public void setUser_level(Integer user_level) {
        this.user_level = user_level;
    }

    public Double getOrder_amount() {
        return order_amount;
    }

    public void setOrder_amount(Double order_amount) {
        this.order_amount = order_amount;
    }

    public Double getLast_order_amount() {
        return last_order_amount;
    }

    public void setLast_order_amount(Double last_order_amount) {
        this.last_order_amount = last_order_amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getOrder_type() {
        return order_type;
    }

    public void setOrder_type(Integer order_type) {
        this.order_type = order_type;
    }

    public String getNeed_invoice() {
        return need_invoice;
    }

    public void setNeed_invoice(String need_invoice) {
        this.need_invoice = need_invoice;
    }

    public Integer getInvoice_type() {
        return invoice_type;
    }

    public void setInvoice_type(Integer invoice_type) {
        this.invoice_type = invoice_type;
    }

    public String getInvoice_payable() {
        return invoice_payable;
    }

    public void setInvoice_payable(String invoice_payable) {
        this.invoice_payable = invoice_payable;
    }

    public Double getUse_yoho_coin() {
        return use_yoho_coin;
    }

    public void setUse_yoho_coin(Double use_yoho_coin) {
        this.use_yoho_coin = use_yoho_coin;
    }

    public Coupon getOrders_coupons() {
        return orders_coupons;
    }

    public void setOrders_coupons(Coupon orders_coupons) {
        this.orders_coupons = orders_coupons;
    }

    public Integer getCoupon_id() {
        return coupon_id;
    }

    public void setCoupon_id(Integer coupon_id) {
        this.coupon_id = coupon_id;
    }

    public String getCoupon_code() {
        return coupon_code;
    }

    public void setCoupon_code(String coupon_code) {
        this.coupon_code = coupon_code;
    }

    public Double getCoupon_amount() {
        return coupon_amount;
    }

    public void setCoupon_amount(Double coupon_amount) {
        this.coupon_amount = coupon_amount;
    }

    public String getCoupon_title() {
        return coupon_title;
    }

    public void setCoupon_title(String coupon_title) {
        this.coupon_title = coupon_title;
    }

    public Integer getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(Integer payment_type) {
        this.payment_type = payment_type;
    }

    public Integer getPayment() {
        return payment;
    }

    public void setPayment(Integer payment) {
        this.payment = payment;
    }

    public Double getShipping_cost() {
        return shipping_cost;
    }

    public void setShipping_cost(Double shipping_cost) {
        this.shipping_cost = shipping_cost;
    }

    public Integer getReceipt_time() {
        return receipt_time;
    }

    public void setReceipt_time(Integer receipt_time) {
        this.receipt_time = receipt_time;
    }

    public Integer getReceipt_time_type() {
        return receipt_time_type;
    }

    public void setReceipt_time_type(Integer receipt_time_type) {
        this.receipt_time_type = receipt_time_type;
    }

    public String getOrder_referer() {
        return order_referer;
    }

    public void setOrder_referer(String order_referer) {
        this.order_referer = order_referer;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getIs_print_price() {
        return is_print_price;
    }

    public void setIs_print_price(String is_print_price) {
        this.is_print_price = is_print_price;
    }

    public String getIs_contact() {
        return is_contact;
    }

    public void setIs_contact(String is_contact) {
        this.is_contact = is_contact;
    }

    public String getIs_need_rapid() {
        return is_need_rapid;
    }

    public void setIs_need_rapid(String is_need_rapid) {
        this.is_need_rapid = is_need_rapid;
    }

    public Integer getAttribute() {
        return attribute;
    }

    public void setAttribute(Integer attribute) {
        this.attribute = attribute;
    }

    public Integer getActivities_id() {
        return activities_id;
    }

    public void setActivities_id(Integer activities_id) {
        this.activities_id = activities_id;
    }

    public String getConsignee_name() {
        return consignee_name;
    }

    public void setConsignee_name(String consignee_name) {
        this.consignee_name = consignee_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getShipping_manner() {
        return shipping_manner;
    }

    public void setShipping_manner(Integer shipping_manner) {
        this.shipping_manner = shipping_manner;
    }

    public String getArea_code() {
        return area_code;
    }

    public void setArea_code(String area_code) {
        this.area_code = area_code;
    }

    public String getIs_jit() {
        return is_jit;
    }

    public void setIs_jit(String is_jit) {
        this.is_jit = is_jit;
    }

    public List<ERPPromotion> getFit_promotions() {
        return fit_promotions;
    }

    public void setFit_promotions(List<ERPPromotion> fit_promotions) {
        this.fit_promotions = fit_promotions;
    }

    public List<ERPOrderGoods> getGoods_list() {
        return goods_list;
    }

    public void setGoods_list(List<ERPOrderGoods> goods_list) {
        this.goods_list = goods_list;
    }

    public Double getVip_cutdown_amount() {
        return vip_cutdown_amount;
    }

    public void setVip_cutdown_amount(Double vip_cutdown_amount) {
        this.vip_cutdown_amount = vip_cutdown_amount;
    }

    public Map<String, Object> getReceiver_info() {
        return receiver_info;
    }

    public void setReceiver_info(Map<String, Object> receiver_info) {
        this.receiver_info = receiver_info;
    }

    public String getPromo_code() {
        return promo_code;
    }

    public void setPromo_code(String promo_code) {
        this.promo_code = promo_code;
    }

    public double getPromo_code_discount() {
        return promo_code_discount;
    }

    public void setPromo_code_discount(double promo_code_discount) {
        this.promo_code_discount = promo_code_discount;
    }

    public double getPromo_code_amount() {
        return promo_code_amount;
    }

    public void setPromo_code_amount(double promo_code_amount) {
        this.promo_code_amount = promo_code_amount;
    }

    public int getPromo_id() {
        return promo_id;
    }

    public void setPromo_id(int promo_id) {
        this.promo_id = promo_id;
    }


    public void hideMobile(String mobile) {
        String hiddenMobile = PrivacyUtils.mobile(mobile);
        //模糊电话
        this.setMobile(hiddenMobile);
        this.getReceiver_info().put("mobile", hiddenMobile);
    }


    public void unHideMobile(String mobile) {
        this.setMobile(mobile);
        this.getReceiver_info().put("mobile", mobile);
    }

    public long getParent_order_code() {
        return parent_order_code;
    }

    public void setParent_order_code(long parent_order_code) {
        this.parent_order_code = parent_order_code;
    }


    public String getIs_multi_package() {
        return is_multi_package;
    }

    public void setIs_multi_package(String is_multi_package) {
        this.is_multi_package = is_multi_package;
    }

    public int getSub_order_num() {
        return sub_order_num;
    }

    public void setSub_order_num(int sub_order_num) {
        this.sub_order_num = sub_order_num;
    }

    public Integer getInvoice_types() {
        return invoice_types;
    }

    public void setInvoice_types(Integer invoice_types) {
        this.invoice_types = invoice_types;
    }

    public Double getYohocoin_shipping_cost() {
        return yohocoin_shipping_cost;
    }

    public void setYohocoin_shipping_cost(Double yohocoin_shipping_cost) {
        this.yohocoin_shipping_cost = yohocoin_shipping_cost;
    }
    @Override
    public String toString(){
        return ReflectionToStringBuilder.toStringExclude(this,new String[]{"mobile","receiver_info"});
    }
}

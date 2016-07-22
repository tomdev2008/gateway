package com.yoho.yhorder.shopping.model;

import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.common.model.Coupon;
import com.yoho.yhorder.common.model.VirtualInfo;
import com.yoho.yhorder.shopping.charge.model.PromotionCodeChargeResult;
import com.yoho.yhorder.shopping.utils.Constants;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JXWU on 2015/11/30.
 * <p/>
 * 订单接收人、优惠券信息分别在OrderReceiver、Coupon对象属性中
 */
public class Order {

    //订单表id
    private Integer orderId;

    //用户uid
    private Integer uid;

    //使用YOHO币
    private Double useYohoCoin = 0.00;

    //yoho币抵运费
    private double yohoCoinShippingCost = 0.00;

    //订单号
    private Long orderCode;
    //VIP 级别
    private Integer userLevel = 0;

    //订单金额
    private Double orderAmount = 0.00;
    //最终订单金额
    private Double lastOrderAmount = 0.00;

    private Double amount;
    //订单类型(渠道)
    private Integer orderType = 1;

    //是否需要发票
    private String needInvoice = "";

    /**
     * 发票类型,实际是发票内容ID
     */
    @Deprecated
    private Integer invoiceType = 0;
    /**
     * 发票类型 ,引入电子发票后
     */
    private Integer invoiceTypes;
    /**
     * 发票抬头
     *
     * @var string
     */
    @Deprecated
    private String invoicePayable = "";

    private String invoiceHeader;
    /**
     * 收票人电话
     */
    private String receiverMobile;

    /**
     * 发票内容
     */
    private Integer invoiceContent;

    private Double yohoCoinNum;

    /**
     * 优惠金额
     */
    private Double discountAmount = 0.00;


    /**
     * 优惠券使用金额
     */
    private Double couponUseAmount = 0.00;


    /**
     * 优惠券对象
     */
    private Coupon orderCoupon;

    /**
     * 支付类型 1 在线支付，2 货到付款
     */
    private Integer paymentType = 1;

    /**
     * 支付渠道 支付宝 、微信
     */
    private Integer payment = 0;


    /**
     * 运费
     */
    private Double shippingCost;

    /**
     * 送货时间
     */
    private Integer receiptTime = 0;

    /**
     *
     */
    private Integer receiptTimeType = 0;

    /**
     * 订单来源
     *
     * @var string
     */
    private String orderReferer = "";

    /**
     * 备注
     *
     * @var string
     */
    private String remark = "";


    /**
     * 是否打印
     *
     * @var string
     */
    private String isPrintPrice = "N";

    /**
     * 是否提前联系
     *
     * @var string
     */
    private String isPreContact = "N";

    private String isNeedRapid = "N";

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
    private Integer attribute = 1;

    /**
     * 活动ID
     *
     * @var int
     */
    private Integer activitiesId = 0;

    //订单接收人信息
    private OrderReceiver receiver;

    //购物车商品表id
    private List<Integer> shoppingCartItemIds = new ArrayList<Integer>();

    private List<OrderGoods> goodsList;

    //促销信息
    private List<PromotionBO> promotionInfoList;

    /**
     * 是否是JIT
     *
     * @var string
     */
    private String isJit = "N";

    /**
     * 使用红包
     *
     * @var int
     */
    private Double useRedEnvelopes = 0.00;

    /////////////////订单默认值//////////////////////
    //N 未付款 Y 已付款
    private String paymentStatus;

    private Integer shippingTypeId;

    private Integer expressId;

    private String userName;

    private Integer receivingTime;

    private Integer exceptionStatus;

    private String isLock;

    private String isArrive;

    private Integer status;

    private String isCancel;

    private Integer cancelType;

    private Integer exchangeStatus;

    private Integer refundStatus;

    private Integer arriveTime;

    private Integer shipmentTime;

    //TODO 表orders对应的字段,插入时都是默认值，后续是客服来更新的么
    //private String isInvoice;
//    private Integer invoicesType;
//    private String invoicesPayable;

    //快递单号
    private String expressNumber;

    private String bankCode;

    /**
     * 查询一次，订单创建成功后，需要写入到order_default_preferences表
     */
    private UserAddressRspBO userAddressRspBO;

    /**
     * VIP 享受金额
     */
    private double vipCutdownAmount = 0;

    /**
     * 第三方联盟数据
     */
    private String unionData;

    //
    private String clientType;

    //订单入库时间 秒数
    private Integer createTime;

    //货币转换为yoho币的转换比率 ￥ -> yoho币
    private int yohoCoinRatio = 1;

    /**
     * 优惠码信息，包括折扣、折扣金额
     */
    private PromotionCodeChargeResult promotionCodeChargeResult;

    /**
     * 客户端IP，用于黑名单校验
     */
    private String clientIP = null;

    private String chargeType = Constants.ORDINARY_CHARGE_TYPE;

    private List<ShoppingItem> shoppingItems;

    private boolean mustOnlinePayment;

    private String mustOnlinePaymentReason;

    //预售
    private String isAdvance ="N";


    //是否拆分多包
    private String isMultiPackage ="N";

    //子订单数量
    private int subOrderNum = 0;
    
    /**
     * 赠送的有货币
     */
    private int deliverYohoCoin;

    //虚拟信息
    private VirtualInfo virtualInfo;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Double getUseYohoCoin() {
        return useYohoCoin;
    }

    public void setUseYohoCoin(Double useYohoCoin) {
        this.useYohoCoin = useYohoCoin;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
    }

    public Double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Double getLastOrderAmount() {
        return lastOrderAmount;
    }

    public void setLastOrderAmount(Double lastOrderAmount) {
        this.lastOrderAmount = lastOrderAmount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public String getNeedInvoice() {
        return needInvoice;
    }

    public void setNeedInvoice(String needInvoice) {
        this.needInvoice = needInvoice;
    }

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getInvoicePayable() {
        return invoicePayable;
    }

    public void setInvoicePayable(String invoicePayable) {
        this.invoicePayable = invoicePayable;
    }

    public Double getYohoCoinNum() {
        return yohoCoinNum;
    }

    public void setYohoCoinNum(Double yohoCoinNum) {
        this.yohoCoinNum = yohoCoinNum;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getCouponUseAmount() {
        return couponUseAmount;
    }

    public void setCouponUseAmount(Double couponUseAmount) {
        this.couponUseAmount = couponUseAmount;
    }

    public Coupon getOrderCoupon() {
        return orderCoupon;
    }

    public void setOrderCoupon(Coupon orderCoupon) {
        this.orderCoupon = orderCoupon;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getPayment() {
        return payment;
    }

    public void setPayment(Integer payment) {
        this.payment = payment;
    }

    public Double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Integer getReceiptTime() {
        return receiptTime;
    }

    public void setReceiptTime(Integer receiptTime) {
        this.receiptTime = receiptTime;
    }

    public Integer getReceiptTimeType() {
        return receiptTimeType;
    }

    public void setReceiptTimeType(Integer receiptTimeType) {
        this.receiptTimeType = receiptTimeType;
    }

    public String getOrderReferer() {
        return orderReferer;
    }

    public void setOrderReferer(String orderReferer) {
        this.orderReferer = orderReferer;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getIsPrintPrice() {
        return isPrintPrice;
    }

    public void setIsPrintPrice(String isPrintPrice) {
        if (StringUtils.isNotEmpty(isPrintPrice)) {
            this.isPrintPrice = isPrintPrice;
        }
    }

    public String getIsContact() {
        return isPreContact;
    }

    public String getIsNeedRapid() {
        return isNeedRapid;
    }

    public void setIsNeedRapid(String isNeedRapid) {
        this.isNeedRapid = isNeedRapid;
    }

    public Integer getAttribute() {
        return attribute;
    }

    public void setAttribute(Integer attribute) {
        this.attribute = attribute;
    }

    public Integer getActivitiesId() {
        return activitiesId;
    }

    public void setActivitiesId(Integer activitiesId) {
        this.activitiesId = activitiesId;
    }

    public OrderReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(OrderReceiver receiver) {
        this.receiver = receiver;
    }

    public List<OrderGoods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<OrderGoods> goodsList) {
        this.goodsList = goodsList;
    }

    public List<PromotionBO> getPromotionInfoList() {
        return promotionInfoList;
    }

    public void setPromotionInfoList(List<PromotionBO> promotionInfoList) {
        this.promotionInfoList = promotionInfoList;
    }

    public String getIsJit() {
        return isJit;
    }

    public void setIsJit(String isJit) {
        this.isJit = isJit;
    }

    public Double getUseRedEnvelopes() {
        return useRedEnvelopes;
    }

    public void setUseRedEnvelopes(Double useRedEnvelopes) {
        this.useRedEnvelopes = useRedEnvelopes;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getShippingTypeId() {
        return shippingTypeId;
    }

    public void setShippingTypeId(Integer shippingTypeId) {
        this.shippingTypeId = shippingTypeId;
    }

    public Integer getExpressId() {
        return expressId;
    }

    public void setExpressId(Integer expressId) {
        this.expressId = expressId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getReceivingTime() {
        return receivingTime;
    }

    public void setReceivingTime(Integer receivingTime) {
        this.receivingTime = receivingTime;
    }

    public Integer getExceptionStatus() {
        return exceptionStatus;
    }

    public void setExceptionStatus(Integer exceptionStatus) {
        this.exceptionStatus = exceptionStatus;
    }

    public String getIsLock() {
        return isLock;
    }

    public void setIsLock(String isLock) {
        this.isLock = isLock;
    }

    public String getIsArrive() {
        return isArrive;
    }

    public void setIsArrive(String isArrive) {
        this.isArrive = isArrive;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getIsCancel() {
        return isCancel;
    }

    public void setIsCancel(String isCancel) {
        this.isCancel = isCancel;
    }

    public Integer getCancelType() {
        return cancelType;
    }

    public void setCancelType(Integer cancelType) {
        this.cancelType = cancelType;
    }

    public Integer getExchangeStatus() {
        return exchangeStatus;
    }

    public void setExchangeStatus(Integer exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

    public Integer getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(Integer refundStatus) {
        this.refundStatus = refundStatus;
    }

    public Integer getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(Integer arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Integer getShipmentTime() {
        return shipmentTime;
    }

    public void setShipmentTime(Integer shipmentTime) {
        this.shipmentTime = shipmentTime;
    }

    public String getIsPreContact() {
        return isPreContact;
    }

    public void setIsPreContact(String isPreContact) {
        if (StringUtils.isNotEmpty(isPreContact)) {
            this.isPreContact = isPreContact;
        }
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public List<Integer> getShoppingCartItemIds() {
        return shoppingCartItemIds;
    }

    public void setShoppingCartItemIds(List<Integer> shoppingCartItemIds) {
        this.shoppingCartItemIds = shoppingCartItemIds;
    }

    public UserAddressRspBO getUserAddressRspBO() {
        return userAddressRspBO;
    }

    public void setUserAddressRspBO(UserAddressRspBO userAddressRspBO) {
        this.userAddressRspBO = userAddressRspBO;
    }

    public double getVipCutdownAmount() {
        return vipCutdownAmount;
    }

    public void setVipCutdownAmount(double vipCutdownAmount) {
        this.vipCutdownAmount = vipCutdownAmount;
    }

    public String getUnionData() {
        return unionData;
    }

    public void setUnionData(String unionData) {
        this.unionData = unionData;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }


    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public int getYohoCoinRatio() {
        return yohoCoinRatio;
    }

    public void setYohoCoinRatio(int yohoCoinRatio) {
        this.yohoCoinRatio = yohoCoinRatio;
    }

//    public String getIsInvoice() {
//        return isInvoice;
//    }
//
//    public void setIsInvoice(String isInvoice) {
//        this.isInvoice = isInvoice;
//    }

//    public Integer getInvoicesType() {
//        return invoicesType;
//    }
//
//    public void setInvoicesType(Integer invoicesType) {
//        this.invoicesType = invoicesType;
//    }
//
//    public String getInvoicesPayable() {
//        return invoicesPayable;
//    }
//
//    public void setInvoicesPayable(String invoicesPayable) {
//        this.invoicesPayable = invoicesPayable;
//    }

    public PromotionCodeChargeResult getPromotionCodeChargeResult() {
        return promotionCodeChargeResult;
    }

    public void setPromotionCodeChargeResult(PromotionCodeChargeResult promotionCodeChargeResult) {
        this.promotionCodeChargeResult = promotionCodeChargeResult;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getChargeType() {
        return chargeType;
    }

    public void setChargeType(String chargeType) {
        this.chargeType = chargeType;
    }

    public List<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    public void setShoppingItems(List<ShoppingItem> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }

    public boolean isMustOnlinePayment() {
        return mustOnlinePayment;
    }

    public void setMustOnlinePayment(boolean mustOnlinePayment) {
        this.mustOnlinePayment = mustOnlinePayment;
    }

    public String getMustOnlinePaymentReason() {
        return mustOnlinePaymentReason;
    }

    public void setMustOnlinePaymentReason(String mustOnlinePaymentReason) {
        this.mustOnlinePaymentReason = mustOnlinePaymentReason;
    }

    public String getIsAdvance() {
        return isAdvance;
    }

    public void setIsAdvance(String isAdvance) {
        this.isAdvance = isAdvance;
    }

    public String getIsMultiPackage() {
        return isMultiPackage;
    }

    public void setIsMultiPackage(String isMultiPackage) {
        this.isMultiPackage = isMultiPackage;
    }

    public int getSubOrderNum() {
        return subOrderNum;
    }

    public void setSubOrderNum(int subOrderNum) {
        this.subOrderNum = subOrderNum;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, "userAddressRspBO");
    }

	public int getDeliverYohoCoin() {
		return deliverYohoCoin;
	}

	public void setDeliverYohoCoin(int deliverYohoCoin) {
		this.deliverYohoCoin = deliverYohoCoin;
	}

    public Integer getInvoiceContent() {
        return invoiceContent;
    }

    public void setInvoiceContent(Integer invoiceContent) {
        this.invoiceContent = invoiceContent;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public String getInvoiceHeader() {
        return invoiceHeader;
    }

    public void setInvoiceHeader(String invoiceHeader) {
        this.invoiceHeader = invoiceHeader;
    }

    public Integer getInvoiceTypes() {
        return invoiceTypes;
    }

    public void setInvoiceTypes(Integer invoiceTypes) {
        this.invoiceTypes = invoiceTypes;
    }

    public double getYohoCoinShippingCost() {
        return yohoCoinShippingCost;
    }

    public void setYohoCoinShippingCost(double yohoCoinShippingCost) {
        this.yohoCoinShippingCost = yohoCoinShippingCost;
    }

    public VirtualInfo getVirtualInfo() {
        return virtualInfo;
    }

    public void setVirtualInfo(VirtualInfo virtualInfo) {
        this.virtualInfo = virtualInfo;
    }
}
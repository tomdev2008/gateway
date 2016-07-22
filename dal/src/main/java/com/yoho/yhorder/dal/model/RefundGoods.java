package com.yoho.yhorder.dal.model;

import java.math.BigDecimal;

public class RefundGoods extends Base {
    private static final long serialVersionUID = 6420803720050597720L;
    /**
     * 退货ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer uid;

    /**
     * 初始订单号
     */
    private Long initOrderCode;

    /**
     * 源订单号
     */
    private Long sourceOrderCode;

    /**
     * 订单号
     */
    private Long orderCode;

    /**
     * 换货申请ID
     */
    private Integer changePurchaseId;

    /**
     * 退款方式
     * 0、无选择
     * 1、原卡返回
     * 2、银行
     * 3、支付宝
     * 4、YOHO币
     */
    private Byte returnAmountMode;

    /**
     * 退货方式
     */
    private Byte returnMode;

    /**
     * 退运费
     */
    private BigDecimal returnShippingCost;

    /**
     * 退款金额
     */
    private BigDecimal returnAmount;

    /**
     * 是否退券
     */
    private String isReturnCoupon;

    /**
     * 退yoho币
     */
    private Integer returnYohoCoin;

    /**
     * 备注
     */
    private String remark;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 地区编号
     */
    private String areaCode;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String county;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 卡号
     */
    private String bankCard;

    /**
     * 支付宝账号
     */
    private String alipayAccount;

    /**
     * 支付宝用户名
     */
    private String alipayName;

    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String expressNumber;

    /**
     * 快递ID
     */
    private Integer expressId;

    /**
     * 驳回
     * 91 客服驳回
     * 92 物流驳回
     */
    private Byte reject;

    /**
     * erp退货申请ID
     */
    private Integer erpRefundId;

    /**
     * 状态
     * 0   提交
     * 10 审核通过
     * 20 商品寄回
     * 30 已入库
     * 40 付款结束
     * 91 客服拒退
     */
    private Byte status;

    /**
     * 创建日期
     */
    private Integer createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Long getInitOrderCode() {
        return initOrderCode;
    }

    public void setInitOrderCode(Long initOrderCode) {
        this.initOrderCode = initOrderCode;
    }

    public Long getSourceOrderCode() {
        return sourceOrderCode;
    }

    public void setSourceOrderCode(Long sourceOrderCode) {
        this.sourceOrderCode = sourceOrderCode;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getChangePurchaseId() {
        return changePurchaseId;
    }

    public void setChangePurchaseId(Integer changePurchaseId) {
        this.changePurchaseId = changePurchaseId;
    }

    public Byte getReturnAmountMode() {
        return returnAmountMode;
    }

    public void setReturnAmountMode(Byte returnAmountMode) {
        this.returnAmountMode = returnAmountMode;
    }

    public Byte getReturnMode() {
        return returnMode;
    }

    public void setReturnMode(Byte returnMode) {
        this.returnMode = returnMode;
    }

    public BigDecimal getReturnShippingCost() {
        return returnShippingCost;
    }

    public void setReturnShippingCost(BigDecimal returnShippingCost) {
        this.returnShippingCost = returnShippingCost;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public String getIsReturnCoupon() {
        return isReturnCoupon;
    }

    public void setIsReturnCoupon(String isReturnCoupon) {
        this.isReturnCoupon = isReturnCoupon == null ? null : isReturnCoupon.trim();
    }

    public Integer getReturnYohoCoin() {
        return returnYohoCoin;
    }

    public void setReturnYohoCoin(Integer returnYohoCoin) {
        this.returnYohoCoin = returnYohoCoin;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName == null ? null : payeeName.trim();
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode == null ? null : areaCode.trim();
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county == null ? null : county.trim();
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName == null ? null : bankName.trim();
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard == null ? null : bankCard.trim();
    }

    public String getAlipayAccount() {
        return alipayAccount;
    }

    public void setAlipayAccount(String alipayAccount) {
        this.alipayAccount = alipayAccount == null ? null : alipayAccount.trim();
    }

    public String getAlipayName() {
        return alipayName;
    }

    public void setAlipayName(String alipayName) {
        this.alipayName = alipayName == null ? null : alipayName.trim();
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany == null ? null : expressCompany.trim();
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber == null ? null : expressNumber.trim();
    }

    public Integer getExpressId() {
        return expressId;
    }

    public void setExpressId(Integer expressId) {
        this.expressId = expressId;
    }

    public Byte getReject() {
        return reject;
    }

    public void setReject(Byte reject) {
        this.reject = reject;
    }

    public Integer getErpRefundId() {
        return erpRefundId;
    }

    public void setErpRefundId(Integer erpRefundId) {
        this.erpRefundId = erpRefundId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }
}
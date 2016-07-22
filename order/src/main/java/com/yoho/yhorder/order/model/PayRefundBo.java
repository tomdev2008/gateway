package com.yoho.yhorder.order.model;

public class PayRefundBo {
	//退款申请是否成功
	private int refundStatus;
	private String refundMsg;
	
	//退款前检查成功与否？
	private boolean refundValid = true;
	
	private long orderCode;
	
	//订单原始金额
	private double orderTotalFee;
	
	//退款单号
	private String refundOrderCode;
	
	private int payment;
	
	//本次退款金额
	private double amount;
	
	private String serialNo;
	
	public int getRefundStatus() {
		return refundStatus;
	}
	public void setRefundStatus(int refundStatus) {
		this.refundStatus = refundStatus;
	}
	public String getRefundMsg() {
		return refundMsg;
	}
	public void setRefundMsg(String refundMsg) {
		this.refundMsg = refundMsg;
	}

	public String getRefundOrderCode() {
		return refundOrderCode;
	}
	public void setRefundOrderCode(String refundOrderCode) {
		this.refundOrderCode = refundOrderCode;
	}
	public long getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(long orderCode) {
		this.orderCode = orderCode;
	}
	public int getPayment() {
		return payment;
	}
	public void setPayment(int payment) {
		this.payment = payment;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public double getOrderTotalFee() {
		return orderTotalFee;
	}
	public void setOrderTotalFee(double orderTotalFee) {
		this.orderTotalFee = orderTotalFee;
	}
	public boolean isRefundValid() {
		return refundValid;
	}
	public void setRefundValid(boolean refundValid) {
		this.refundValid = refundValid;
	}
	
}

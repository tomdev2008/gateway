package com.yoho.yhorder.order.model;

/**
 * 蚂蚁花呗分期参数
 * @author yoho
 *
 */
public class AntInstallmentVO {
	//分期期数
	private int stageNumber;
	
	//税率百分比
	private double taxRate;
	
	//卖家承担收费比例
	private int sellerPercent;

	public int getStageNumber() {
		return stageNumber;
	}

	public void setStageNumber(int stageNumber) {
		this.stageNumber = stageNumber;
	}

	public int getSellerPercent() {
		return sellerPercent;
	}

	public void setSellerPercent(int sellerPercent) {
		this.sellerPercent = sellerPercent;
	}

	public double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}
	
    @Override  
    public String toString() {  
        return "AntInstallmentVO [stageNumber=" + stageNumber + ", taxRate=" + taxRate 
        		+ ", sellerPercent=" + sellerPercent + "]";  
    }  
}

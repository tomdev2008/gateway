package com.yoho.yhorder.dal.domain;

import com.yoho.core.common.helpers.ImagesHelper;

/**
 * 待晒单商品数据
 */
public class ShareOrderGoodsInfo {

    /**
     * 订单编号
     */
    private String orderId;
    /**
     * 订单编号
     */
    private String orderCode;
    /**
     * 商品id
     */
    private String productId;
    /**
     * skc id
     */
    private String goodsId;
    /**
     * 精确到尺码的id
     */
    private String erpSkuId;

    private String productSkn;
    /**
     * 图片地址
     */
    private String imageUrl;
    /**
     * 奖励状态
     */
    private int rewardStatus;
    /**
     * 商品名称
     */
    private String goodsName;
    
    /**
     * 订单创建时间
     */
    private Integer createTime;


    public String getProductSkn() {
        return productSkn;
    }

    public void setProductSkn(String productSkn) {
        this.productSkn = productSkn;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getErpSkuId() {
        return erpSkuId;
    }

    public void setErpSkuId(String erpSkuId) {
        this.erpSkuId = erpSkuId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getImageUrl() {
        return ImagesHelper.template(imageUrl, ImagesHelper.SYS_BUCKET.get(ImagesHelper.SYS_PASSPORT_NAME), 1);
    }

    public String getImageOrigUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getRewardStatus() {
        return rewardStatus;
    }

    public void setRewardStatus(int rewardStatus) {
        this.rewardStatus = rewardStatus;
    }

    public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	@Override
    public String toString() {
        return "ShareOrderGoodsInfo{" +
                "orderId='" + orderId + '\'' +
                "orderCode='" + orderCode + '\'' +
                ", productId='" + productId + '\'' +
                ", goodsId='" + goodsId + '\'' +
                ", erpSkuId='" + erpSkuId + '\'' +
                ", productSkn='" + productSkn + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", rewardStatus=" + rewardStatus +
                ", goodsName='" + goodsName + '\'' +
                '}';
    }

	public Integer getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Integer createTime) {
		this.createTime = createTime;
	}
}

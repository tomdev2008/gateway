package com.yoho.yhorder.shopping.charge.model;

import com.yoho.service.model.promotion.CouponsBo;

import java.util.List;

/**
 * Created by wujiexiang on 16/4/7.
 */
public class CouponWapper {

    private int uid;
    
    private boolean isNewUser;
    /**
     * 用户等级
     */
    private int userLevel;
    /**
     * 优惠券对象
     */
    private CouponsBo couponsBo;
    /**
     * 满足优惠券使用的商品列表
     */
    private List<ChargeGoods> goodsList;
    
    /**
     * 优惠券是否可用
     */
    private boolean isUsable = false;

    public CouponsBo getCouponsBo() {
        return couponsBo;
    }

    public void setCouponsBo(CouponsBo couponsBo) {
        this.couponsBo = couponsBo;
    }

    public List<ChargeGoods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<ChargeGoods> goodsList) {
        this.goodsList = goodsList;
    }

    public boolean isUsable() {
        return isUsable;
    }

    public void setUsable(boolean usable) {
        isUsable = usable;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

	public int getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	public boolean isNewUser() {
		return isNewUser;
	}

	public void setNewUser(boolean isNewUser) {
		this.isNewUser = isNewUser;
	}
}

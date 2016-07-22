package com.yoho.yhorder.dal.model;


/**
 * 赠送有货币数据库model
 * @author mali
 *
 */
public class OrdersYohoCoin {
	/**
	 * 需要增送的状态
	 */
	public static final Integer STATUS_NEED_DELIVER = 0;
	
	/**
	 * 已经赠送的状态
	 */
	public static final Integer STATUS_ALREADY_DELIVER = 1;
	
	/**
	 * 已经取消的订单，不再赠送的
	 */
	public static final Integer STATUS_CACEL_DELIVER = 2;
	
    private Integer id;

    private Long orderCode;

    /**
     *  赠送有货币数目，已稀释
     */
    private Integer yohoCoinNum;

    /**
     * 0 代表 需要赠送 1代表已赠送 2代表订单取消不赠送
     * 默认0
     */
    private Integer status;

    private Integer createTime;
    
    /**
     * 用户Id
     */
    private Integer uid;
    
    /**
     * 赠送的时间
     */
    private Integer deliverTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYohoCoinNum() {
        return yohoCoinNum;
    }

    public void setYohoCoinNum(Integer yohoCoinNum) {
        this.yohoCoinNum = yohoCoinNum;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

	@Override
	public String toString() {
		return "OrdersYohoCoin [id=" + id + ", orderCode=" + orderCode
				+ ", yohoCoinNum=" + yohoCoinNum + ", status=" + status
				+ ", createTime=" + createTime + ", uid=" + uid
				+ ", deliverTime=" + deliverTime + "]";
	}

	public Long getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(Long orderCode) {
		this.orderCode = orderCode;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Integer getDeliverTime() {
		return deliverTime;
	}

	public void setDeliverTime(Integer deliverTime) {
		this.deliverTime = deliverTime;
	}
}
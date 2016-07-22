package com.yoho.yhorder.order.config;

/**
 * Created by CaoQi on 2015/10/28.
 * 状态的枚举类
 * SUCCESS 成功 值：0
 */

public enum OrderChangeNewDeliveryType {


    ORDER_CHANGE_DELIVERY_TYPE_10("您的换货申请已通过审核", 10, "请将商品连同发货单准备好一起交给快递员"),
    ORDER_CHANGE_DELIVERY_TYPE_30("您寄回的商品已收到", 30, ""),
    ORDER_CHANGE_DELIVERY_TYPE_20("快递员已出发", 20, "请将商品连同发货单一起交给快递员。"),
    ORDER_CHANGE_DELIVERY_TYPE_50("换货已完成", 50, "");


    OrderChangeNewDeliveryType(String title, int id, String remark) {
        this.title = title;
        this.id = id;
        this.remark = remark;
    }

    private String title;
    private int id;
    private String remark;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public static void main(String[] args) {
        for (OrderChangeNewDeliveryType s : OrderChangeNewDeliveryType.values())
            System.out.println(s + ", ordinal " + s.getId());
    }
}

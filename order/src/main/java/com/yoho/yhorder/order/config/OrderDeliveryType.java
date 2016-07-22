package com.yoho.yhorder.order.config;

/**
 * Created by CaoQi on 2015/10/28.
 * 状态的枚举类
 * SUCCESS 成功 值：0
 */

public enum OrderDeliveryType {


    ORDER_CHANGE_DELIVERY_TYPE_0("您的换货申请已成功提交！", 0, "请您耐心等待审核"),
    ORDER_CHANGE_DELIVERY_TYPE_10("您的换货申请已通过审核，请把相关货品寄回至：", 10, "南京市江宁区江宁经济技术开发区苏源大道87号YOHO!有货物流中心东一楼 邮编：211106 收件人：YOHO!有货仓库 联系电话：400-889-9646"),
    ORDER_CHANGE_DELIVERY_TYPE_30("您寄回的商品已收到", 30, "请您耐心等待换货商品发出"),
    ORDER_CHANGE_DELIVERY_TYPE_40("您寄回的商品已收到", 40, "请您耐心等待换货商品发出"),
    ORDER_CHANGE_DELIVERY_TYPE_50("换货商品已发出", 50, "请关注“我的订单”中新生成的换货订单。"),
    ORDER_CHANGE_DELIVERY_TYPE_91("您的换货申请已取消", 91, "如有疑问请您联系在线客服"),
    ORDER_CHANGE_DELIVERY_TYPE_20("商品寄回物流信息", 20, "");

    OrderDeliveryType(String title, int id, String remark) {
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


    public static  void main (String args[]){

    }

}

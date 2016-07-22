package com.yoho.yhorder.common.constants;

/**
 * Created by wujiexiang on 16/4/18.
 * erp侧订单状态
 */
public interface ErpOrderStatus {

    //用户取消
    int CANCLE_BY_USER = 900;

    //客服取消
    int CANCLE_BY_CS = 901;

    //系统自动取消
    int AUTO_CANCLE_BY_SYSTEM = 906;
}

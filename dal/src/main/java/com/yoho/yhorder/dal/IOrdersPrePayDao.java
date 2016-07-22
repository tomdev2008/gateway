package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrdersPrePay;

public interface IOrdersPrePayDao {

    //无记录则插入，有记录则更新
    int insertOnDuplicateUpdate(OrdersPrePay record);

    OrdersPrePay selectByPrimaryKey(Long orderCode);


}
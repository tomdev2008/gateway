package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrdersPayBank;

public interface IOrdersPayBankDAO {

    int insert(OrdersPayBank record);

    OrdersPayBank selectByPrimaryKey(Integer id);
    
    OrdersPayBank selectByOrderCode(Long orderCode);

    int updateByPrimaryKeySelective(OrdersPayBank record);

    int updateByPrimaryKey(OrdersPayBank record);
    
    int updateByOrderCodeSelective(OrdersPayBank record);
}
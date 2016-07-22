package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ExpressOrders;

public interface IExpressOrdersMapper {

    int insert(ExpressOrders record);

    int insertSelective(ExpressOrders record);

    ExpressOrders selectByExpressOrders(ExpressOrders record);

    int updateByPrimaryKeySelective(ExpressOrders record);

    int updateByPrimaryKey(ExpressOrders record);
}
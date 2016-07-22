package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrderCancel;

public interface OrderCancelMapper {
    int deleteByPrimaryKey(Long orderCode);

    int insert(OrderCancel record);

    int insertSelective(OrderCancel record);

    OrderCancel selectByPrimaryKey(Long orderCode);

    int updateByPrimaryKeySelective(OrderCancel record);

    int updateByPrimaryKey(OrderCancel record);
}
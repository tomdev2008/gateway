package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrderPreferences;

public interface IOrderPreferencesDAO {
    int deleteByPrimaryKey(Integer uid);

    int insert(OrderPreferences record);

    int insertSelective(OrderPreferences record);

    OrderPreferences selectByPrimaryKey(Integer uid);

    int updateByPrimaryKeySelective(OrderPreferences record);

    int updateByPrimaryKey(OrderPreferences record);
}
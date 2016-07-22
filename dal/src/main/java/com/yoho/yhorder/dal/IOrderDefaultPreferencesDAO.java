package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrderDefaultPreferences;
import org.apache.ibatis.annotations.Param;

public interface IOrderDefaultPreferencesDAO {
    OrderDefaultPreferences selectByPrimaryKey(Integer uid);

    int insertDefaultPreferences(@Param("uid") Integer uid, @Param("orderDefaultPreferences") String orderDefaultPreferences);
}
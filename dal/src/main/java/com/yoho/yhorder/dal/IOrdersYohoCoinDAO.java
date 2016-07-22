package com.yoho.yhorder.dal;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.yoho.yhorder.dal.model.OrdersYohoCoin;

/**
 * 赠送有货币数据库Dao
 * @author mali
 *
 */
public interface IOrdersYohoCoinDAO {
    int deleteByPrimaryKey(Integer id);

    int insert(OrdersYohoCoin record);

    int insertSelective(OrdersYohoCoin record);

    List<OrdersYohoCoin> selectTop100List(@Param("ordersYohoCoin")OrdersYohoCoin ordersYohoCoin, @Param("createTime")Integer createTime);

    int updateByPrimaryKeySelective(OrdersYohoCoin record);

    int updateByPrimaryKey(OrdersYohoCoin record);
}
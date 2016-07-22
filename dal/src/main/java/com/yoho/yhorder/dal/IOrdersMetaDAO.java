package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrdersMeta;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IOrdersMetaDAO {

    int insert(OrdersMeta record);

    int insertBatch(@Param("records") List<OrdersMeta> records);

    List<OrdersMeta> selectByOrdersIdsAndMetaKey(@Param("ordersIds") List<Integer> ordersIds, @Param("metaKey") String metaKey);

    OrdersMeta selectByPrimaryKey(Integer id);

    int updateByPrimaryKey(OrdersMeta record);

    OrdersMeta selectByOrdersIdAndMetaKey(@Param("ordersId") Integer ordersId, @Param("metaKey") String metaKey);

    int updateMetaValueByOrdersIdsAndMetaKey(@Param("ordersIds") List<Integer> ordersIds, @Param("metaKey") String metaKey, @Param("metaValue") String metaValue);

}
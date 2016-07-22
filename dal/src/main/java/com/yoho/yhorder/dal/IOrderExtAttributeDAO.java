package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrderExtAttribute;
import com.yoho.yhorder.dal.model.OrderPay;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2016/2/16.
 */
public interface IOrderExtAttributeDAO {
    int insert(@Param("orderCode") Long orderCode, @Param("uid") int uid, @Param("attribute") String attribute);

    String selectExtAttributeByOrderCodeAndUid(@Param("orderCode") Long orderCode, @Param("uid") int uid);

    List<OrderExtAttribute> selectByUidAndOrderCodes(@Param("uid") int uid, @Param("orderCodes") List<Long> orderCodes);
}
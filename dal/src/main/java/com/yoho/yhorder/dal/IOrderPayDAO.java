package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrderPay;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2016/1/23.
 */
public interface IOrderPayDAO {
    int insert(@Param("orderCode") Long orderCode, @Param("uid") int uid, @Param("paymentId") int paymentId);

    int selectCountByOrderCode(@Param("orderCode") Long orderCode);

    List<OrderPay> selectByOrderCodes(@Param("orderCodes") List<Long> orderCodes);
}

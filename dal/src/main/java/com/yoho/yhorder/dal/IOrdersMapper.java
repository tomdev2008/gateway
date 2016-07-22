package com.yoho.yhorder.dal;


import com.yoho.service.model.order.response.Orders;
import com.yoho.yhorder.dal.domain.CustomerServiceAddress;
import com.yoho.yhorder.dal.domain.DeliveryAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IOrdersMapper {

    int insert(Orders record);

    int insertSelective(Orders record);


    /**
     * @deprecated 在4.8版本删除
     */
    Orders selectByPrimaryKey(Integer id);

    /**
     * @deprecated 在4.8版本删除
     */
    List<Orders> selectByPrimaryKeys(@Param("ids") List<Integer> ids);

    /**
     * @deprecated 在4.8版本删除
     */
    Orders selectByOrderCode(String orderCode);

    Orders selectByUidAndOrderCode(@Param("uid") long uid, @Param("orderCode") long orderCode);

    Orders selectByCodeAndUid(@Param("orderCode") String orderCode, @Param("uid") String uid);

    Orders selectByCodeAndUidNoStatus(@Param("orderCode") String orderCode, @Param("uid") String uid);

    /**
     * @deprecated 在4.8版本删除
     */
    int updateByPrimaryKeySelective(Orders record);

    int selectCountByUidAndStatus(@Param("uid") Integer uid, @Param("status") List<Integer> status);

    int selectCountByUid(@Param("uid") Integer uid);

    int selectCountByUidAndStatusAndPaymentStatusAndIsCancel(@Param("uid") Integer uid,
                                                             @Param("status") List<Integer> status,
                                                             @Param("paymentStatus") String paymentStatus,
                                                             @Param("isCancel") String isCancel);

    List<Orders> selectByUidAndStatusAndPaymentStatusAndIsCancel(@Param("uid") Integer uid,
                                                                 @Param("status") List<Integer> status,
                                                                 @Param("paymentStatus") String paymentStatus,
                                                                 @Param("isCancel") String isCancel, @Param("start") int start, @Param("limit") int limit);

    int selectCountByUidAndPaymentStatusAndPaymentTypeAndIsCancel(@Param("uid") Integer uid,
                                                                  @Param("paymentStatus") String paymentStatus,
                                                                  @Param("paymentType") Byte paymentType,
                                                                  @Param("isCancel") String isCancel);

    int selectCurrentMonthOrderCount(Integer uid);

    /**
     * 查询用户最后一条订单
     *
     * @param uid
     * @return
     */
    Orders selectLastOrderByUid(Integer uid);

    /**
     * 创建购物车订单
     *
     * @param order
     * @return
     */
    int insertShoppingOrder(Orders order);

    /**
     * 获取除OrdersCode以外的妥投订单
     *
     * @param uid
     * @param orderCode
     * @return
     */
    Integer selectCountOrderByUid(@Param("uid") Integer uid, @Param("orderCode") Long orderCode);


    /**
     * 检查用户是否老用户
     */
    Integer selectUserTypeByOrders(@Param("uid") Integer uid, @Param("status") Integer status, @Param("startTime") Integer startTime);

    List<Orders> getBatchByOrdercode(@Param("paramsList") List<Map<String, String>> paramsList);


    int selectOrdersNumByStatus(@Param("uid") Integer uid);


    int selectNewUserOrderCountForPromotionCode(@Param("uid") Integer uid);

    /**
     * @param startTime
     * @param endTime
     * @return
     * @deprecated 在4.8版本删除
     */
    Integer selectCountByCreateTimeBetween(@Param("startTime") Integer startTime, @Param("endTime") Integer endTime);

    /**
     * @param startTime
     * @param endTime
     * @param start
     * @param size
     * @return
     * @deprecated 在4.8版本删除
     */
    List<Long> selectOrderCodeByCreateTimeBetween(@Param("startTime") Integer startTime, @Param("endTime") Integer endTime, @Param("start") int start, @Param("size") int size);


    /**
     * @param uids
     * @param status
     * @param shipmentTimeStart
     * @param shipmentTimeEnd
     * @return
     */
    List<Orders> selectByUidsAndStatusGreaterThanAndShipmentTimeBetween(@Param("uids") List<Integer> uids, @Param("status") Integer status, @Param("shipmentTimeStart") Integer shipmentTimeStart, @Param("shipmentTimeEnd") Integer shipmentTimeEnd);

    /**
     * 无关联订单,修改收货人地址
     *
     * @deprecated 在4.8版本删除
     */
    int updateByOrderCodeSelective(DeliveryAddress deliveryAddress);

    /**
     * JIT拆单和预售商品拆单,有关联订单,批量修改收货人地址
     *
     * @deprecated 在4.8版本删除
     */
    int updateByParentOrderCodeSelective(DeliveryAddress deliveryAddress);

    int updateBatchByOrderCodeSelective(@Param("list") List<CustomerServiceAddress> productPayDeliveryList);

    /**
     * 根据主订单号获取所有子订单
     *
     * @param orderCode
     * @return
     * @deprecated 在4.8版本删除
     */
    List<Orders> selectByParentOrderCode(String orderCode);

    /**
     * 更新订单已拆分
     *
     * @param orderCode
     * @return
     */
    int updateOrderAlreadySplit(@Param("orderCode") long orderCode, @Param("uid") int uid);

    /**
     * 根据父订单号批量更新子订单的取消状态
     *
     * @param parentOrderCode
     * @deprecated 在4.8版本删除
     */
    int updateSubOrderCancelStatusByParentCode(@Param("parentOrderCode") long parentOrderCode, @Param("cancelType") int cancelType);
}
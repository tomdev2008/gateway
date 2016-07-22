package com.yoho.yhorder.dal;


import com.yoho.service.model.order.request.OrderGoodsRequest;
import com.yoho.service.model.order.request.OrderGoodsUidRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.yhorder.common.page.Page;
import com.yoho.yhorder.dal.domain.OrdersPrice;
import com.yoho.yhorder.dal.domain.ShareOrderGoodsInfo;
import com.yoho.yhorder.dal.domain.UserOrdersGoodsStat;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * OrdersGoodsMapper
 */
public interface IOrdersGoodsMapper {

    /**
     * 批量插入
     *
     * @param goodsList
     * @return
     */
    int batchInsertOrderGoods(@Param("list") List<OrdersGoods> goodsList);

    OrdersGoods selectByPrimaryKey(Integer id);

    List<OrdersPrice> selectOrdersPriceByOrderIds(@Param("orderIds") List<Integer> orderIds);

    List<OrdersGoods> selectOrderGoodsByOrderId(List<Integer> orderIdList);

    List<OrdersGoods> selectByOrderIdInAndGoodsTypeIn(@Param("orderIds") List<Integer> orderIds, @Param("goodsTypes") List<Integer> goodsTypes);

    List<OrdersGoods> selectOrderByGoodsId(OrderGoodsUidRequest orderGoodsUidRequest);

    //查询订单列表
    List<OrdersGoods> selectOrderGoodsByOrderList(List<OrderGoodsRequest> order);

    List<OrdersGoods> selectOrderGoodsByOrder(Orders order);

    List<Map<String, Object>> selectNumByUidAndSkn(@Param("uid") Integer uid, @Param("list") List<Integer> skns);

    List<OrdersGoods> selectGoodsBySku(@Param("paramsList") List<Map<String, String>> paramsList);

    List<OrdersGoods> selectByUidAndSku(@Param("uid") Integer uid, @Param("productSku") Integer productSku);


    List<Integer> selectByProductId(@Param("uid") Integer uid, @Param("productId") Integer productId);

    List<UserOrdersGoodsStat> selectUserOrdersGoodsStatByUidsAndStatusGreaterThanAndShipmentTimeLessThan(@Param("uids") List<Integer> uids, @Param("status") Integer status, @Param("shipmentTime") Integer shipmentTime);

    /**
     * 查询sku的购物数量
     * @param uid
     * @param productSku
     * @return
     */
    int selectSkuBuyNumberByUidAndSku(@Param("uid") Integer uid, @Param("productSku") Integer productSku);

    /**
     * 通过用户订单号查询订单的商品信息
     * @param uid
     * @param orderCode
     * @return
     */
    List<ShareOrderGoodsInfo> selectOrderGoodsListByOrderCode(@Param("uid")int uid,@Param("orderCode")long orderCode);

    /**
     * 查询用户其他订单的商品信息
     * @param uid
     * @param orderCode
     * @return
     */
    List<ShareOrderGoodsInfo> selectOtherOrderGoodsList(@Param("uid")int uid,@Param("orderCode")long orderCode);

    /**
     * 查询用户所有订单的商品信息
     * @param uid
     * @return
     */
    List<ShareOrderGoodsInfo> selectAllOrderGoodsList(@Param("uid")int uid);

    /**
     * 根据uid查询待晒单总数
     * @param uid
     * @return
     */
    int selectToShareOrderNumByUid(@Param("uid") String uid);

    /**
     * 查询用户所有订单的商品信息
     * @param uid
     * @return
     */
    List<ShareOrderGoodsInfo> selectAllOrderGoodsListByUid(@Param("uid") String uid);

}
package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.model.PaymentBO;
import com.yoho.service.model.order.model.HistoryOrderBO;
import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.*;
import com.yoho.yhorder.order.restapi.bean.ResponseBean;

import java.util.List;


public interface IYohoOrderService {


    /**
     * 查询一段时间内的订单号
     *
     * @param request
     * @return
     */
    OrdersStatResponse findOrderCodeByCreateTimeBetween(OrdersStatRequest request);

    /**
     * 获取待处理订单总数
     *
     * @param uid
     * @return
     */
    int findPendingOrderCountByUid(Integer uid);

    /**
     * 获取订单列表总数
     *
     * @param request
     * @return
     */
    int getOrderListCount(OrderListRequest request);

    /**
     * 获取订单列表
     *
     * @param request
     * @return
     */
    List<Orders> getOrderList(OrderListRequest request);

    /**
     * 获取历史订单列表总数
     *
     * @param request
     * @return
     */
    int getHistoryOrderCount(OrderListRequest request);

    /**
     * 获取历史订单列表
     *
     * @param request
     * @return
     */
    List<HistoryOrderBO> getHistoryOrderList(OrderListRequest request);

    Orders getOrderByCode(String orderCode);

    /**
     * 根据订单号确认订单
     *
     * @param orderCode
     */
    void confirmOrderByCode(Long orderCode);

    OrderSuccessResponse success(OrderSuccessRequest orderSuccessRequest);

    /**
     * 根据订单号删除订单
     *
     * @param orderCode
     */
    void deleteOrderByCode(Long orderCode);

    /**
     * 根据订单号获取电子票列表
     *
     * @param orderCode
     * @return
     */
    TicketsQr getQrByOrderCode(Long orderCode,Integer uid);

    /**
     * 更新订单支付方式
     *
     * @param orderCode
     * @param payment
     * @deprecated
     */
    void updateOrdersPaymentByCode(Long orderCode, Byte payment);

    /**
     * 更新订单支付状态
     *
     * @param id
     * @param payment
     * @param paymentStatus
     * @param bankCode
     */
    void updateOrderPaymentStatusById(Integer id, Byte payment, String paymentStatus, String bankCode);

    void updateOrderStatusById(Integer id, Byte status, Integer updateTime);

    void paySuccess(Integer id, Byte payment, String bankCode);

    /**
     * 根据uid,获取订单状态统计信息
     *
     * @param uid
     * @return
     */
    OrdersStatusStatistics getOrdersStatusStatisticsByUid(Integer uid);


    /**
     * 根据uid,status,获取订单数量
     *
     * @param uid
     * @return
     */
    int getOrdersCountByUidAndStatus(Integer uid, List<Integer> status);

    /**
     * 获取订单信息
     *
     * @param uid
     * @param orderCode
     * @return
     */
    OrderInfoResponse getOrderDetail(int uid, long orderCode);

    /**
     * 获取优惠码新客订单数(未取消 + 发货之后取消的)
     *
     * @param uid
     * @return
     */
    int getNewUserOrderCountForPromotionCode(int uid);

    /**
     * 根据订单号获取物流详情信息
     *
     * @param orderCode
     * @return
     */
    ResponseBean findLogisticsDetail(int uid, long orderCode);

    /**
     * 根据uid和productId检查是否已经购买过
     *
     * @param checkHasBuyingRequest
     * @return false=没有购买过  true=购买过
     */
    boolean checkHasBuying(CheckHasBuyingRequest checkHasBuyingRequest);

    int getOrderCountByUid(Integer uid);

    PaymentBO getPaymentById(int id);

    List<PaymentBO> getPaymentList();

    /**
     * 获取订单的支付银行
     *
     * @param orderCode
     * @return
     */
    OrderPayBankBO getOrderPayBank(long orderCode);

    /**
     * 添加订单支付银行记录
     *
     * @param orderCode
     * @param payment
     * @param bankCode
     * @return
     */
    void addOrderPayBank(long orderCode, byte payment, String bankCode);

    /**
     * 更改订单支付银行记录
     *
     * @param orderCode
     * @param payment
     * @param bankCode
     * @return
     */
    void modifyOrderPayBank(long orderCode, byte payment, String bankCode);

    /**
     * 保存更新预支付方式
     *
     * @param request
     */
    void saveOrUpdatePrePayment(PrePaymentRequest request);

    /**
     * 获取订单花呗分期详情
     *
     * @param orderCode
     * @return
     */
    List<AntHbfqBO> getAntHbfqDetail(long orderCode);

}

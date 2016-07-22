package com.yoho.yhorder.order.service;

import com.alibaba.fastjson.JSONObject;
import com.yoho.service.model.order.model.RefundGoodsBO;
import com.yoho.service.model.order.model.RefundOrder;
import com.yoho.service.model.order.model.refund.Goods;
import com.yoho.service.model.order.model.refund.Payment;
import com.yoho.service.model.order.response.PageResponse;

import java.util.List;
import java.util.Map;

/**
 * qianjun
 * 2015/12/3
 */
public interface IRefundService {

    RefundOrder findRefundOrderById(Integer id);

    /**
     * 获取退货订单列表
     *
     * @param uid
     * @param pageNum
     * @param pageSize
     */
    PageResponse<RefundGoodsBO> getListByUid(Integer uid, Integer pageNum, Integer pageSize);

    /**
     * 获取退货订单商品列表
     */
    Map<String, Object> goodsList(Integer uid, Long orderCode);

    /**
     * 提交退货申请
     */
    Map<String, Object> submit(Long orderCode, Integer uid, String areaCode, List<Goods> goodsList, Payment payment);

    /**
     * 退货详情
     */
    Map<String, Object> detail(Integer id, Integer uid);

    /**
     * 保存快递信息
     */
    void setExpress(Integer id, Integer uid, String expressCompany, String expressNumber, Integer expressId);


    int setRefund(Long orderCode, Integer uid, JSONObject refundOrder);

    /**
     * 功能描述: 根据erp传入mq消息同步更新前台表的退货信息
     *
     * @param id
     * @param erpRefundId
     * @param status
     * @param returnAmount
     * @param isReturnCoupon
     * @param returnYohoCoin
     */
    void syncRefundStatus(int id, int erpRefundId, int status, double returnAmount, String isReturnCoupon, int returnYohoCoin);

    /**
     * 功能描述: 根据erp传入mq消息同步更新前台表的退货状态
     *
     * @param erpRefundId
     * @param status
     */
    void syncRefundStatus(int erpRefundId, int status, double returnAmount, String isReturnCoupon, int returnYohoCoin);

    /**
     * 根据uid获取退换货总数
     *
     * @param uid
     * @return
     */
    int getCountByUid(Integer uid);
    
    /**
     * 取消退货
     */
    void cancelRefund(Integer id, Integer uid);
}

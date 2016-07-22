package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.model.ChangeGoodsBO;
import com.yoho.service.model.order.model.ChangeGoodsListBO;
import com.yoho.service.model.order.model.ChangeOrder;
import com.yoho.service.model.order.request.OrderChangeDeliveryReq;
import com.yoho.service.model.order.request.OrderChangeExpressReq;
import com.yoho.service.model.order.request.OrderChangeGoodsApplyReq;
import com.yoho.service.model.order.request.OrderChangeGoodsReq;
import com.yoho.service.model.order.response.orderChange.OrderChangeDeliveryRsp;
import com.yoho.service.model.order.response.orderChange.OrderChangeGoodsApplyRsp;
import com.yoho.yhorder.order.model.OrderChangeGoodsApplyErpRsp;

import java.util.List;

/**
 * 退换货操作
 *
 * @author CaoQi
 * @Time 2015/11/20
 */
public interface IChangeGoodsService {

    ChangeOrder findChangeOrderById(Integer id);


    /**
     * 获取换货方式
     *
     * @return
     */
    List<OrderChangeDeliveryRsp> getChangeDeliveryList(OrderChangeDeliveryReq orderChangeDeliveryReq);


    /**
     * 保存快递信息
     *
     * @param orderChangeExpressReq
     */
    void saveExpressInfo(OrderChangeExpressReq orderChangeExpressReq);


    /**
     * 获取换货订单列表
     *
     * @return
     */
    ChangeGoodsListBO getChangeGoodsList(OrderChangeGoodsReq orderChangeGoodsReq);


    /**
     * 获取换货订单详细信息
     *
     * @return
     */
    ChangeGoodsBO getChangeGoodsDetail(OrderChangeGoodsReq orderChangeGoodsReq);
    
    /**
     * 
     * 功能描述: 取消换货
     *
     * @param orderChangeGoodsReq
     */
    void changeCancel(OrderChangeGoodsReq orderChangeGoodsReq);


    /**
     * 换货申请
     *
     * @return
     */
    OrderChangeGoodsApplyRsp saveChangeGoodsApply(OrderChangeGoodsApplyReq orderChangeGoodsApplyReq);

    /**
     * 处理 Erp 返回信息
     */
    void asyncUpdateChangeGoodsApply(OrderChangeGoodsApplyErpRsp erpRsp);

    /**
     * Erp 更新换货状态
     */
    void syncChangeGoodsStatus(Integer erpRefundId, Integer status);


}

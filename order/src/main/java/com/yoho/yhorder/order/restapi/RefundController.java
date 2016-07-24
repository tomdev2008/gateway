package com.yoho.yhorder.order.restapi;

import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.model.ChangeOrder;
import com.yoho.service.model.order.model.RefundGoodsBO;
import com.yoho.service.model.order.model.RefundOrder;
import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.CountBO;
import com.yoho.service.model.order.response.PageResponse;
import com.yoho.yhorder.order.service.IRefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * qianjun
 * 2015/12/3
 * 4.0
 */
@Controller
@RequestMapping("/refundInfo")
@ServiceDesc(serviceName = "order")
public class RefundController {

    @Autowired
    private IRefundService refundService;

    @RequestMapping("/findRefundOrderById")
    @ResponseBody
    @Database(ForceMaster = true)
    public RefundOrder findRefundOrderById(@RequestBody OrderRequest request) {
        return refundService.findRefundOrderById(request.getId());
    }

    /**
     * 获取退货订单商品列表
     */
    @RequestMapping("/goodsList")
    @ResponseBody
    public Map<String, Object> goodsList(@RequestBody RefundGoodsListRequest refundGoodsListRequest) {
        return refundService.goodsList(refundGoodsListRequest.getUid(), refundGoodsListRequest.getOrderCode());
    }

    /**
     * 提交退货申请
     */
    @RequestMapping("/submit")
    @ResponseBody
    public Map<String, Object> submit(@RequestBody RefundSubmitRequest refundSubmitRequest) {
        return refundService.submit(refundSubmitRequest.getOrderCode(), refundSubmitRequest.getUid(), refundSubmitRequest.getAreaCode(), refundSubmitRequest.getGoodsList(), refundSubmitRequest.getPayment());
    }

    /**
     * 退货详情
     */
    @RequestMapping("/detail")
    @ResponseBody
    public Map<String, Object> detail(@RequestBody RefundGoodsRequest refundGoodsRequest) {
        return refundService.detail(refundGoodsRequest.getId(), refundGoodsRequest.getUid());
    }

    /**
     * 保存快递信息
     */
    @RequestMapping("/setExpress")
    @ResponseBody
    public void setExpress(@RequestBody RefundGoodsRequest refundGoodsRequest) {
        refundService.setExpress(refundGoodsRequest.getId(), refundGoodsRequest.getUid(), refundGoodsRequest.getExpressCompany(), refundGoodsRequest.getExpressNumber(), refundGoodsRequest.getExpressId());
    }

    /**
     * 获取退货订单列表
     *
     * @param refundRequest
     * @return
     */
    @RequestMapping("/getList")
    @ResponseBody
    public PageResponse<RefundGoodsBO> getList(@RequestBody RefundRequest refundRequest) {
        PageResponse<RefundGoodsBO> pageResponse = refundService.getListByUid(refundRequest.getUid(), refundRequest.getPage(), refundRequest.getLimit());
        return pageResponse;
    }

    /**
     * 根据UID获取退换货总数
     *
     * @param refundRequest
     * @return
     */
    @RequestMapping("/getCountByUid")
    @ResponseBody
    public CountBO getCountByUid(@RequestBody RefundRequest refundRequest) {
        return CountBO.valueOf(refundService.getCountByUid(refundRequest.getUid()));
    }
    
    /**
     * 功能描述: 取消退货
     * 〈功能详细描述〉
     *
     * @param refundGoodsRequest
     */
    @RequestMapping("/refundCancel")
    @ResponseBody
    public void refundCancel(@RequestBody RefundGoodsRequest refundGoodsRequest) {
        refundService.cancelRefund(refundGoodsRequest.getId(), refundGoodsRequest.getUid());
    }

}






















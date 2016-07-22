package com.yoho.yhorder.order.restapi;

import com.alibaba.fastjson.JSON;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.service.model.order.model.ChangeGoodsBO;
import com.yoho.service.model.order.model.ChangeGoodsListBO;
import com.yoho.service.model.order.model.ChangeOrder;
import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.orderChange.OrderChangeDeliveryRsp;
import com.yoho.service.model.order.response.orderChange.OrderChangeGoodsApplyRsp;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.order.service.IChangeGoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * lijian 订单换货处理
 * 2015/12/3
 */
@Controller
@RequestMapping("/orderChange")
@ServiceDesc(serviceName = "order")
public class OrderChangeController {

    Logger logger = LoggerFactory.getLogger(OrderChangeController.class);

    @Autowired
    IChangeGoodsService iChangeGoodsService;

    @Autowired
    IOrdersMapper iOrdersMapper;

    @RequestMapping("/findChangeOrderById")
    @ResponseBody
    @Database(ForceMaster = true)
    public ChangeOrder findChangeOrderById(@RequestBody OrderRequest request) {
        return iChangeGoodsService.findChangeOrderById(request.getId());
    }

    @RequestMapping("/queryDeliveryList")
    @ResponseBody
    public List<OrderChangeDeliveryRsp> queryDeliveryList(@RequestBody OrderChangeDeliveryReq orderChangeDeliveryReq) {
        List<OrderChangeDeliveryRsp> orderChangeDeliveryRspList = iChangeGoodsService.getChangeDeliveryList(orderChangeDeliveryReq);
        return orderChangeDeliveryRspList;
    }


    /**
     * 设置快递保存方式
     * param：OrderGoodsRequest
     *
     * @param orderChangeExpressReq [id, expressId, expressNumber, uid]
     * @return
     */

    @RequestMapping("/setExpress")
    @ResponseBody
    @ServiceDesc(serviceName = "setChangeExpress")
    public String setExpress(@RequestBody OrderChangeExpressReq orderChangeExpressReq) {

        logger.info("params is {}", JSON.toJSONString(orderChangeExpressReq));

        iChangeGoodsService.saveExpressInfo(orderChangeExpressReq);

        return "200";

    }


    /**
     * 保存换货*
     *
     * @param orderChangeGoodsApplyReq
     * @return
     */

    @RequestMapping("/submitChangeGoods")
    @ResponseBody
    public OrderChangeGoodsApplyRsp submitChangeGoods(@RequestBody OrderChangeGoodsApplyReq orderChangeGoodsApplyReq) {
        return iChangeGoodsService.saveChangeGoodsApply(orderChangeGoodsApplyReq);
    }


    /**
     * 换货商品列表
     */
    @RequestMapping("/queryChangeGoodsList")
    @ResponseBody
    public ChangeGoodsListBO queryChangeGoodsList(@RequestBody OrderChangeGoodsReq orderChangeGoodsReq) {
        return iChangeGoodsService.getChangeGoodsList(orderChangeGoodsReq);
    }


    /**
     * 换货商品详情
     *
     * @param orderChangeGoodsReq [id, uid]
     */
    @RequestMapping("/queryChangeGoodsDetail")
    @ResponseBody
    public ChangeGoodsBO queryChangeGoodsDetail(@RequestBody OrderChangeGoodsReq orderChangeGoodsReq) {
        ChangeGoodsBO changeGoodsBO = iChangeGoodsService.getChangeGoodsDetail(orderChangeGoodsReq);
        return changeGoodsBO;
    }
    
    /**
     * 
     * 功能描述: 取消换货
     * 〈功能详细描述〉
     *
     * @param req
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    @RequestMapping("/changeCancel")
    @ResponseBody
    public void changeCancel(@RequestBody OrderChangeGoodsReq req) {
        iChangeGoodsService.changeCancel(req);
    }
}






















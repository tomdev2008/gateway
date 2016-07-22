package com.yoho.yhorder.order.restapi;

import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.model.HistoryOrderBO;
import com.yoho.service.model.order.model.PaymentBO;
import com.yoho.service.model.order.model.simple.SimpleIntBO;
import com.yoho.service.model.order.payment.OrdersPayRefundRequest;
import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.*;
import com.yoho.yhorder.order.model.PayRefundBo;
import com.yoho.yhorder.order.payment.service.PaymentService;
import com.yoho.yhorder.order.restapi.bean.ResponseBean;
import com.yoho.yhorder.order.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@RequestMapping("/orderInfo")
@ServiceDesc(serviceName = "order")
public class YohoOrderController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Logger payConfirmLog = LoggerFactory.getLogger("payConfirmLog");

    @Autowired
    private IYohoOrderService yohoOrderService;

    @Autowired
    private IOrderCancelService orderCancelService;

    @Autowired
    private IPaymentDataService iPaymentDataService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IOrderSplitService subOrderService;

    @Autowired
    private IOrderCodeService orderCodeService;

    @RequestMapping("/findUidByOrderCode")
    @ResponseBody
    public SimpleIntBO findUidByOrderCode(@RequestBody OrderRequest request) {
        if (request.getOrderCode() == null) {
            return SimpleIntBO.valueOf(0);
        }
        return SimpleIntBO.valueOf(orderCodeService.findUidByOrderCode(request.getOrderCode()));
    }

    @RequestMapping("/findOrderCodeByCreateTimeBetween")
    @ResponseBody
    public OrdersStatResponse findOrderCodeByCreateTimeBetween(@RequestBody OrdersStatRequest request) {
        return yohoOrderService.findOrderCodeByCreateTimeBetween(request);
    }


    /**
     * 获取待处理订单总数
     *
     * @param request
     * @return
     */
    @RequestMapping("/findPendingOrderCountByUid")
    @ResponseBody
    public CountBO findPendingOrderCountByUid(@RequestBody Orders request) {
        return CountBO.valueOf(yohoOrderService.findPendingOrderCountByUid(request.getUid()));
    }

    /**
     * @param request
     * @return
     */
    @RequestMapping("/getOrderCount")
    @ResponseBody
    @Database(ForceMaster = true)
    public CountBO getOrderCount(@RequestBody OrderListRequest request) {
        return CountBO.valueOf(yohoOrderService.getOrderListCount(request));
    }

    /**
     * 获取订单列表
     * param：OrderListRequest
     *
     * @param request
     * @return
     */

    @RequestMapping("/getOrderList")
    @ResponseBody
    @Database(ForceMaster = true)
    public List<Orders> getOrderList(@RequestBody OrderListRequest request) {
        return yohoOrderService.getOrderList(request);
    }


    /**
     * 订单详情接口
     *
     * @return
     */
    @RequestMapping("/orderDetail")
    @ResponseBody
    @Database(ForceMaster = true)
    public OrderInfoResponse orderDetail(@RequestBody OrderDetailRequest request) {
        logger.info("handler order detail, orderCode is {}, uid is {}", request.getOrderCode(), request.getUid());
        long orderCode = Long.parseLong(request.getOrderCode());
        int uid = getUidFromOrderDetailRequest(request);
        OrderInfoResponse orderInfoResponse = yohoOrderService.getOrderDetail(uid, orderCode);
        logger.info("handler order detail success, orderCode is {}, uid is {}", request.getOrderCode(), request.getUid());
        return orderInfoResponse;
    }

    private int getUidFromOrderDetailRequest(@RequestBody OrderDetailRequest request) {
        int uid;
        if (StringUtils.isNotEmpty(request.getUid())) {
            uid = Integer.parseInt(request.getUid());
        } else {
            uid = 0;
        }
        if (uid == 0) {
            uid = orderCodeService.findUidByOrderCode(Long.parseLong(request.getOrderCode()));
            if (uid == 0) {
                logger.info("handler order detail fail, orderCode is {}, uid is {} can not find uid.", request.getOrderCode(), request.getUid());
                throw new ServiceException(ServiceError.ORDER_NULL);
            }
        }
        return uid;
    }


    /**
     * 根据订单号取消订单
     */
    @RequestMapping("/closeOrderByCode")
    @ResponseBody
    @Database(ForceMaster = true)
    public void closeOrderByCode(@RequestBody OrderCancelRequest request) {
        orderCancelService.cancelByUser(request);
    }

    /**
     * 根据订单号确认订单
     */
    @RequestMapping("/confirmOrderByCode")
    @ResponseBody
    public void confirmOrderByCode(@RequestBody Orders orders) {
        yohoOrderService.confirmOrderByCode(orders.getOrderCode());
    }

    @RequestMapping("/success")
    @ResponseBody
    public OrderSuccessResponse success(@RequestBody OrderSuccessRequest orderSuccessRequest) {
        return yohoOrderService.success(orderSuccessRequest);
    }

    /**
     * 根据订单号删除订单
     *
     * @return
     */
    @RequestMapping("/deleteOrderByCode")
    @ResponseBody
    public void deleteOrderByCode(@RequestBody Orders orders) {
        yohoOrderService.deleteOrderByCode(orders.getOrderCode());
    }

    /**
     * 根据订单号获取电子票列表
     */
    @RequestMapping("/getQrByOrderCode")
    @ResponseBody
    public TicketsQr getQrByOrderCode(@RequestBody Orders orders) {
        return yohoOrderService.getQrByOrderCode(orders.getOrderCode(), orders.getUid());
    }

    @RequestMapping("/getOrdersByCode")
    @ResponseBody
    @Database(ForceMaster = true)
    public Orders getOrdersByCode(@RequestBody OrderRequest request) {
        //(1)验证请求参数
        if (request == null || request.getOrderCode() == null) {
            logger.warn("order code is empty");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        //(2)根据订单号查询订单
        return yohoOrderService.getOrderByCode(request.getOrderCode().toString());
    }

    /**
     * 更新订单支付方式
     *
     * @param request
     * @deprecated
     */
    @RequestMapping("/updateOrdersPaymentByCode")
    @ResponseBody
    @Database(ForceMaster = true)
    public void updateOrdersPaymentByCode(@RequestBody OrderRequest request) {
        yohoOrderService.updateOrdersPaymentByCode(request.getOrderCode(), request.getPayment());
    }

    /**
     * 更新订单支付状态
     *
     * @deprecated
     */
    @RequestMapping("/updateOrderPaymentStatusById")
    @ResponseBody
    public void updateOrderPaymentStatusById(@RequestBody OrderRequest request) {
        yohoOrderService.updateOrderPaymentStatusById(request.getId(), request.getPayment(), request.getPaymentStatus(), request.getBankCode());
        yohoOrderService.updateOrderStatusById(request.getId(), request.getStatus(), request.getUpdateTime());
    }

    /**
     * 支付成功
     *
     * @param request
     */
    @RequestMapping("/paySuccess")
    @ResponseBody
    @Database(ForceMaster = true)
    public void paySuccess(@RequestBody OrderRequest request) {
        yohoOrderService.paySuccess(request.getId(), request.getPayment(), request.getBankCode());
    }

    /**
     * 根据uid,更新订单状态
     */
    @RequestMapping("/updateOrderStatusById")
    @ResponseBody
    public void updateOrderStatusById(@RequestBody OrderRequest request) {
        yohoOrderService.updateOrderStatusById(request.getId(), request.getStatus(), request.getUpdateTime());
    }


    /**
     * 根据uid,获取订单状态统计信息
     */
    @RequestMapping("/getOrdersStatusStatisticsByUid")
    @ResponseBody
    public OrdersStatusStatistics getOrdersStatusStatisticsByUid(@RequestBody OrdersStatusStatisticsRequest request) {
        return yohoOrderService.getOrdersStatusStatisticsByUid(request.getUid());
    }

    @RequestMapping("/getOrdersCountByUidAndStatus")
    @ResponseBody
    public CountBO getOrdersCountByUidAndStatus(@RequestBody OrdersStatusStatisticsRequest request) {
        int count = yohoOrderService.getOrdersCountByUidAndStatus(request.getUid(), request.getStatus());
        return CountBO.valueOf(count);
    }


    /**
     * 获取物流详情信息
     *
     * @deprecated 在7.30版本删除
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseBean get(@RequestBody String orderCode) {
        return li(orderCode);
    }


    /**
     * 获取物流详情信息
     *
     * @deprecated 在7.30版本删除
     */
    @RequestMapping("/li")
    @ResponseBody
    public ResponseBean li(@RequestBody String orderCode) {
        if (StringUtils.isEmpty(orderCode)) {
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        OrderRequest request = new OrderRequest();
        request.setUid(0);
        request.setOrderCode(Long.valueOf(orderCode));
        return findLogisticsDetail(request);
    }


    /**
     * 获取订单物流详情信息
     */
    @RequestMapping("/findLogisticsDetail")
    @ResponseBody
    public ResponseBean findLogisticsDetail(@RequestBody OrderRequest request) {
        logger.info("handler order logistics detail, orderCode is {}, uid is {}", request.getOrderCode(), request.getUid());
        if (request.getUid() == 0) {
            int uid = orderCodeService.findUidByOrderCode(request.getOrderCode());
            if (uid == 0) {
                logger.info("handler order logistics detail fail, orderCode is {}, uid is {}, can not find uid.", request.getOrderCode(), request.getUid());
                throw new ServiceException(ServiceError.ORDER_NULL);
            }
            request.setUid(uid);
        }
        ResponseBean response = yohoOrderService.findLogisticsDetail(request.getUid(), request.getOrderCode());
        logger.info("handler order logistics detail success, orderCode is {}, uid is {}", request.getOrderCode(), request.getUid());
        return response;
    }

    /**
     * 根据订单号获取最新的支付信息
     *
     * @param request
     */
    @RequestMapping("/getPaymentData")
    @ResponseBody
    @Database(ForceMaster = true)
    public PaymentData getPaymentData(@RequestBody OrderRequest request) {
        // 1. 验证请求参数
        if (request == null || request.getOrderCode() == null) {
            logger.warn("order code is empty");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        // 2. 根据订单号查询支付数据
        return iPaymentDataService.getPaymentDataByOrderCode(request.getOrderCode());
    }

    /**
     * 保存支付数据
     *
     * @param paymentData
     */

    @RequestMapping("/savePaymentData")
    @ResponseBody
    public void savePaymentData(@RequestBody PaymentData paymentData) {

        iPaymentDataService.savePaymentData(paymentData);
    }

    /**
     * 根据用户id获取已支付订单数量
     *
     * @param uid
     * @return
     */
    @RequestMapping("/getNewUserOrderCountForPromotionCode")
    @ResponseBody
    public int getNewUserOrderCountForPromotionCode(@RequestBody int uid) {
        return yohoOrderService.getNewUserOrderCountForPromotionCode(uid);
    }

    @RequestMapping("/payConfirm")
    @ResponseBody
    public PaymentConfirmResponse payConfirm(@RequestBody PaymentConfirmReq req) {
        payConfirmLog.info("\n\n\n****************************************************");
        payConfirmLog.info("enter YohoOrderController ,params are {} ", req);
        if (StringUtils.isEmpty(req.getOrder_code()) || req.getUid() < 1 || req.getPayment_id() < 1) {

            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        PaymentConfirmResponse response = paymentService.payConfirm(req);
        payConfirmLog.info("exit YohoOrderController,response is {} ", response);
        return response;
    }

    @RequestMapping("/checkHasBuying")
    @ResponseBody
    public BooleanResponse checkHasBuying(@RequestBody CheckHasBuyingRequest checkHasBuyingRequest) {
        return BooleanResponse.valueOf(yohoOrderService.checkHasBuying(checkHasBuyingRequest));
    }


    /**
     * 获取历史订单总数
     *
     * @param request
     * @return
     */
    @RequestMapping("/getHistoryOrderCount")
    @ResponseBody
    public CountBO getHistoryOrderCount(@RequestBody OrderListRequest request) {
        return CountBO.valueOf(yohoOrderService.getHistoryOrderCount(request));
    }

    /**
     * 获取历史订单列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/getHistoryOrderList")
    @ResponseBody
    public List<HistoryOrderBO> getHistoryOrderList(@RequestBody OrderListRequest request) {
        return yohoOrderService.getHistoryOrderList(request);
    }

    /**
     * 获取用户的订单总数
     */
    @RequestMapping("/getOrderCountByUid")
    @ResponseBody
    @Database(ForceMaster = true)
    public CountBO getOrderCountByUid(@RequestBody OrdersStatusStatisticsRequest request) {
        logger.info("Get order count by uid {}", request.getUid());

        int count = yohoOrderService.getOrderCountByUid(request.getUid());

        logger.info("Get order count by uid result: uid {}, count {}", request.getUid(), count);
        return CountBO.valueOf(count);
    }

    /**
     * 通过ID获取支付途径
     */
    @RequestMapping("getPaymentById")
    @ResponseBody
    public PaymentBO getPaymentById(@RequestBody int id) {
        logger.info("Get payment by id, id: {}", id);

        PaymentBO paymentBO = yohoOrderService.getPaymentById(id);

        logger.info("End get payment by id, id: {}, payCode: {}, payName: {}", id, paymentBO.getPayCode(), paymentBO.getPayName());
        return paymentBO;
    }

    /**
     * 获取所有支付途径
     */
    @RequestMapping("getPaymentList")
    @ResponseBody
    public List<PaymentBO> getPaymentList() {
        logger.info("Begin get payment list");
        List<PaymentBO> result = yohoOrderService.getPaymentList();
        logger.info("End get payment list");
        return result;
    }

    /**
     * 获取订单的支付银行
     *
     * @param orderCode
     * @return
     */
    @RequestMapping("/getOrderPayBank")
    @ResponseBody
    @Database(ForceMaster = true)
    public OrderPayBankBO getOrderPayBank(@RequestBody long orderCode) {
        logger.info("[{}] getOrderPayBank", orderCode);
        if (orderCode < 1) {
            logger.warn("order code is invalid");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        return yohoOrderService.getOrderPayBank(orderCode);
    }

    /**
     * 订单支付银行记录增、改的参数校验
     *
     * @param orderPayBank
     */
    private void checkOrderPayBankParams(OrderPayBankBO orderPayBank) {
        if (orderPayBank.getOrderCode() < 1) {
            logger.warn("order code is invalid");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        if (orderPayBank.getPayment() < 1) {
            logger.warn("order payment is invalid");
            throw new ServiceException(ServiceError.ORDER_PAYMENT_IS_EMPTY);
        }
    }

    /**
     * 添加订单支付银行记录
     *
     * @param orderPayBank
     * @return
     */
    @RequestMapping("/addOrderPayBank")
    @ResponseBody
    public void addOrderPayBank(@RequestBody OrderPayBankBO orderPayBank) {
        logger.info("[{}] addOrderPayBank: payment {}, bankCode {}", orderPayBank.getOrderCode(), orderPayBank.getPayment(), orderPayBank.getBankCode());
        //参数校验
        checkOrderPayBankParams(orderPayBank);

        yohoOrderService.addOrderPayBank(orderPayBank.getOrderCode(), orderPayBank.getPayment(), orderPayBank.getBankCode());
        logger.info("[{}] finish addOrderPayBank", orderPayBank.getOrderCode());
    }

    /**
     * 修改订单支付银行记录
     *
     * @param orderPayBank
     * @return
     */
    @RequestMapping("/modifyOrderPayBank")
    @ResponseBody
    public void modifyOrderPayBank(@RequestBody OrderPayBankBO orderPayBank) {
        logger.info("[{}] modifyOrderPayBank: payment {}, bankCode {}", orderPayBank.getOrderCode(), orderPayBank.getPayment(), orderPayBank.getBankCode());
        //参数校验
        checkOrderPayBankParams(orderPayBank);

        yohoOrderService.modifyOrderPayBank(orderPayBank.getOrderCode(), orderPayBank.getPayment(), orderPayBank.getBankCode());

        logger.info("[{}] finish modifyOrderPayBank", orderPayBank.getOrderCode());
    }

    /**
     * 更新订单的状态
     */
    @RequestMapping("updatePaymentStatus")
    @ResponseBody
    public void updatePaymentStatus(@RequestBody OrderRequest request) {
        logger.info("Update payment status: id {}", request.getId());
        yohoOrderService.updateOrderPaymentStatusById(request.getId(), request.getPayment(), request.getPaymentStatus(), request.getBankCode());
        logger.info("Finish update payment status: id {}", request.getId());

    }

    /**
     * 保存预支付信息
     */
    @RequestMapping("/saveOrUpdatePrePayment")
    @ResponseBody
    @Database(ForceMaster = true)
    public void saveOrUpdatePrePayment(@RequestBody PrePaymentRequest request) {
        logger.info("Update prepayment uid:{},orderCode:{}", request.getUid(), request.getOrderCode());
        yohoOrderService.saveOrUpdatePrePayment(request);
        logger.info("Finish update prepayment uid:{},orderCode:{}", request.getUid(), request.getOrderCode());

    }

    @RequestMapping("splitOrder")
    @ResponseBody
    @Database(ForceMaster = true)
    public int splitOrder(@RequestBody OrderRequest request) {
        logger.info("split order in controller, request is {}", request);
        int subOrderNum = subOrderService.splitOrder(request.getUid(), request.getOrderCode());
        logger.info("split order success in controller, request is {},sub order num is {}", request, subOrderNum);
        return subOrderNum;
    }

    /**
     * 获取订单花呗分期详情
     */
    @RequestMapping("/getAntHbfqDetail")
    @ResponseBody
    @Database(ForceMaster = true)
    public List<AntHbfqBO> getAntHbfqDetail(@RequestBody long orderCode) {
        return yohoOrderService.getAntHbfqDetail(orderCode);
    }

    /**
     * 支付结果查询
     *
     * @param orderCode
     * @return
     */
    @RequestMapping("/paymentOrderQuery")
    @ResponseBody
    @Database(ForceMaster = true)
    public PaymentOrderQueryBO paymentOrderQuery(@RequestBody String orderCode) {
        logger.info("Query order payment from third pay gateway, orderCode: {}", orderCode);
        return paymentService.paymentOrderQuery(orderCode);
    }


    @RequestMapping("/validateCancelStatusForErp")
    @ResponseBody
    @Database(ForceMaster = true)
    public String validateCancelStatusForErp(@RequestBody OrderRequest request) {
        logger.info("validateCancelStatusForErp from erp, orderCode: {}", request.getOrderCode());
        return orderCancelService.validateCancelStatus(request.getOrderCode());
    }

    /**
     * 支付退款
     *
     * @return
     */
    @RequestMapping("/paymentRefund")
    @ResponseBody
    @Database(ForceMaster = true)
    public PaymentOrderQueryBO paymentRefund(@RequestBody OrdersPayRefundRequest request) {
        logger.info("payment refund for order: {}", request.getOrderCode());
        PaymentOrderQueryBO returnBo = new PaymentOrderQueryBO();
        returnBo.setOrderCode(String.valueOf(request.getOrderCode()));
        returnBo.setAmount(request.getAmount());
        returnBo.setPayment(request.getPayment());
        PayRefundBo payRefundBo = paymentService.paymentRefund(request.getOrderCode(), request.getPayment(), request.getAmount());
        returnBo.setResultCode(payRefundBo.getRefundStatus());
        returnBo.setResultMsg(payRefundBo.getRefundMsg());
        returnBo.setTradeNo(payRefundBo.getRefundOrderCode());
        return returnBo;
    }

    /**
     * 支付退款
     *
     * @return
     */
    @RequestMapping("/paymentRefundQuery")
    @ResponseBody
    @Database(ForceMaster = true)
    public PaymentOrderQueryBO paymentRefundQuery(@RequestBody OrdersPayRefundRequest request) {
        logger.info("payment refund for order: {}", request.getOrderCode());
        PaymentOrderQueryBO returnBo = new PaymentOrderQueryBO();
        returnBo.setOrderCode(String.valueOf(request.getOrderCode()));
        returnBo.setPayment(request.getPayment());
        PayRefundBo payRefundBo = paymentService.paymentRefundQuery(request.getOrderCode(), request.getPayment());
        returnBo.setAmount(payRefundBo.getAmount());
        returnBo.setResultCode(payRefundBo.getRefundStatus());
        returnBo.setResultMsg(payRefundBo.getRefundMsg());
        return returnBo;
    }
}

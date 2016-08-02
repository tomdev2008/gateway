package com.yoho.yhorder.order.payment.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.LocalIp;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.PaymentConfirmReq;
import com.yoho.service.model.order.response.OrderCancelReasonBO;
import com.yoho.service.model.order.response.OrderPaymentStatusBO;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.PaymentConfirmResponse;
import com.yoho.service.model.order.response.PaymentOrderQueryBO;
import com.yoho.yhorder.dal.IOrderPayDAO;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.OrdersPayRefundMapper;
import com.yoho.yhorder.dal.model.OrdersPayRefund;
import com.yoho.yhorder.order.config.Constant;
import com.yoho.yhorder.order.config.OrderConstant;
import com.yoho.yhorder.order.model.PayQueryBo;
import com.yoho.yhorder.order.model.PayRefundBo;
import com.yoho.yhorder.order.model.PayType;
import com.yoho.yhorder.order.payment.alipay.AliPayQuerier;
import com.yoho.yhorder.order.payment.alipay.AliPayRefunder;
import com.yoho.yhorder.order.payment.qqwallet.QQWallet;
import com.yoho.yhorder.order.payment.unionpay.UnionPayQuerier;
import com.yoho.yhorder.order.payment.wechat.NewWechatQuerier;
import com.yoho.yhorder.order.service.IOrderCancelService;
import com.yoho.yhorder.order.service.IYohoOrderService;

/**
 * Created by JXWU on 2016/1/23.
 */
@Service
public class PaymentService {

    private final Logger logger = LoggerFactory.getLogger("payConfirmLog");

    private final Logger paymentDBLogger =LoggerFactory.getLogger("paymentDBLog");

    public final  static String PAYMENT_FINISH_MQ_TOPTIC ="order.payment";

    @Autowired
    IOrderPayDAO orderPayDAO;

    @Autowired
    IOrdersMapper ordersMapper;
    
    @Autowired
    OrdersPayRefundMapper refundMapper;

    @Resource
    AliPayRefunder aliPayRefunder;
    
    @Resource
    AliPayQuerier aliPayQuerier;
    
    @Autowired
    private IOrderCancelService orderCancelService;

    @Resource
    UnionPayQuerier unionPayQuerier;

//    @Resource
//    WechatQuerier wechatQuerier;

    @Resource
    NewWechatQuerier wechatWapQuerier;

    @Resource
    NewWechatQuerier wechatAppQuerier;

    @Autowired
    IYohoOrderService yohoOrderService;
    
    @Resource
    QQWallet qqWallet;

    @Value("${erp.order.status.url}")
    private String erpOrderStatusUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private YhProducerTemplate producerTemplate;

    @Value("${erp.message.sync.type}")
    private String erpMessageSyncType;

    public PaymentConfirmResponse payConfirm(PaymentConfirmReq paymentConfirmReq) {
        logger.info("payservice query order code {}", paymentConfirmReq.getOrder_code());
        Orders order = getOrderAndCheckIfNull(paymentConfirmReq.getOrder_code());
        logger.info("result: order code {} id {},status {},amount {},paymentStatus {},payment {},isCancle {},orderStatus {}",
                order.getOrderCode(),
                order.getId(),
                order.getStatus(),
                order.getAmount(),
                order.getPaymentStatus(),
                order.getPayment(),
                order.getIsCancel(),
                order.getOrdersStatus());


        orderPayDAO.insert(order.getOrderCode(), paymentConfirmReq.getUid(), paymentConfirmReq.getPayment_id());
        if (isNotPaidOrder(order)) {
            //订单还没有支付，需要根据支付方式去第三方平台确认
            syncPaidOrderStatus(order, paymentConfirmReq.getPayment_id());
        } else {
            logger.info("order code {} was paid by [{}] notify", order.getOrderCode(), order.getPayment());
        }
        //构造返回结果
        return bulidPaymentConfirmResponse(order.getPaymentStatus(), order.getIsCancel());
    }

    private Orders getOrderAndCheckIfNull(String orderCode) throws ServiceException {
        Orders order = ordersMapper.selectByOrderCode(orderCode);
        if (order == null) {
            logger.info("order is null,select by order code {}", orderCode);
            throw new ServiceException(ServiceError.ORDER_DOES_NOT_EXIST);
        }

        return order;
    }

    private boolean isNotPaidOrder(Orders order) {
        return !"Y".equals(order.getPaymentStatus());
    }

    private PaymentConfirmResponse bulidPaymentConfirmResponse(String isPaid, String isCancle) {
        PaymentConfirmResponse response = new PaymentConfirmResponse();
        response.setIs_paid(isPaid);
        response.setIs_cancle(isCancle);
        return response;
    }

    private void syncPaidOrderStatus(Orders orders, int paymentId) {
        PayQueryBo payQueryBo = queryPayStatus(orders, paymentId);
        if (isEquals(orders, payQueryBo)) {
            logger.info("order code {} is already paid with [{}],but not notify us", orders.getOrderCode(), paymentId);
            notify(orders, payQueryBo);
        } else {
            logger.warn("order code {} is not paid with [{}],pay query result is {}", orders.getOrderCode(), paymentId, payQueryBo);
        }
    }

    private PayQueryBo queryPayStatus(Orders orders, int paymentId) {
        PayQueryBo queryBo = new PayQueryBo();
        String tradeNo = String.valueOf(orders.getOrderCode());
        PayType payType = PayType.valueOf(paymentId);
        switch (payType) {
            case UNIONPAY:
            case UNIONPAY_WEB:
                String txnTime = formatDate(orders.getCreateTime(), "yyyyMMddHHmmss");
                queryBo = unionPayQuerier.query(tradeNo, txnTime, payType);
                break;
            case ALIPAY:
            case ALIPAYWAP:
            case ALIPAY_PC:
            case ALIPAY_BANK:
                queryBo = aliPayQuerier.query(tradeNo, payType);
                break;
            case WECHATAPP:
                // 微信订单号以 "YOHOBuy_"开头,需处理
                tradeNo = Constant.WECHAT_QUERY_TRADE_PREFIX + tradeNo;
            	queryBo = wechatAppQuerier.query(tradeNo, payType);
                break;
            case WECHATWAP:
            case WECHAT_QRCODE:
                // 微信订单号以 "YOHOBuy_"开头,需处理
                tradeNo = Constant.WECHAT_QUERY_TRADE_PREFIX + tradeNo;
                queryBo = wechatWapQuerier.query(tradeNo, payType);
                break;
            case QQ_WALLET:
            	//qq手机钱包
            	queryBo=qqWallet.queryTrans(tradeNo);
            	break;
            default:
            	queryBo.paymentSupport = false;	//不支持主动确认
                break;
        }
        queryBo.payType  = payType;
        return queryBo;
    }

    private boolean isEquals(Orders orders, PayQueryBo payQueryBo) {
        return payQueryBo != null && payQueryBo.valid && String.valueOf(orders.getOrderCode()).equals(payQueryBo.orderCode) && (YHMath.equalTo(orders.getAmount(), BigDecimal.valueOf(payQueryBo.amount)));
    }


    private String formatDate(Integer time, String pattern) {
        if (time == null || pattern == null) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date((long) time * 1000));
    }


    private void notify(Orders order, PayQueryBo payQueryBo) {
        logger.info("order code {} will be update to paid", order.getOrderCode());
        //修改本地库中的状态
        yohoOrderService.paySuccess(order.getId(), (byte) payQueryBo.payType.getPayId(), payQueryBo.bankCode);
        logger.info("order code {} was update to paid success", order.getOrderCode());

        if (isCreateERPOrderByMQ()) {
            sendMQ(payQueryBo);
        } else {
            //通知erp
            notifyERP(payQueryBo);
        }

        //大数据日志
        writePaymentBDLog(order,payQueryBo);

        //手动设置，不在从数据库查询了,本地成功，erp失败怎么弄
        order.setPaymentStatus("Y");
    }

    private void sendMQ(PayQueryBo payResult) throws ServiceException
    {
        JSONObject statusData = new JSONObject();
        statusData.put("paymentCode", payResult.payType.getPayId());
        statusData.put("bankCode", payResult.bankCode);
        statusData.put("bankName", payResult.bankName);
        statusData.put("amount", payResult.amount);
        statusData.put("payment", payResult.payType.getPayId());
        statusData.put("payOrderCode", payResult.payOrderCode);
        statusData.put("tradeNo", payResult.tradeNo);
        statusData.put("bankBillNo", payResult.bankBillNo);
        JSONObject data = new JSONObject();
        data.put("orderCode", payResult.orderCode);
        data.put("status", 200);
        data.put("statusData", statusData);

        logger.info("[{}] send MQ message is : {}", payResult.orderCode, data);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("order_code", payResult.orderCode);

            producerTemplate.send(PAYMENT_FINISH_MQ_TOPTIC, data, map);
            logger.info("[{}] send MQ message success", payResult.orderCode);
        } catch (Exception ex) {
            logger.error("[{}] send MQ fail, json:{}", payResult.orderCode, data, ex);
            throw new ServiceException(ServiceError.ORDER_SUBMIT_CALL_ERP);
        }
    }


    private void notifyERP(PayQueryBo payResult) throws ServiceException {

        JSONObject statusData = new JSONObject();
        statusData.put("paymentCode", payResult.payType.getPayId());
        statusData.put("bankCode", payResult.bankCode);
        statusData.put("bankName", payResult.bankName);
        statusData.put("amount", payResult.amount);
        statusData.put("payment", payResult.payType.getPayId());
        statusData.put("payOrderCode", payResult.payOrderCode);
        statusData.put("tradeNo", payResult.tradeNo);
        statusData.put("bankBillNo", payResult.bankBillNo);
        JSONObject data = new JSONObject();
        data.put("orderCode", payResult.orderCode);
        data.put("status", 200);
        data.put("statusData", statusData);
        LinkedMultiValueMap<String, Object> req = new LinkedMultiValueMap<String, Object>();
        req.add("data", data.toJSONString());

        logger.info("[{}] ERP req: {}", payResult.orderCode, req.get("data"));

        String json = null;
        JSONObject jsonObject;

        try {
            json = restTemplate.postForObject(erpOrderStatusUrl, req, String.class);
            jsonObject = JSONObject.parseObject(json);
            logger.info("[{}] ERP resp: {}", payResult.orderCode, json);
        } catch (Exception e) {
            logger.error("[{}] Erp order status call {} fail, json:{}", payResult.orderCode, erpOrderStatusUrl, json, e);
            throw new ServiceException(ServiceError.ORDER_SUBMIT_CALL_ERP);
        }
        int code = jsonObject.getIntValue("code");
        // logger.debug("ERP return code: [{}]", code);
        if (code != 200) {
            logger.warn("[{}] ERP call return invalid code", payResult.orderCode);
            throw new ServiceException(ServiceError.ORDER_SUBMIT_CALL_ERP);
        }
    }

    /**
     * 开关
     * @return
     */
    private boolean isCreateERPOrderByMQ() {
        if ("mq".equalsIgnoreCase(erpMessageSyncType)) {
            return true;
        }
        return false;
    }

    /**
     *  大数据日志
     * @param order
     * @param payQueryBo
     */
    private void writePaymentBDLog(Orders order, PayQueryBo payQueryBo)
    {
        JSONObject logJson = new JSONObject();
        logJson.put("order_code",String.valueOf(order.getOrderCode()));
        logJson.put("pay_amount",String.valueOf(order.getAmount()));
        logJson.put("pay_channel",String.valueOf(order.getPayment()));
        logJson.put("uid",String.valueOf(order.getUid()));
        logJson.put("call_time",payQueryBo.callbackTime);
        logJson.put("pay_time",payQueryBo.paymentTime);
        logJson.put("collect_ip", LocalIp.getLocalIp());
        logJson.put("user_agent",getUserAgentForCollectLog(order.getOrderType()));
        logJson.put("order_type",String.valueOf(order.getOrderType()));
        logJson.put("service_key","payment_log");
        paymentDBLogger.info("{}",logJson);
    }

    public String getUserAgentForCollectLog(int orderType) {
        if (orderType == 3 || orderType == 4 || orderType == 20) {
            return "2";
        } else if (orderType == 6 || orderType == 17) {
            return "3";
        } else {
            return "1";
        }
    }
    	
    /**
     * 判断支付方式是否支付宝系
     * @param payment
     * @return
     */
    private boolean isAliPayment(int payment) {
    	PayType payType = PayType.valueOf(payment);
    	boolean result = false;
    	switch (payType) {
	        case ALIPAY:
	        case ALIPAYWAP:
	        case ALIPAY_PC:
	        case ALIPAY_BANK:
	        	result = true;
	        	break;
	        default:
	        	result = false;
	        	break;
    	}
    	return result;
    }	
    
    /**
     * 取消订单接口调用此支付主动查询方法
     * 调用场景：取消订单。避免实际支付成功，订单确能够取消
     * 说明：4.3版本APP选择支付方式后订单锁定10分钟，不允许取消。
     * @return 支付成功/失败
     */
    public boolean queryTradeStatus(Orders orders){
    	if(orders == null)
    		return false;

        PayQueryBo queryBo = queryPayStatus(orders, orders.getPayment());
        //如果检查到已支付，直接返回
        if(queryBo != null && queryBo.valid){
            return true;
        }

    	//通常各类支付在预支付接口中就会更改订单支付方式
        //有一种情况，支付不成功客户选择另外一种支付方式，
    	//还有一种情况，4.3版本之前的APP，支付宝没有预支付流程，此时的payment没有刷新。
        //以上这两种情况统一再查询一次支付宝支付状态
        if(!isAliPayment(orders.getPayment())) {
        	PayQueryBo nextQueryBo = queryPayStatus(orders, PayType.ALIPAY.getPayId());
            if(nextQueryBo != null && nextQueryBo.valid){
                return true;
            }	
        }
        
    	return false;
    }
    
    /**
     * 到第三方支付系统查询订单支付结果
     * @param orderCode
     * @return
     */
    public PaymentOrderQueryBO paymentOrderQuery(String orderCode) {
    	logger.info("payment orderquery for order: {}", orderCode);
    	PaymentOrderQueryBO bo = new PaymentOrderQueryBO();
    	bo.setOrderCode(orderCode);
    	
    	long orderCodeTmp = 0;
    	try {
			orderCodeTmp = Long.valueOf(orderCode);
		} catch (Exception e) {
		}
    	
    	if(orderCodeTmp < 1) {
    		bo.setResultCode(OrderConstant.PAYMENT_QUERY_RESULTCODE_ORDER_NULL);
    		bo.setResultMsg("订单不存在");
    		logger.error("order code is invalid, orderCode: {}", orderCode);
    		return bo;
    	}
    	
    	Orders orderInfo = ordersMapper.selectByOrderCode(orderCode);
    	if(orderInfo == null) {
    		bo.setResultCode(OrderConstant.PAYMENT_QUERY_RESULTCODE_ORDER_NULL);
    		bo.setResultMsg("订单不存在");
    		logger.error("no such order, orderCode: {}", orderCode);
    		return bo;
    	}
    	
    	if(orderInfo.getPayment() <= 0) {
    		bo.setResultCode(OrderConstant.PAYMENT_QUERY_RESULTCODE_PAYMENT_NULL);
    		bo.setResultMsg("订单支付渠道不存在");
    		logger.error("no payment for this order, orderCode: {}, payment: {}", orderCode, orderInfo.getPayment());
    		return bo;
    	}
    	bo.setPayment(orderInfo.getPayment());
    	
    	//到第三方支付系统查询支付结果
    	PayQueryBo payQueryBo = queryPayStatus(orderInfo, orderInfo.getPayment());
    	if(!payQueryBo.paymentSupport) {
    		bo.setResultCode(OrderConstant.PAYMENT_QUERY_RESULTCODE_NOT_SUPPORT);
    		bo.setResultMsg("不支持该支付方式的查询");
    		logger.warn("payment query not support, orderCode: {}, payment: {}", orderCode, orderInfo.getPayment());
    		return bo;
    	}
    	
    	if(payQueryBo.valid) {
    		bo.setResultCode(OrderConstant.PAYMENT_QUERY_RESULTCODE_HAVE_PAY);
    		bo.setAmount(payQueryBo.amount);
    		bo.setTradeNo(payQueryBo.tradeNo);
    		bo.setPayment(orderInfo.getPayment());
    		bo.setPaymentTime(payQueryBo.paymentTime);
    		bo.setResultMsg("该笔订单已支付");
    	}
    	else {
    		bo.setResultCode(OrderConstant.PAYMENT_QUERY_RESULTCODE_NOT_PAY);
    		bo.setResultMsg("未查到支付记录");
    	}
    	logger.info("payment orderquery result: {}", bo);
    	return bo;
    }
    
    /**
     * 退款接口
     * @param orderCode
     * @param payment
     * @param amount
     * @return
     */
    public PayRefundBo paymentRefund(long orderCode, int payment, double amount) {
    	logger.info("payment refund for order: {}, payment: {}, amount: {}", orderCode, payment, amount);
    	
    	PayRefundBo refundBO = getAndCheckRefundData(orderCode, payment, amount);
    	if(!refundBO.isRefundValid()){
    		logger.error("payment refund check failed, orderCode: {}, refundCode: {}", orderCode, refundBO.getRefundStatus());
    		return refundBO;
    	}
    	
    	PayRefundBo refundBo = refund(refundBO);
    	logger.info("payment refund result, orderCode: {}, refundCode: {}", orderCode, refundBo.getRefundStatus());
    	
    	//退款申请成功的记录添加到orders_pay_refund表
    	if(refundBo.getRefundStatus() == OrderConstant.PAYMENT_REFUND_RESULTCODE_SUCCESS) {
    		recordPayRefund(refundBo);
    	}
    	
    	return refundBo;
    }
    
    /**
     * 
     * @param orderCode
     * @param payment
     * @return
     */
    public PayRefundBo paymentRefundQuery(long orderCode, int payment) {
    	logger.info("payment refund query for order: {}, payment: {}", orderCode, payment);
    	PayRefundBo refundBo = refundQuery(orderCode, payment);
    	logger.info("payment refund query end, orderCode: {}, result: {}", orderCode, refundBo.getRefundStatus());
    	return refundBo;
    }
    
    /**
     * 退款前检查
     * @param orderCode
     * @param payment
     * @param amount
     * @return
     */
    public PayRefundBo getAndCheckRefundData(long orderCode, int payment, double amount) {
    	logger.info("payment refund check, orderCode: {}, payment, amount: {}", orderCode, payment, amount);
    	
    	PayRefundBo refundCheckBO = new PayRefundBo();
    	refundCheckBO.setOrderCode(orderCode);
    	refundCheckBO.setPayment(payment);
    	refundCheckBO.setAmount(amount);
    	
    	//1.订单是否存在
    	Orders orderInfo = ordersMapper.selectByOrderCode(String.valueOf(orderCode));
    	if(orderInfo == null) {
    		refundCheckBO.setRefundValid(false);
    		refundCheckBO.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_ORDERNULL);
    		refundCheckBO.setRefundMsg("退款校验订单不存在");
    		logger.error("no such order, orderCode: {}", orderCode);
    		return refundCheckBO;
    	}
    	refundCheckBO.setOrderTotalFee(orderInfo.getAmount().doubleValue());
    	
		//if(orderInfo.getPaymentStatus().equals("N")) {
		//  refundBo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_ORDERNOTPAY);
		//  refundBo.setRefundMsg("订单尚未支付状态");
		//  logger.error("order has not pay, orderCode: {}", orderCode);
		//  return refundBo;
		//}
    	
    	
    	//退款金额是否大于订单金额
    	double refundAmount = 0;
    	List<OrdersPayRefund> refundDetailList = refundMapper.selectByOrderCode(orderCode);
//    	if(refundDetailList == null || refundDetailList.size() == 0) {
//    		if(amount == orderInfo.getAmount().doubleValue()) {
//    			refundCheckBO.setRefundOrderCode(String.valueOf(orderCode));
//    			return refundCheckBO;
//    		}
//    	}
	
		for(OrdersPayRefund refundRecord : refundDetailList) {
			refundAmount += refundRecord.getAmount().doubleValue();
		}
    	
		//本次退款金额加上已退款金额大于订单金额
    	if((amount + refundAmount) > refundCheckBO.getOrderTotalFee()) {
    		refundCheckBO.setRefundValid(false);
    		refundCheckBO.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_AMOUNTDISMATCH);
    		refundCheckBO.setRefundMsg("退款金额大于订单金额");
    		logger.error("refund amount more than order amount, orderCode: {}, refundAmount: {}", orderCode, amount);
    		return refundCheckBO;
    	}
    	
    	//全额退款
    	if(refundAmount == 0 && amount == refundCheckBO.getOrderTotalFee()) {
			refundCheckBO.setRefundOrderCode(String.valueOf(orderCode));
			return refundCheckBO;    		
    	}
    	
    	//部分退款
    	String refundOrderCode = String.valueOf(orderCode) + String.format("%03d", refundDetailList.size() + 1);
    	refundCheckBO.setRefundOrderCode(refundOrderCode);
	    	 
    	return refundCheckBO;
    }
    
    /**
     * 退款
     * @param orderCode
     * @param payment
     * @param amount
     * @return
     */
    public PayRefundBo refund(PayRefundBo refundBo) {
    	
        PayType payType = PayType.valueOf(refundBo.getPayment());
        switch (payType) {
	        
	        case ALIPAY:
	        case ALIPAYWAP:
	        case ALIPAY_PC:
	        case ALIPAY_BANK:
	            refundBo = aliPayRefunder.refund(refundBo);
	            break;
            case WECHATAPP:
                // 微信订单号以 "YOHOBuy_"开头,需处理
            	refundBo = wechatAppQuerier.refund(refundBo);
                break;
            case WECHATWAP:
            case WECHAT_QRCODE:
                // 微信订单号以 "YOHOBuy_"开头,需处理
            	refundBo = wechatWapQuerier.refund(refundBo);
                break;	            
	        default:
	    		refundBo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_UNABLE);
	    		refundBo.setRefundMsg("不支持退款的支付方式");
	    		logger.error("payment type not support refund, payment: {}", refundBo.getPayment());
	            break;
        }
    	 
        return refundBo;
    }
    
    /**
     * 数据库记录退款申请
     * @param refundBo
     */
    public void recordPayRefund(PayRefundBo refundBo) {
    	logger.info("refund record, refundOrderCode: {}, serialNo: {}", refundBo.getRefundOrderCode(), refundBo.getSerialNo());
    	OrdersPayRefund refundRecord = refundMapper.selectByRefundOrderCode(refundBo.getRefundOrderCode());
    	if(refundRecord != null ) {
    		logger.info("order refund for repeat serialNo: {}, no need to record", refundBo.getSerialNo());
    		return;
    	}
    	
		OrdersPayRefund newRecord = new OrdersPayRefund();
		newRecord.setOrderCode(refundBo.getOrderCode());
		newRecord.setRefundOrderCode(Long.valueOf(refundBo.getRefundOrderCode()));
		newRecord.setAmount(BigDecimal.valueOf(refundBo.getAmount()));
		newRecord.setPayment((byte)refundBo.getPayment());
		newRecord.setStatus((byte)0);
		newRecord.setSerialNo(refundBo.getSerialNo());
		
		int timeStamp = (int)(new Date().getTime()/1000);
		newRecord.setCreateTime(timeStamp);
		newRecord.setUpdateTime(timeStamp);
		refundMapper.insert(newRecord);
		logger.info("refund record finished");    	    	
    	
    }
    
    public PayRefundBo refundQuery(long orderCode, int payment) {
    	PayType payType = PayType.valueOf(payment);
    	
    	PayRefundBo refundBo = new PayRefundBo();
    	refundBo.setOrderCode(orderCode);
    	refundBo.setPayment(payment);
    	
        switch (payType) {
        
	        case ALIPAY:
	        case ALIPAYWAP:
	        case ALIPAY_PC:
	        case ALIPAY_BANK:
	            refundBo = aliPayQuerier.refundQuery(refundBo);
	            break;
	        case WECHATAPP:
	            // 微信订单号以 "YOHOBuy_"开头,需处理
	        	//refundBo = wechatAppQuerier.refund(refundBo);
	            break;
	        case WECHATWAP:
	        case WECHAT_QRCODE:
	            // 微信订单号以 "YOHOBuy_"开头,需处理
	        	//refundBo = wechatWapQuerier.refund(refundBo);
	            break;	            
	        default:
	    		refundBo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_UNABLE);
	    		refundBo.setRefundMsg("不支持退款查询的支付方式");
	    		logger.error("payment type not support refund, payment: {}", refundBo.getPayment());
	            break;
        }
        return refundBo;
    }
    public OrderPaymentStatusBO getOrderPayStatusInfo(Long orderCode, Integer uid) {
		logger.info("payment orderquery for order: {}", orderCode);
		Orders orders = ordersMapper.selectByUidAndOrderCode(uid,orderCode);
		OrderPaymentStatusBO orderPaymentStatusBO = new OrderPaymentStatusBO();
		orderPaymentStatusBO.setUid(uid);
		orderPaymentStatusBO.setOrderCode(orderCode);
		if(Orders.PAYMENT_TYPE_ONLINE.equals(orders.getPaymentType())){
			String payment_status = orderCancelService.getOrdersOnlinePaymentStatus(orders);
			orders.setPaymentStatus(payment_status);
		}
		orderPaymentStatusBO.setPayment_status(orders.getPaymentStatus());
        logger.info("payment getOrderPayInfo result: {}", orderPaymentStatusBO);
        return orderPaymentStatusBO;
	}

	public List<OrderPaymentStatusBO> findOrderCancelReason(
			List<OrderCancelReasonBO> orderCancelReasons) {
		// TODO Auto-generated method stub
		return null;
	}	
}

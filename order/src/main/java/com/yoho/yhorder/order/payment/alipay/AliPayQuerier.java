package com.yoho.yhorder.order.payment.alipay;

import java.util.Map;

import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.exception.ServiceException;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.common.utils.MD5Utils;
import com.yoho.yhorder.common.utils.RSAUtils;
import com.yoho.yhorder.order.config.OrderConstant;
import com.yoho.yhorder.order.model.PayQueryBo;
import com.yoho.yhorder.order.model.PayRefundBo;
import com.yoho.yhorder.order.model.PayType;
import com.yoho.yhorder.order.model.RequestBuilder;

/**
 * Created by JXWU on 2016/1/22.
 */
@Component
public class AliPayQuerier {

    private final Logger logger = LoggerFactory.getLogger("payConfirmLog");

    // 合作身份者ID，以2088开头由16位纯数字组成的字符串
    public static String PARTNER = "2088701661478015";

    // 字符编码格式 目前支持 gbk 或 utf-8
    public static String INPUT_CHARSET = "utf-8";

    public static String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOfywqINCNC+IFvg" +
            "zqq3aEYxmh89WamWL59FukMKoKYMDR1NWMgFLNX2ld6aRWWdlJCBUsTzH/8uabEj" +
            "fBOM5BK+N08GfkpCYRmzww2y1H8RZ4P3wtKu95UYdaq3Ir5ucfgKxGy/1ay2qQFZ" +
            "PAkvPtTw+qdkVOgPakvqCWgcFL8LAgMBAAECgYAYeTnmJV/vvo/lgePsiWucNHGh" +
            "qDSEu08NDCtqFX375zufAuFCQaGIdfs8QKTf4u+hC7QzCcAvibMnOKpH2c7apAEc" +
            "RUfTUUcY/UB/yIkvzjkvkMKZSXI6y3lyFn8mNEwQei9u4OdSg1IBVuWqZoyqilCm" +
            "ARMzXyoeAOY55MelSQJBAPP+sEbPadMENkzzkiiHCHuo3Y0OEF5Zv2h3LJmtRx/6" +
            "B60mrfaUSSbU/iNq0uzQSoDQlY2DxXb21l1LYZzgdn8CQQDzXFXNj/qkXIuTLjfK" +
            "YxIhwiNKYXXKeG9C6+MlviY2zVrKruuuHJMcJ8238hzRcPsX+TCwFd3VhHuL61jB" +
            "2+l1AkBhUNTH+VQQ6N4rhP5nkawNfkWXS+O1bgBMzzOHu7fhhhznr8S002H1zf/q" +
            "6mFkOJNum0L65XKtxzeqkDVHl7NLAkEAg/jKvxMZRRC60DH8J1DagFwbbzay/f2Z" +
            "uJzbLZiUeJucZNW/EUiFrnsXYG13m0y9nh6QfK0fA684oIQcOeTcEQJBAIxCxNUP" +
            "jXsvqTV2ypek3ktvutFwSFuvo0zD2sn2HNlMSfh3K6RZV2Q0Q4W7bLELKEEkxmJX" +
            "IMLSK3hQB7jYD0Q=";

    public static String PRIVATE_KEY_MD5 = "kcxawi9bb07mzh0aq2wcirsf9znusobw";  // MD5

    private String signType = "RSA";

    @Value("${alipay.gateway}")
    private String aliPayGatewayUrl;


    @Autowired
    private ServiceCaller serviceCaller;

    public PayQueryBo query(String tradeNo) {
        logger.info("enter AliPayQuerier to query tradeNo {}", tradeNo);
        RequestBuilder builder = newRequestBuider(tradeNo);
        String responseText = sendRequest(builder);
        PayQueryBo queryBo = convert(responseText);
        logger.info("exit AliPayQuerier query result {}", queryBo);
        return queryBo;
    }
    
    public PayQueryBo query(String tradeNo, PayType payType) {
    	PayQueryBo queryBo = query(tradeNo);
    	queryBo.payType = payType;
    	logger.info("AliPayQuerier payment, tradeNo: {}, payment: {}", tradeNo, payType.getPayId());
    	return queryBo;
    }

    private RequestBuilder newRequestBuider(String tradeNo) {
        RequestBuilder builder = new RequestBuilder();
        builder.addParameter("service", "single_trade_query")
                .addParameter("partner", PARTNER)
                .addParameter("out_trade_no", tradeNo)
                .addParameter("_input_charset", INPUT_CHARSET);

        String signStr = sign(builder.toLinkString());
        builder.addParameter("sign", signStr)
                .addParameter("sign_type", signType);
        return builder;
    }

    private String sign(String prestr) {
        String mysign = "";
        if ("RSA".equals(signType)) {
            mysign = RSAUtils.sign(prestr, PRIVATE_KEY, INPUT_CHARSET);
        } else if ("MD5".equals(signType)) {
            mysign = MD5Utils.sign(prestr, PRIVATE_KEY_MD5, INPUT_CHARSET);
        }
        return mysign;
    }

    private String sendRequest(RequestBuilder builder) throws ServiceException {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();
        for (Map.Entry<String, String> entry : builder.getParamsMap().entrySet()) {
            request.add(entry.getKey(), entry.getValue());
        }

        logger.info("AliPayQuerier post request is {}", request);
        String responseText = null;
        try {
            responseText = serviceCaller.post("order.AliPay", aliPayGatewayUrl, request, String.class, null).get();
        } catch (Exception e) {
            logger.warn("AliPayQuerier post {} error,responseText is {}", aliPayGatewayUrl, responseText, e);
        }
        logger.info("AliPayQuerier post response is {}", responseText);
        return responseText;
    }

    /**
     * 将xml转换为对象
     *
     * @param xml
     * @return
     */
    private PayQueryBo convert(String xml) {
        PayQueryBo queryBo = new PayQueryBo();
        if (StringUtils.isEmpty(xml)) {
            return queryBo;
        }
        try {

            XMLSerializer xmlSerializer = new XMLSerializer();
            net.sf.json.JSON json = xmlSerializer.read(xml);
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) json;

            if ("T".equals(jsonObject.getString("is_success"))) {
                //成功
                net.sf.json.JSONObject responseJson = jsonObject.getJSONObject("response");
                net.sf.json.JSONObject tradeJson = responseJson.getJSONObject("trade");
                if ("TRADE_SUCCESS".equals(tradeJson.getString("trade_status"))) {
                    queryBo.valid = true;
                    queryBo.orderCode = tradeJson.getString("out_trade_no");
                    queryBo.amount = tradeJson.getDouble("total_fee");
                    queryBo.payOrderCode = tradeJson.getString("out_trade_no");
                    queryBo.tradeNo = tradeJson.getString("trade_no");
                    queryBo.paymentTime = (null == tradeJson.get("gmt_payment") ? "" : tradeJson.getString("gmt_payment"));
                    queryBo.callbackTime = (null == tradeJson.get("notify_time") ? DateUtil.getCurrentTime() : tradeJson.getString("notify_time"));

                    //queryBo.payType = PayType.ALIPAY;  --后续主动查询需支持APP端、PC端、H5端支付宝、支付宝直连，故这里不能写死
                }
            }

            if (queryBo.valid == false) {
                logger.warn("AliPayQuerier query trade is invalid,trade record is {}", xml);
            }

        } catch (Exception e) {
            logger.warn("AliPayQuerier convert {}  error", xml, e);
        }
        return queryBo;
    }

    
    public PayRefundBo refundQuery(PayRefundBo refundBo) {
        logger.info("enter AliPayQuerier refund query, tradeNo {}", refundBo.getOrderCode());
        RequestBuilder builder = newRequestBuider(String.valueOf(refundBo.getOrderCode()));
        String responseText = sendRequest(builder);
        refundBo = refundQueryConvert(responseText, refundBo);
        logger.info("exit AliPayQuerier refund query result, refundStatus: {}, refundMsg: {}", refundBo.getRefundStatus(), refundBo.getRefundMsg());
    	return refundBo;
    }
    
    
    /**
     * 将xml转换为对象
     *
     * @param xml
     * @return
     */
    private PayRefundBo refundQueryConvert(String xml, PayRefundBo bo) {
    	//PayRefundBo bo = new PayRefundBo();
        if (StringUtils.isEmpty(xml)) {
        	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_REQERR);
        	bo.setRefundMsg("退款结果查询失败");
            return bo;
        }
        try {

            XMLSerializer xmlSerializer = new XMLSerializer();
            net.sf.json.JSON json = xmlSerializer.read(xml);
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) json;

            if ("T".equals(jsonObject.getString("is_success"))) {
                //成功
                net.sf.json.JSONObject responseJson = jsonObject.getJSONObject("response");
                net.sf.json.JSONObject tradeJson = responseJson.getJSONObject("trade");

                //有金额填写金额
            	if(tradeJson.getString("refund_fee") != null) {
            		bo.setAmount(Double.valueOf(tradeJson.getString("refund_fee")));
            	}
            	
            	//查看退款状态
            	if(tradeJson.getString("refund_status") == null) {
            		bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_NOREFUND);
            		bo.setRefundMsg("没有申请过退款");
            	}                		
            	else if("REFUND_SUCCESS".equals(tradeJson.getString("refund_status"))) {
            		bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_SUCCESS);
            		bo.setRefundMsg("退款已完成");
            	}
            	else {
            		bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_NOTSUCCESSYET);
            		bo.setRefundMsg("退款未完成：" + tradeJson.getString("refund_status"));
            	}
 
            }
            else {
            	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_REQERR);
            	bo.setRefundMsg("退款结果查询失败: " + jsonObject.getString("error"));
            }
            
        } catch (Exception e) {
        	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_REQERR);
        	bo.setRefundMsg("退款结果查询失败");
        }
        return bo;    	
    }
    
}

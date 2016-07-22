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
import com.yoho.yhorder.common.utils.RSAUtils;
import com.yoho.yhorder.order.config.OrderConstant;
import com.yoho.yhorder.order.model.PayRefundBo;
import com.yoho.yhorder.order.model.RequestBuilder;

@Component
public class AliPayRefunder {
	
	private final Logger logger = LoggerFactory.getLogger("payConfirmLog");
	
    @Value("${alipay.gateway}")
    private String aliPayGatewayUrl;

    @Autowired
    private ServiceCaller serviceCaller;
    
    
    public PayRefundBo refund(PayRefundBo refundBo) {
    	logger.info("enter AliPayRefunder to refund tradeNo {}, amount {}", refundBo.getRefundOrderCode(), refundBo.getAmount());
    	RequestBuilder builder = newRequestBuider(String.valueOf(refundBo.getOrderCode()), refundBo.getRefundOrderCode(), refundBo.getAmount());
    	String responseText = sendRequest(builder);
    	//logger.info("refund response for alipay: {}", responseText);
    	PayRefundBo bo = convert(responseText, refundBo);
    	logger.info("exit AliPayRefunder refund, refundStatus: {}, refundMsg", bo.getRefundStatus(), bo.getRefundMsg());
    	return bo;
    }
    
    
    private RequestBuilder newRequestBuider(String tradeNo, String refundNo, double amount) {
        RequestBuilder builder = new RequestBuilder();
        builder.addParameter("service", "alipay.acquire.refund")
               .addParameter("partner", AlipayConfig.partner)
               .addParameter("_input_charset", AlipayConfig.input_charset)
               .addParameter("out_trade_no", tradeNo)
               .addParameter("out_request_no", refundNo)
               .addParameter("refund_amount", String.valueOf(amount));

        String signStr = RSAUtils.sign(builder.toLinkString(), AlipayConfig.private_key, AlipayConfig.input_charset);
        
        builder.addParameter("sign", signStr)
               .addParameter("sign_type", "RSA");
        return builder;
    }
    
    private String sendRequest(RequestBuilder builder) throws ServiceException {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();
        for (Map.Entry<String, String> entry : builder.getParamsMap().entrySet()) {
            request.add(entry.getKey(), entry.getValue());
        }

        logger.info("AliPayRefunder post request is {}", request);
        String responseText = null;
        try {
            responseText = serviceCaller.post("order.AliPay", aliPayGatewayUrl, request, String.class, null).get();
        } catch (Exception e) {
            logger.warn("AliPayRefunder post {} error,responseText is {}", aliPayGatewayUrl, responseText, e);
        }
        logger.info("AliPayRefunder post response is {}", responseText);
        return responseText;
    }
    
    
    /**
     * 将xml转换为对象
     *
     * @param xml
     * @return
     */
    private PayRefundBo convert(String xml, PayRefundBo bo) {
    	//PayRefundBo bo = new PayRefundBo();
        if (StringUtils.isEmpty(xml)) {
        	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_REQERR);
        	bo.setRefundMsg("退款申请请求失败");
            return bo;
        }
        try {

            XMLSerializer xmlSerializer = new XMLSerializer();
            net.sf.json.JSON json = xmlSerializer.read(xml);
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) json;

            if ("T".equals(jsonObject.getString("is_success"))) {
                //成功
                net.sf.json.JSONObject responseJson = jsonObject.getJSONObject("response");
                net.sf.json.JSONObject alipayJson = responseJson.getJSONObject("alipay");
                if ("SUCCESS".equals(alipayJson.getString("result_code"))) {
                	bo.setSerialNo(alipayJson.getString("trade_no"));
                    bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_SUCCESS);
                    bo.setRefundMsg("退款申请成功");
                }
                else if("FAIL".equals(alipayJson.getString("result_code"))) {
                	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
                	bo.setRefundMsg(alipayJson.getString("detail_error_code") + ": " + alipayJson.getString("detail_error_des"));
                }
                else {
                	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_UNKNOWN);
                	bo.setRefundMsg("退款申请结果未知");
                }
            }
            else {
            	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_REQERR);
            	bo.setRefundMsg("退款申请请求失败: " + jsonObject.getString("error"));
            }
            
        } catch (Exception e) {
        	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_REQERR);
        	bo.setRefundMsg("退款申请请求失败");
        }
        return bo;
    }


//    public static void main(String[] args) {
//    	String refund_str = "<?xml version=\"1.0\" encoding=\"GBK\"?><alipay><is_success>T</is_success><request><param name=\"sign\">25gx+QsTS7nmFJNF12DgojU5gu1nffKhDGwFU/PhGOCDMt7PF+2mCW9TkYWV/dnyBGEmJqhMrqwczAw0noo1E0v796dCJlNCaYaGANIlCefQ5TWUenxpIhtN7Uwpe5Tqz/YVujYDEPJke6pWxnvRVCUvF76DbeF8IfDJWBq9haU=</param><param name=\"_input_charset\">utf-8</param><param name=\"sign_type\">RSA</param><param name=\"service\">alipay.acquire.refund</param><param name=\"partner\">2088701661478015</param><param name=\"refund_amount\">0.01</param><param name=\"out_trade_no\">1615698830</param></request><response><alipay><buyer_logon_id>tao***@163.com</buyer_logon_id><buyer_user_id>2088502902755470</buyer_user_id><fund_change>Y</fund_change><gmt_refund_pay>2016-06-15 10:54:44</gmt_refund_pay><out_trade_no>1615698830</out_trade_no><refund_fee>0.01</refund_fee><result_code>SUCCESS</result_code><trade_no>2016061421001004470225529697</trade_no></alipay></response><sign>CvuAvrFHXzjIf09wyTkC5E/ezPXvzHAliFXMkJsmqXHhHC4Zyy5jT1dOt7XtPup1w8eTo/zgG4MHsW3JGV33VPxm/NQ/eHQXZRdwJY5W+DV47y1Z5a5q9TbbIElQ+uAfw1OF1QjqidKa/n5gBUQyMP14ekjboQw+/E+07k2rt64=</sign><sign_type>RSA</sign_type></alipay>";
//    	AliPayRefunder refundServic = new  AliPayRefunder();
//    	PaymentOrderQueryBO bo = refundServic.convert(refund_str);
//    	System.out.println("===" + bo.getResultCode() + ":" + bo.getResultMsg());
//    }
}

package com.yoho.yhorder.order.payment.wechat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;

import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.yoho.core.common.utils.YHMath;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.service.model.order.response.PaymentOrderQueryBO;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.order.config.Constant;
import com.yoho.yhorder.order.config.OrderConstant;
import com.yoho.yhorder.order.model.PayQueryBo;
import com.yoho.yhorder.order.model.PayRefundBo;
import com.yoho.yhorder.order.model.PayType;
import com.yoho.yhorder.order.payment.tenpay.util.MD5Util;

/**
 * Created by JXWU on 2016/1/25.
 *
 *  https://pay.weixin.qq.com/wiki/doc/api/app.php?chapter=9_2&index=4
 *
 *  相对WechatQuerier，不需要accesstoken
 *
 */
public abstract class NewWechatQuerier {
    private final Logger logger = LoggerFactory.getLogger("payConfirmLog");

    // App 支付参数
	// @Value("${wechat.wap.mchid}")
	// private String mchid;
	//
	// @Value("${wechat.wap.mchkey}")
	// private String mchkey;
	//
	// @Value("${wechat.wap.appid}")
	// private String appId;
	
	// @Value("${wechat.wap.appsecret}")
	// private String appSecret;

    @Value("${wechat.wap.queryurl}")
    private String queryUrl;	//微信App支付升级API后，也使用这个url查询
    
    private static final String refundQueryUrl = "https://api.mch.weixin.qq.com/pay/refundquery";

    @Autowired
    private ServiceCaller serviceCaller;
    
    //退款接口需要使用ssl
    private HttpClientBuilder httpClientBuilder;
    
	public void init() {
		logger.info("WechatQuerier init begin");
		//退款接口使用到ssl，查询接口无需使用
        try {
        	initSSLHttpFactory();
		} catch (Exception e) {
			logger.info("failed to init cert for ssl, exception: {}", e.getCause());
		} 
        logger.info("WechatQuerier init finished");
	}

    public PayQueryBo query(String tradeNo) {
        logger.info("enter NewWechatQuerier to query tradeNo {}", tradeNo);
        TreeMap<Object, Object> treeMap = buildRequestBuilder(tradeNo);
        String sign = getSignParamterValue(treeMap);
        String xml = buildXml(treeMap, sign);
        String responseText = sendRequest(queryUrl, xml);

        PayQueryBo queryBo = convert(responseText);
        logger.info("exit NewWechatQuerier query result {}", queryBo);
        return queryBo;
    }
    
    public PayQueryBo query(String tradeNo, PayType payType) {
    	PayQueryBo queryBo = query(tradeNo);
    	queryBo.payType = payType;
    	logger.info("NewWechatQuerier payment, tradeNo: {}, payment: {}", tradeNo, payType.getPayId());
    	return queryBo;
    }

    private String sendRequest(String url, String request) {
//        MultiValueMap<String, String> request = new LinkedMultiValueMap<String, String>();
//        for (Map.Entry<String, String> entry : builder.getParamsMap().entrySet()) {
//            request.add(entry.getKey(), entry.getValue());
//        }
        logger.info("NewWechatQuerier post {} request is {}", url, request);
        String responseText = null;
        try {
            responseText = serviceCaller.post("order.WechatWapPay", url, request, String.class, null).get();
        } catch (Exception e) {
            logger.warn("NewWechatQuerier post {} error,responseText is {}", url, responseText, e);
        }
        logger.info("NewWechatQuerier post response is {}", responseText);
        return responseText;
    }

    private TreeMap<Object, Object> buildRequestBuilder(String tradeNo) {

        TreeMap<Object, Object> treeMap = new TreeMap<Object, Object>();
        // RequestBuilder builder = new RequestBuilder();
        treeMap.put("appid", getAppId());
        treeMap.put("mch_id", getMchId());
        treeMap.put("out_trade_no", tradeNo);
        treeMap.put("nonce_str", getRandomString(32));

        //treeMap.put("sign", sign);
        return treeMap;
    }

    private String buildXml(TreeMap<Object, Object> treeMap, String sign) {
        StringBuilder xmlBuilder = new StringBuilder("<xml>");
        xmlBuilder.append("<appid>").append(treeMap.get("appid")).append("</appid>");
        xmlBuilder.append("<mch_id>").append(treeMap.get("mch_id")).append("</mch_id>");
        xmlBuilder.append("<nonce_str>").append(treeMap.get("nonce_str")).append("</nonce_str>");
        xmlBuilder.append("<out_trade_no>").append(treeMap.get("out_trade_no")).append("</out_trade_no>");
        xmlBuilder.append("<sign>").append(sign).append("</sign>");
        xmlBuilder.append("</xml>");

        return xmlBuilder.toString();
    }

    public String getSignParamterValue(TreeMap<Object, Object> treeMap) {
        String signStr = createSign("UTF-8", treeMap);
        return signStr.toUpperCase();
    }


    /**
     * @param characterEncoding 编码格式
     * @param parameters        请求参数
     * @return
     * @Description：sign签名
     */
    public String createSign(String characterEncoding, SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + getMchKey());
        String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }


    private PayQueryBo convert(String xml) {
        PayQueryBo queryBo = new PayQueryBo();
        if (StringUtils.isEmpty(xml)) {
            return queryBo;
        }
        try {
            XMLSerializer xmlSerializer = new XMLSerializer();
            net.sf.json.JSON json = xmlSerializer.read(xml);
            net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) json;
            String return_code = jsonObject.getString("return_code");
            if ("SUCCESS".equals(return_code)) {
                String trade_state = jsonObject.getString("trade_state");
                if ("SUCCESS".equals(trade_state)) {
                    queryBo.valid = true;
                    String out_trade_no = jsonObject.getString("out_trade_no");
                    int index = out_trade_no.indexOf(Constant.WECHAT_QUERY_TRADE_PREFIX);
                    if (index >= 0) {
                        out_trade_no = out_trade_no.substring(index + Constant.WECHAT_QUERY_TRADE_PREFIX.length());
                    }
                    queryBo.orderCode = out_trade_no;
                    double total_fee = jsonObject.getDouble("total_fee");
                    queryBo.amount = YHMath.mul(total_fee, 0.01);
                    queryBo.bankCode = jsonObject.getString("bank_type");
                    queryBo.bankName = jsonObject.getString("bank_type");
                    queryBo.tradeNo = jsonObject.getString("transaction_id");
                    queryBo.paymentTime = DateUtil.formatDateString(jsonObject.getString("time_end"),DateUtil.yyyyMMddHHmmss,DateUtil.yyyy_MM_dd_HH_mm_SS);
                    queryBo.callbackTime = DateUtil.getCurrentTime();
                    //queryBo.payType= PayType.WECHATWAP;
                }
            }
            if (queryBo.valid == false) {
                logger.warn("NewWechatQuerier query trade is invalid,trade record is {}", xml);
            }
        } catch (Exception e) {
            logger.warn("NewWechatQuerier convert {}  error", xml, e);
        }
        return queryBo;
    }


    public static String getRandomString(int length) {
        StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        int range = buffer.length();
        for (int i = 0; i < length; i++) {
            sb.append(buffer.charAt(random.nextInt(range)));
        }
        return sb.toString();
    }
    
    
    protected abstract String getMchId();
    
    protected abstract String getMchKey();
    
    protected abstract String getAppId();
    
    protected abstract String getMchCertPath();
    
    
    //*****************************************************
    //**	退款相关接口
    //*****************************************************
    
    public PayRefundBo refund(PayRefundBo refundBo) {
    	logger.info("enter WechatPayRefunder to refund tradeNo {}", refundBo.getRefundOrderCode());
        //TreeMap<Object, Object> treeMap = buildRefundRequestBuilder(Constant.WECHAT_QUERY_TRADE_PREFIX + refundBo.getOrderCode(), refundBo.getRefundOrderCode(), refundBo.getAmount());
        TreeMap<Object, Object> treeMap = buildRefundRequestBuilder(refundBo);
        String requestXml = createWeixinXml(treeMap);
        String responseXml = null;
		try {
			responseXml = sendRefundRequest(requestXml);
		} catch (Exception e) {
			logger.error("wechatpay refund failed: {}", e.getCause());
		}
		PayRefundBo bo = refundConvert(responseXml, refundBo);
    	logger.info("exit WechatPayRefunder refund result: {}", bo.getRefundStatus());
    	return bo;
    }
    
    public String sendRefundRequest(String requestXml) throws Exception {
    	CloseableHttpClient httpClient = getHttpClient();
    	if(httpClient == null) {
    		logger.error("http ssl client create failed");
    		return null;
    	}
    	logger.info("wechatpay refund request: {}", requestXml);
    	
        StringBuilder contentBuilder = new StringBuilder();
        try {

            HttpPost httppost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
            httppost.setEntity(new StringEntity(requestXml, "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httppost);
            
            try {
                HttpEntity entity = response.getEntity();
                logger.info("response statusLine: {}", response.getStatusLine());
                
                if(response.getStatusLine().getStatusCode() != 200) {
                	logger.error("wechatpay refund request failed");
                	return null;
                }
                
                if (entity != null) {
                    //System.out.println("Response content length: " + entity.getContentLength());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String text;
                    while ((text = bufferedReader.readLine()) != null) {
                    	contentBuilder.append(text);
                    }
                }
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
        	httpClient.close();
        }
        
        String content = new String(contentBuilder.toString().getBytes(), "UTF-8");
        logger.info("refund response from wechatpay: {}", content);
        return content;
    }
    
	/**
	 * 获取ssl http连接
	 */
	public CloseableHttpClient getHttpClient() {
		if (httpClientBuilder == null)
			return null;
		
		return httpClientBuilder.build();
	}
	
    private TreeMap<Object, Object> buildRefundRequestBuilder(PayRefundBo refundBo) {

        TreeMap<Object, Object> treeMap = new TreeMap<Object, Object>();
        treeMap.put("appid", getAppId());
        treeMap.put("mch_id", getMchId());
        treeMap.put("nonce_str", getRandomString(32));
        treeMap.put("out_trade_no", Constant.WECHAT_QUERY_TRADE_PREFIX + refundBo.getOrderCode());
        treeMap.put("out_refund_no", refundBo.getRefundOrderCode());
        
        //String totalFee = String.valueOf((int)YHMath.mul(amount, 100));	//微信金额必须以分为单位，且不能包含小数点
        treeMap.put("total_fee", String.valueOf((int)YHMath.mul(refundBo.getOrderTotalFee(), 100)));
        treeMap.put("refund_fee", String.valueOf((int)YHMath.mul(refundBo.getAmount(), 100)));
        treeMap.put("op_user_id", getMchId());
        
        //签名
        String signStr = createSign("UTF-8", treeMap);
        treeMap.put("sign", signStr);
      		
        return treeMap;
    }
    
    private PayRefundBo refundConvert(String responseXml, PayRefundBo bo) {
    	//PayRefundBo bo = new PayRefundBo();
        if (StringUtils.isEmpty(responseXml)) {
        	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_REQERR);
        	bo.setRefundMsg("退款申请请求失败");
            return bo;
        }
    	
    	Map<String, String> reponseMap =  parseWeixinXml(responseXml);
		if(!"SUCCESS".equals(reponseMap.get("return_code"))) {
			bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_REQERR);
			bo.setRefundMsg(reponseMap.get("return_msg"));
			return bo;
		}
		if(!"SUCCESS".equals(reponseMap.get("result_code"))) {
			bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
			bo.setRefundMsg(reponseMap.get("err_code") + ": " + reponseMap.get("err_code_des"));
			return bo;
		}
		
        bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_SUCCESS);
        bo.setSerialNo(reponseMap.get("refund_id"));
        bo.setRefundMsg("退款成功");

        return bo;
    }
    
	/**
	 * 生成微信支付应答或请求的xml（新版本API）
	 * @param paramMaps 参数对
	 * @return String 
	 */	
	public static String createWeixinXml(Map<Object, Object> paramMaps)
	{
		Element root = new Element("xml");  
		Document doc = new Document(root); 
		
		for(Map.Entry<Object, Object> entry : paramMaps.entrySet()){
			root.addContent(new Element((String)entry.getKey()).setText((String)entry.getValue()));
		}

		XMLOutputter xmlOut = new XMLOutputter();  

		return xmlOut.outputString(doc);
	}
	
	/**
	 * 微信支付请求应答XML解析（新版本API）
	 * @param String 请求或者应答的xml
	 * @return Map 
	 */		
	public static Map<String, String> parseWeixinXml(String responseXml)
	{
		if(responseXml == null)
			return null;
		
		Map<String, String> paramMaps = new HashMap<String, String>();

		try {
			SAXBuilder sax = new SAXBuilder();
			
			InputStream inputStream = new ByteArrayInputStream(responseXml.getBytes("UTF-8"));
			Document doc = sax.build(inputStream);

			Element root = doc.getRootElement();
			List nodeList = root.getChildren();  
			for(int i = 0; i < nodeList.size(); i++){
				Element node = (Element)nodeList.get(i);
				paramMaps.put(node.getName(), node.getValue());
			}
			
		} catch (Exception e) {
			//loggerErr.error("parse wexin notify response failed: {}, error", responseXml, e);
			return null;
		}
		
		return paramMaps;
	}
	
	/**
	 * 初始化退款请求所需的双向证书
	 * @throws Exception
	 */
	private void initSSLHttpFactory() throws Exception {
    	logger.info("begin init cert for ssl");
    	
		KeyStore keyStore  = KeyStore.getInstance("PKCS12");
		InputStream instream = this.getClass().getResourceAsStream(getMchCertPath());
		if(instream == null ) {
			logger.error("failed to load cert file: {}", getMchCertPath());;
			return;
		}
				
		try {
		    keyStore.load(instream, getMchId().toCharArray());
		    logger.info("finish load keyStore: {}", getMchId());
		} finally {
		    instream.close();
		}

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom()
		        .loadKeyMaterial(keyStore, getMchId().toCharArray())
		        .build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		        sslcontext,
		        new String[] { "TLSv1" },
		        null,
		        SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		
		httpClientBuilder = HttpClients.custom()
		        .setSSLSocketFactory(sslsf);
		
		logger.info("httpClientBuilder prepare finished.");
	}
	
	
	public PayRefundBo refundQuery(PayRefundBo refundBo) {
        TreeMap<Object, Object> treeMap = buildRefundQuery(refundBo);
        String requestXml = createWeixinXml(treeMap);
        String responseText = sendRequest(refundQueryUrl, requestXml);
		PayRefundBo bo = refundQueryConvert(responseText, refundBo);
    	logger.info("exit WechatPayRefunder refund result: {}", bo.getRefundStatus());
    	return bo;
		
	}
	
	
    private TreeMap<Object, Object> buildRefundQuery(PayRefundBo refundBo) {

        TreeMap<Object, Object> treeMap = new TreeMap<Object, Object>();
        treeMap.put("appid", getAppId());
        treeMap.put("mch_id", getMchId());
        treeMap.put("nonce_str", getRandomString(32));
        treeMap.put("out_trade_no", Constant.WECHAT_QUERY_TRADE_PREFIX + refundBo.getOrderCode());
        
        //签名
        String signStr = createSign("UTF-8", treeMap);
        treeMap.put("sign", signStr);
      		
        return treeMap;
    }
    
    
    PayRefundBo refundQueryConvert(String responseText, PayRefundBo bo) {
        if (StringUtils.isEmpty(responseText)) {
        	bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_STATUS_REQERR);
        	bo.setRefundMsg("退款申请请求失败");
            return bo;
        }
    	
    	Map<String, String> reponseMap =  parseWeixinXml(responseText);
		if(!"SUCCESS".equals(reponseMap.get("return_code"))) {
			bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_REQERR);
			bo.setRefundMsg(reponseMap.get("return_msg"));
			return bo;
		}
		if(!"SUCCESS".equals(reponseMap.get("result_code"))) {
			bo.setRefundStatus(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
			bo.setRefundMsg(reponseMap.get("err_code") + ": " + reponseMap.get("err_code_des"));
			return bo;
		}
		return bo;
    }
}

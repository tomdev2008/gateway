package com.yoho.yhorder.order.payment.qqwallet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.yoho.core.common.utils.YHMath;
import com.yoho.service.model.order.response.PaymentOrderQueryBO;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.order.config.OrderConstant;
import com.yoho.yhorder.order.model.PayQueryBo;
import com.yoho.yhorder.order.payment.tenpay.client.ClientResponseHandler;
import com.yoho.yhorder.order.payment.tenpay.client.TenpayHttpClient;
import com.yoho.yhorder.order.payment.tenpay.handlers.RequestHandler;
import com.yoho.yhorder.order.payment.tenpay.util.MD5Util;

@Component
public class QQWallet {

	private final Logger logger = LoggerFactory.getLogger("QQWallet");
	
	@Value("${qq.partnerid}")
	private String partnerid;

    @Value("${qq.partnerkey}")
	private String partnerkey;

    @Value("${qq.appid}")
    private String appId;

    @Value("${qq.appkey}")
    private String appKey;
    
    //读取证书相关配置信息,
    @Value("${qq.caPath}")
    private String caPath ;//= "E:/certs/qq/cacert.pem";
    
    @Value("${qq.certPath}")
    private String certPath	;//= "E:/certs/qq/1284612001_20160620172528.pfx";
    //证书密码,正式商户号的证书密码通过短信方式发送到合同登记的手机号，系统上线前请注意修改为正确值
    
    @Value("${qq.certPassword}")
    private String certPassword ;//= "100128";
    
    @Value("${qq.partnerPassword}")
    private String userpasswd;
    
    
	/**
	 * qq钱包订单查询
	 * @param ordercode
	 * @return
	 */
	public PayQueryBo queryTrans(String ordercode) {
		logger.info("(qqwallet.queryTrans) enter qqwallet query , params is ordercode={}",ordercode);
	    //创建查询请求对象
	    RequestHandler reqHandler = setQueryRequestHandler(ordercode);
	    //通信对象
	    TenpayHttpClient httpClient = new TenpayHttpClient();
	    httpClient.setTimeOut(5);	
	    PayQueryBo queryBo = sendQueryRequest(reqHandler,httpClient);
	    logger.info("(qqwallet.queryTrans) exit qqwallet query ,params is ordercode={},result={}",ordercode,queryBo);
		return queryBo;
	}
	
	/**
	 * qq钱包订单退款
	 * @param ordercode
	 * @param amount
	 * @return
	 */
	public PaymentOrderQueryBO refund(String ordercode,double amount){
		logger.info("(qqwallet.refund) enter qqwallet refund , params is ordercode={},amount={}",ordercode,amount);
	    //创建查询请求对象
	    RequestHandler reqHandler = setRefundRequestHandler(ordercode,amount);
//	    RequestHandler reqHandler =s(ordercode);
	    //通信对象
	    TenpayHttpClient httpClient = setRefundHttpclient();
	    //发送请求
	    PaymentOrderQueryBO result = sendRefundRequest(reqHandler,httpClient);
	    logger.info("(qqwallet.refund) exit qqwallet refund ,params is ordercode={},amount={},result={}",ordercode,amount,result);
	    return result;
	}
	
	private RequestHandler setQueryRequestHandler(String ordercode){
		RequestHandler reqHandler = new RequestHandler(null, null);
		//-----------------------------
	    //设置请求参数
	    //-----------------------------
	    reqHandler.init();
	    reqHandler.setKey(partnerkey);
	    reqHandler.setGateUrl("https://gw.tenpay.com/gateway/normalorderquery.xml");
	    
	    //-----------------------------
	    //设置接口参数
	    //-----------------------------
	    reqHandler.setParameter("partner", partnerid);    //商户号
	    
	    //out_trade_no和transaction_id至少一个必填，同时存在时transaction_id优先
	    reqHandler.setParameter("out_trade_no", ordercode);    	    	                //商家订单号
	    //reqHandler.setParameter("transaction_id", "1900000109201101270026218385");	//财付通交易单号    
	    return reqHandler;
	}
	

	private RequestHandler setRefundRequestHandler(String ordercode,double amount){
		//创建查询请求对象
	    RequestHandler reqHandler = new RequestHandler(null, null);
	    //-----------------------------
	    //设置请求参数
	    //-----------------------------
	    reqHandler.init();
	    reqHandler.setKey(partnerkey);
	    reqHandler.setGateUrl("https://api.mch.tenpay.com/refundapi/gateway/refund.xml");
	    
	    //-----------------------------
	    //设置接口参数
	    //-----------------------------
	    reqHandler.setParameter("service_version", "1.1");
	    reqHandler.setParameter("partner", partnerid);	
	    reqHandler.setParameter("out_trade_no", ordercode);	
	    reqHandler.setParameter("out_refund_no", ordercode+"yoho");
	    String totalFee = String.valueOf((int)YHMath.mul(amount, 100));
	    reqHandler.setParameter("total_fee", totalFee);	
	    reqHandler.setParameter("refund_fee", totalFee);
	    reqHandler.setParameter("op_user_id", partnerid);	//"1900000109"
	    //操作员密码,MD5处理                                        1284612001          100898104
	    reqHandler.setParameter("op_user_passwd",userpasswd );	// md5加密过的
	    	
	    return reqHandler;
	}
	
	private TenpayHttpClient setRefundHttpclient(){
		TenpayHttpClient httpClient = new TenpayHttpClient();
		//-----------------------------
	    //设置通信参数
	    //-----------------------------
	    //设置请求返回的等待时间
	    httpClient.setTimeOut(5);	
	    
	    org.springframework.core.io.Resource resource = new ClassPathResource("/certs");
        String dirpathRaw = null;
        try {
            dirpathRaw = resource.getURI().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dirpath = dirpathRaw.replace("file:", "");
        System.err.println(dirpath+caPath);
	    //设置ca证书
	    httpClient.setCaInfo(new File(dirpath+caPath));		
	    //设置商户证书
	    httpClient.setCertInfo(new File(dirpath+certPath), certPassword);
	    
	    //设置发送类型POST
	    httpClient.setMethod("POST");  
	    return httpClient;
	}
	
	private PayQueryBo sendQueryRequest(RequestHandler reqHandler,TenpayHttpClient httpClient){
		//应答对象
	    ClientResponseHandler resHandler = new ClientResponseHandler();
		PayQueryBo queryBo = new PayQueryBo();
	    //queryBo.orderCode=ordercode;
	    //设置请求内容
	    try {
			String requestUrl = reqHandler.getRequestURL();
			httpClient.setReqContent(requestUrl);
			String rescontent = "null";
			//后台调用
			if(httpClient.call()) {
				//设置结果参数
				rescontent = httpClient.getResContent();
				resHandler.setContent(rescontent);
				resHandler.setKey(partnerkey);
				//获取返回参数
				String retcode = resHandler.getParameter("retcode");
				
				//判断签名及结果
				if(resHandler.isTenpaySign()&& "0".equals(retcode)) {
					logger.info("(qqwallet.sendQueryRequest) query success,result={}",resHandler.getAllParameters());
					
					//商户订单号
		            String out_trade_no = resHandler.getParameter("out_trade_no");
		            //财付通订单号
		            String transaction_id = resHandler.getParameter("transaction_id");
		            //金额,以分为单位
		            String total_fee = resHandler.getParameter("total_fee");
		            //如果有使用折扣券，discount有值，total_fee+discount=原请求的total_fee
		            String discount = resHandler.getParameter("discount");
		            //支付结果
		            String trade_state = resHandler.getParameter("trade_state");
		            //支付成功
		            if("0".equals(trade_state)) {
		            	//业务处理
		            	logger.info("transaction_id=" + transaction_id +" out_trade_no=" + out_trade_no+" total_fee=" + total_fee +" discount=" + resHandler.getParameter("discount"));
		            	queryBo.valid = true;
		            	queryBo.orderCode = out_trade_no;
		            	double totalfee = Double.parseDouble(total_fee == null ? "0" : total_fee);
		            	queryBo.amount = YHMath.mul(totalfee, 0.01);
	                    queryBo.bankCode = resHandler.getParameter("bank_type");
	                    queryBo.bankName = resHandler.getParameter("bank_type");
	                    queryBo.tradeNo = transaction_id;
	                    queryBo.paymentTime = DateUtil.formatDateString(resHandler.getParameter("time_end"),DateUtil.yyyyMMddHHmmss,DateUtil.yyyy_MM_dd_HH_mm_SS);
	                    queryBo.callbackTime = DateUtil.getCurrentTime();
		    	        return queryBo;              
		            }
				} else {
					//错误时，返回结果未签名，记录retcode、retmsg看失败详情。
					logger.warn("(qqwallet.sendQueryRequest) retcode:" + resHandler.getParameter("retcode")+" retmsg:" + resHandler.getParameter("retmsg"));
				}	
			} else {
				logger.warn("(qqwallet.sendQueryRequest) calls failed,responsecode={},errinfo={}",httpClient.getResponseCode(),httpClient.getErrInfo());   
				//有可能因为网络原因，请求已经处理，但未收到应答。
			}
			
			//获取debug信息,建议把请求、应答内容、debug信息，通信返回码写入日志，方便定位问题
			logger.info("(qqwallet.sendQueryRequest) http res:" + httpClient.getResponseCode() + "," + httpClient.getErrInfo()
					+"\r\n req url:" + requestUrl
					+"\r\n req debug:" + reqHandler.getDebugInfo()
					+"\r\n res content:" + rescontent
					+"\r\n res debug:" + resHandler.getDebugInfo());
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.warn("(qqwallet.sendQueryRequest) sendRequest failed,e={}",e.getMessage());;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.warn("(qqwallet.sendQueryRequest) sendRequest failed,e={}",e.getMessage());
		}
	    return queryBo;
	}
	
	private PaymentOrderQueryBO sendRefundRequest(RequestHandler reqHandler,TenpayHttpClient httpClient){
		//应答对象
	    ClientResponseHandler resHandler = new ClientResponseHandler();
	    PaymentOrderQueryBO result=new PaymentOrderQueryBO();
	    try {
			String requestUrl = reqHandler.getRequestURL();
			httpClient.setReqContent(requestUrl);
			String rescontent = "null";

			//后台调用
			if(httpClient.call()) {
				//设置结果参数
				rescontent = httpClient.getResContent();
				resHandler.setContent(rescontent);
				resHandler.setKey(partnerkey);
				   	
				//获取返回参数
				String retcode = resHandler.getParameter("retcode");
				
				//判断签名及结果
				if(resHandler.isTenpaySign()&& "0".equals(retcode)) {
					/*退款状态	refund_status	
						4，10：退款成功。
						3，5，6：退款失败。
						8，9，11:退款处理中。
						1，2: 未确定，需要商户原退款单号重新发起。
						7：转入代发，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，资金回流到商户的现金帐号，需要商户人工干预，通过线下或者财付通转账的方式进行退款。
						*/
					String refund_status=resHandler.getParameter("refund_status");
					String out_refund_no=resHandler.getParameter("out_refund_no");
					String refund_fee=resHandler.getParameter("refund_fee");
					result.setOrderCode(resHandler.getParameter("out_trade_no"));
//					System.err.println(resHandler.getContent());
					switch(Integer.parseInt(refund_status)){
					case 1:
					case 2:
						result.setResultMsg("未确定，需要商户原退款单号重新发起");
						result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_UNKNOWN);
						break;
					case 3:
					case 5:
					case 6:
						result.setResultMsg("退款失败");
						result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
						break;
					case 4:
					case 10:
						result.setResultMsg("退款成功");
						result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_SUCCESS);
						double refundfee = Double.parseDouble(refund_fee == null ? "0" : refund_fee);
		            	result.setAmount(YHMath.mul(refundfee, 0.01));
		            	result.setTradeNo(resHandler.getParameter("transaction_id"));
						break;
					case 8:
					case 9:
					case 11:
						result.setResultMsg("退款处理中");
						result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
						break;
					case 7:
						result.setResultMsg("转入代发");
						result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
						break;
					default:
						result.setResultMsg("返回结果不对");
						result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
						break;
					}
					logger.info("(qqwallet.sendRefundRequest) out_refund_no="+out_refund_no+",refund status is"+refund_status);
				} else {
					if("0".equals(retcode)){
						logger.warn("(qqwallet.sendRefundRequest) Signature verification failed");
					}else{
						logger.warn("(qqwallet.sendRefundRequest) Business failed");
					}
					result.setResultMsg(resHandler.getParameter("retmsg"));
					result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
					//错误时，记录retcode、retmsg看失败详情。
					logger.warn("(qqwallet.sendRefundRequest) retcode:" + resHandler.getParameter("retcode")+" retmsg:" + resHandler.getParameter("retmsg"));
				}	
				
			} else {
				result.setResultMsg("请求qq钱包失败");
				result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
				logger.warn("(qqwallet.sendRefundRequest) calls failed,responsecode={},errinfo={}",httpClient.getResponseCode(),httpClient.getErrInfo());   	
				//有可能因为网络原因，请求已经处理，但未收到应答。
			}
			
			//获取debug信息,建议把请求、应答内容、debug信息，通信返回码写入日志，方便定位问题
			logger.info("(qqwallet.sendRefundRequest) http res:" + httpClient.getResponseCode() + "," + httpClient.getErrInfo()
					+"\r\n req url:" + requestUrl
					+"\r\n req debug:" + reqHandler.getDebugInfo()
					+"\r\n res content:" + rescontent
					+"\r\n res debug:" + resHandler.getDebugInfo());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			result.setResultMsg(e.getMessage());
			result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
			logger.warn("(qqwallet.sendRefundRequest) sendRequest failed,e={}",e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result.setResultMsg(e.getMessage());
			result.setResultCode(OrderConstant.PAYMENT_REFUND_RESULTCODE_FAIL);
			logger.warn("(qqwallet.sendRefundRequest) sendRequest failed,e={}",e.getMessage());
		}
	    
	    return result;
	}
}

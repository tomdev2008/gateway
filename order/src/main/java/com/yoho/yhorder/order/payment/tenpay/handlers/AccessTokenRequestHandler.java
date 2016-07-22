package com.yoho.yhorder.order.payment.tenpay.handlers;

import com.yoho.yhorder.order.payment.tenpay.client.TenpayHttpClient;
import com.yoho.yhorder.order.payment.tenpay.util.ConstantUtil;
import com.yoho.yhorder.order.payment.tenpay.util.JsonUtil;
import com.yoho.yhorder.order.payment.tenpay.util.WXUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AccessTokenRequestHandler extends RequestHandler {

	private static final Logger logger = LoggerFactory.getLogger("wechatLogger");

	public AccessTokenRequestHandler(HttpServletRequest request,
			HttpServletResponse response) {
		super(request, response);
	}

	private static String access_token = "";

	/**
	 * 获取凭证access_token
	 * @return
	 */
	public static String getAccessToken(String appId, String appSecret) {
		if ("".equals(access_token)) {// 如果为空直接获取
			return getTokenReal(appId, appSecret);
		}

		if (tokenIsExpire(access_token, appId, appSecret)) {// 如果过期重新获取
			return getTokenReal(appId, appSecret);
		}
		return access_token;
	}

	/**
	 * 实际获取access_token的方法
	 * @return
	 */
	protected static String getTokenReal(String appId, String appSecret) {
		String requestUrl = ConstantUtil.TOKENURL + "?grant_type=" + ConstantUtil.GRANT_TYPE + "&appid="
				+ appId + "&secret=" + appSecret;
		String resContent = "";
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setMethod("GET");
		httpClient.setReqContent(requestUrl);
		if (httpClient.call()) {
			resContent = httpClient.getResContent();
			if (resContent.indexOf(ConstantUtil.ACCESS_TOKEN) > 0) {
				access_token = JsonUtil.getJsonValue(resContent, ConstantUtil.ACCESS_TOKEN);
			} else {
				logger.error("GET access_token return error!" + httpClient.getErrInfo());
			}
		} else {
			logger.error("Get access_token communication failed! Rescode: {}, Err info: {}", httpClient.getResponseCode(), httpClient.getErrInfo());
			// 有可能因为网络原因，请求已经处理，但未收到应答。
		}

		return access_token;
	}

	/**
	 * 判断传递过来的参数access_token是否过期
	 * @param access_token
	 * @return
	 */
	public static boolean tokenIsExpire(String access_token, String appId, String appKey) {
		boolean flag = false;
		PrepayIdRequestHandler wxReqHandler = new PrepayIdRequestHandler(null, null);
		wxReqHandler.setParameter("appid", appId);
		wxReqHandler.setParameter("appkey",appKey);
		wxReqHandler.setParameter("noncestr", WXUtil.getNonceStr());
		wxReqHandler.setParameter("package", ConstantUtil.packageValue);
		wxReqHandler.setParameter("timestamp", WXUtil.getTimeStamp());
		wxReqHandler.setParameter("traceid", ConstantUtil.traceid);

		// 生成支付签名
		String sign = wxReqHandler.createSHA1Sign();
		wxReqHandler.setParameter("app_signature", sign);
		wxReqHandler.setParameter("sign_method", ConstantUtil.SIGN_METHOD);
		String gateUrl = ConstantUtil.GATEURL + access_token;
		wxReqHandler.setGateUrl(gateUrl);

		// 发送请求
		String accesstoken = wxReqHandler.sendAccessToken();
		if (ConstantUtil.EXPIRE_ERRCODE.equals(accesstoken) || ConstantUtil.FAIL_ERRCODE.equals(accesstoken))
			flag = true;
		return flag;
	}

}

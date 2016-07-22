package com.yoho.yhorder.order.payment.wechat;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 微信支付交易查询（WAP支付、PC端扫码支付）
 * 其商户号和APP移动支付不一样
 */
@Component("wechatWapQuerier")
public class WechatWapQuerier extends NewWechatQuerier {
	
    @Value("${wechat.wap.mchid}")
    private String mchid;

    @Value("${wechat.wap.mchkey}")
    private String mchkey;

    @Value("${wechat.wap.appid}")
    private String appId;
    
    public static final String WECHAT_PAY_PARTNER_CERT = "/certs_wx/apiclient_cert_web.p12";
	
    @PostConstruct
    public void init() {
    	super.init();
    }
    
	@Override
	protected String getMchId() {
		return mchid;
	}

	@Override
	protected String getMchKey() {
		return mchkey;
	}

	@Override
	protected String getAppId() {
		return appId;
	}
	
	@Override
	protected String getMchCertPath() {
		return WECHAT_PAY_PARTNER_CERT;
	}

}

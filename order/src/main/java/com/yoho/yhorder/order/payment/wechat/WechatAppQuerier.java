package com.yoho.yhorder.order.payment.wechat;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("wechatAppQuerier")
public class WechatAppQuerier extends NewWechatQuerier {
    // App 支付参数
    @Value("${wechat.app.partnerid}")
    private String mchid;

    @Value("${wechat.app.partnerkey}")
    private String mchkey;

    @Value("${wechat.app.appid}")
    private String appId;
    
    public static final String WECHAT_PAY_PARTNER_CERT = "/certs_wx/apiclient_cert_app.p12";
	
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

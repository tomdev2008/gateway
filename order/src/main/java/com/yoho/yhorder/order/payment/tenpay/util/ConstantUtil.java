package com.yoho.yhorder.order.payment.tenpay.util;

public class ConstantUtil {
	/**
	 * 商家可以考虑读取配置文件
	 */
	// {"app_id":"wx049fdaa3ba9cdd7a",
	// "app_secret":"f973fdb412307ea7b97d0252fd675104",
	// "partner_key":"b22de5cfd0ded341e0516505f72649a9",
	// "pay_sign_key":"wGwAsgU5SeeM62glYaoC6ALBKhtOrF7Ek9LzE8trEuUG7jHeFdnSlyA1jblOYYS57QzWr8dYVsWGdeWhzeonnrKFZakgwFWPYVtyeP4XqSu9Qvxps8LEgxoFBEpRPm6C",
	// "partner_id":1218934901}
	
	//初始化
	//public static String APP_ID = "wx049fdaa3ba9cdd7a"; // "wxd930ea5d5a258f4f";//微信开发平台应用id
	//public static String APP_SECRET = "f973fdb412307ea7b97d0252fd675104"; //"db426a9829e4b49a0dcac7b4162da6b6";//应用对应的凭证
	//应用对应的密钥
	//public static String APP_KEY = "wGwAsgU5SeeM62glYaoC6ALBKhtOrF7Ek9LzE8trEuUG7jHeFdnSlyA1jblOYYS57QzWr8dYVsWGdeWhzeonnrKFZakgwFWPYVtyeP4XqSu9Qvxps8LEgxoFBEpRPm6C"; // "L8LrMqqeGRxST5reouB0K66CaYAWpqhAVsq7ggKkxHCOastWksvuX1uvmvQclxaHoYd3ElNBrNO2DHnnzgfVG9Qs473M3DTOZug5er46FhuGofumV8H2FVR9qkjSlC5K";
	//public static String PARTNER = "1218934901"; // "1900000109";//财付通商户号
	//public static String PARTNER_KEY = "b22de5cfd0ded341e0516505f72649a9"; // "8934e7d15453e97507ef794cf7b0519d";//商户号对应的密钥
	public static String TOKENURL = "https://api.weixin.qq.com/cgi-bin/token";//获取access_token对应的url
	public static String GRANT_TYPE = "client_credential";//常量固定值 
	public static String EXPIRE_ERRCODE = "42001";//access_token失效后请求返回的errcode
	public static String FAIL_ERRCODE = "40001";//重复获取导致上一次获取的access_token失效,返回错误码
	public static String GATEURL = "https://api.weixin.qq.com/pay/genprepay?access_token=";//获取预支付id的接口url
	public static String ACCESS_TOKEN = "access_token";//access_token常量值
	public static String ERRORCODE = "errcode";//用来判断access_token是否失效的值
	public static String SIGN_METHOD = "sha1";//签名算法常量值
	//package常量值
	public static String packageValue = "bank_type=WX&body=%E8%AE%A2%E5%8D%95%E5%8F%B7%3A1619199705&fee_type=1&input_charset=UTF-8&notify_url=http%3A%2F%2Fdevservice.yoho.cn%3A58077%2Fpayment%2Fwechat_notify&out_trade_no=YOHOBuy_1619199705&partner=1218934901&sign=1E967995AA1F2E5DB5B03969ADEA2FA0&spbill_create_ip=172.16.8.137&time_expire=20160121181546&total_fee=239900";
	public static String traceid = "testtraceid001";//测试用户id

}

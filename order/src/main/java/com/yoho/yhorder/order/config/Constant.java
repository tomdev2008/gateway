package com.yoho.yhorder.order.config;

/**
 * Global constants definition
 */
public interface Constant {

	final  Integer ORDER_EXCHANGE_LIMIT_DAY =15*86400;

	//换货时间
	final  Integer EXCHANGE_LIMIT_DAY =18*86400;

	//换货缓存表示
	final  String CACHE_ORDER_EXCHANGE_CODE ="CACHE_ORDER_CHANGE_CODE_";

	//退货缓存表示
	String CACHE_ORDER_REFUND_CODE ="CACHE_ORDER_REFUND_CODE_";

	//保存快递信息缓存表示
	String CACHE_ORDER_SET_EXPRESS_CODE ="CACHE_ORDER_SET_EXPRESS_CODE_";

	//换货提交缓存时间
	final  Integer CHANGE_SUBMIT_TIM_SECOND =15;

	final  String TRUE ="Y";

	final  String FASLE ="N";

	//查询微信支付订单前缀
	 String WECHAT_QUERY_TRADE_PREFIX = "YOHOBuy_";

	//修改收货地址缓存表示
	String CACHE_ORDER_FIRST_UPDATE_ADDRESS_CODE ="CACHE_ORDER_FIRST_UPDATE_ADDRESS_CODE_";
}
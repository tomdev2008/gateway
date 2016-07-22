package com.yoho.yhorder.order.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单状态配置
 * @author lijian
 *
 */
public  class OrderConstant {
     
	/**
	 * 取消订单与支付的时间间隔判定标识，默认10分钟，单位秒；
	 */
	public static  final int PRE_PAY_Time=10*60;
	
	
    /**
     * 换货原因
     * @var unknown
     */

    //换货图片配置信息
    public static  final String IMG_BUCKET="evidenceImages";
    public static  final String IMG_POSITION="center";
    public static  final String IMG_BACKGROUND="YmxhY2s=";
    public static  final String IMG_CHANGE_BACKGROUND="d2hpdGU=";
    public static  final String IMG_CHANGE_BUCKET="goodsimg";

    //换货类型配置
    public static  final String YML_CHANGE_TYPE="changeType";

    //换货原因配置
    public static  final String YML_CHANGE_REASON_TYPE="exchangeType";


    public static final   List<Byte> CHANGE_REASON_TYPE = new ArrayList<>();
    public static List<Byte> getChangeReasonType() {
        return CHANGE_REASON_TYPE;
    }

    static
	{
        initChangeStatu();
 	}

    public synchronized static void initChangeStatu()
	{

        CHANGE_REASON_TYPE.add((byte) 4);
        CHANGE_REASON_TYPE.add((byte) 6);
        CHANGE_REASON_TYPE.add((byte) 8);
	}
	
    /**
     * 支付查询状态码
     */
    public static final int PAYMENT_QUERY_RESULTCODE_NOT_PAY = 0;		//未支付
    public static final int PAYMENT_QUERY_RESULTCODE_HAVE_PAY = 1;		//已支付
    public static final int PAYMENT_QUERY_RESULTCODE_ORDER_NULL = 2;	//订单不存在
    public static final int PAYMENT_QUERY_RESULTCODE_PAYMENT_NULL = 3;	//支付渠道不存在
    public static final int PAYMENT_QUERY_RESULTCODE_NOT_SUPPORT = 4;	//支付方式不支持主动确认
    
    /**
     * 支付退款状态码
     */
    public static final int PAYMENT_REFUND_RESULTCODE_FAIL = 1;		//退款失败
    public static final int PAYMENT_REFUND_RESULTCODE_SUCCESS = 2;	//退款成功
    public static final int PAYMENT_REFUND_RESULTCODE_UNKNOWN = 3;	//退款情况未知    
    public static final int PAYMENT_REFUND_RESULTCODE_UNABLE = 4;	//不能退款，如订单不存在、第三方没有查到付款等
    public static final int PAYMENT_REFUND_RESULTCODE_REQERR = 5;	//退款请求失败
    public static final int PAYMENT_REFUND_RESULTCODE_ORDERNULL = 6; //校验订单不存在
    public static final int PAYMENT_REFUND_RESULTCODE_AMOUNTDISMATCH = 7; //订单金额不一致
    public static final int PAYMENT_REFUND_RESULTCODE_ORDERNOTPAY = 8;	//订单为未支付状态
    
    public static final int PAYMENT_REFUND_STATUS_NOTSUCCESSYET = 1;	
    public static final int PAYMENT_REFUND_STATUS_SUCCESS = 2;
    public static final int PAYMENT_REFUND_STATUS_NOREFUND = 3;
    public static final int PAYMENT_REFUND_STATUS_UNABLE = 4;		//无法查询，如不支持的支付渠道
    public static final int PAYMENT_REFUND_STATUS_REQERR = 5;		//退款结果查询失败
}

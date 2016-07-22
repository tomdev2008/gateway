package com.yoho.error.event;


import java.util.Map;

/**
 *  Gateway Access 事件
 * Created by chunhua.zhang@yoho.cn on 2016/2/3.
 */
public class GatewayAccessEvent extends CommonEvent {


    /**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = 5636937696455359734L;

	//返回的响应码
    private final int statusCode;

    //时间
    private final long cost;


    //请求参数
    private final Map<String, Object> requestParams;




    /**
     * gateway 访问事件
     * @param url   URL或者method
     * @param statusCode 状态码
     * @param cost 延时
     * @param requestParams http请求参数
     */
    public GatewayAccessEvent(String url, int statusCode, long cost, Map<String, Object> requestParams) {
        super(url);

        this.statusCode = statusCode;
        this.cost = cost;
        this.requestParams = requestParams;
    }


    public int getStatusCode() {
        return statusCode;
    }

    public long getCost() {
        return cost;
    }



    public Map<String, Object> getRequestParams() {
        return requestParams;
    }


}

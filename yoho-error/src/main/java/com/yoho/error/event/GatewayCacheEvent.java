package com.yoho.error.event;



/**
 *  Gateway cache 命中统计
 *
 * Created by chunhua.zhang@yoho.cn on 2016/2/2.
 */
public class GatewayCacheEvent extends CommonEvent {

    /**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = -6095963872667322633L;
	private final String key;
    private final String status;
    private final Throwable exception;

    /**
     * Create a new ApplicationEvent.
     * @param  methodName gateway方法
     * @param  key  cache key
     * @param  status  命中状态
     * @param  exception 异常
     */
    public GatewayCacheEvent(String methodName, String key, String status, Throwable exception) {
        super(methodName);
        this.key = key;
        this.status = status;
        this.exception = exception;
    }


    public String getKey() {
        return key;
    }

    public String getStatus() {
        return status;
    }

    public Throwable getException() {
        return exception;
    }

}

package com.yoho.error.event;



/**
 *  服务调用事件
 *
 * Created by chunhua.zhang@yoho.cn on 2016/2/2.
 */
public class ServiceCallEvent extends CommonEvent {


    /**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = 6769811962872763165L;
	private final String path;
    private final String callSource;
    private final Throwable exception;

    /**
     *  服务调用事件
     * @param name : 服务名称
     * @param  path: 服务地址
     * @param callSource： 调用来源，对gateway调用服务有值
     * @param exception： 异常信息
     */
    public ServiceCallEvent(String name, String path, String callSource, Throwable exception) {
        super(name);
        this.path = path;
        this.callSource = callSource;
        this.exception = exception;
    }

    public String getPath() {
        return path;
    }

    public Throwable getException() {
        return exception;
    }

    public String getCallSource() {
        return callSource;
    }
}

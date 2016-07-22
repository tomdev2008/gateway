package com.yoho.error.event;


/**
 * 服务模块，对外提供http服务，出现异常的事件
 * <p/>
 * Created by chunhua.zhang@yoho.cn on 2016/2/2.
 */
public class ServiceServerExceptionEvent extends CommonEvent {


    /**
	 * @Fields serialVersionUID
	 */
	private static final long serialVersionUID = -6906935866415083159L;
	/**
     * RequestURI
     */
    private final String serviceName;
    private final Throwable exception;

    /**
     * Create a new ApplicationEvent.
     *
     * @param name : requestURI
     * @param  exception: 异常
     */
    public ServiceServerExceptionEvent(String name, Throwable exception) {
        super(name);
        this.serviceName = name;
        this.exception = exception;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Throwable getException() {
        return exception;
    }

}

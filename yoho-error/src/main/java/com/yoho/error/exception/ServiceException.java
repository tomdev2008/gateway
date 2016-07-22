package com.yoho.error.exception;

import com.yoho.error.ServiceError;

/**
 *  服务调用的异常
 * Created by chunhua.zhang@yoho.cn on 2015/11/18.
 */
public class ServiceException extends RuntimeException{

    /**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = -3434467003214522941L;
	//服务错误码
    private final ServiceError serviceError;
    private final int code;
    private final String errorMessage;



    private String[] params;

    //是否输出堆栈
    private boolean printStack = false;

    /**
     * 服务异常。建议用 {@link #ServiceException(ServiceError)} 来构造
     * @param code 错误码
     * @param message 错误消息
     */
    public ServiceException(int code, String message) {
        this(code,message,false);
     }


    public ServiceException(int code, String message, boolean printStack) {
        this.code = code;
        this.errorMessage = message;
        this.printStack = printStack;

        ServiceError found = ServiceError.getServiceErrorByCode(code);
        this.serviceError = found == null ?   ServiceError.OTHER_ERROR : found;
    }


    /**
     * 设置params。
     * @param params
     */
    public void setParams(String ... params){
        this.params = params;
    }
    public String[] getParams() {
        return params;
    }
    /**
     *  服务异常
     * @param serviceError 服务异常
     */
    public ServiceException(ServiceError serviceError) {
        this(serviceError,false);
    }


    /**
     *  服务异常
     * @param serviceError 服务异常
     */
    public ServiceException(ServiceError serviceError, boolean printStack) {
        this.serviceError = serviceError;
        this.code = serviceError.getCode();
        this.errorMessage = serviceError.getMessage();
        this.printStack = printStack;
    }
    /**
     *  服务异常
     * @param serviceError 服务异常
     * @param cause 服务异常
     */

    public ServiceException(ServiceError serviceError, Throwable cause) {
        super(serviceError.getMessage(), cause);
        this.serviceError = serviceError;
        this.code = serviceError.getCode();
        this.errorMessage = serviceError.getMessage();
    }

    /**
     * 如果使用ServiceException(int code, String message)构造，可能会找不到ServiceError
     * @return
     */
    public ServiceError getServiceError() {
        return serviceError;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return "[" + this.code + ":" + this.errorMessage + "]";
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public  boolean isPrintStack(){
        return this.printStack;
    }


}

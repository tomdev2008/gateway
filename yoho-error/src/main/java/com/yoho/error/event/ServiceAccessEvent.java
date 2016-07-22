package com.yoho.error.event;



/**
 *   服务提供的restful接口访问事件
 *
 * Created by chunhua.zhang@yoho.cn on 2016/2/2.
 */
public class ServiceAccessEvent extends CommonEvent {




    /**
	 * @Fields serialVersionUID
	 */
	private static final long serialVersionUID = 2370305262095540320L;
	private final long cost;
    private final long response_code;
    private final String stack;



    //来源于gateway的哪个接口？
    private String srcService;

    /**
     * Create a new ApplicationEvent.
     *
     * @param url :  服务提供的URL
     * @param cost: http服务响应时间
     * @param response_code: 响应吗
     * @param  stack: 如果时间超过500ms,调用耗用时间堆栈
     */
    public ServiceAccessEvent(String url, long cost, long response_code, String stack) {
        super(url);

        this.cost = cost;
        this.response_code = response_code;
        this.stack = stack;
    }

    public long getCost() {
        return cost;
    }

    public long getResponse_code() {
        return response_code;
    }

    public String getStack() {
        return stack;
    }

    public void setSrcService(String srcService) {
        this.srcService = srcService;
    }

    public String getSrcService() {
        return srcService;
    }

}

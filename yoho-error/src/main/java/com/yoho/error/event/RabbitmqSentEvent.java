package com.yoho.error.event;


import java.util.Map;

/**
 *  rabbitmq 发送事件
 *
 * Created by chunhua.zhang@yoho.cn on 2016/2/2.
 */
public class RabbitmqSentEvent extends CommonEvent {

    /**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = -7120188904547178276L;
	private final Object message;
    private final Map<String, Object> pros;

    /**
     *  服务调用事件
     */
    public RabbitmqSentEvent(String  topic, Object message,  Map<String, Object> pros) {
        super(topic);
        this.message = message;
        this.pros = pros;
    }

    public Object getMessage() {
        return message;
    }

    public Map<String, Object> getPros() {
      return pros;
    }


}

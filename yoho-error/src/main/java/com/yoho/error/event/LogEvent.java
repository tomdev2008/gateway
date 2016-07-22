package com.yoho.error.event;

import java.util.HashMap;
import java.util.Map;

/**
 *  日志事件
 * Created by chunhua.zhang@yoho.cn on 2016/2/19.
 */
public class LogEvent extends  CommonEvent {


    /**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = 620565801839108835L;
	/**
     * builder
     */
    public static class Builder{

        private LogEvent event;


        /**
         * 事件的名称，例如 "login", "register"， 类似数据库的表名字
         * @param name 事件的名称
         */
        public Builder (String name){
            event = new LogEvent(name);
        }


        /**
         *  添加一个事件参数，不会索引key
         * @param key key
         * @param value value
         * @return 事件的参数
         */
        public Builder addArg(String key, Object value){
            event.args.put(key, value);
            return  this;
        }

        /**
         *  添加一个事件tags，会索引key
         * @param key key
         * @param value value
         * @return 事件的tags
         */
        public Builder addTag(String key, Object value){
            if(value != null) {
                event.tags.put(key, value);
            }
            return  this;
        }

        /**
         *  事件属于哪个serial，默认是“logs"
         * @param catalog 事件的类别，例如”users“， ”messages"
         * @return this
         */
        public Builder catalog(String catalog){
             event.catelog = catalog;
            return  this;
        }

        /**
         * 构造一个event
         * @return event
         */
        public LogEvent build(){
            return  this.event;
        }

    }


    private  final  Map<String, Object> tags = new HashMap<>();
    private  final  Map<String, Object> args = new HashMap<>();
    private  String catelog;

    /**
     * Create a new ApplicationEvent.
     *
     * @param name :  事件的名称
     *
     */
    private LogEvent(String name) {
        super(name);
        this.catelog =  "yh_logs";
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public Map<String, Object> getArgs() {
        return args;
    }
    public String getCatelog() {
        return catelog;
    }

}

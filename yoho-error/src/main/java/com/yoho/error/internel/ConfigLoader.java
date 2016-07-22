package com.yoho.error.internel;


import com.yoho.error.ErrorCode;
import com.yoho.error.GatewayError;
import com.yoho.error.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 读取配置文件，并且设置枚举类型。 注意配置文件 gateway-error.yml 使用UTF-8格式
 * <p>
 * Created by chang@yoho.cn on 2015/11/3.
 */
public class ConfigLoader {

    private final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private Map<String, Object> serviceErrorMap;
    private Map<String, Object> gatewayErrorMap;

    /**
     * 初始化error code. 由spring 初始化
     */
    public void init() {

        logger.info("start to init error code info");

        for (ServiceError service_error : ServiceError.values()) {
            this.initErrorContent(service_error, this.serviceErrorMap);
        }

        for (GatewayError gateway_error : GatewayError.values()) {
            this.initErrorContent(gateway_error, this.gatewayErrorMap);
        }


        logger.info("init error code success. gateway error size {}, service error size {}",
                this.gatewayErrorMap.size(), this.serviceErrorMap.size());

    }

    @SuppressWarnings("unchecked")
    private void initErrorContent(ErrorCode errorCode, Map<String, Object> map) {
        int code = errorCode.getCode();
        String key = "[" + code + "]";  //yml读取的map，key为[422]

        Object messageObj = map.get(key);
        if (messageObj != null) {

            Map<String, Object> errorInfo = (Map<String, Object>) messageObj;

            errorCode.setErrorContent(errorInfo);
        }
    }

    public void setServiceErrorMap(Map<String, Object> serviceErrorMap) {
        this.serviceErrorMap = serviceErrorMap;
    }

    public void setGatewayErrorMap(Map<String, Object> gatewayErrorMap) {
        this.gatewayErrorMap = gatewayErrorMap;
    }


}

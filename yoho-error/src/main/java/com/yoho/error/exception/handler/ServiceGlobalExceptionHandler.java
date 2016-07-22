package com.yoho.error.exception.handler;

import com.alibaba.fastjson.support.spring.FastJsonJsonView;
import com.yoho.error.event.ServiceServerExceptionEvent;
import com.yoho.error.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 服务的全局异常处理
 * spring mvc server 通用的异常处理。 2种响应：
 * 1. 返回200，并且用json作为消息体，并且设置消息头（方便resttemplate处理）
 * 2. 返回500，设置消息头
 * Created by chunhua.zhang@yoho.cn on 2015/11/19.
 */
public class ServiceGlobalExceptionHandler implements HandlerExceptionResolver, ApplicationEventPublisherAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private   ApplicationEventPublisher publisher;

    private static final String SPLIT_ADD = "%%";

    //如果服务异常，需要把异常信息加入到头中
    public static String HEADER_ERROR_CODE = "x-yh-service-error-code";
    public static String HEADER_ERROR_MESSAGE = "x-yh-service-error-message";
    public static String HEADER_ERROR_ADD = "x-yh-service-error-addon";



    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception exception) {


        //publish event
        publisher.publishEvent(new ServiceServerExceptionEvent(httpServletRequest.getRequestURI(), exception));

        //如果不是服务异常，直接返回500，并且打印异常
        if (!(exception instanceof ServiceException)) {
            logger.error("unknown exception happened at:{}", httpServletRequest.getRequestURI());
            logger.error("unknown exception is ", exception);
            httpServletResponse.setStatus(500);
            httpServletResponse.addHeader(HEADER_ERROR_MESSAGE, exception.getMessage());
            return new ModelAndView();
        }


        //处理服务异常。 需要添加异常信息到头中，并且返回json
        ServiceException se =  (ServiceException) exception;
        int code = se.getCode();
        String message = se.getErrorMessage();

        //add header
        httpServletResponse.addHeader(HEADER_ERROR_CODE, String.valueOf(code));
        httpServletResponse.addHeader(HEADER_ERROR_MESSAGE, message);
        String params[] = se.getParams();  //添加其他的参数，用于重组ServiceException
        if (params != null) {
                try {
                    httpServletResponse.addHeader(HEADER_ERROR_ADD, URLEncoder.encode(String.join(SPLIT_ADD, params), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.error("encode exception.", e);
                }
         }
        ModelAndView mv = getErrorJsonView(code, message);
        return mv;
    }


    public static String[] getParams(String headerValue) {
        return headerValue.split(SPLIT_ADD);
    }

    /**
     * 使用FastJson提供的FastJsonJsonView视图返回，不需要捕获异常
     */
    public static ModelAndView getErrorJsonView(int code, String message) {
        ModelAndView modelAndView = new ModelAndView();
        FastJsonJsonView jsonView = new FastJsonJsonView();
        Map<String, Object> errorInfoMap = new HashMap<>();
        errorInfoMap.put("code", code);
        errorInfoMap.put("message", message);
        jsonView.setAttributesMap(errorInfoMap);
        modelAndView.setView(jsonView);
        return modelAndView;
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
           this.publisher = applicationEventPublisher;
    }
}

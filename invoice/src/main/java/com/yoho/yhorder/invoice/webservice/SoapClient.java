package com.yoho.yhorder.invoice.webservice;

/**
 * Created by chenchao on 2016/6/17.
 */
public interface SoapClient {

    String httpsRequest(String interfaceCode,String content) throws Exception;
}

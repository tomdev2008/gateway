package com.yoho.yhorder.shopping.utils;

import com.yoho.yhorder.shopping.exception.HttpResponseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by JXWU on 2015/12/2.
 */
public abstract class HttpClientTool {

    private final static Logger logger = LoggerFactory.getLogger(HttpClientTool.class);
    /**
     * 默认的系统超时时间
     */
    private final static int SOCKET_TIMEOUT = 10000;
    private final static int CONNECT_TIMEOUT = 10000;
    private final static String REQUEST_CHARSET = "UTF-8";
    private final static String RESPONSE_CHARSET = "UTF-8";


    public static String post(String url, Map<String, Object> requestMap) {
        String returnMsg = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = null;
        CloseableHttpResponse httpResponse = null;
        try {
            post = new HttpPost(url);
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
            }
            post.setEntity(new UrlEncodedFormEntity(list, REQUEST_CHARSET));

            RequestConfig config = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setConnectionRequestTimeout(CONNECT_TIMEOUT).build();//设置请求和传输超时时间
            post.setConfig(config);
            httpResponse = client.execute(post);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new HttpResponseException(statusCode, "erp response statuscode:" + statusCode);
            }
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                returnMsg = EntityUtils.toString(httpEntity, RESPONSE_CHARSET);
            }
        } catch (SocketTimeoutException e) {
            logger.warn("time out url:{},prarms:{}", url, requestMap);
            throw new RuntimeException("time out", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {

                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                }
            }
        }
        return returnMsg;

    }
}

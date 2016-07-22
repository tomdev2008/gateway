package com.yoho.yhorder.order.payment.wechat;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.order.config.Constant;
import com.yoho.yhorder.order.model.PayQueryBo;
import com.yoho.yhorder.order.model.PayType;
import com.yoho.yhorder.order.payment.tenpay.handlers.AccessTokenRequestHandler;
import com.yoho.yhorder.order.payment.tenpay.util.MD5Util;
import com.yoho.yhorder.order.payment.tenpay.util.Sha1Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by JXWU on 2016/1/22.
 */
@Component
public class WechatQuerier {

    private final Logger logger = LoggerFactory.getLogger("payConfirmLog");

    // App 支付参数
    @Value("${wechat.app.partnerid}")
    private String partnerIdApp;

    @Value("${wechat.app.partnerkey}")
    private String partnerKeyApp;

    @Value("${wechat.app.appid}")
    private String appIdApp;

    @Value("${wechat.app.paysignkey}")
    private String paySignKey;

    @Value("${wechat.app.appsecret}")
    private String appSecretApp;

    //"https://api.weixin.qq.com/pay/orderquery?access_token="
    //https://api.weixin.qq.com/pay/orderquery
    @Value("${wechat.app.queryurl}")
    private String queryUrl;

    @Autowired
    private YHValueOperations<String, String> valueOperations;

    @Resource(name = "yhRedisTemplate")
    private YHRedisTemplate redis;

    @Autowired
    private ServiceCaller serviceCaller;

    public PayQueryBo query(String tradeNo) {
        logger.info("enter WechatQuerier to query tradeNo {}", tradeNo);
        String accessToken = getAccessToken();
        String url = queryUrl + "?access_token=" + accessToken;
        String request = buildRequestString(tradeNo);
        String responseText = sendRequest(url, request);

        PayQueryBo queryBo = convert(responseText);
        logger.info("exit WechatQuerier query result {}", queryBo);
        return queryBo;
    }

    private String getAccessToken() {
        String accessToken = null;
        String key = "weixin-token-" + appIdApp;
        try {
            boolean haskey = redis.hasKey(key);
            if (haskey) {
                accessToken = valueOperations.get(key);
                logger.info("get access token from redis success,key:{}, accesstoken:{}", key, accessToken);
            }
            boolean newTokenFlag = false;
            if (StringUtils.isEmpty(accessToken)) {
                logger.info("accesstoken is null,key:{}", key);
                newTokenFlag = true;
            } else {
                //是否过期
                if (AccessTokenRequestHandler.tokenIsExpire(accessToken, appIdApp, appSecretApp)) {
                    logger.info("accesstoken:{} expired,key:{}", accessToken, key);
                    newTokenFlag = true;
                }
            }

            if (newTokenFlag) {
                accessToken = createAccessTokenAndCache(key);
            }
            logger.info("return accesstoken:{} of key:{}", accessToken, key);

        } catch (Exception e) {
            logger.warn("get cache value faid,key {}", key, e);
        }
        return accessToken;
    }

    private String createAccessTokenAndCache(String key) {
        String accessToken = AccessTokenRequestHandler.getAccessToken(appIdApp, appSecretApp);
        //设置值
        valueOperations.set(key, accessToken);
        //缓存2h
        redis.expire(key, 2, TimeUnit.HOURS);
        logger.info("set access token to redis success,key:{}, accesstoken:{},expired after 2h", key, accessToken);
        return accessToken;
    }

    private String sendRequest(String url, String request) {
        logger.info("WechatQuerier post request is {}", request);
        String responseText = null;
        try {
            responseText = serviceCaller.post("order.WechatAppPay", url, request, String.class, null).get();
        } catch (Exception e) {
            logger.warn("WechatQuerier post {} error,responseText is {}", url, responseText, e);
        }
        logger.info("WechatQuerier post response is {}", responseText);
        return responseText;
    }

    private String buildRequestString(String tradeNo) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appid", appIdApp);

        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("sign_method", "sha1");

        String out_trade_no = tradeNo;
        String sign = "out_trade_no=" + out_trade_no + "&partner="
                + partnerIdApp + "&key=" + partnerKeyApp;

        String sha1Sign = MD5Util.MD5Encode(sign, "UTF-8");
        sign = sha1Sign.toUpperCase();

        StringBuilder packageStr = new StringBuilder();
        packageStr.append("out_trade_no=").append(out_trade_no).append("&partner=").append(partnerIdApp).append("&sign=").append(sign);
//        packageStr = "out_trade_no=" + out_trade_no + "&partner="
//                + partnerIdApp + "&sign=" + sign;

        jsonObject.put("package", packageStr);

        SortedMap<String, String> prePayParams = new TreeMap<String, String>();
        prePayParams.put("appid", appIdApp);
        prePayParams.put("appkey", paySignKey);
        prePayParams.put("package", packageStr.toString());
        prePayParams.put("timestamp", timestamp);
        // 生成签名
        String app_signature = createSHA1Sign(prePayParams);
        jsonObject.put("app_signature", app_signature);

        return jsonObject.toString();
    }

    public static String createSHA1Sign(SortedMap map) {
        StringBuffer sb = new StringBuffer();
        Set es = map.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            sb.append(k + "=" + v + "&");
        }
        String params = sb.substring(0, sb.lastIndexOf("&"));
        String appsign = Sha1Util.getSha1(params);

        return appsign;
    }

    private PayQueryBo convert(String str) {
        PayQueryBo queryBo = new PayQueryBo();
        if (StringUtils.isEmpty(str)) {
            return queryBo;
        }
        try {
            JSONObject mdata = JSONObject.parseObject(str);
            String errcode = mdata.get("errcode").toString();
            if (errcode.equals("0")) {
                JSONObject order_info = JSONObject.parseObject(mdata.get(
                        "order_info").toString());
                String ret_code = order_info.get("ret_code").toString();
                String trade_state = order_info.getString("trade_state");
                if ("0".equals(ret_code) && "0".equals(trade_state)) {
                    queryBo.valid = true;
                    String out_trade_no = order_info.getString("out_trade_no");
                    int index = out_trade_no.indexOf(Constant.WECHAT_QUERY_TRADE_PREFIX);
                    if (index >= 0) {
                        out_trade_no = out_trade_no.substring(index + Constant.WECHAT_QUERY_TRADE_PREFIX.length());
                    }
                    queryBo.orderCode = out_trade_no;
                    double total_fee = order_info.getDoubleValue("total_fee");
                    queryBo.amount = YHMath.mul(total_fee, 0.01);
                    queryBo.payType = PayType.WECHATAPP;
                    queryBo.bankCode = order_info.getString("bank_type");
                    queryBo.bankName = order_info.getString("bank_type");
                    queryBo.tradeNo = order_info.getString("transaction_id");
                    queryBo.paymentTime = DateUtil.formatDateString(order_info.getString("time_end"),DateUtil.yyyyMMddHHmmss,DateUtil.yyyy_MM_dd_HH_mm_SS);
                    queryBo.callbackTime = DateUtil.getCurrentTime();
                }
            }

            if (queryBo.valid == false) {
                logger.warn("WechatQuerier query trade is invalid,trade record is {}", str);
            }
        } catch (Exception e) {
            logger.warn("WechatQuerier convert {}  error", str, e);
        }
        return queryBo;
    }
}

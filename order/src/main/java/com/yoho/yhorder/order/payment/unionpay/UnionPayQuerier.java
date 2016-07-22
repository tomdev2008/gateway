package com.yoho.yhorder.order.payment.unionpay;

import com.yoho.core.common.utils.YHMath;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.order.model.PayQueryBo;
import com.yoho.yhorder.order.model.PayType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by JXWU on 2016/1/22.
 */
@Component
public class UnionPayQuerier {

    private final Logger logger = LoggerFactory.getLogger("payConfirmLog");


    @Value("${unionpay.env}")
    private String unionpayEnv;

    @Value("${unionpay.merid}")
    private String unionpayMerId;

    @Value("${unionpay.sign.cert}")
    private String unionpaySignCert;

    @PostConstruct
    public void init() {
        SDKConfig.getConfig().loadPropertiesFromSrc();// 从classpath加载acp_sdk.properties文件

        org.springframework.core.io.Resource resource = new ClassPathResource("/certs");
        String dirpathRaw = null;
        try {
            dirpathRaw = resource.getURI().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dirpath = dirpathRaw.replace("file:", "");
        SDKConfig.getConfig().setValidateCertDir(dirpath);
        SDKConfig.getConfig().setSignCertPath(dirpath + unionpaySignCert);

        logger.debug("Absolute Cert PATH: {}", dirpath);
        logger.debug("SignCertPath: {}", SDKConfig.getConfig().getSignCertPath());
        logger.debug("ValidateCertDir: {}", SDKConfig.getConfig().getValidateCertDir());
    }

    public PayQueryBo query(String tradeNo, String txnTime) {

        logger.info("enter UnionPayQuerier to query tradeNO {},txnTime {}", tradeNo, txnTime);

        Map<String, String> signParams = buildSignRequestParams(tradeNo, txnTime);
        String singleQueryUrl = SDKConfig.getConfig().getSingleQueryUrl();
        Map<String, String> resmap = SDKUtil.submitUrl(signParams, singleQueryUrl, SDKUtil.encoding_UTF8);

        logger.info("exit UnionPayQuerier ,query result is {}", resmap);

        PayQueryBo queryBo = convert(resmap);
        return queryBo;
    }
    
    public PayQueryBo query(String tradeNo, String txnTime, PayType payType) {
    	PayQueryBo queryBo = query(tradeNo, txnTime);
    	queryBo.payType = payType;
    	logger.info("UnionPayQuerier payment, tradeNo: {}, payment: {}", tradeNo, payType.getPayId());
    	return queryBo;
    }


    private Map<String, String> buildSignRequestParams(String tradeNo, String txnTime) {
        Map<String, String> params = buildRequestParams(tradeNo, txnTime);

        return sign(params, SDKUtil.encoding_UTF8);
    }

    private Map<String, String> buildRequestParams(String tradeNo, String txnTime) {
        /**
         * 组装请求报文
         */
        Map<String, String> data = new HashMap<String, String>();
        // 版本号
        data.put("version", "5.0.0");
        // 字符集编码 默认"UTF-8"
        data.put("encoding", "UTF-8");
        // 签名方法 01 RSA
        data.put("signMethod", "01");
        // 交易类型 00
        data.put("txnType", "00");
        // 交易子类型 默认00
        data.put("txnSubType", "00");
        // 业务类型
        data.put("bizType", "000000");
        // 接入类型
        data.put("accessType", "0");
        // 商户代码
        data.put("merId", unionpayMerId); // "898111453110466");
        // 订单发送时间
        data.put("txnTime", txnTime);
        // 商户订单号
        data.put("orderId", tradeNo);
        return data;
    }


    public static Map<String, String> sign(Map<String, String> contentData, String encoding) {
        Map<String, String> submitFromData = paraFilter(contentData);
        SDKUtil.sign(submitFromData, encoding);
        return submitFromData;
    }

    public static Map<String, String> paraFilter(Map<String, ?> contentData) {

        Map.Entry<String, String> obj = null;
        Map<String, String> submitFromData = new HashMap<String, String>();
        for (Iterator<?> it = contentData.entrySet().iterator(); it.hasNext(); ) {
            obj = (Map.Entry<String, String>) it.next();
            String value = obj.getValue();
            if (StringUtils.isNotBlank(value)) {
                // 对value值进行去除前后空处理
                submitFromData.put(obj.getKey(), value.trim());
            }
        }

        return submitFromData;
    }

    private PayQueryBo convert(Map<String, String> resmap) {
        PayQueryBo queryBo = new PayQueryBo();
        if (resmap == null) {
            return queryBo;
        }

        String respCode = resmap.get("respCode");
        String origRespCode = resmap.get("origRespCode");
        //必须respCode跟origRespCode同时为00，才能确认是已支付
        if ("00".equals(respCode) && "00".equals(origRespCode)) {
            //成功
            queryBo.valid = true;
            queryBo.orderCode = resmap.get("orderId");
            double total_fee = Double.parseDouble(resmap.get("txnAmt") == null ? "0" : resmap.get("txnAmt"));
            queryBo.amount = YHMath.mul(total_fee, 0.01);

            queryBo.payOrderCode = resmap.get("orderId");
            queryBo.tradeNo = resmap.get("queryId");

            /**
             * 时间戳格式转换, "20160120163217" => "2016-01-20 16:32:17"
             */
            queryBo.paymentTime = DateUtil.formatDateString(resmap.get("txnTime"),DateUtil.yyyyMMddHHmmss,DateUtil.yyyy_MM_dd_HH_mm_SS);
            queryBo.callbackTime = DateUtil.getCurrentTime();

            queryBo.payType = PayType.UNIONPAY;
        }

        if (queryBo.valid == false) {
            logger.warn("UnionPayQuerier query trade is invalid,trade record is {}", resmap);
        }
        return queryBo;
    }


}

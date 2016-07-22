package com.yoho.yhorder.order.payment.alipay;

/**
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。

 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayConfig {

    // 合作身份者ID，以2088开头由16位纯数字组成的字符串
    public static String partner = "2088701661478015";

    // 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
    public static String seller_id = partner;
    // 商户的私钥
    public static String private_key_md5 = "kcxawi9bb07mzh0aq2wcirsf9znusobw";  // MD5

   // public static String private_key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM21B57hDupXKMiSst8gpw5qOfggZDIAuRJyUV0KthxndaEVZ0f5yO38IZbwq7FfgIlZk7QsQID1q8kWyKmpnff2PPidaZrnIygukQW7nknawkTcy/VRJ77f96BF86YcZZrcYcA+OCXK7HXRlK0uRSgxKs1nFJ4MqdoBpgtjGYH7AgMBAAECgYB2oXcCnmI+rQM9ILT9TtRo0MQbn3qJB7eztq/Li5jYqqXAvxov/RJuhOBOLv+lLCFxlHJFDtwoQwXN01Dn34TUabVeptkgoBvwPXom1xWs8F0oEJvkM6LNb/qcpSR2OjYWO5IHqdkLLmlmjLGMeS84pnQj5h+NiI1kYIJxGdhCaQJBAOojtWyLLCCpdQJqcrOj9Tz57FWh+F7wutx32k96iVoD0v1qYKlbtU4FKXCdK4PvFfvv/1ctSjdHLWc97AA2xD8CQQDg6b6nlmNHfa/icmS4TksQy0Mh6LUeHPi12wGr/Eh9j/A3Llnwqai+mfQPXEXio330TumntKNHH+VbpVKf/iNFAkEAxcQ7rd8v+ranus6m682Fi2eelAYngCnd6/LgV1mtQncQdDSySNrJnYkGo7Rd2SbNbuwcxUnf8ikQ1K+c8+FOiQJAHDk4047rpmz/RUtVmEs64EChi//Hup+oFioFa6+c6STG4CXQ44JmyoRHPCY4HtilVaMRbxob4zD9dyKn26vogQJAMvAzTC+UIbJBo4KWGXOVY+rSmASmeckzCR/JdvHTcJsPJdh3HoUN+pwIqUdx8d6+4JVzNHhpPj+tQKUkHf0CpQ==";

    public static String private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOfywqINCNC+IFvg" +
            "zqq3aEYxmh89WamWL59FukMKoKYMDR1NWMgFLNX2ld6aRWWdlJCBUsTzH/8uabEj" +
            "fBOM5BK+N08GfkpCYRmzww2y1H8RZ4P3wtKu95UYdaq3Ir5ucfgKxGy/1ay2qQFZ" +
            "PAkvPtTw+qdkVOgPakvqCWgcFL8LAgMBAAECgYAYeTnmJV/vvo/lgePsiWucNHGh" +
            "qDSEu08NDCtqFX375zufAuFCQaGIdfs8QKTf4u+hC7QzCcAvibMnOKpH2c7apAEc" +
            "RUfTUUcY/UB/yIkvzjkvkMKZSXI6y3lyFn8mNEwQei9u4OdSg1IBVuWqZoyqilCm" +
            "ARMzXyoeAOY55MelSQJBAPP+sEbPadMENkzzkiiHCHuo3Y0OEF5Zv2h3LJmtRx/6" +
            "B60mrfaUSSbU/iNq0uzQSoDQlY2DxXb21l1LYZzgdn8CQQDzXFXNj/qkXIuTLjfK" +
            "YxIhwiNKYXXKeG9C6+MlviY2zVrKruuuHJMcJ8238hzRcPsX+TCwFd3VhHuL61jB" +
            "2+l1AkBhUNTH+VQQ6N4rhP5nkawNfkWXS+O1bgBMzzOHu7fhhhznr8S002H1zf/q" +
            "6mFkOJNum0L65XKtxzeqkDVHl7NLAkEAg/jKvxMZRRC60DH8J1DagFwbbzay/f2Z" +
            "uJzbLZiUeJucZNW/EUiFrnsXYG13m0y9nh6QfK0fA684oIQcOeTcEQJBAIxCxNUP" +
            "jXsvqTV2ypek3ktvutFwSFuvo0zD2sn2HNlMSfh3K6RZV2Q0Q4W7bLELKEEkxmJX" +
            "IMLSK3hQB7jYD0Q=";


    // 支付宝的公钥，无需修改该值
    public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

    // 字符编码格式 目前支持 gbk 或 utf-8
    public static String input_charset = "utf-8";

    // 签名方式 不需修改
    public static String sign_type = "RSA"; // "MD5";
    public static String sign_type_wap = "MD5";

}

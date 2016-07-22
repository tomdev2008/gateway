package com.yoho.yhorder.common.utils.cache;

/**
 * Created by yun on 2016/5/27.
 */
public class KeyGenerator {

    public static String generateOrderCodeUidKey(long orderCode) {
        return "yh:orders:order_code_uid:" + orderCode;
    }

}

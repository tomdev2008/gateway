package com.yoho.yhorder.common.utils;

/**
 * Created by yoho on 2016/3/18.
 */
public class PrivacyUtils {

    public static String mobile(String mobile) {
        if (mobile == null) {
            return null;
        }
        String tmpMobile = mobile.trim();
        int len = tmpMobile.length();
        if (len > 7) {
            return new StringBuffer(tmpMobile.substring(0, 3)).append("****").append(tmpMobile.substring(7)).toString();
        } else if (len > 3) {
            StringBuffer sb = new StringBuffer(tmpMobile.substring(0, 3));
            for (int i = 0; i < len - 3; i++) {
                sb.append("*");
            }
            return sb.toString();
        } else {
            return tmpMobile;
        }
    }

}

package com.yoho.yhorder.common.utils;

import java.util.List;

/**
 * Created by yoho on 2015/11/3.
 */
public class SqlUtils {

    /**
     * 返回like参数
     * <bind name="size" value="@com.yoho.yhsns.common.utils.SqlUtils@getSize(propertyValueEntityList)"/>
     */
    public static int getSize(List param) {
        if (null != param) {
            return param.size();
        } else {
            return 0;
        }
    }

}

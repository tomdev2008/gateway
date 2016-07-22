package com.yoho.yhorder.common.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sunjiexiang on 2015/11/28.
 * 快递公司工具类
 */
public class ExpressUtils {

    private static Map<String, Object> expressCompanyMap;

    /**
     * 获取单个快递公司
     *
     * @param key
     * @return
     */
    public static Map<String, Object> getExpressCompany(String key) {
        if (ExpressUtils.containsKey(key) != true) {
            LinkedHashMap<String, Object> defaultMap = new LinkedHashMap<>();
            LinkedHashMap<String, Object> infoMap = new LinkedHashMap<>();
            infoMap.put("caption", "ems");
            infoMap.put("url", "http://www.ems.com.cn/");
            infoMap.put("logo", "http://static.yohobuy.com/images/v3/express/order_carrier_ems_logo.png");
            infoMap.put("is_support", "2");
            defaultMap.put("[1]", infoMap);
            return defaultMap;
        }

        return copyMap((Map) expressCompanyMap.get(converKeyToYAML(key)));
    }

    public static Map<String, Object> createEmptyExpress() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("url", "");
        dataMap.put("logo", "");
        dataMap.put("caption", "");
        dataMap.put("is_support", "2");
        return dataMap;
    }

    /**
     * 获取所有快递公司
     *
     * @return
     */
    public static Map<String, Object> getExpressCompanyMap() {

        return copyMap(expressCompanyMap);
    }

    public static boolean containsKey(String key) {

        return expressCompanyMap.containsKey(converKeyToYAML(key));
    }

    private static Map<String, Object> copyMap(Map map) {
        return new HashMap<>(map);
    }

    /**
     * 将key转换为YAML格式KEY
     *
     * @param key 待转换KEY
     * @return
     */
    private static String converKeyToYAML(String key) {
        try {
            Double.parseDouble(key);
            return new StringBuilder("[").append(key).append("]").toString();
        } catch (NumberFormatException e) {
            return key;
        }
    }

    public void setExpressCompanyMap(Map<String, Object> expressCompanyMap) {
        this.expressCompanyMap = expressCompanyMap;
    }
}

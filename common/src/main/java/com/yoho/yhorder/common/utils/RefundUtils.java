package com.yoho.yhorder.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 退货yml配置文件解析工具类
 * qianjun
 * 2015/12/11.
 */
public class RefundUtils {
    private static Map<String, Object> refundMap;

    public void setRefundMap(Map<String, Object> refundMap) {
        this.refundMap = refundMap;
    }

    /**
     * 获取退货单个配置map
     * @param key
     * @return
     */
    public static Map<String, Object> getRefundMap(String key){
        return copyMap((Map) refundMap.get(convertKeyToYAML(key)));
    }

    /**
     * 获取所有退货yml配置的map
     * @return
     */
    public static Map<String, Object> getRefundMap(){
        return copyMap(refundMap);
    }

    public static boolean containsKey(String key){
        return refundMap.containsKey(convertKeyToYAML(key));
    }

    /**
     * 拷贝map
     * @param map
     * @return
     */
    private static Map<String, Object> copyMap(Map map){
        return new LinkedHashMap<>(map);
    }

    /**
     * 将key转换为YAML格式key
     * @param key  待转换key
     */
    public static String convertKeyToYAML(String key){
        try {
            Double.parseDouble(key);
            return new StringBuilder("[").append(key).append("]").toString();
        } catch (NumberFormatException e) {
            return key;
        }
    }
}

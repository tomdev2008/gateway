package com.yoho.yhorder.common.utils;

import org.apache.commons.collections.map.HashedMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunjiexiang on 2015/12/07.
 * 订单商品CODE工具类
 */
public class OrderGoodsTypeUtils {

    private static Map<String, Object> orderGoodsTypeMap;

    /**
     * 获取单个订单商品CODE信息
     *
     * @param key
     * @return
     */
    public static Map<String, Object> getOrderGoodsTypeMap(String key) {
        return copyMap((Map) orderGoodsTypeMap.get(converKeyToYAML(key)));
    }

    /**
     * 获取所有订单商品CODE信息
     *
     * @return
     */
    public static Map<String, Object> getOrderGoodsTypeMap() {
        return copyMap(orderGoodsTypeMap);
    }

    public static boolean containsKey(String key) {
        return orderGoodsTypeMap.containsKey(converKeyToYAML(key));
    }

    private static Map<String, Object> copyMap(Map map) {
        if (map == null) {
            return new HashedMap();
        } else {
            return new HashMap<>(map);
        }
    }

    /**
     * 将key转换为YAML格式KEY
     *
     * @param key 待转换KEY
     * @return
     */
    public static String converKeyToYAML(String key) {
        try {
            Double.parseDouble(key);
            return new StringBuilder("[").append(key).append("]").toString();
        } catch (NumberFormatException e) {
            return key;
        }
    }

    public void setOrderGoodsTypeMap(Map<String, Object> orderGoodsTypeMap) {
        this.orderGoodsTypeMap = orderGoodsTypeMap;
    }
}

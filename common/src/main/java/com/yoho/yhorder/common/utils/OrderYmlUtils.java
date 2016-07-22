package com.yoho.yhorder.common.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sunjiexiang on 2015/12/9.
 * 订单yml配置文件工具类
 */
public class OrderYmlUtils {

    public final static String YAML_CASH_ON_DELIVERY_KEY = "cashOnDelivery";

    //购物车
    public final static String YAML_SHOPPING_CART_KEY = "shoppingCart";

    //运费中文显示模版
    public final static String SHIPPING_COST_MESSAGE_ZH = "shipingCost-zh";

    private static LinkedHashMap<String, Object> orderConfig;

    private static Map<String, Object> orderGoodsType;


    public static Map<String, Object> getOrderGoodsType(String key) {
        return copyMap((Map) orderConfig.get(converKeyToYAML(key)));
    }

    /**
     * 获取单个配置MAP
     * @param key
     * @return
     */
    public static LinkedHashMap<String, Object> getOrderConfig(String key){
        return copyMap((LinkedHashMap) orderConfig.get(converKeyToYAML(key)));
    }

    /**
     * 获取所有配置
     * @return
     */
    public static Map<String, Object> getOrderConfig(){
        return copyMap(orderConfig);
    }

    public static boolean containsKey(String key){
        return orderConfig.containsKey(converKeyToYAML(key));
    }

    private static LinkedHashMap<String, Object> copyMap(Map map){
        return new LinkedHashMap<>(map);
    }

    /**
     * 将key转换为YAML格式KEY
     * @param key       待转换KEY
     * @return
     */
    public static String converKeyToYAML(String key){
        try {
            Double.parseDouble(key);
            return new StringBuilder("[").append(key).append("]").toString();
        } catch (NumberFormatException e) {
            return key;
        }
    }

    public void setOrderConfig(LinkedHashMap<String, Object> orderConfig) {
        this.orderConfig = orderConfig;
    }


    /**
     * 货到付款提示信息
     * @param rkey
     * @return
     */
    public static String getCashOnDeliveryMessage(String rkey) {
        Map<String, Object> reasonMap = OrderYmlUtils.getOrderConfig(YAML_CASH_ON_DELIVERY_KEY);
        return reasonMap != null ? (String) reasonMap.get(rkey) : "";
    }

    /**
     * 购物车-运费
     * @return
     */
    public static String getShippingCostShowTemplate() {
        Map<String, Object> reasonMap = OrderYmlUtils.getOrderConfig(YAML_SHOPPING_CART_KEY);
        return reasonMap != null ? (String) reasonMap.get(SHIPPING_COST_MESSAGE_ZH) : "";
    }


    public  static  void  main(String args[]){

       // Map<String, Object> changeTypeMap = OrderYmlUtils.getOrderConfig("changeType");

        //通过yml操作工具类获取key为changeStatus的定义数据，然后根据换货状态获取配置内容
        String statusName = OrderYmlUtils.getOrderConfig("changeStatus").get(3) + "";
        System.out.print(JSONObject.toJSONString(statusName));

    }

}

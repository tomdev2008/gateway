package com.yoho.yhorder.shopping.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by JXWU on 2015/12/3.
 */
public abstract class ReflectTool {

    private final static Logger logger = LoggerFactory.getLogger(ReflectTool.class);

    public static Object invokeGetMethod(String name, Object bo) throws Exception {

        Method method = bo.getClass().getMethod(name);
        if (method != null) {
            return method.invoke(bo);
        }
        return null;
    }

    public static void invokeSetMethod(String name, Object bo, Object value, Class[] paramType) throws Exception {
        Method method = bo.getClass().getMethod(name, paramType);
        if (method != null) {
            method.invoke(bo, value);
        }
    }


    public static void injectDefaultValue(Object destObj, Map<String, Object> injectMap) {
        Set<String> keys = injectMap.keySet();
        for (String key : keys) {
            Object value = injectMap.get(key);
            String getName = MyStringUtils.generateGetterOrSetter("get", key);
            try {
                Object realValues = ReflectTool.invokeGetMethod(getName, destObj);
                //为空或者空字符串都重新设置
                if (realValues == null || ((realValues instanceof String) && org.apache.commons.lang3.StringUtils.isEmpty((String) realValues))) {
                    String setName = MyStringUtils.generateGetterOrSetter("set", key);
                    try {
                        ReflectTool.invokeSetMethod(setName, destObj, value, new Class[]{value.getClass()});
                    } catch (Exception e) {
                        throw new IllegalStateException("set method " + setName + " in " + destObj.getClass() + ",value=" + value);
                        //logger.warn("set method {} in {},value ={} error", setName, destObj.getClass(), value);
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("get method " + getName + " in " + destObj.getClass());
                //logger.warn("get method {} in {} error", getName, destObj.getClass());
            }
        }
    }


    /**
     * 反射注入属性
     *
     * @param dest
     * @param methodName
     * @param value
     */
    public static void injectAttributeValue(Object dest, String methodName, Object value) {
        if (value == null) {
            return;
        }

        Method method = null;
        try {
            method = dest.getClass().getMethod(methodName, value.getClass());
            method.invoke(dest, value);
        } catch (Exception e) {
            //尝试String
            try {
                method = dest.getClass().getMethod(methodName, String.class);
                if (value.getClass() == String.class) {
                    method.invoke(dest, value);
                } else {
                    method.invoke(dest, String.valueOf(value));
                }
            } catch (Exception e1) {
                //尝试Double
                try {
                    method = dest.getClass().getMethod(methodName, Double.class);
                    if (value.getClass() == Double.class) {
                        method.invoke(dest, value);
                    } else if (value.getClass() == String.class) {
                        //空字符串
                        if (org.apache.commons.lang3.StringUtils.isEmpty((String) value)) {
                            method.invoke(dest, Double.parseDouble("0"));
                        } else {
                            method.invoke(dest, Double.parseDouble((String) value));
                        }
                    } else if (value.getClass() == Integer.class) {
                        method.invoke(dest, new Double((Integer) value));
                    } else if (value.getClass() == BigDecimal.class) {
                        method.invoke(dest, ((BigDecimal) value).doubleValue());
                    } else {
                        method.invoke(dest, (Double) value);
                    }
                } catch (Exception e2) {
                    //尝试Integer
                    try {
                        method = dest.getClass().getMethod(methodName, Integer.class);
                        if (value.getClass() == Integer.class) {
                            method.invoke(dest, value);
                        } else if (value.getClass() == String.class) {
                            if (org.apache.commons.lang3.StringUtils.isEmpty((String) value)) {
                                method.invoke(dest, new Integer("0"));
                            } else {
                                method.invoke(dest, new Integer((String) value));
                            }
                        } else if (value.getClass() == BigDecimal.class) {
                            method.invoke(dest, ((BigDecimal) value).intValue());
                        } else {
                            method.invoke(dest, (Integer) value);
                        }
                    } catch (Exception e3) {
                        throw new IllegalStateException("inject attribute " + value + "(" + value.getClass() + ") for " + methodName + " in " + dest.getClass());
                        //logger.warn("inject property {} ({}) for ShoppingGoods  by method {} error", value, value.getClass(), methodName);
                    }
                }
            }
        }
    }
}

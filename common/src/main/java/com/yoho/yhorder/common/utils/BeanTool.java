package com.yoho.yhorder.common.utils;

import com.alibaba.fastjson.JSON;
import com.yoho.yhorder.common.annotation.Mapping;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author lijian
 *         对象copy工具类
 */
public class BeanTool {

    @SuppressWarnings("serial")
    private final static List<Class<?>> PrimitiveClasses = new ArrayList<Class<?>>() {
        {
            add(Long.class);
            add(Double.class);
            add(Integer.class);
            add(String.class);
            add(Boolean.class);
            add(Date.class);
            add(java.sql.Date.class);
        }
    };

    private final static boolean _IsPrimitive(Class<?> cls) {
        return cls.isPrimitive() || PrimitiveClasses.contains(cls);
    }

    /**
     * copy不同的pojo数据对象
     *
     * @param fromObj
     * @param toObjClazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T copyObject(Object fromObj, Class<T> toObjClazz) {
        try {

            Class<?> fromObjClazz = fromObj.getClass();
            // 普通类型直接返回
            if (_IsPrimitive(toObjClazz))
                return (T) fromObj;

            T toObj = toObjClazz.newInstance();

            Field[] fields = toObjClazz.getDeclaredFields();

            for (Field toF : fields) {
                try {

                    int mod = toF.getModifiers();
                    // 静态成员及常量成员不copy
                    if (Modifier.isFinal(mod) || Modifier.isStatic(mod))
                        continue;

                    String toFieldName = toF.getName();
                    String fromFieldName;
                    Mapping mapping = toF.getAnnotation(Mapping.class);

                    if (mapping == null || mapping.name() == null
                            || mapping.name().trim().equals(""))
                        fromFieldName = toFieldName;
                    else
                        fromFieldName = mapping.name();

                    toF.setAccessible(true);
                    Field fromF = fromObjClazz.getDeclaredField(fromFieldName);
                    fromF.setAccessible(true);
                    // System.out.println("aaaaa"+fromF.get(fromObj));
                    toF.set(toObj, fromF.get(fromObj));
                    // System.out.println(toF.get(toObj));
                } catch (Exception e) {
                    if (e instanceof IllegalArgumentException)
                        e.printStackTrace();
                    // e.printStackTrace();
                }
            }
            return toObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * copy list对象
     *
     * @param fromObjList
     * @param toObjClazz
     * @return
     */
    public static <T> List<T> copyList(List<?> fromObjList, Class<T> toObjClazz) {
        List<T> toObjList = new ArrayList<T>(fromObjList.size());

        for (int i = 0; i < fromObjList.size(); i++) {
            toObjList.add(copyObject(fromObjList.get(i), toObjClazz));
        }
        return toObjList;
    }

    /**
     * copy map 对象
     *
     * @param fromObjMap
     * @param toObjClazz
     * @return
     */
    public static <T> Map<String, T> copyMap(Map<String, ?> fromObjMap,
                                             Class<T> toObjClazz) {
        Map<String, T> toObjMap = new HashMap<String, T>(fromObjMap.size());
        Iterator<String> iter = fromObjMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Object fromObj = fromObjMap.get(key);

            // if(List.class.isAssignableFrom(fromObj.getClass())){
            // toObjMap.put(key, copyList((List<?>)fromObj, toObjClazz));
            // }

            toObjMap.put(key, copyObject(fromObj, toObjClazz));
        }
        return toObjMap;
    }

    public static <T> List<T> copyListMap(List<Map<String, ?>> mapList,
                                          Class<T> toObjClass) {
        List<T> toObjList = new ArrayList<T>(mapList.size());
        for (Map<String, ?> map : mapList) {
            toObjList.add(copyMapToBean(map, toObjClass));
        }
        return toObjList;
    }

    public static <T> T copyMapToBean(Map<String, ?> map, Class<T> toObjClass) {
        try {
            Set<String> set = map.keySet();
            T objT = toObjClass.newInstance();
            for (String key : set) {
                try {
                    Object value = map.get(key);
                    Field toF = toObjClass.getDeclaredField(key);
                    toF.setAccessible(true);
                    toF.set(objT, value);
                } catch (Exception e) {
                    // 吃掉这个异常
                }
            }
            return objT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 复制集合
     *
     * @param <E>
     * @param source
     * @param destinationClass
     * @return
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static <E> List<E> copyToList(List<?> source, Class<E> destinationClass) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (source.size() == 0) return Collections.emptyList();
        List<E> res = new ArrayList<E>(source.size());
        for (Object o : source) {
            E e = destinationClass.newInstance();
            BeanUtils.copyProperties(e, o);
            res.add(e);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public static <T> T string2Value(String value, Class<T> clazz) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if (clazz.getName().equalsIgnoreCase("java.lang.String")) {
            return (T) value;
        }
        return (T) JSON.parseObject(value, clazz);
    }
}
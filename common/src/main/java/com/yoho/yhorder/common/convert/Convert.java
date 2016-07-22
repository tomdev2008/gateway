package com.yoho.yhorder.common.convert;

import java.util.List;

/**
 * 用做两个对象之间的拷贝
 *
 * @author xieyong
 */
public interface Convert {
    /**
     * 将源对象拷贝到目标对象，并返回目标对象
     *
     * @param source 源对象
     * @param target 目标对象
     * @param clazz  返回的类型
     * @return
     */
    <T> T convertFrom(Object source, Object target, Class<T> clazz);


    @SuppressWarnings("unchecked")
    public <T> List<T> convertFromBatch(List sourceList, List<T> targetList, Class<T> clazz) ;

}

package com.yoho.yhorder.dal.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * 基础bean, 实现toString方法
 *  qianjun 2016/2/18
 *
 */
public class Base implements Serializable {


    private static final long serialVersionUID = -3479617543803051076L;

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }

}

package com.yoho.yhorder.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 帐号安全注释
 * @Target(ElementType.METHOD) 标识注解应用的范围是方法上面
 * @Retention(RetentionPolicy.RUNTIME) 标识注解是运作时RUNTIME生效
 * @author caoqi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Security {

    // 这里是权限对应的url
    String url();

}

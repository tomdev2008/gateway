package com.yoho.yhorder.shopping.utils;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import org.springframework.util.Assert;

/**
 * Created by JXWU on 2015/11/30.
 */
public abstract class MyAssert {

    public static void isTrue(boolean expression, ServiceError serviceError) {
        Assert.notNull(serviceError,"service error must not be null");
        if (expression) {
            throw new ServiceException(serviceError);
        }
    }

    public static void isFalse(boolean expression, ServiceError serviceError) {
        Assert.notNull(serviceError,"service error must not be null");
        if (!expression) {
            throw new ServiceException(serviceError);
        }
    }

    public static Object isNull(String name, Object bo, ServiceError error) throws IllegalStateException {
        Assert.notNull(error,"service error must not be null");
        Object value = null;
        try {
            value = ReflectTool.invokeGetMethod(name, bo);
            MyAssert.isTrue(value == null, error);
            return value;
        } catch (Exception e) {
            throw new IllegalStateException("get method " + name + " of  " + bo.getClass().getName() + " is null,value is " + value);
        }
    }

}

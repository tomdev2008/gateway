package com.yoho.yhorder.common.utils;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;

/**
 * 统一抛出 <code>com.yoho.error.exception.ServiceException</code> 异常
 * 有新增到参考：
 * @see com.google.common.base.Preconditions
 * @author LiQZ on 2016/3/15.
 */
public final class YHPreconditions {

    private YHPreconditions() {}

    /**
     * 检测 id, 包装类型，基本类型都可以用
     */
    public static int checkID(Integer id, ServiceError errorMessage) {
        if (id ==  null || id <= 0) {
            throw new ServiceException(errorMessage);
        }
        return id;
    }

    public static void checkNotEmpty(Collection collection, ServiceError errorMessage) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ServiceException(errorMessage);
        }
    }

    /**
     * @see com.google.common.base.Preconditions#checkNotNull(Object, Object)
     */
    public static <T> T checkNotNull(T reference, ServiceError errorMessage) {
        if (reference == null) {
            throw new ServiceException(errorMessage);
        }
        return reference;
    }

    /**
     * @see com.google.common.base.Preconditions#checkArgument(boolean, Object)
     */
    public static void checkArgument(boolean expression, ServiceError errorMessage) {
        if (!expression) {
            throw new ServiceException(errorMessage);
        }
    }

    public static void checkState(boolean expression, ServiceError errorMessage) {
        if (!expression) {
            throw new ServiceException(errorMessage);
        }
    }


}

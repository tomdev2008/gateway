package com.yoho.yhorder.shopping.event;

import com.yoho.yhorder.shopping.union.UnionContext;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by wujiexiang on 16/4/1.
 */
public class UnionPushCartEvent {

    //http请求头
    private String userAgent;

    private UnionContext unionContext;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public UnionContext getUnionContext() {
        return unionContext;
    }

    public void setUnionContext(UnionContext unionContext) {
        this.unionContext = unionContext;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

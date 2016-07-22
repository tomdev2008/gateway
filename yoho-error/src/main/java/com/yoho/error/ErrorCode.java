package com.yoho.error;

import java.util.Map;

/**
 *
 * Created by chang@yoho.cn on 2015/11/3.
 */
public interface ErrorCode {

    /**
     * 设置错误内容
     * @param content
     */
    public void setErrorContent(Map<String, Object> content);

    /**
     * 获取错误码
     * @return 错误码
     */
    public int getCode();
}

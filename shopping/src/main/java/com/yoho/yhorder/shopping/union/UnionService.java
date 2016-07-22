package com.yoho.yhorder.shopping.union;

/**
 * Created by JXWU on 2015/12/5.
 * 订单联盟
 */
public interface UnionService {

    /**
     * @param userAgent
     * @param unionContext
     */
    public void run(String userAgent, UnionContext unionContext);

}

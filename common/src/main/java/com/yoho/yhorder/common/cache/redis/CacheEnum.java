package com.yoho.yhorder.common.cache.redis;

/**
 * Created by wujiexiang on 16/6/28.
 */
public enum CacheEnum {


    USER_VIP("yh:order:vip:", 7200, "用户vip"),
    USER_BACKLIST("yh:order:erpbacklist:",300,"用户黑名单,老erp拉黑"),
    USER_REPEATABLESUBMIT("yh:order:repeatablesubmit:uid",3,"用户重复提交订单"),
    ;

    private String cachePreKey;
    //查询时间(单位秒)
    private long expireTime;
    private String desc;

    CacheEnum(String cachePreKey, long expireTime, String desc) {
        this.cachePreKey = cachePreKey;
        this.expireTime = expireTime;
        this.desc = desc;
    }

    public String getCachePreKey() {
        return cachePreKey;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public String getDesc() {
        return desc;
    }

    public String getCacheKey(String cachePostKey) {
        return this.getCachePreKey() + cachePostKey;
    }
}

package com.yoho.yhorder.shopping.cache;

import com.google.common.util.concurrent.AtomicLongMap;
import org.springframework.stereotype.Component;

/**
 * Created by JXWU on 2016/1/12.
 */
@Component
public class StatCacheService {
    /**
     * erp submit 失败个数统计
     */
    private static AtomicLongMap<String> erpSubmitCounterMapSinceStartup = AtomicLongMap.create();

    public  void incrementErpSubmit(String key)
    {
        erpSubmitCounterMapSinceStartup.incrementAndGet(key);
    }


    public static AtomicLongMap<String> getErpSubmitCounterMapSinceStartup() {
        return erpSubmitCounterMapSinceStartup;
    }
}

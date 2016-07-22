package com.yoho.yhorder.shopping.cache;

import com.yoho.core.cache.LocalCache;
import com.yoho.core.cache.LocalCacheCallback;
import com.yoho.yhorder.dal.ISysConfigMapper;
import com.yoho.yhorder.dal.model.SysConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by wujiexiang on 16/7/4.
 */
@Component
public class SysConfigCacheService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String TICKET_2016_SKN_KEY = "yohood_2016_skn";

    @Autowired
    private ISysConfigMapper iSysConfigMapper;

    private LocalCache localCache = new LocalCache();

    @PostConstruct
    private void init() {
        localCache.init("SysConfig Cache", 1, TimeUnit.HOURS, new LocalCacheCallback() {
            @Override
            public Object load(String key, Object oldvalue) throws Exception {
                Object newValue = iSysConfigMapper.selectByConfigKey(key);
                logger.info("key {},old value is {},new value is {}", key, oldvalue, newValue);
                return newValue;
            }
        });
    }

    public SysConfig getSysConfig(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        return (SysConfig)localCache.get(key);
    }

    public SysConfig getTicketConfigOfThisYear() {
        return getSysConfig(TICKET_2016_SKN_KEY);
    }

}

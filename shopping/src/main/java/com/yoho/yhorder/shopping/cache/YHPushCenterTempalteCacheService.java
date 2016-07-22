package com.yoho.yhorder.shopping.cache;

import com.yoho.core.cache.LocalCache;
import com.yoho.core.cache.LocalCacheCallback;
import com.yoho.yhorder.dal.IYHPushCenterTempalteDAO;
import com.yoho.yhorder.dal.model.YHPushCenterTempalte;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by JXWU on 2016/2/24.
 */
@Component
public class YHPushCenterTempalteCacheService {

    private final static String USE_COIN_APP_KEY = "usecoin-app";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private LocalCache localCache = new LocalCache();

    @PostConstruct
    private void init() {
        localCache.init("YHPushCenterTempalte Cache", 7, TimeUnit.DAYS, new LocalCacheCallback() {
            @Override
            public Object load(String key, Object oldvalue) throws Exception {
                String[] arrays = key.split("[-]");
                Object newValue = yhPushCenterTempalteDAO.selectPushCenterTempalte(arrays[0], arrays[1]);
                logger.info("key {},old value is {},new value is {}", key, oldvalue, newValue);
                return newValue;
            }
        });
    }

    @Autowired
    private IYHPushCenterTempalteDAO yhPushCenterTempalteDAO;

    public YHPushCenterTempalte getUseCoinTemplate() {
        return this.getTemplate(USE_COIN_APP_KEY);
    }

    public YHPushCenterTempalte getTemplate(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return (YHPushCenterTempalte) localCache.get(key);
    }
}

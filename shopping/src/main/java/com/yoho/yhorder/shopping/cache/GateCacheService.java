package com.yoho.yhorder.shopping.cache;

import com.yoho.core.cache.LocalCache;
import com.yoho.core.cache.LocalCacheCallback;
import com.yoho.yhorder.dal.IGateDAO;
import com.yoho.yhorder.dal.model.Gate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by JXWU on 2015/11/25.
 */
@Component
public class GateCacheService {
    //红包
    public final static String CART_USE_RED_ENVELOPE = "cartUseRedEnvelope";

    public final static String FREE_SHIPPING_LIMIT  ="freeShippingLimit";

    //优惠码
    public final static String CART_USE_PROMOTION_CODE = "cartUsePromotionCode";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private LocalCache localCache = new LocalCache();

    @PostConstruct
    private void init() {
        localCache.init("Gate Cache", 30, TimeUnit.MINUTES, new LocalCacheCallback() {
            @Override
            public Object load(String key, Object oldvalue) throws Exception {
                Object newValue = gateMapper.selectByMetaKey(key);
                logger.info("key {},old value is {},new value is {}", key, oldvalue, newValue);
                return newValue;
            }
        });
    }

    @Autowired
    private IGateDAO gateMapper;

    public Gate getGateFor(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        return (Gate)localCache.get(key);
    }

    /**
     * 是否开启红包开关
     * @return
     */
    public boolean isOpenRedEnvelope() {
        Gate gate = this.getGateFor(GateCacheService.CART_USE_RED_ENVELOPE);
        if (gate != null && gate.getStatus()) {
            return true;
        }
        return false;
    }
}

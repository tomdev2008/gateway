package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.promotion.LimitCodeUserBo;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MyAssert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wujiexiang on 16/3/22.
 */
@Component
public class LimitCodeCharge {

    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    @Autowired
    private ExternalService externalService;

    public void charge(ChargeContext chargeContext) {
        ChargeParam chargeParam = chargeContext.getChargeParam();
        List<ShoppingItem> items = chargeParam.getShoppingItemList();
        if (CollectionUtils.isNotEmpty(items)) {
            int uid = chargeParam.getUid();
            validUserHaveLimitcode(uid, items);
        }
    }


    /**
     * 校验立即购买的数据项
     *
     * @param uid
     * @param items
     * @throws ServiceException
     */
    private void validUserHaveLimitcode(int uid, List<ShoppingItem> items) throws ServiceException {
        for (ShoppingItem item : items) {
            if (Constants.LIMITCODE_CHARGE_TYPE.equals(item.getType())) {
                checkLimitCodeAndSetupLimitCode(uid, item);
            }
        }
    }


    private void checkLimitCodeAndSetupLimitCode(int uid, ShoppingItem item) {
        logger.info("before check,item is {}", item);
        MyAssert.isTrue(uid < 1, ServiceError.SHOPPING_UID_IS_NULL);

        String skn = String.valueOf(item.getSkn());
        MyAssert.isTrue(StringUtils.isEmpty(skn), ServiceError.SHOPPING_REQUESTPARAMS_IS_NULL);

        String sku = String.valueOf(item.getSku());
        MyAssert.isTrue(StringUtils.isEmpty(sku),ServiceError.SHOPPING_REQUESTPARAMS_IS_NULL);

        String limitProductCode = String.valueOf(item.getLimitProductCode());
        MyAssert.isTrue(StringUtils.isEmpty(limitProductCode), ServiceError.SHOPPING_REQUESTPARAMS_IS_NULL);

        LimitCodeUserBo limitCodeUserBo = externalService.queryLimitCodeUserBo(uid, skn, sku, limitProductCode);
        logger.info("query limit code,uid is {},skn is {},limitProductCode is {},response is {}", uid, skn, limitProductCode, limitCodeUserBo);
        item.setLimitCode(limitCodeUserBo.getLimitCode());
        logger.info("after check,item is {}", item);
    }

}

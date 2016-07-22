package com.yoho.yhorder.shopping.transaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by fruwei on 2016/6/6.
 */
@Component
public class TxPromotionService {
    Logger logger= LoggerFactory.getLogger(TxPromotionService.class);

    public  void usePromotion(String promotion_code){
        logger.info("use promotion code : {}",promotion_code);
    }
}

package com.yoho.yhorder.shopping.transaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by fruwei on 2016/6/6.
 */
@Component
public class TxRedEnvelopeService {
    Logger logger= LoggerFactory.getLogger(TxRedEnvelopeService.class);

    public  void useRedEnvelope(double amount){
        logger.info("use redenvelope : {}",amount);
    }
}

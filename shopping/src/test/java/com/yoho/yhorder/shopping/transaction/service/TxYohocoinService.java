package com.yoho.yhorder.shopping.transaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by fruwei on 2016/6/6.
 */
@Component
public class TxYohocoinService {
    Logger logger= LoggerFactory.getLogger(TxYohocoinService.class);

    public  void useYohocoin(int num){
        logger.info("use yohocoin : {}",num);
    }
}

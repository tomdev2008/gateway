package com.yoho.yhorder.shopping.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import com.yoho.core.transaction.annoation.YHTransaction;
import com.yoho.service.model.inbox.InBoxModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by fruwei on 2016/6/6.
 */
@Component
public class TxStorageService {
    Logger logger= LoggerFactory.getLogger(TxStorageService.class);

    public TxStorageService(){
        logger.info("TxStorageService ...");
    }

    public Map<Integer,Integer> storage=new HashMap<Integer,Integer>();

    @TxCompensatable(value = TxStorageService.class)
    public  void decreaseStorage(@TxCompensateArgs("sku")int sku,
                                 @TxCompensateArgs("num")int num){
        logger.info("decrease sku {} - {} ",sku,num);

    }

    public void compensate(String message) {
        returnStorage(message);
    }

    public void returnStorage(String message){
        JSONObject json = JSON.parseObject(message);
        logger.info("return storage sku: {} + {} ",json.getString("sku"),json.getString("num"));
    }

}

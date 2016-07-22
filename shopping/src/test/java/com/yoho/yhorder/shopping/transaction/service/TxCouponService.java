package com.yoho.yhorder.shopping.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by fruwei on 2016/6/6.
 */
@Component
public class TxCouponService {
    Logger logger= LoggerFactory.getLogger(TxCouponService.class);


    @TxCompensatable(value = TxCouponService.class)
    public  void useCoupon(@TxCompensateArgs("coupon_code") String coupon_code){
        logger.info("use coupon code : {}",coupon_code);
        if(coupon_code.charAt(0)=='a'){
            throw new RuntimeException("coupon exception");
        }

    }

    public void compensate(String message) {
        returnCoupon(message);
    }

    public void returnCoupon(String message){
        JSONObject json = JSON.parseObject(message);
        logger.info("return coupon code : {}",json.getString("coupon_code"));
    }

}

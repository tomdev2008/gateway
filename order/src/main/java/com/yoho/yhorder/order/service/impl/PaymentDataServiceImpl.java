package com.yoho.yhorder.order.service.impl;

import com.yoho.yhorder.dal.PaymentDataMapper;
import com.yoho.service.model.order.response.PaymentData;
import com.yoho.yhorder.order.service.IPaymentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ming on 15/11/30.
 */
@Service
public class PaymentDataServiceImpl implements IPaymentDataService {

    @Autowired
    PaymentDataMapper paymentDataMapper;

    @Override
    public PaymentData getPaymentDataByOrderCode(Long orderCode) {
        PaymentData paymentData = paymentDataMapper.selectByOrderCode(orderCode);
        if (null == paymentData) {
            return null;
        }
        int curr = (int) (System.currentTimeMillis() / 1000);
        if (paymentData.getExpireTime() - curr < 100) {
            return null;
        }
        return paymentData;
    }

    @Override
    public void savePaymentData(PaymentData paymentData) {
        paymentDataMapper.insertSelective(paymentData);
    }
}

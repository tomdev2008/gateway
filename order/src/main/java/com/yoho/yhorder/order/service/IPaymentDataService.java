package com.yoho.yhorder.order.service;

import com.yoho.service.model.order.response.PaymentData;

/**
 * Created by ming on 15/11/30.
 */
public interface IPaymentDataService {

    PaymentData getPaymentDataByOrderCode(Long orderCode);

    void savePaymentData(PaymentData paymentData);
}

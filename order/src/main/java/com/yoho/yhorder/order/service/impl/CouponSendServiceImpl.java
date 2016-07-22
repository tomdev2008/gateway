package com.yoho.yhorder.order.service.impl;

import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.promotion.request.ParamsConfigReq;
import com.yoho.service.model.promotion.response.EventConfigRsp;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.model.CouponSend;
import com.yoho.yhorder.order.service.CouponSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yoho on 2016/1/7.
 */
@Service
public class CouponSendServiceImpl implements CouponSendService {


    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private IOrdersMapper ordersMapper;

    public void sendOrderConfirmCoupon(CouponSend couponSend) {
        Orders orders = ordersMapper.selectByOrderCode(couponSend.getOrderCode().toString());
        if (orders == null) {
            logger.warn("SendOrderConfirmCoupon fail, can not find order {}.", couponSend.getOrderCode());
            return;
        }
        int refundStatus = orders.getRefundStatus().intValue();
        //0:正常订单, 1 => '退货审核中', 2 => '退货审核不通过', 3 => '退货审核通过', 4 => '退货商品寄回', 5 => '退货库房入库',  6 => '财务退款',  7 => '完成'
        if (refundStatus == 0 || refundStatus == 2) {
            try {
                ParamsConfigReq paramsConfigReq = new ParamsConfigReq();
                paramsConfigReq.setUid(couponSend.getUid());
                paramsConfigReq.setEventCode("SEND_ORDER_CONFIRM_COUPON");
                paramsConfigReq.setType(1);
                EventConfigRsp eventConfigRsp = serviceCaller.call("promotion.sendCouponByConfig", paramsConfigReq, EventConfigRsp.class);
                if (eventConfigRsp.getFlag() == 1) {
                    logger.info("coupons send success, uid {} order {}.", couponSend.getUid(), couponSend.getOrderCode());
                } else {
                    logger.warn("SendOrderConfirmCoupon fail, uid {} order {}.", couponSend.getUid(), couponSend.getOrderCode());
                }
            } catch (ServiceException e) {
                //已经发送过优惠券
                if (e.getServiceError() == ServiceError.PROMOTION_COUPON_SEND_FAIL) {
                    logger.info("coupons has sent.uid {} order {}.", couponSend.getUid(), couponSend.getOrderCode());
                } else if (e.getServiceError() == ServiceError.PROMOTION_COUPON_HAS_RECEIVED) {
                    logger.info("coupons has recevied.uid {} order {}.", couponSend.getUid(), couponSend.getOrderCode());
                } else {
                    logger.warn("SendOrderConfirmCoupon fail, uid {} order {} service error {} {}.", couponSend.getUid(), couponSend.getOrderCode(), e.getServiceError().getCode(), e.getServiceError().getMessage());
                }
            } catch (Exception e) {
                logger.warn("SendOrderConfirmCoupon fail.", e);
            }
        } else if (refundStatus == 1 || refundStatus == 3 || refundStatus == 4 || refundStatus == 5 || refundStatus == 6) {
            logger.warn("SendOrderConfirmCoupon fail, order {} is refunding...", couponSend.getOrderCode());
        } else if (refundStatus == 7) {
            logger.warn("SendOrderConfirmCoupon fail, order {} is refunded.", couponSend.getOrderCode());
        } else {
            logger.warn("SendOrderConfirmCoupon fail, order {} unknown refundStatus {}.", couponSend.getOrderCode(), refundStatus);
        }
    }

}

package com.yoho.yhorder.order.service.impl;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.OrderPromotionInfoReq;
import com.yoho.service.model.order.response.OrderPromotionInfoBo;
import com.yoho.yhorder.dal.OrderPromotionInfoMapper;
import com.yoho.yhorder.dal.model.OrderPromotionInfo;
import com.yoho.yhorder.order.service.IOrderPromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * OrderPromotionServiceImpl
 * 订单优惠券信息类
 *
 * @author lijian
 * @date 2015/11/22
 */
@Service
public class OrderPromotionServiceImpl implements IOrderPromotionService {

    @Autowired
    private OrderPromotionInfoMapper orderPromotionInfoMapper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ArrayList<OrderPromotionInfoBo> selectOrdPromotionListByUserInfo(OrderPromotionInfoReq orderPromotionInfoReq) {

        if (orderPromotionInfoReq == null || (orderPromotionInfoReq != null && orderPromotionInfoReq.getUid() == null
                && orderPromotionInfoReq.getOrderCode() == null)){
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }

       ArrayList <OrderPromotionInfo> orderPromotionInfos=orderPromotionInfoMapper.selectOrdPromotionListByUserInfo(orderPromotionInfoReq);
        ArrayList <OrderPromotionInfoBo> orderPromotionInfoBos=new ArrayList<OrderPromotionInfoBo>();
        if(orderPromotionInfoBos!=null&&orderPromotionInfoBos.size()>0){

            for(OrderPromotionInfo orderPromotion: orderPromotionInfos){
                OrderPromotionInfoBo orderPromotionBO=new OrderPromotionInfoBo();
                BeanUtils.copyProperties(orderPromotion,orderPromotionBO);
                orderPromotionInfoBos.add(orderPromotionBO);
            }

        }
            return orderPromotionInfoBos;
    }

    @Override
    public OrderPromotionInfoBo selectByOrderCode(Long orderCode) {
        OrderPromotionInfo orderPromotion = orderPromotionInfoMapper.selectByOrderCode(orderCode);
        OrderPromotionInfoBo orderPromotionBO=new OrderPromotionInfoBo();
        if(orderPromotion != null){
            BeanUtils.copyProperties(orderPromotion,orderPromotionBO);
        }
        logger.info("query promotion info by order code {}, result {}. ", orderCode , orderPromotion );

        return orderPromotionBO;
    }
}

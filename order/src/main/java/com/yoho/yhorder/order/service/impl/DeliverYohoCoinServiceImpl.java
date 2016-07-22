package com.yoho.yhorder.order.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yoho.core.common.utils.LocalIp;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.request.YohoCoinRecordReq;
import com.yoho.service.model.response.CommonRspBO;
import com.yoho.yhorder.common.utils.CalendarUtils;
import com.yoho.yhorder.dal.IOrdersGoodsMapper;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.IOrdersYohoCoinDAO;
import com.yoho.yhorder.dal.IRefundGoodsDao;
import com.yoho.yhorder.dal.model.OrdersYohoCoin;
import com.yoho.yhorder.dal.model.RefundGoods;
import com.yoho.yhorder.order.service.DeliverYohoCoinService;
import com.yoho.yhorder.order.service.UseYohoCoinService;

/**
 * 赠送有货币
 * @author yoho
 *
 */
@Service
public class DeliverYohoCoinServiceImpl implements DeliverYohoCoinService{
    private final static Logger logger = LoggerFactory.getLogger(DeliverYohoCoinServiceImpl.class);
    
    // 购买商品赠送
    public final static int ORDER_SUBMIT_USE_YOHO_COIN_TYPE = 13;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private IOrdersYohoCoinDAO ordersYohoCoinDAO;
    
    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Value("${deliver.yohocoin.time.interval:604800}")
    private int deliverYohoCoinTimeInterval;												// 7day == 604800s

    @Value("${order.task.host:localhost}")
    private String taskHost;

    private String localHost;
    
    @Autowired
    private IOrdersMapper ordersMapper;
    
    @Autowired
    private IRefundGoodsDao refundGoodsDao;
    
    @Autowired
    private UseYohoCoinService useYohoCoinService;


    public DeliverYohoCoinServiceImpl() {
        localHost = LocalIp.getLocalIp();
    }
    
    /**
     * 订单完成7天后，赠送有货币
     */
    @Override
    @Scheduled(fixedRate = 300000)
    public void deliverYohoCoin() {
        if (!taskHost.equals(localHost)) {
            logger.info("can not exec task,because of task host is {} local host is {}.", taskHost, localHost);
            return;
        }
        int createTime = (int) (CalendarUtils.getSystemSeconds() - deliverYohoCoinTimeInterval);
        
        List<OrdersYohoCoin> selectList = selectTop100List(createTime);
        if (selectList.isEmpty()) {
            logger.info("can not find any ordersYohoCoin to deliver. deliverYohoCoinTimeInterval is {}", deliverYohoCoinTimeInterval);
            return;
        }
        
        for (OrdersYohoCoin item : selectList) {
        	deliverYohoCoin(item);
		}
        
        logger.info("deliverYohoCoin success.");
    }

    // 查询一百条需要赠送有货币的记录
	private List<OrdersYohoCoin> selectTop100List(int createTime) {
		OrdersYohoCoin ordersYohoCoin = new OrdersYohoCoin();
        ordersYohoCoin.setStatus(OrdersYohoCoin.STATUS_NEED_DELIVER);
		List<OrdersYohoCoin> selectList = ordersYohoCoinDAO.selectTop100List(ordersYohoCoin, createTime);		// 查询100个需要赠送有货币的数据
		return selectList;
	}

	private void deliverYohoCoin(OrdersYohoCoin ordersYohoCoin) {
		Orders orders = ordersMapper.selectByOrderCode(String.valueOf(ordersYohoCoin.getOrderCode()));
		
		// 判断是否有退货记录，如果没有记录，则表明没有退货
		List<RefundGoods> refundGoodsList = refundGoodsDao.selectByOrderCode(ordersYohoCoin.getOrderCode());
		if (CollectionUtils.isEmpty(refundGoodsList)) {
			logger.info("useYohoCoin success, OrderCode {}.", orders.getOrderCode());
			useYohoCoin(orders, ordersYohoCoin.getId());     // 赠送有货币，更新状态为已赠送，填写当前时间为赠送时间
		} else {
			boolean flag = true;
			for (RefundGoods refundGoods : refundGoodsList) {
				if (Byte.valueOf((byte)40).equals(refundGoods.getStatus())) {   // 如果有部分退货完成，则将订单不赠送有货币
					updateDeliver(ordersYohoCoin.getId(), OrdersYohoCoin.STATUS_CACEL_DELIVER, 0);		// 已退货，设置成不能赠送有货币
					logger.info("deliverYohoCoin fail, order {} is refunded.", orders.getOrderCode());
					return;
				} else if (!Byte.valueOf((byte)91).equals(refundGoods.getStatus())){
					flag = false;
				}
			}
			
			// 如果所有的商品都退货驳回，则赠送有货币
			if (flag) {
				logger.info("useYohoCoin success, OrderCode {}.", orders.getOrderCode());
				useYohoCoin(orders, ordersYohoCoin.getId());     // 赠送有货币，更新状态为已赠送，填写当前时间为赠送时间
			}
		}
		
		
        /*if (orders == null) {
            logger.warn("deliverYohoCoin fail, can not find order {}.", ordersYohoCoin);
            return;
        }
        int refundStatus = orders.getRefundStatus().intValue();
        //0:正常订单, 1 => '退货审核中', 2 => '退货审核不通过', 3 => '退货审核通过', 4 => '退货商品寄回', 5 => '退货库房入库',  6 => '财务退款',  7 => '完成'
        if (refundStatus == 0 || refundStatus == 2) {
            useYohoCoin(orders, ordersYohoCoin.getId());     // 赠送有货币，更新状态为已赠送，填写当前时间为赠送时间
        } else if (refundStatus == 1 || refundStatus == 3 || refundStatus == 4 || refundStatus == 5 || refundStatus == 6) {    // 退货中，则不进行任何处理
            logger.info("deliverYohoCoin fail, order {} is refunding...", orders.getOrderCode());
        } else if (refundStatus == 7) {   
        	updateDeliver(ordersYohoCoin.getId(), OrdersYohoCoin.STATUS_CACEL_DELIVER, 0);		// 已退货，设置成不能赠送有货币
            logger.info("deliverYohoCoin fail, order {} is refunded.", orders.getOrderCode());
        } else {
            logger.warn("deliverYohoCoin fail, order {} unknown refundStatus {}.", orders.getOrderCode(), refundStatus);
        }*/
	}

	// 更新订单赠送有货币的状态
	private void updateDeliver(Integer orderId, Integer status, Integer deliverTime) {
		OrdersYohoCoin record = new OrdersYohoCoin();
		record.setId(orderId);
		record.setStatus(status);
		record.setDeliverTime(deliverTime);
		ordersYohoCoinDAO.updateByPrimaryKeySelective(record);
	}
	
	// 赠送有货币，更新状态为已赠送，填写当前时间为赠送时间
	private void useYohoCoin(Orders order, Integer ordersYohoCoinId) {
		// 用户的有货币表中默认存放 有货币数    100个有货币数 == 1元       商品价格表的有货币也是有货币数
		int useYohoCoin = order.getDeliverYohoCoin();
        try {
	        if (useYohoCoin > 0) {
	            logger.info("order code {} use yoho coin,yohoCoin amount is {},yohoCoin num is {},uid is {}", order.getOrderCode(), order.getYohoCoinNum(), useYohoCoin, order.getUid());
	            CommonRspBO rspBO = useYohoCoin(order);
	            logger.info("after order {} use yoho coin,uid {} has yohoCoin {},", order.getOrderCode(), order.getUid(), rspBO);
	            
	            if (null == rspBO || rspBO.getCode() != 200) {
	            	logger.warn("useYohoCoin fail. order {} use yoho coin,uid {} has yohoCoin {},", order.getOrderCode(), order.getUid(), rspBO);
	            }
	        }
        } catch (ServiceException e) {
        	logger.warn("deliver YohoCoin to user find fail. ordersYohoCoinId is {}" + ordersYohoCoinId, e);
        } catch (Exception e) {
            logger.warn("deliverYohoCoin fail. ordersYohoCoinId is {}" + ordersYohoCoinId, e);
        } finally {
        	updateDeliver(ordersYohoCoinId, OrdersYohoCoin.STATUS_ALREADY_DELIVER, CalendarUtils.getSystemIntSeconds());     // 只要调用用户的接口，则代表已发放。不然就会出现异常后，一直会发放有货币
        }
    }
	
	
	private CommonRspBO useYohoCoin(Orders order) {
		List<OrdersGoods> goodsByOrderId = ordersGoodsMapper.selectOrderGoodsByOrderId(Lists.newArrayList(order.getId()));
		if (CollectionUtils.isEmpty(goodsByOrderId)) {
			logger.warn("selectOrderGoodsByOrderId is null. order is {}", order);
			return null;
		}
		Integer uid = order.getUid();
		Long orderCode = order.getOrderCode();
		
		List<YohoCoinRecordReq> list = new ArrayList<YohoCoinRecordReq>();
		JSONObject params;
		for (OrdersGoods ordersGoods : goodsByOrderId) {
			params = new JSONObject();
            params.put("order_code", orderCode);
            params.put("product_skn", ordersGoods.getProductSkn());
            params.put("product_sku", ordersGoods.getProductSku());
			list.add(useYohoCoinService.buildYohoCoinRecordReq(uid, orderCode, 
					ordersGoods.getGetYohoCoin() * (ordersGoods.getNum() == null ? 1 : ordersGoods.getNum()), ORDER_SUBMIT_USE_YOHO_COIN_TYPE, params.toJSONString()));
		}
		
		return useYohoCoinService.useBatchYohoCoin(list);
	}
}

package com.yoho.yhorder.order.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.service.model.request.YohoCoinCostReqBO;
import com.yoho.service.model.request.YohoCoinLogReqBO;
import com.yoho.service.model.request.YohoCoinRecordReq;
import com.yoho.service.model.request.YohoCoinReqBO;
import com.yoho.service.model.response.CommonRspBO;
import com.yoho.yhorder.order.service.UseYohoCoinService;

/**
 * 用户使用有货币接口
 * @author yoho
 *
 */
@Service
public class UseYohoCoinServiceImpl implements UseYohoCoinService {
	private final static Logger logger = LoggerFactory.getLogger(UseYohoCoinServiceImpl.class);
	
    public final static String USERS_ADDRECORD_YOHOCOIN_REST_URL = "users.batchAddRecord";
    
    @Autowired
    private ServiceCaller serviceCaller;

	@Override
	public CommonRspBO useBatchYohoCoin(List<YohoCoinRecordReq> list) {
        logger.info("UseYohoCoinService call service users.batchAddRecord to use yoho coin");
        
        CommonRspBO rspBO = serviceCaller.call(USERS_ADDRECORD_YOHOCOIN_REST_URL, list, CommonRspBO.class);
        
        logger.info("UseYohoCoinService call service {} to use yoho coin,result is {}", USERS_ADDRECORD_YOHOCOIN_REST_URL, rspBO);
        return rspBO;
	}
	
	public YohoCoinRecordReq buildYohoCoinRecordReq(int uid, long orderCode, int yohoCoinNum, int type, String params) {
		YohoCoinRecordReq req = new YohoCoinRecordReq();
		
		YohoCoinCostReqBO yohoCoinCostReq = buildYohoCoinCostReq(uid, orderCode, yohoCoinNum, type, params);
        YohoCoinLogReqBO yohoCoinHistory = buildYohoHistoryReq(uid, orderCode, yohoCoinNum, type, params);
        req.setCost(yohoCoinCostReq);
        req.setHistory(yohoCoinHistory);
		
		YohoCoinReqBO reqBO = new YohoCoinReqBO();
        reqBO.setUid(uid);
        reqBO.setNum(yohoCoinNum);
        reqBO.setOrder_code(orderCode);
        reqBO.setType(type);
        reqBO.setParams(params);
        
        return req;
	}
	
	private YohoCoinLogReqBO buildYohoHistoryReq(int uid, long orderCode, int yohoCoinNum, int type, String params) {
		YohoCoinLogReqBO req = new YohoCoinLogReqBO();
        req.setUid(uid);
        req.setChangeNum(Short.valueOf((short)yohoCoinNum));
        req.setChangeType(Byte.valueOf((byte)type));
        req.setChangeParams(params);
        return req;
	}

	private YohoCoinCostReqBO buildYohoCoinCostReq(int uid, long orderCode, int yohoCoinNum, int type, String params){
			YohoCoinCostReqBO yohoCoinCostReq = new YohoCoinCostReqBO();
			yohoCoinCostReq.setUid(uid);
			yohoCoinCostReq.setNum(yohoCoinNum);
			yohoCoinCostReq.setType(Byte.valueOf((byte)type));
			yohoCoinCostReq.setOrderCode(String.valueOf(orderCode));
			yohoCoinCostReq.setParams(params);
			return yohoCoinCostReq;
	}
}

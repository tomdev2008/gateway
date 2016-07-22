package com.yoho.yhorder.order.service;

import java.util.List;

import com.yoho.service.model.request.YohoCoinRecordReq;
import com.yoho.service.model.response.CommonRspBO;

public interface UseYohoCoinService {
	/**
     * 使用yoho币
     * @return
     */
	CommonRspBO useBatchYohoCoin(List<YohoCoinRecordReq> list);
	
	/**
	 * 构建有货币记录的对象
	 * @param uid
	 * @param orderCode
	 * @param yohoCoinNum
	 * @param type
	 * @param params
	 * @return
	 */
	YohoCoinRecordReq buildYohoCoinRecordReq(int uid, long orderCode, int yohoCoinNum, int type, String params);
}

package com.yoho.yhorder.order.payment.alipay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yoho.service.model.order.response.AntHbfqBO;
import com.yoho.yhorder.dal.IPaymentDAO;
import com.yoho.yhorder.dal.model.Payment;
import com.yoho.yhorder.order.model.AntInstallmentVO;

@Component
public class AntHbfqPayService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	//花呗分期支付方式ID
	private static final short ANT_HBFQ_PAYMENT_ID = 31;

	@Autowired
	IPaymentDAO paymentDao;
	
	List<AntInstallmentVO> hbfqParamList = new ArrayList<AntInstallmentVO>();
	
	@PostConstruct
	void init() {
		logger.info("AntHbfqPayService init");
		
		Payment payment =  paymentDao.selectByPrimaryKey(ANT_HBFQ_PAYMENT_ID);
		if(null == payment) {
			logger.error("Ant hbfq get params failed, payment {} params null", ANT_HBFQ_PAYMENT_ID);
			return;
		}
		
		hbfqParamList = getHbfqParamList(payment.getPayParams());		
		logger.info("AntHbfqPayService init end");
	}
	
	private List<AntInstallmentVO> getHbfqParamList(String hbfqParam) {
		List<AntInstallmentVO> paramList = new ArrayList<AntInstallmentVO>();
		logger.info("Ant hbfq params: {}", hbfqParam);
		
		try {
			paramList = JSON.parseArray(hbfqParam, AntInstallmentVO.class);
		} catch (Exception e) {
			logger.error("Ant hbfq params parse failed: {}", hbfqParam);
		}		
		
		logger.info("Ant hbfq params detail: {}", paramList);
		return paramList;
	}
	
	/**
	 * 根据订单金额计算分期详情
	 * @param orderAmount
	 * @return
	 */
	public List<AntHbfqBO> getAntHbfqDetail(double orderAmount) {
		List<AntHbfqBO> hbfqDetailList = new ArrayList<AntHbfqBO>();
		
		for(AntInstallmentVO hbfqMeta : hbfqParamList) {
			AntHbfqBO hbfqDetail = new AntHbfqBO();
			hbfqDetail.setStageNumber(hbfqMeta.getStageNumber());
			hbfqDetail.setSellerPercent(hbfqMeta.getSellerPercent());
			hbfqDetail.setTaxRate(hbfqMeta.getTaxRate());
			
			//总手续费
			double totalTaxFee = orderAmount * hbfqMeta.getTaxRate() / 100;
			//总实付款
			double totalAmount = orderAmount + totalTaxFee;
			//保留小数点后两位
			hbfqDetail.setTotalFee(new BigDecimal(totalAmount).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			hbfqDetail.setFeePerStage(new BigDecimal(totalAmount / hbfqMeta.getStageNumber()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			hbfqDetail.setTaxPerStage(new BigDecimal(totalTaxFee / hbfqMeta.getStageNumber()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			
			hbfqDetailList.add(hbfqDetail);
		}
		
		return hbfqDetailList;
	} 
	
	/**
	public static void main(String[] args) {
		String params = "[{\"stageNumber\":\"3\",\"taxRate\":\"2.5\",\"sellerPercent\":\"0\"},{\"stageNumber\":\"6\",\"taxRate\":\"4.5\",\"sellerPercent\":\"0\"}]";
		
		AntHbfqPayService service = new AntHbfqPayService();
		service.hbfqParamList = service.getHbfqParamList(params);
		
		System.out.println("====" + service.getAntHbfqDetail(3888));
	}
	*/
}

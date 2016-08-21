package com.yoho.yhorder.common.interceptor;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yoho.service.model.BaseBO;
import com.yoho.service.model.order.constants.YohoAppTypeDef;

/**
 * 
 * create by dh on 2016年8月7日
 * 下午5:06:06
 */
@Aspect
@Component
public class YohoTypeAspectj {
	Logger log = LoggerFactory.getLogger(YohoTypeAspectj.class);
	
	private static final ThreadLocal<Integer> app_type = new ThreadLocal<Integer>();
	
	@Before("@annotation(com.yoho.yhorder.common.annotation.YohoType) and args(request)")
	public void before(BaseBO request){
		if(request.getAppType() == YohoAppTypeDef.APP_TYPE_BLK){
			app_type.set(YohoAppTypeDef.APP_TYPE_BLK);
		}else{
			app_type.set(YohoAppTypeDef.APP_TYPE_YOHO);
		}
	}
	
	@After("@annotation(com.yoho.yhorder.common.annotation.YohoType)")
	public void after(){
		app_type.remove();
	}
	
	public static void setBLK(){
		app_type.set(YohoAppTypeDef.APP_TYPE_BLK);
	}
	public static boolean isBLK(){
		return app_type.get() !=null && app_type.get()==1 ?true:false;
	}
	public static String isBLKYN(){
		return app_type.get() !=null && app_type.get()==1 ? "Y":"N";
	}
	public static String getAppTypeName(){
		return app_type.get() !=null && app_type.get() ==1 ? YohoAppTypeDef.APP_TYPE_BLK_STR:YohoAppTypeDef.APP_TYPE_YOHO_STR;
	}

}

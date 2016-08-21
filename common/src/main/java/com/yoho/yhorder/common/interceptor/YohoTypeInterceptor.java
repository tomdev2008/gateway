package com.yoho.yhorder.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 
 * create by dh on 2016年8月7日
 * 下午6:04:06
 */
public class YohoTypeInterceptor extends HandlerInterceptorAdapter{
	private static final ThreadLocal<Integer> yohoType = new ThreadLocal<>();
	
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex){
		yohoType.remove();
	}
	
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		yohoType.set(0);
		if(request.getParameterMap().containsKey("yoho_type")){
			if(request.getParameter("yoho_type").equals("blk")){
				yohoType.set(1);
			}
		}
		return true;
	}
	public static boolean isBLK(){
		return yohoType.get() == 1?true:false;
	}
}

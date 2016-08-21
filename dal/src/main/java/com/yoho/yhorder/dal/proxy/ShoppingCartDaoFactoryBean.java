package com.yoho.yhorder.dal.proxy;

import java.lang.reflect.Method;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.yoho.yhorder.common.interceptor.YohoTypeAspectj;
import com.yoho.yhorder.dal.IBLKShoppingCartDAO;
import com.yoho.yhorder.dal.IShoppingCartDAO;
import com.yoho.yhorder.dal.IYohoShoppingCartDAO;

public class ShoppingCartDaoFactoryBean implements FactoryBean<IShoppingCartDAO>{
	@Autowired
	private IYohoShoppingCartDAO yohoShoppingDAO;
	@Autowired
	private IBLKShoppingCartDAO blkShoppingDAO;
	@Override
	public IShoppingCartDAO getObject() throws Exception {
		return (IShoppingCartDAO) Enhancer.create(IShoppingCartDAO.class, new ShoppingCartDAOAdvice());
	}
	@Override
	public Class<?> getObjectType() {
		return IShoppingCartDAO.class;
	}
	@Override
	public boolean isSingleton() {
		return true;
	}
	class ShoppingCartDAOAdvice implements MethodInterceptor{

		@Override
		public Object intercept(Object obj, Method method, Object[] args,
				MethodProxy proxy) throws Throwable {
			// TODO Auto-generated method stub
			if(YohoTypeAspectj.isBLK()){
				return proxy.invoke(blkShoppingDAO, args);
			}else{
				return proxy.invoke(blkShoppingDAO, args);
			}
		}
		
	}

}

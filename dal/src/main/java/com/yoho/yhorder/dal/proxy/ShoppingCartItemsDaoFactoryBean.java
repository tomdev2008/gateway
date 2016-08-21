package com.yoho.yhorder.dal.proxy;

import java.lang.reflect.Method;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.yoho.yhorder.common.interceptor.YohoTypeAspectj;
import com.yoho.yhorder.dal.IBLKShoppingCartDAO;
import com.yoho.yhorder.dal.IBLKShoppingCartItemsDAO;
import com.yoho.yhorder.dal.IShoppingCartDAO;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.IYohoShoppingCartDAO;
import com.yoho.yhorder.dal.IYohoShoppingCartItemsDAO;

public class ShoppingCartItemsDaoFactoryBean implements FactoryBean<IShoppingCartItemsDAO>{
	@Autowired
	private IYohoShoppingCartItemsDAO yohoShoppingItemsDAO;
	@Autowired
	private IBLKShoppingCartItemsDAO blkShoppingItemsDAO;
	@Override
	public IShoppingCartItemsDAO getObject() throws Exception {
		return (IShoppingCartItemsDAO) Enhancer.create(IShoppingCartItemsDAO.class, new ShoppingCartItemsDAOAdvice());
	}
	@Override
	public Class<?> getObjectType() {
		return IShoppingCartDAO.class;
	}
	@Override
	public boolean isSingleton() {
		return true;
	}
	class ShoppingCartItemsDAOAdvice implements MethodInterceptor{

		@Override
		public Object intercept(Object obj, Method method, Object[] args,
				MethodProxy proxy) throws Throwable {
			// TODO Auto-generated method stub
			if(YohoTypeAspectj.isBLK()){
				return proxy.invoke(blkShoppingItemsDAO, args);
			}else{
				return proxy.invoke(yohoShoppingItemsDAO, args);
			}
		}
		
	}

}

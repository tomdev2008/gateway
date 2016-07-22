package com.yoho.yhorder.common.convert;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xieyong
 *
 */
@Service
public class BeanConvert implements Convert{

	private Logger logger= LoggerFactory.getLogger(getClass());
	
	/* (non-Javadoc)
	 * @see com.yoho.product.product.convert.Convert#convertFrom(java.lang.Object, java.lang.Object, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertFrom(Object source, Object target, Class<T> clazz) {
		if (source == null){
			return null;
		}
		Preconditions.checkNotNull(target, "target can't be null");
//		BeanCopier copier = BeanCopier.create(source.getClass(), target.getClass(), false);

		BeanUtils.copyProperties(source,target);
//		copier.copy(source, target, null);
		return (T) target;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> convertFromBatch(List sourceList, List<T> targetList, Class<T> clazz) {

		for (Object source:sourceList){
			try {
				Object target = clazz.newInstance();
				convertFrom(source,target,clazz);
				targetList.add((T)target);
			} catch (Exception e) {
				logger.error("转换对象异常",e);
			}

		}
		return targetList;
	}


	public static void main(String[] args) {
//		ShareOrder shareOrder = new ShareOrder();
//		shareOrder.setId("xxxxxx");
//		List<Object> shareOrderList = new ArrayList<>();
//		shareOrderList.add(shareOrder);
//
//		List<ShareOrderBo> shareOrderBoList = new ArrayList<>();
//		new BeanConvert().convertFromBatch(shareOrderList,shareOrderBoList,ShareOrderBo.class);
//
//
//
//		System.out.println(shareOrderBoList.get(0).getId());
	}

}

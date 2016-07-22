package com.yoho.yhorder.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * 
 * 功能描述： 数字相关的
 * 
 * @author lijian
 */
public class NumUtil {
	/**
	 * 日志类
	 */
	private static Logger logger = LoggerFactory.getLogger(NumUtil.class);

	/**
	 * 生成此范围随机数
	 */
	public static int getIntRandom(double mi, double ma) {
		Random random = new Random();
		int min=  (int) mi;
		int max=(int)ma;

		int s = random.nextInt(max) % (max - min + 1) + min;
		return s;
	}

	/**
	 * 判断数字或是字符串是否是空或是0
	 */
	public static boolean checkNumIsZero(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof Integer) {
			return ((Integer) o).intValue() <= 0 ? true : false;
		} else if (o instanceof Double) {
			return ((Double) ((Double) o).doubleValue()) <= 0 ? true : false;
		} else if (o instanceof Float) {
			return (((Float) o).floatValue()) <= 0 ? true : false;
		} else if (o instanceof String) {
			return Double.valueOf((String) o) <= 0 ? true : false;
		}
		return false;

	}

	public  static  void  main(String args[]){




	}
}

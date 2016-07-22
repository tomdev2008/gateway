package com.yoho.yhorder.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有关手机号码相关的工具类
 *
 * @author xinfei
 */
public class PhoneUtil {

    private final static String CHINA_AREA_CODE = "86";

    private static Map<String, String> areaPatternMap = new HashMap<String, String>();

    private static Logger logger = LoggerFactory.getLogger(PhoneUtil.class);


    static {
        //------区域码，以及区域码对应的号码的校验正则表达式
        //中国
        areaPatternMap.put("86", "^1[3|4|5|8|7][0-9]{9}$");
        //中国香港
        areaPatternMap.put("852", "^[9|6|5][0-9]{7}$");
        //中国澳门
        areaPatternMap.put("853", "^[0-9]{8}$");
        //中国台湾
        areaPatternMap.put("886", "^[0-9]{10}$");
        //新加坡
        areaPatternMap.put("65", "^[9|8][0-9]{7}$");
        //马来西亚
        areaPatternMap.put("60", "^1[1|2|3|4|6|7|9][0-9]{8}$");
        //加拿大&美国
        areaPatternMap.put("1", "^[0-9]{10}$");
        //韩国
        areaPatternMap.put("82", "^01[0-9]{9}$");
        //英国
        areaPatternMap.put("44", "^7[7|8|9][0-9]{8}$");
        //日本
        areaPatternMap.put("81", "^0[9|8|7][0-9]{9}$");
        //澳大利亚
        areaPatternMap.put("61", "^[0-9]{11}$");
    }


    /**
     * 根据区域码，对手机号码进行处理，如果不是中国手机号码，在号码之前加区域码
     *
     * @param area   区域码
     * @param mobile 手机号码
     * @return String 处理之后的手机号码
     */
    public static String makePhone(String area, String mobile) {
        if (null == mobile || mobile.isEmpty()) {
            logger.info("makePhone: mobile is null. area is {}, mobile is {}", area, mobile);
            return null;
        }
        if (null == area || area.isEmpty() || CHINA_AREA_CODE.equals(area)) {
            logger.debug("makePhone: mobile is {}, area is {}", mobile, area);
            return mobile;
        }
        logger.debug("makePhone: mobile is {}, area is {}", mobile, area);
        return area + "-" + mobile;
    }

    /**
     * 根据区域码，校验各个国家和地区的号码格式是否正确
     *
     * @param mobile
     * @return
     */
    public static boolean mobileVerify(String mobile) {
        logger.debug("Enter areaMobielVerify: mobile is {}", mobile);
        //(1)如果号码不存在,返回空记录
        if (null == mobile || mobile.isEmpty()) {
            return false;
        }
        //(2)根据国家或者地区码返回匹配模式, 如果号码不包含-, 那么则按照中国国家码校验
        if (mobile.indexOf("-") == -1) {
            return areaMobileVerify(null, mobile);
        }
        //(3)如果号码包含-, 那么当前号码就是国际号码, 对号码进行校验
        String[] areaMobiles = mobile.split("-");
        if (null == areaMobiles || areaMobiles.length != 2) {
            return false;
        }
        //(4)对国际号码进行校验
        return areaMobileVerify(areaMobiles[0], areaMobiles[1]);
    }

    /**
     * 根据区域码，校验各个国家和地区的号码格式是否正确
     *
     * @param area
     * @param mobile
     * @return
     */
    public static boolean areaMobileVerify(String area, String mobile) {
        String[] arr = mobile.split("-");
        if (StringUtils.isEmpty(area) && arr.length == 1) {
            area = "86";
        } else if (arr.length == 2) {
            area = arr[0];
            mobile = arr[1];
        }

        //根据国家或者地区码返回匹配模式
        String reg = areaPatternMap.get(area);
        if (null == reg) {
            return false;
        }
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(mobile);
        boolean ret = m.matches();
        return ret;
    }

    /**
     * 获取四位短信验证码
     *
     * @return String
     */
    public static String getPhoneVerifyCode() {
        Random random = new Random();
        int result = random.nextInt(10000);
        logger.debug("getPhoneVerifyCode: areaCode is {}", result);
        return String.valueOf(result);
    }

/*	*//**
     * 验证手机号是否正确，并且返回重新组装过的手机号和国家码
     * @param area
     * @param mobile
     * @return
     *//*
    public static Map<String, String> makeMobileAndCheck(String area, String mobile){
		if (StringUtils.isEmpty(mobile)) {
			return null;
		}
		String[] arr = mobile.split("-");
		if(StringUtils.isEmpty(area) && arr.length == 1) {
			area = "86";
		} else if (arr.length == 2) {
			area = arr[0];
			mobile = arr[1];
		}
		
		//根据国家或者地区码返回匹配模式
		String reg = areaPatternMap.get(area);
		if(null == reg){
			return null;
		}
		boolean b = mobile.matches(reg);
		if (!b) {
			return null;
		} else {
			Map<String, String> map = new HashMap<String, String>();
			map.put("area", area);
			map.put("mobile", mobile);
			return map;
		}
	}*/

}

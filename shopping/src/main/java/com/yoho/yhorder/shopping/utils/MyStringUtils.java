package com.yoho.yhorder.shopping.utils;

import com.yoho.service.model.order.response.shopping.PromotionFormula;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JXWU on 2015/11/19.
 */
public class MyStringUtils {

    private final static Logger logger = LoggerFactory.getLogger(MyStringUtils.class);

    private final static String IPV4_MATCH_REGEXP ="(\\d{1,3}\\.){3}\\d{1,3}";

    private MyStringUtils() {
    }


    /**
     * 自动生成一个key
     */
    public static String getShoppingKey() {
        return getMd5(UUID.randomUUID().toString());
    }

    public static String getMd5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(str.getBytes());
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        return null;
    }

    public static int getYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);

    }

    public static String generateGetterOrSetter(String pre, String key) {
        org.springframework.util.Assert.hasText(key);
        StringBuilder builder = new StringBuilder(pre);
        builder.append(String.valueOf(key.charAt(0)).toUpperCase());
        if (key.length() > 1) {
            builder.append(key.substring(1));
        }
        return builder.toString();
    }

    public static Map<String, String> asStringToMap(String userAgent) {
        Map<String, String> resultMap = new HashMap<>();
        if (org.apache.commons.lang.StringUtils.isEmpty(userAgent)) {
            return resultMap;
        }
        //YH_Mall_iPhone/3.8.0.1511200002(Model/iPhone 4S;OS/iOS8.4.1;Scale/2.00;Channel/2919;Resolution/320*480;Udid/157683811de0c7fbe3e3c83c41a4ffbd0fd5b04f;sid/69e4b86ecbe0e01e4e291666ddde6fe4;ts/1449464993;uid/;ifa/5FED334D-5E8F-4083-AAC4-0CC7FBBEC87E)
        int beginIndex = userAgent.indexOf("(");
        int endIndex = userAgent.indexOf(")");
        if (beginIndex >= endIndex) {
            return resultMap;
        }
        try {
            String validUserAgent = userAgent.substring(beginIndex + 1, endIndex);
            String[] array = validUserAgent.split("[;]");
            if (array != null) {
                for (String str : array) {
                    String[] subArray = str.split("[/]");
                    if (subArray.length == 2) {
                        resultMap.put(subArray[0], subArray[1]);
                    }
                }
            }
        } catch (Exception e) {
            //nothing
        }
        return resultMap;
    }

    public static String formatSecond(Integer second) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date =new Date();
        date.setTime(second * 1000L);
        Calendar cal = Calendar.getInstance();
        return format.format(cal.getTime());
    }

    public static String toString(List<PromotionFormula> promotionFormulaList) {
        StringBuilder textBuilder = new StringBuilder();
        for (PromotionFormula promotionFormula : promotionFormulaList) {
            textBuilder.append(promotionFormula.formula());
        }
        return textBuilder.toString();
    }

    public static String filterUtf8mb4String(String text) {
        try {
            byte[] bytes = text.getBytes("UTF-8");
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            int i = 0;
            while (i < bytes.length) {
                short b = bytes[i];
                if (b > 0) {
                    buffer.put(bytes[i++]);
                    continue;
                }
                b += 256;
                if ((b ^ 0xC0) >> 4 == 0) {
                    buffer.put(bytes, i, 2);
                    i += 2;
                } else if ((b ^ 0xE0) >> 4 == 0) {
                    buffer.put(bytes, i, 3);
                    i += 3;
                } else if ((b ^ 0xF0) >> 4 == 0) {
                    i += 4;
                }
            }
            buffer.flip();
            return new String(buffer.array(), "utf-8");
        } catch (Exception e) {
            logger.warn("filterUtf8mb4String error ,text is {}", text);
            return "";
        }
    }

    /**
     * 将字符串表示的ip地址转换为long表示.
     *
     * @param ip ip地址
     * @return 以32位整数表示的ip地址
     */
    public static final long ip2Long(final String ip) {
        if (StringUtils.isEmpty(ip)) {
            return 0;
        }
        try {
            if (isExactlyMatches(IPV4_MATCH_REGEXP, ip)) {
                final String[] ipNums = ip.split("\\.");
                return (Long.parseLong(ipNums[0]) << 24)
                        + (Long.parseLong(ipNums[1]) << 16)
                        + (Long.parseLong(ipNums[2]) << 8)
                        + (Long.parseLong(ipNums[3]));
            }
        } catch (Exception e) {
            logger.warn("ip:{} convert long error", ip, e);
        }
        return 0;
    }

    public static boolean isExactlyMatches(String regexp, String source) {
        try {
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(source);
            return matcher.matches();
        } catch (Exception e) {
            logger.warn("{} match {} error", source, regexp, e);
            return false;
        }
    }

    public static boolean isEmpty(String str)
    {
        return org.apache.commons.lang3.StringUtils.isEmpty(str);
    }

    public static int string2int(String str) {
        if (StringUtils.isEmpty(str)) {
            return 0;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            logger.warn("parse {} to int error", str, ex);
            return 0;
        }
    }


    public static  void main(String[] args)
    {
        System.out.print(MyStringUtils.getShoppingKey());
    }


}

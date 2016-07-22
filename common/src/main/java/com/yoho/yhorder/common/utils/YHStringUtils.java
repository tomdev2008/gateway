package com.yoho.yhorder.common.utils;

import com.yoho.core.common.utils.MD5;
import com.yoho.service.model.order.response.shopping.PromotionFormula;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by JXWU on 2015/11/19.
 */
public class YHStringUtils {

    private final static Logger logger = LoggerFactory.getLogger(YHStringUtils.class);

    private YHStringUtils() {
    }

    public static String truncate(String str, int length) {
        if (StringUtils.isNotEmpty(str) && str.length() > length) {
            return str.substring(0, length);
        }
        return str;
    }

    public static String filterEmojiCharacter(String str) {
        if (StringUtils.isNotEmpty(str)) {
            return filterUtf8mb4String(str);
        } else {
            return str;
        }
    }

    public static String truncateAndFilterEmojiCharacter(String str, int length) {
        if (StringUtils.isNotEmpty(str)) {
            String filterStr = filterEmojiCharacter(str);
            if (StringUtils.isNotEmpty(filterStr) && filterStr.length() > length) {
                filterStr = filterStr.substring(0, length);
            }
            return filterStr;
        }
        return str;
    }


    public static String md5(String str) {
        return MD5.md5(str);
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

    public static int parseInt(String str) {
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

}

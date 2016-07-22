package com.yoho.yhorder.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LUOXC on 2016/3/9.
 */
public class BankUtils {

    private static final Map<String, String> banks;

    static {
        banks = new HashMap<>();
        banks.put("BOCB2C", "中国银行");
        banks.put("ICBCB2C", "中国工商银行");
        banks.put("ICBCBTB", "中国工商银行(B2B)");
        banks.put("CMB", "招商银行");
        banks.put("CCB", "中国建设银行");
        banks.put("CCBBTB", "中国建设银行(B2B)");
        banks.put("ABC", "中国农业银行");
        banks.put("ABCBTB", "中国农业银行(B2B)");
        banks.put("SPDB", "上海浦东发展银行");
        banks.put("SPDBB2B", "上海浦东发展银行(B2B)");
        banks.put("CIB", "兴业银行");
        banks.put("GDB", "广东发展银行");
        banks.put("SDB", "深圳发展银行");
        banks.put("CMBC", "中国民生银行");
        banks.put("COMM", "交通银行");
        banks.put("CITIC", "中信银行");
        banks.put("CEBBANK", "光大银行");
        banks.put("NBBANK", "宁波银行");
        banks.put("HZCBB2C", "杭州银行");
        banks.put("SHBANK", "上海银行");
        banks.put("SPABANK", "平安银行");
        banks.put("BJRCB", "北京农村商业银行");
    }

    public static String findBankNameByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        } else {
            return banks.get(code);
        }
    }
}

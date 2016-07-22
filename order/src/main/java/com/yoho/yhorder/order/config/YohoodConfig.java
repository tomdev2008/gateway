package com.yoho.yhorder.order.config;

import com.google.common.collect.Maps;
import com.yoho.yhorder.common.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by yoho on 2016/7/8.
 */
public class YohoodConfig {

    /**
     *  入场时间
     *
     */
    private static final Map<String , String> entranceTime= Maps.newHashMap();

    /**
     *  初始化演出入场时间
     *
     */
    static{
        entranceTime.put("2016-09-16 10:00", "13:00");
        entranceTime.put("2016-09-16 13:30", "16:30");
        entranceTime.put("2016-09-17 10:00", "13:00");
     }

    /**
     * 获取演出入场时间
     *
     */
    public static String getEntranceTime(Date date){
        String beginDate = DateUtil.format(date,DateUtil.yyyy_MM_dd_HH_mm );
        return  entranceTime.get(beginDate);
    }

    /**
     * 根据日期取得星期几
     *
     */
    public static String getWeek(Date date){
        String[] weeks = {"周日","周一","周二","周三","周四","周五","周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if(weekIndex<0){
            weekIndex = 0;
        }
        return weeks[weekIndex];
    }
}

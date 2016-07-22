package com.yoho.yhorder.shopping.model;

import com.yoho.yhorder.common.utils.Constants;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2015/12/15.
 */
public class OrderYohoCoin {
    //yoho币数量
    private int yohoCoinNum = 0;
    /**
     * yoho币单位,yuan代表元,jiao代表角,fen代表分
     */
    private String coinUnit;

    /**
     * 转换为钱，参与购物车运算
     *
     * @return
     */
    public double ratedYohoCoin() {
        return yohoCoinNum * 1.0 / ratio();
    }

    /**
     * 提交给erp时，转换为yoho币,
     *
     * @return
     */
    public int ratio() {
        int rate = Constants.YOHOCOIN_YUAN__CURRENCY_DILUTION_RATIO;
        //yoho币单位,yuan代表元,jiao代表角,fen代表分
        if ("yuan".equals(this.coinUnit)) {
            rate = Constants.YOHOCOIN_YUAN__CURRENCY_DILUTION_RATIO;
        } else if ("jiao".equals(this.coinUnit)) {
            rate = Constants.YOHOCOIN_JIAO_CURRENCY_DILUTION_RATIO;
        } else if ("fen".equals(this.coinUnit)) {
            rate = Constants.YOHOCOIN_FEN_CURRENCY_DILUTION_RATIO;
        }
        return rate;
    }

    public void setYohoCoinNum(int yohoCoinNum) {
        this.yohoCoinNum = yohoCoinNum;
    }

    public void setCoinUnit(String coinUnit) {
        this.coinUnit = coinUnit;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}

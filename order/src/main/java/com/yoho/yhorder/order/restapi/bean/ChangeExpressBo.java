package com.yoho.yhorder.order.restapi.bean;

/**
 * Created by sunjiexiang on 2015/11/23.
 * 物流对象
 */
public class ChangeExpressBo {
    /**
     * 换货申请id
     */

    private Integer applyId;

    /**
     * 物流公司
     */
    private String expressNumber;

    /**
     * 快递名称
     */
    private String expressName;

    /**
     * 快递id
     */
    private Integer expressId;

    public Integer getApplyId() {
        return applyId;
    }

    public void setApplyId(Integer applyId) {
        this.applyId = applyId;
    }

    public String getExpressNumber() {
        return expressNumber;
    }

    public void setExpressNumber(String expressNumber) {
        this.expressNumber = expressNumber;
    }

    public String getExpressName() {
        return expressName;
    }

    public void setExpressName(String expressName) {
        this.expressName = expressName;
    }

    public Integer getExpressId() {
        return expressId;
    }

    public void setExpressId(Integer expressId) {
        this.expressId = expressId;
    }

    /*

    'apply_id' => $params['id'],
            'express_number' => $params['express_number'],
            'express_name' => $params['express_company'],
            'express_id' => $params['express_id']*/

}
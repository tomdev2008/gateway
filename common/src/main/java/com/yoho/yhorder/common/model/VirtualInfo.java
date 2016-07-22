package com.yoho.yhorder.common.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by wujiexiang on 16/7/4.
 */
public class VirtualInfo {
    //虚拟种类,兼容以后的虚拟商品，3为门票
    @JSONField(name="virtual_type")
    private String virtutalType = "3";

    //门票类型，1展览票 2套票
    @JSONField(name ="ticket_type")
    private String ticketType;

    public String getVirtutalType() {
        return virtutalType;
    }

    public void setVirtutalType(String virtutalType) {
        this.virtutalType = virtutalType;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }
}

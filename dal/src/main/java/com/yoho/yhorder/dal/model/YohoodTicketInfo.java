package com.yoho.yhorder.dal.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class YohoodTicketInfo {
    private Integer id;

    private Long ticketCode;

    private Short status;

    private Integer updatetime;

    private Integer createTime;

    private String employCode;

    private String ticketUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(Long ticketCode) {
        this.ticketCode = ticketCode;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Integer getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Integer updatetime) {
        this.updatetime = updatetime;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String getEmployCode() {
        return employCode;
    }

    public void setEmployCode(String employCode) {
        this.employCode = employCode == null ? null : employCode.trim();
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }


    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
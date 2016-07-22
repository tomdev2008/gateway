package com.yoho.yhorder.dal.model;

/**
 * Created by JXWU on 2016/1/27.
 */
public class UserBlacklist {
    //uid
    private int uid;
    //0 正常
    private int status;

    //ip地址
    private long ip;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getIp() {
        return ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }
}

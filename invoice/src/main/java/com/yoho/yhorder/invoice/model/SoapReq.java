package com.yoho.yhorder.invoice.model;

/**
 * Created by chenchao on 2016/6/7.
 */
public class SoapReq {

    private String appId;
    private String interfaceCode;
    private String content;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getInterfaceCode() {
        return interfaceCode;
    }

    public void setInterfaceCode(String interfaceCode) {
        this.interfaceCode = interfaceCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private boolean nullFlag = false;

    public boolean beNull(){
        return nullFlag;
    }

    public SoapReq newNull() {
        nullFlag = true;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SoapReq{");
        sb.append("appId='").append(appId).append('\'');
        sb.append(", interfaceCode='").append(interfaceCode).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <globalInfo>
 <appId>DZFPQZ</appId>
 <interfaceId/>
 <interfaceCode/>
 <requestCode>DZFPQZ</requestCode>
 <requestTime>2016-06-17 17:08:02:629</requestTime>
 <responseCode>1</responseCode>
 <dataExchangeId>DZFPQZDFXJ10012016-06-17818800454</dataExchangeId>
 </globalInfo>
 * Created by chenchao on 2016/6/17.
 */
@XStreamAlias("globalInfo")
public class GlobalInfo {
    //<appId>DZFPQZ</appId>
    private String appId;

    //<interfaceId/>
    private String interfaceId;

    //<interfaceCode/>
    private String interfaceCode;

    //<requestCode>DZFPQZ</requestCode>
    private String requestCode;

    //<requestTime>2016-06-17 17:08:02:629</requestTime>
    private String requestTime;

    //<responseCode>1</responseCode>
    private int responseCode;

    //<dataExchangeId>DZFPQZDFXJ10012016-06-17818800454</dataExchangeId>
    private String dataExchangeId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getInterfaceCode() {
        return interfaceCode;
    }

    public void setInterfaceCode(String interfaceCode) {
        this.interfaceCode = interfaceCode;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getDataExchangeId() {
        return dataExchangeId;
    }

    public void setDataExchangeId(String dataExchangeId) {
        this.dataExchangeId = dataExchangeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("appId", appId)
                .append("interfaceId", interfaceId)
                .append("interfaceCode", interfaceCode)
                .append("requestCode", requestCode)
                .append("requestTime", requestTime)
                .append("responseCode", responseCode)
                .append("dataExchangeId", dataExchangeId)
                .toString();
    }
}

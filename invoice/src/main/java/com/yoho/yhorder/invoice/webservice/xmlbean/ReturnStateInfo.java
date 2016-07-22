package com.yoho.yhorder.invoice.webservice.xmlbean;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by chenchao on 2016/6/17.
 */
public class ReturnStateInfo {

    //<returnCode>0000</returnCode>
    private String returnCode;

    //<returnMessage>成功</returnMessage>
    private String returnMessage;


    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("returnCode", returnCode)
                .append("returnMessage", returnMessage)
                .toString();
    }
}

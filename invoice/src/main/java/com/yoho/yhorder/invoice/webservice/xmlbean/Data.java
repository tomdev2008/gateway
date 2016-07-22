package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by chenchao on 2016/6/17.
 */
@XStreamAlias("Data")
public class Data {

    private DataDescription dataDescription;

    //<content>
    private String content;

    public DataDescription getDataDescription() {
        return dataDescription;
    }

    public void setDataDescription(DataDescription dataDescription) {
        this.dataDescription = dataDescription;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("dataDescription", dataDescription)
                .append("content", content)
                .toString();
    }

    public static class DataDescription{

        //<zipCode>0</zipCode>
        private int zipCode;
        //<encryptCode>0</encryptCode>
        private int encryptCode;
        //<codeType/>

        private String codeType;

        public int getZipCode() {
            return zipCode;
        }

        public void setZipCode(int zipCode) {
            this.zipCode = zipCode;
        }

        public int getEncryptCode() {
            return encryptCode;
        }

        public void setEncryptCode(int encryptCode) {
            this.encryptCode = encryptCode;
        }

        public String getCodeType() {
            return codeType;
        }

        public void setCodeType(String codeType) {
            this.codeType = codeType;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("zipCode", zipCode)
                    .append("encryptCode", encryptCode)
                    .append("codeType", codeType)
                    .toString();
        }
    }
}

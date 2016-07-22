package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * sb.append("<interface xmlns=\"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:schemaLocation=\"http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd\" version=\""
 + "DZFPQZ0.2" + "\">");
 * Created by chenchao on 2016/6/7.
 */
@XStreamAlias("interface")
@XStreamInclude({InterfaceReq.GlobalInfo.class, InterfaceReq.ReturnStateInfo.class, InterfaceReq.Data.class})
public class InterfaceReq {
    @XStreamAsAttribute
    @XStreamAlias("xmlns")
    protected String xmlns="";

    @XStreamAsAttribute
    @XStreamAlias("xmlns:xsi")
    protected String xsi = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * xmlns:schemaLocation="http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd"
     */
    @XStreamAsAttribute
    @XStreamAlias("xmlns:schemaLocation")
    protected String schemaLocation = "http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd";

    @XStreamAsAttribute
    @XStreamAlias("version")
    protected String version = "DZFPQZ0.2";

    /**
     * <globalInfo>
     */
    @XStreamAlias("globalInfo")
    private GlobalInfo  globalInfo;

    /**
     *
     */
    @XStreamAlias("returnStateInfo")
    private ReturnStateInfo  returnStateInfo;

    /**
     *
     */
    @XStreamAlias("Data")
    private Data  data;


    public GlobalInfo getGlobalInfo() {
        return globalInfo;
    }

    public void setGlobalInfo(GlobalInfo globalInfo) {
        this.globalInfo = globalInfo;
    }

    public ReturnStateInfo getReturnStateInfo() {
        return returnStateInfo;
    }

    public void setReturnStateInfo(ReturnStateInfo returnStateInfo) {
        this.returnStateInfo = returnStateInfo;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    public InterfaceReq(){
        this.globalInfo = new GlobalInfo();
        this.data = new Data();
        this.returnStateInfo = new ReturnStateInfo();
    }

    @XStreamAlias("globalInfo")
    public static class GlobalInfo{
        /**
         *sb.append("<appId>bea5aeb4bc2e174aeeee22bea7ed65b7702156a39f4b9cd6ef040e669ce56ebb</appId>");
         */
        @XStreamAlias("appId")
        private String  appId;


        /**
         * sb.append("<interfaceId></interfaceId>");
         */
        @XStreamAlias("interfaceId")
        private String  interfaceId;

        /**
         * sb.append("<interfaceCode>" + interfaceCode + "</interfaceCode>");
         */
        @XStreamAlias("interfaceCode")
        private String interfaceCode;

        /**
         * sb.append("<requestCode>DZFPQZ</requestCode>");
         */
        @XStreamAlias("requestCode")
        private String  requestCode;

        /**
         * sb.append("<requestTime>" + formatToTime() + "</requestTime>");
         */
        @XStreamAlias("requestTime")
        private String  requestTime;


        /**
         *sb.append("<responseCode>Ds</responseCode>");
         */
        @XStreamAlias("responseCode")
        private String  responseCode;


        /**
         *sb.append("<dataExchangeId>" + "DZFPQZ" + interfaceCode + formatToDay() + randNineData() + "</dataExchangeId>");
         */
        @XStreamAlias("dataExchangeId")
        private String  dataExchangeId;

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

        public String getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(String responseCode) {
            this.responseCode = responseCode;
        }

        public String getDataExchangeId() {
            return dataExchangeId;
        }

        public void setDataExchangeId(String dataExchangeId) {
            this.dataExchangeId = dataExchangeId;
        }

        public GlobalInfo() {
            this.appId = "";
            this.interfaceId = "";
            this.interfaceCode = "";
            this.requestCode = "";
            this.requestTime = "";
            this.responseCode = "";
            this.dataExchangeId = "";
        }
    }

    @XStreamAlias("returnStateInfo")
    public static class ReturnStateInfo{

        /**
         *sb.append("<returnCode></returnCode>");
         */
        @XStreamAlias("returnCode")
        private String  returnCode;

        /**
         *sb.append("<returnMessage></returnMessage>");
         */
        @XStreamAlias("returnMessage")
        private String  returnMessage;


        /**
         *sb.append("</returnStateInfo>");
         */
        @XStreamAlias("returnStateInfo")
        private String  returnStateInfo;

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

        public String getReturnStateInfo() {
            return returnStateInfo;
        }

        public void setReturnStateInfo(String returnStateInfo) {
            this.returnStateInfo = returnStateInfo;
        }

        public ReturnStateInfo() {
            this.returnCode = "";
            this.returnMessage = "";
            this.returnStateInfo = "";
        }
    }

    @XStreamAlias("Data")
    @XStreamInclude({DataDescription.class})
    public static class Data{

        /**
         *
         */
        @XStreamAlias("dataDescription")
        private DataDescription dataDescription;

        /**
         * <content>
         */
        @XStreamAlias("content")
        private String content = "0";

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

        public Data() {
            this.dataDescription = new DataDescription();
            this.content = "";
        }

    }

    @XStreamAlias("dataDescription")
    public static class DataDescription{
        @XStreamAlias("zipCode")
        protected String zipCode = "0";

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

    }


    public static class InterfaceReqBuilder{
        private InterfaceReq req;

        public InterfaceReqBuilder(){
            req = new InterfaceReq();
        }

        public InterfaceReqBuilder buildGlobalInfo(GlobalInfo globalInfo){
            req.globalInfo = globalInfo;
            return this;
        }

        public InterfaceReqBuilder buildReturnStateInfo(ReturnStateInfo returnStateInfo){
            req.returnStateInfo = returnStateInfo;
            return this;
        }

        public InterfaceReqBuilder buildData(Data data){
            req.data = data;
            return this;
        }

        public InterfaceReq build(){
            return req;
        }

    }
}

package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <?xml version="1.0" encoding="utf-8"?>

 <interface xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:schemaLocation="http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd" version="DZFPQZ1.0">
 <globalInfo>
 <appId>DZFPQZ</appId>
 <interfaceId/>
 <interfaceCode/>
 <requestCode>DZFPQZ</requestCode>
 <requestTime>2016-06-17 17:08:02:629</requestTime>
 <responseCode>1</responseCode>
 <dataExchangeId>DZFPQZDFXJ10012016-06-17818800454</dataExchangeId>
 </globalInfo>
 <returnStateInfo>
 <returnCode>0000</returnCode>
 <returnMessage>成功</returnMessage>
 </returnStateInfo>
 <Data>
 <dataDescription>
 <zipCode>0</zipCode>
 <encryptCode>0</encryptCode>
 <codeType/>
 </dataDescription>
 <content>PFJFU1BPTlNFPjxGUFFRTFNIPjIxMTUxMzM3NDU2NDQ0NjUzPC9GUFFRTFNIPjxGUF9ETT4wNTAw MDM1MjMzMzM8L0ZQX0RNPjxGUF9ITT42NzA2MjQ4NDwvRlBfSE0+PEpZTT4wOTAwNzI2MTE1OTk4 Mjc1NDE0MzwvSllNPjxLUFJRPjIwMTYwNjE3MTcxNjU2PC9LUFJRPjxQREZfVVJMPjwhW0NEQVRB W2h0dHA6Ly8yMDIuMTA0LjExMy4yNjo4MTAxL2R6ZnAtcGxhdGZvcm0vZG93bmxvYWRBY3Rpb24u ZG8/bWV0aG9kPWRvd25sb2FkJnJlcXVlc3Q9Q0huSzM5SDg3eVlycG95YSpDT2h4VUtEaElTSmRM Z3RsOTR6dThuVWxkbHhkZlZzRC1rOEQwS1N1Yks2Ylowd3BXYWNLR0gtMFg4XyU1RWJCZ0RkYkJi aGhdXT48L1BERl9VUkw+PC9SRVNQT05TRT4=</content>
 </Data>
 </interface>
 * Created by chenchao on 2016/6/17.
 */

public class CommonResp {

    //<?xml version="1.0" encoding="utf-8"?>

    //<interface xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:schemaLocation="http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd" version="DZFPQZ1.0">
    @XStreamAlias("interface")
    private RespInterface respInterface;


    public RespInterface getRespInterface() {
        return respInterface;
    }

    public void setRespInterface(RespInterface respInterface) {
        this.respInterface = respInterface;
    }


    @XStreamAlias("interface")
    public static class RespInterface{
        @XStreamAsAttribute
        private String xmlns;

        @XStreamAsAttribute
        private String version;

        @XStreamAsAttribute
        @XStreamAlias("xmlns:xsi")
        private String xsi;

        @XStreamAlias("globalInfo")
        private GlobalInfo globalInfo;

        @XStreamAlias("returnStateInfo")
        private ReturnStateInfo returnStateInfo;

        @XStreamAlias("Data")
        private Data data;

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

        public String getXmlns() {
            return xmlns;
        }

        public void setXmlns(String xmlns) {
            this.xmlns = xmlns;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getXsi() {
            return xsi;
        }

        public void setXsi(String xsi) {
            this.xsi = xsi;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("xmlns", xmlns)
                    .append("version", version)
                    .append("xsi", xsi)
                    .append("globalInfo", globalInfo)
                    .append("returnStateInfo", returnStateInfo)
                    .append("data", data)
                    .toString();
        }
    }
}

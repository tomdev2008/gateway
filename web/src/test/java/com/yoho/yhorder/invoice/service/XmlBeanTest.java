package com.yoho.yhorder.invoice.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.yoho.yhorder.invoice.webservice.xmlbean.CommonResp;
import com.yoho.yhorder.invoice.webservice.xmlbean.FpkjResp;
import sun.misc.BASE64Decoder;

import java.io.IOException;

/**
 * Created by chenchao on 2016/6/17.
 */
public class XmlBeanTest {
    public static void main(String[] args) {
        String xml = "<?xml version='1.0' encoding='UTF-8'?><interface xmlns=\"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:schemaLocation=\"http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd\" version=\"DZFPQZ1.0\"> <globalInfo><appId>DZFPQZ</appId><interfaceId></interfaceId><interfaceCode></interfaceCode><requestCode>DZFPQZ</requestCode><requestTime>2016-06-17 17:08:02:629</requestTime><responseCode>1</responseCode><dataExchangeId>DZFPQZDFXJ10012016-06-17818800454</dataExchangeId></globalInfo><returnStateInfo><returnCode>0000</returnCode><returnMessage>成功</returnMessage></returnStateInfo><Data><dataDescription><zipCode>0</zipCode><encryptCode>0</encryptCode><codeType /></dataDescription><content>PFJFU1BPTlNFPjxGUFFRTFNIPjIxMTUxMzM3NDU2NDQ0NjUzPC9GUFFRTFNIPjxGUF9ETT4wNTAw\n" +
                "MDM1MjMzMzM8L0ZQX0RNPjxGUF9ITT42NzA2MjQ4NDwvRlBfSE0+PEpZTT4wOTAwNzI2MTE1OTk4\n" +
                "Mjc1NDE0MzwvSllNPjxLUFJRPjIwMTYwNjE3MTcxNjU2PC9LUFJRPjxQREZfVVJMPjwhW0NEQVRB\n" +
                "W2h0dHA6Ly8yMDIuMTA0LjExMy4yNjo4MTAxL2R6ZnAtcGxhdGZvcm0vZG93bmxvYWRBY3Rpb24u\n" +
                "ZG8/bWV0aG9kPWRvd25sb2FkJnJlcXVlc3Q9Q0huSzM5SDg3eVlycG95YSpDT2h4VUtEaElTSmRM\n" +
                "Z3RsOTR6dThuVWxkbHhkZlZzRC1rOEQwS1N1Yks2Ylowd3BXYWNLR0gtMFg4XyU1RWJCZ0RkYkJi\n" +
                "aGhdXT48L1BERl9VUkw+PC9SRVNQT05TRT4=</content></Data></interface>";


        XStream xstream = new XStream(new XppDriver(new NoNameCoder()));
        xstream.autodetectAnnotations(true);

        xstream.alias("interface", CommonResp.RespInterface.class);
        CommonResp.RespInterface resp = (CommonResp.RespInterface) xstream.fromXML(xml);

        System.out.println(resp);

        String content = resp.getData().getContent();
        try {
            byte[] content_hr = new BASE64Decoder().decodeBuffer(content);
            String contentXml = new String(content_hr);
            xstream.alias("RESPONSE", FpkjResp.class);
            FpkjResp fpkjResp = (FpkjResp) xstream.fromXML(contentXml);
            System.out.println(contentXml);

            System.out.println(fpkjResp);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

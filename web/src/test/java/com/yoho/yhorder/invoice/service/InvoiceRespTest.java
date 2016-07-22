package com.yoho.yhorder.invoice.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.yoho.yhorder.invoice.webservice.constant.InterfaceCode;
import com.yoho.yhorder.invoice.webservice.constant.InvoiceSoapErrorCode;
import com.yoho.yhorder.invoice.webservice.manager.XStreamManager;
import com.yoho.yhorder.invoice.webservice.xmlbean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

/**
 * Created by chenchao on 2016/7/8.
 */
public class InvoiceRespTest {
    private static Logger logger = LoggerFactory.getLogger(InvoiceRespTest.class);
    public static void main(String[] args) {
        String resp = "<?xml version='1.0' encoding='UTF-8'?><interface xmlns=\"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:schemaLocation=\"http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd\" version=\"DZFPQZ1.0\"> <globalInfo><appId>DZFPQZ</appId><interfaceId></interfaceId><interfaceCode></interfaceCode><requestCode>DZFPQZ</requestCode><requestTime>2016-07-08 11:29:43:556</requestTime><responseCode>1</responseCode><dataExchangeId>DZFPQZDFXJ10012016-07-08524739123</dataExchangeId></globalInfo><returnStateInfo><returnCode>0000</returnCode><returnMessage>成功\n" +
                "</returnMessage></returnStateInfo><Data><dataDescription><zipCode>0</zipCode><encryptCode>0</encryptCode><codeType /></dataDescription><content>PFJFU1BPTlNFPjxGUFFRTFNIPnJfMV9nMTI0Njk4NzU8L0ZQUVFMU0g+PEZQX0RNPjA1MDAwMzUy\n" +
                "MzMzMzwvRlBfRE0+PEZQX0hNPjY3MzYyODk2PC9GUF9ITT48SllNPjA3OTc1OTgxNTAzODU3NzIx\n" +
                "NTU0PC9KWU0+PEtQUlE+MjAxNjA3MDgxMTMyMTg8L0tQUlE+PFBERl9VUkw+PCFbQ0RBVEFbaHR0\n" +
                "cDovLzIwMi4xMDQuMTEzLjI2OjgxMDEvZHpmcC1wbGF0Zm9ybS9kb3dubG9hZEFjdGlvbi5kbz9t\n" +
                "ZXRob2Q9ZG93bmxvYWQmcmVxdWVzdD1DSG5LMzlIODd5WXJwb3lhKkNPaHhjMXgzdDViaVZwZVN4\n" +
                "d1AwV2lxcjdFVG9FZjg3bGJrVDdteFdKdHhhZmswNUw4RmpSUU0xN1FfJTVFY2lHaWlkSmdCZ11d\n" +
                "PjwvUERGX1VSTD48L1JFU1BPTlNFPg==</content></Data></interface>";

        buildFpkjResp(resp);
    }

    private static FpkjResp buildFpkjResp(String respStr){
        FpkjResp fpkjResp = null;
        ReturnStateInfo returnStateInfo;
        try {
            XStream xstream = new XStream(new XppDriver(new NoNameCoder()));
            xstream.autodetectAnnotations(true);

            logger.debug("get invoice resp from http {}", respStr);
            xstream.alias("interface", CommonResp.RespInterface.class);
            CommonResp.RespInterface resp = (CommonResp.RespInterface) xstream.fromXML(respStr);
            logger.debug("xml 2 javabean resp is {}", resp);
            returnStateInfo = resp.getReturnStateInfo();
            if (returnStateInfo != null){
                if(InvoiceSoapErrorCode.SUCCESS_KP.equalsIgnoreCase(returnStateInfo.getReturnCode())){
                    String content = resp.getData().getContent();
                    byte[] content_hr = new BASE64Decoder().decodeBuffer(content);
                    String contentXml = new String(content_hr);
                    logger.debug("contentXml is {}", contentXml);
                    xstream.alias("RESPONSE", FpkjResp.class);
                    fpkjResp = (FpkjResp) xstream.fromXML(contentXml);
                    fpkjResp.setIssueSuccess(true);
                    logger.debug("fpkjResp is {}", fpkjResp);
                }else{
                    fpkjResp = new FpkjResp();
                }
                fpkjResp.setReturnStateInfo(returnStateInfo);
                logger.info("buildFpkjResp without exception");
            }
        }catch (Exception e){
            returnStateInfo = new ReturnStateInfo();
            returnStateInfo.setReturnCode(InvoiceSoapErrorCode.FAIL_KP);
            returnStateInfo.setReturnMessage(e.getMessage());
            fpkjResp = new FpkjResp();
            fpkjResp.setReturnStateInfo(returnStateInfo);
            logger.error("issue invoice by https connect to baiwang(vendor) occurs error {}", e);
        }finally {
            logger.info("in buildFpkjResp fpkjResp {}", fpkjResp);
            return fpkjResp;
        }
    }
}

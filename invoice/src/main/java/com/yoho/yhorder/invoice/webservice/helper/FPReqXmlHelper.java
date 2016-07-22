package com.yoho.yhorder.invoice.webservice.helper;


import com.yoho.yhorder.invoice.model.SoapReq;
import com.yoho.yhorder.invoice.webservice.manager.XStreamManager;
import com.yoho.yhorder.invoice.webservice.xmlbean.InterfaceReq;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenchao on 2016/5/31.
 */
public final class FPReqXmlHelper {

    public static String getCommonXml(SoapReq req) {
        if (req.beNull()){
            return "";
        }
        String appId = req.getAppId();
        String interfaceCode = req.getInterfaceCode();
        String content = req.getContent();

        StringBuffer sb = new StringBuffer("");
        sb.append("<?xml version='1.0' encoding='UTF-8' ?>");
        InterfaceReq.GlobalInfo globalInfo = new InterfaceReq.GlobalInfo();
        globalInfo.setAppId(appId);
        globalInfo.setInterfaceCode(interfaceCode);
        globalInfo.setRequestTime(formatToTime());
        globalInfo.setDataExchangeId(new StringBuilder("DZFPQZ")
                .append(interfaceCode)
                .append(formatToDay())
                .append(randNineData())
                .toString());
        //ReturnStateInfo
        InterfaceReq.ReturnStateInfo returnStateInfo = new InterfaceReq.ReturnStateInfo();

        //data
        InterfaceReq.Data data = new InterfaceReq.Data();
        data.setContent(content);
        /**/
        String xmlStr = XStreamManager.getXstream().toXML(new InterfaceReq.InterfaceReqBuilder()
                .buildGlobalInfo(globalInfo)
                .buildReturnStateInfo(returnStateInfo)
                .buildData(data)
                .build());
        sb.append(xmlStr);
        /*
        sb.append("<interface xmlns=\"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:schemaLocation=\"http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd\" version=\""
                + "DZFPQZ0.2" + "\">");
        sb.append("<globalInfo>");
        sb.append("<appId>bea5aeb4bc2e174aeeee22bea7ed65b7702156a39f4b9cd6ef040e669ce56ebb</appId>");
        sb.append("<interfaceId></interfaceId>");
        sb.append("<interfaceCode>" + interfaceCode + "</interfaceCode>");
        sb.append("<requestCode>DZFPQZ</requestCode>");
        sb.append("<requestTime>" + formatToTime() + "</requestTime>");
        sb.append("<responseCode>Ds</responseCode>");
        sb.append("<dataExchangeId>" + "DZFPQZ" + interfaceCode + formatToDay() + randNineData() + "</dataExchangeId>");
        sb.append("</globalInfo>");
        sb.append("<returnStateInfo>");
        sb.append("<returnCode></returnCode>");
        sb.append("<returnMessage></returnMessage>");
        sb.append("</returnStateInfo>");
        sb.append("<Data>");
        sb.append("<dataDescription>");
        sb.append("<zipCode>0</zipCode>");
        sb.append("</dataDescription>");
        sb.append("<content>");
        sb.append(content);
        sb.append("</content>");
        sb.append("</Data>");
        sb.append("</interface>");
        */
        return sb.toString();
    }

    /************************************************************************
     * 格式化时间-时间
     */
    public static String formatToTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format((new Date()));
    }

    /************************************************************************
     * 格式化时间-日
     */
    public static String formatToDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format((new Date()));
    }

    /************************************************************************
     * 9位随机整数
     */
    public static String randNineData() {
        return String.valueOf((int) (Math.random() * 900000000 + 100000000));
    }
}

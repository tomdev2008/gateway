package com.yoho.yhorder.invoice.webservice;

import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.yhorder.BaseTest;
import com.yoho.yhorder.invoice.webservice.constant.InterfaceCode;
import com.yoho.yhorder.invoice.webservice.impl.CXFClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by chenchao on 2016/5/25.
 */
public class FPWebserviceTest extends BaseTest{
    @Autowired
    private SoapClient client;
    /*
    private static String appid = "bea5aeb4bc2e174aeeee22bea7ed65b7702156a39f4b9cd6ef040e669ce56ebb";
    @Autowired
    JaxWsProxyFactoryBean factoryBean;

    @Test
    public void test(){
        DZFPServicePortType  servicePort = (DZFPServicePortType)factoryBean.create();
        System.out.println("getWsdlURL is : " +factoryBean.getAddress());
        String content = "<REQUEST_COMMON_FPKJ class='REQUEST_COMMON_FPKJ'><FPQQLSH>201503014123422252</FPQQLSH><KPLX>0</KPLX><XSF_NSRSBH>440300568519737</XSF_NSRSBH><XSF_MC>电子发票测试</XSF_MC><XSF_DZDH>山东省青岛市</XSF_DZDH><XSF_YHZH>92523123213412341234</XSF_YHZH><GMF_NSRSBH>440300568519737</GMF_NSRSBH><GMF_MC>张三</GMF_MC><GMF_DZDH>浙江省杭州市余杭区文一西路xxx号18234561212</GMF_DZDH><GMF_YHZH>123412341234</GMF_YHZH><GMF_SJH>18234561212</GMF_SJH><GMF_DZYX>mytest@xxx.com</GMF_DZYX><FPT_ZH></FPT_ZH><KPR>小张</KPR><SKR></SKR><FHR>小林</FHR><YFP_DM>111100000000</YFP_DM><YFP_HM>00004349</YFP_HM><JSHJ>1170.00</JSHJ><HJJE>1000.00</HJJE><HJSE>170.00</HJSE><BZ>电子发票测试</BZ><HYLX>1</HYLX><BY1></BY1><BY2></BY2><BY3></BY3><BY4></BY4><BY5></BY5><BY6></BY6><BY7></BY7><BY8></BY8><BY9></BY9><BY10></BY10><COMMON_FPKJ_XMXXS class='COMMON_FPKJ_XMXX' size='1'><COMMON_FPKJ_XMXX><FPHXZ>0</FPHXZ><XMMC>电视机</XMMC><GGXH>X100</GGXH><DW>台</DW><XMSL>10</XMSL><XMDJ>100.00</XMDJ><XMJE>1000.00</XMJE><SL>0.17</SL><SE>170.00</SE><BY1></BY1><BY2></BY2><BY3></BY3><BY4></BY4><BY5></BY5><BY6></BY6></COMMON_FPKJ_XMXX></COMMON_FPKJ_XMXXS></REQUEST_COMMON_FPKJ>";
        String xml = null;
        try {
            SoapReq req = new SoapReq();
            req.setAppId(appid);
            req.setInterfaceCode("DFXJ1004");
            req.setContent(new BASE64Encoder().encodeBuffer(content.getBytes("UTF-8")));
            xml = FPReqXmlHelper.getCommonXml(req);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = servicePort.doService(xml);
        System.out.println("result is "+ result);
    }
    */

    @Test
    public void testCxfClient(){
        String interfaceCode = InterfaceCode.QUERY;
        String content = "<REQUEST_COMMON_FPCX class='REQUEST_COMMON_FPCX'><FPQQLSH>21151337456444654</FPQQLSH><XSF_NSRSBH>440300568519737</XSF_NSRSBH></REQUEST_COMMON_FPCX>";

        try {
            String result = client.httpsRequest(interfaceCode, content);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFpkj(){
        String content = "<REQUEST_COMMON_FPKJ class=\"REQUEST_COMMON_FPKJ\">\n" +
                "  <FPQQLSH>b_1_testwithjingdong</FPQQLSH>\n" +
                "  <KPLX>0</KPLX>\n" +
                "  <XSF_NSRSBH>440300568519737</XSF_NSRSBH>\n" +
                "  <XSF_MC>电子发票测试</XSF_MC>\n" +
                "  <XSF_DZDH>南京市建邺区嘉陵江东街18号05幢17、18层 025-87781000</XSF_DZDH>\n" +
                "  <XSF_YHZH></XSF_YHZH>\n" +
                "  <GMF_NSRSBH></GMF_NSRSBH>\n" +
                "  <GMF_MC>个人</GMF_MC>\n" +
                "  <GMF_DZDH></GMF_DZDH>\n" +
                "  <GMF_YHZH></GMF_YHZH>\n" +
                "  <GMF_SJH>18061464565</GMF_SJH>\n" +
                "  <GMF_DZYX></GMF_DZYX>\n" +
                "  <FPT_ZH></FPT_ZH>\n" +
                "  <WX_OPENID></WX_OPENID>\n" +
                "  <KPR>有货</KPR>\n" +
                "  <SKR></SKR>\n" +
                "  <FHR></FHR>\n" +
                "  <YFP_DM></YFP_DM>\n" +
                "  <YFP_HM></YFP_HM>\n" +
                "  <JSHJ>67.70</JSHJ>\n" +
                "  <HJJE>57.87</HJJE>\n" +
                "  <HJSE>9.83</HJSE>\n" +
                "  <BZ></BZ>\n" +
                "  <HYLX>0</HYLX>\n" +
                "  <BY1></BY1>\n" +
                "  <BY2></BY2>\n" +
                "  <BY3></BY3>\n" +
                "  <BY4></BY4>\n" +
                "  <BY5></BY5>\n" +
                "  <BY6></BY6>\n" +
                "  <BY7></BY7>\n" +
                "  <BY8></BY8>\n" +
                "  <BY9></BY9>\n" +
                "  <BY10></BY10>\n" +
                "  <COMMON_FPKJ_XMXXS class=\"COMMON_FPKJ_XMXX\" size=\"3\">\n" +
                "<COMMON_FPKJ_XMXX>" +
                "<FPHXZ>2</FPHXZ>" +
                "<GGXH></GGXH>" +
                "<DW></DW>" +
                "<XMMC>旺仔</XMMC>" +
                "<XMSL>1</XMSL>" +
                "<XMDJ>51.20</XMDJ>" +
                "<XMJE>51.20</XMJE>" +
                "<SL>0.17</SL>" +
                "<SE>8.70</SE>" +
                "<BY1></BY1>" +
                "<BY2></BY2>" +
                "<BY3></BY3>" +
                "<BY4></BY4>" +
                "<BY5></BY5>" +
                "</COMMON_FPKJ_XMXX>" +

                "<COMMON_FPKJ_XMXX>" +
                "<FPHXZ>2</FPHXZ>" +
                "<GGXH></GGXH>" +
                "<DW></DW>" +
                "<XMMC>好丽友</XMMC>" +
                "<XMSL>1</XMSL>" +
                "<XMDJ>18.72</XMDJ>" +
                "<XMJE>18.72</XMJE>" +
                "<SL>0.17</SL>" +
                "<SE>3.18</SE>" +
                "<BY1></BY1>" +
                "<BY2></BY2>" +
                "<BY3></BY3>" +
                "<BY4></BY4>" +
                "<BY5></BY5>" +
                "</COMMON_FPKJ_XMXX>" +

                "<COMMON_FPKJ_XMXX>" +
                "<FPHXZ>1</FPHXZ>" +
                "<GGXH></GGXH>" +
                "<DW></DW>" +
                "<XMMC>折扣行数2(17.237%)</XMMC>" +
                "<XMJE>-12.05</XMJE>" +
                "<SL>0.17</SL>" +
                "<SE>-2.05</SE>" +
                "<BY1></BY1>" +
                "<BY2></BY2>" +
                "<BY3></BY3>" +
                "<BY4></BY4>" +
                "<BY5></BY5>" +
                "</COMMON_FPKJ_XMXX>" +

                "  </COMMON_FPKJ_XMXXS>" +
                "</REQUEST_COMMON_FPKJ>";

        try {
            String interfaceCode = InterfaceCode.ISSUE;
            String result = client.httpsRequest(interfaceCode, content);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}

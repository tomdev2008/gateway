package com.yoho.yhorder.invoice.webservice.impl;

import com.yoho.yhorder.invoice.webservice.constant.InterfaceCode;

/**
 * Created by chenchao on 2016/6/18.
 */
public class Test {
    public static void main(String[] args) {
        String content = "<REQUEST_COMMON_FPKJ class=\"REQUEST_COMMON_FPKJ\"> \n" +
                "<FPQQLSH>b_1_h999999</FPQQLSH>\n" +
                "  <KPLX>0</KPLX>\n" +
                "  <XSF_NSRSBH>440300568519737</XSF_NSRSBH>\n" +
                "  <XSF_MC>电子发票测试</XSF_MC>\n" +
                "  <XSF_DZDH>南京市建邺区嘉陵江东街18号05幢17、18层 025-87781000</XSF_DZDH>\n" +
                "  <XSF_YHZH></XSF_YHZH>\n" +
                "  <GMF_NSRSBH></GMF_NSRSBH>\n" +
                "  <GMF_MC>西门子(SIEMENS) BCD-322W(KG33NA2L0C)</GMF_MC>\n" +
                "  <GMF_DZDH></GMF_DZDH>\n" +
                "  <GMF_YHZH></GMF_YHZH>\n" +
                "  <GMF_SJH>18234561212</GMF_SJH>\n" +
                "  <GMF_DZYX></GMF_DZYX>\n" +
                "  <FPT_ZH></FPT_ZH>\n" +
                "  <WX_OPENID></WX_OPENID>\n" +
                "  <KPR>有货</KPR>\n" +
                "  <SKR></SKR>\n" +
                "  <FHR></FHR>\n" +
                "  <YFP_DM></YFP_DM>\n" +
                "  <YFP_HM></YFP_HM>\n" +
                "  <JSHJ>888.89</JSHJ>\n" +
                "  <HJJE>759.73</HJJE>\n" +
                "  <HJSE>129.16</HJSE>\n" +
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
                "      <COMMON_FPKJ_XMXX>" +
                "        <FPHXZ>0</FPHXZ>" +
                "        <XMMC>a</XMMC>\n" +
                "        <GGXH></GGXH>\n" +
                "        <DW></DW>\n" +
                "        <XMSL>3</XMSL>\n" +
                "        <XMDJ>85.47</XMDJ>\n" +
                "        <XMJE>256.41</XMJE>\n" +
                "        <SL>0.17</SL>\n" +
                "        <SE>43.59</SE>\n" +
                "<BY1/>\n" +
                "      <BY2/>\n" +
                "      <BY3/>\n" +
                "      <BY4/>\n" +
                "      <BY5/>\n" +
                "      <BY6/>\n" +
                "      </COMMON_FPKJ_XMXX>\n" +
                "      <COMMON_FPKJ_XMXX>\n" +
                "        <FPHXZ>0</FPHXZ>\n" +
                "        <XMMC>b</XMMC>\n" +
                "        <GGXH></GGXH>\n" +
                "        <DW></DW>\n" +
                "        <XMSL>4</XMSL>\n" +
                "        <XMDJ>170.94</XMDJ>\n" +
                "        <XMJE>683.76</XMJE>\n" +
                "        <SL>0.17</SL>\n" +
                "        <SE>116.24</SE>\n" +
                "\t\t<BY1/>\n" +
                "      <BY2/>\n" +
                "      <BY3/>\n" +
                "      <BY4/>\n" +
                "      <BY5/>\n" +
                "      <BY6/>\n" +
                "      </COMMON_FPKJ_XMXX>\n" +
                "      <COMMON_FPKJ_XMXX>\n" +
                "        <FPHXZ>1</FPHXZ>\n" +
                "        <XMMC>折扣行数2(19.192%)</XMMC>\n" +
                "        <GGXH></GGXH>\n" +
                "        <DW></DW>\n" +
                "        <XMSL>0</XMSL>\n" +
                "        <XMDJ>0.0</XMDJ>\n" +
                "        <XMJE>-180.43</XMJE>\n" +
                "        <SL>0.17</SL>\n" +
                "        <SE>-30.67</SE>\n" +
                "\t\t<BY1/>\n" +
                "      <BY2/>\n" +
                "      <BY3/>\n" +
                "      <BY4/>\n" +
                "      <BY5/>\n" +
                "      <BY6/>\n" +
                "      </COMMON_FPKJ_XMXX>\n" +
                "  </COMMON_FPKJ_XMXXS>\n" +
                "</REQUEST_COMMON_FPKJ>";

        try {
            String interfaceCode = InterfaceCode.ISSUE;
            String result = new CXFClient().httpsRequest(interfaceCode, content);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

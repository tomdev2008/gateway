package com.yoho.yhorder.invoice.webservice.impl;

import com.yoho.yhorder.invoice.webservice.ClientService;
import com.yoho.yhorder.invoice.webservice.DZFPService;
import com.yoho.yhorder.invoice.webservice.DZFPServicePortType;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import sun.misc.BASE64Encoder;

import javax.net.ssl.*;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenchao on 2016/5/26.
 */
public class ClientServiceImpl implements ClientService{
    private static final QName SERVICE_NAME = new QName("http://dsqzjk.dzfpqz", "DZFPService");

    static {
        disableSslVerification();
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public String fpkj(String wsdlUrl){
        URL wsdlURL = DZFPService.WSDL_LOCATION;
        if (StringUtils.isNotBlank(wsdlUrl)) {
            try {
                wsdlURL = new URL(wsdlUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        DZFPService ss = new DZFPService(wsdlURL, SERVICE_NAME);
        DZFPServicePortType port = ss.getDZFPServiceHttpPort();
        Client client = org.apache.cxf.frontend.ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();

        httpClientPolicy.setConnectionTimeout(36000);
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setReceiveTimeout(32000);

        conduit.setClient(httpClientPolicy);

        String content = "<REQUEST_COMMON_FPKJ class='REQUEST_COMMON_FPKJ'><FPQQLSH>201503014123422252</FPQQLSH><KPLX>0</KPLX><XSF_NSRSBH>440300568519737</XSF_NSRSBH><XSF_MC>电子发票测试</XSF_MC><XSF_DZDH>山东省青岛市</XSF_DZDH><XSF_YHZH>92523123213412341234</XSF_YHZH><GMF_NSRSBH>440300568519737</GMF_NSRSBH><GMF_MC>张三</GMF_MC><GMF_DZDH>浙江省杭州市余杭区文一西路xxx号18234561212</GMF_DZDH><GMF_YHZH>123412341234</GMF_YHZH><GMF_SJH>18234561212</GMF_SJH><GMF_DZYX>mytest@xxx.com</GMF_DZYX><FPT_ZH></FPT_ZH><KPR>小张</KPR><SKR></SKR><FHR>小林</FHR><YFP_DM>111100000000</YFP_DM><YFP_HM>00004349</YFP_HM><JSHJ>1170.00</JSHJ><HJJE>1000.00</HJJE><HJSE>170.00</HJSE><BZ>电子发票测试</BZ><HYLX>1</HYLX><BY1></BY1><BY2></BY2><BY3></BY3><BY4></BY4><BY5></BY5><BY6></BY6><BY7></BY7><BY8></BY8><BY9></BY9><BY10></BY10><COMMON_FPKJ_XMXXS class='COMMON_FPKJ_XMXX' size='1'><COMMON_FPKJ_XMXX><FPHXZ>0</FPHXZ><XMMC>电视机</XMMC><GGXH>X100</GGXH><DW>台</DW><XMSL>10</XMSL><XMDJ>100.00</XMDJ><XMJE>1000.00</XMJE><SL>0.17</SL><SE>170.00</SE><BY1></BY1><BY2></BY2><BY3></BY3><BY4></BY4><BY5></BY5><BY6></BY6></COMMON_FPKJ_XMXX></COMMON_FPKJ_XMXXS></REQUEST_COMMON_FPKJ>";
        String xml = null;
        try {
            xml = getCommonXml("DFXJ1001", new BASE64Encoder().encodeBuffer(content.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        System.out.println("Invoking doService...");
        String _doService_in0 = xml;
        String _doService__return = port.doService(_doService_in0);
        System.out.println("doService.result=" + _doService__return);



        return _doService__return;
    }
    public static void main(String args[]) throws Exception {
        URL wsdlURL = DZFPService.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) {
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        DZFPService ss = new DZFPService(wsdlURL, SERVICE_NAME);
        DZFPServicePortType port = ss.getDZFPServiceHttpPort();
        Client client = org.apache.cxf.frontend.ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();

        httpClientPolicy.setConnectionTimeout(36000);
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setReceiveTimeout(32000);

        conduit.setClient(httpClientPolicy);

        String content = "<REQUEST_COMMON_FPKJ class='REQUEST_COMMON_FPKJ'><FPQQLSH>201503014123422252</FPQQLSH><KPLX>0</KPLX><XSF_NSRSBH>440300568519737</XSF_NSRSBH><XSF_MC>电子发票测试</XSF_MC><XSF_DZDH>山东省青岛市</XSF_DZDH><XSF_YHZH>92523123213412341234</XSF_YHZH><GMF_NSRSBH>440300568519737</GMF_NSRSBH><GMF_MC>张三</GMF_MC><GMF_DZDH>浙江省杭州市余杭区文一西路xxx号18234561212</GMF_DZDH><GMF_YHZH>123412341234</GMF_YHZH><GMF_SJH>18234561212</GMF_SJH><GMF_DZYX>mytest@xxx.com</GMF_DZYX><FPT_ZH></FPT_ZH><KPR>小张</KPR><SKR></SKR><FHR>小林</FHR><YFP_DM>111100000000</YFP_DM><YFP_HM>00004349</YFP_HM><JSHJ>1170.00</JSHJ><HJJE>1000.00</HJJE><HJSE>170.00</HJSE><BZ>电子发票测试</BZ><HYLX>1</HYLX><BY1></BY1><BY2></BY2><BY3></BY3><BY4></BY4><BY5></BY5><BY6></BY6><BY7></BY7><BY8></BY8><BY9></BY9><BY10></BY10><COMMON_FPKJ_XMXXS class='COMMON_FPKJ_XMXX' size='1'><COMMON_FPKJ_XMXX><FPHXZ>0</FPHXZ><XMMC>电视机</XMMC><GGXH>X100</GGXH><DW>台</DW><XMSL>10</XMSL><XMDJ>100.00</XMDJ><XMJE>1000.00</XMJE><SL>0.17</SL><SE>170.00</SE><BY1></BY1><BY2></BY2><BY3></BY3><BY4></BY4><BY5></BY5><BY6></BY6></COMMON_FPKJ_XMXX></COMMON_FPKJ_XMXXS></REQUEST_COMMON_FPKJ>";
        String xml = getCommonXml("DFXJ1001", new BASE64Encoder().encodeBuffer(content.getBytes("UTF-8")));

        {
            System.out.println("Invoking doService...");
            String _doService_in0 = xml;
            String _doService__return = port.doService(_doService_in0);
            System.out.println("doService.result=" + _doService__return);


        }
        Object[] params = new Object[] { xml, Boolean.TRUE };
        String functionName = "doService";

        //ssl
        TLSClientParameters tlscp = conduit.getTlsClientParameters();
        if (tlscp == null){
            tlscp = new TLSClientParameters();
        }
        tlscp.setSecureSocketProtocol("SSL");
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            InputStream fp = ClassLoader.class.getResourceAsStream("testclient.jks");
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(fp, "123456".toCharArray());
            fp.close();
            factory.init(ks);
            tlscp.setTrustManagers(factory.getTrustManagers());
        } catch (Exception e) {
            e.printStackTrace();
        }
        conduit.setTlsClientParameters(tlscp);
        Object[] result = { "" };
        result = client.invoke(functionName,params);
        System.out.println(result[0].toString());
        System.exit(0);
    }

    public static String getCommonXml(String interfaceCode,String content){
        StringBuffer sb = new StringBuffer("");
        sb.append("<?xml version='1.0' encoding='UTF-8' ?>");
        sb.append("<interface xmlns=\"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:schemaLocation=\"http://www.chinatax.gov.cn/tirip/dataspec/interfaces.xsd\" version=\""
                + "DZFPQZ0.2" + "\"> ");
        sb.append("<globalInfo>");
        sb.append("<appId>502361120b63e2bd10d526aa811687bfa59f2173b552fefbb4ffece161094413</appId>");
        sb.append("<interfaceId></interfaceId>");
        sb.append("<interfaceCode>" + interfaceCode + "</interfaceCode>");
        sb.append("<requestCode>DZFPQZ</requestCode>");
        sb.append("<requestTime>" + formatToTime() + "</requestTime>");
        sb.append("<responseCode>Ds</responseCode>");
        sb.append("<dataExchangeId>" + "DZFPQZ" + interfaceCode
                + formatToDay() + randNineData()
                + "</dataExchangeId>");
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
        return sb.toString();
    }

    /************************************************************************
     * 格式化时间-时间
     */
    public static String formatToTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format((new Date()));
    }

    /************************************************************************
     * 格式化时间-日
     */
    public static String formatToDay(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format((new Date()));
    }

    /************************************************************************
     * 9位随机整数
     */
    public static String randNineData(){
        return String.valueOf((int)(Math.random()*900000000+100000000));
    }

}

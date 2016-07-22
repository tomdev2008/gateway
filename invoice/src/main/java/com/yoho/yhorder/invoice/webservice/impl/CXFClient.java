package com.yoho.yhorder.invoice.webservice.impl;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XStream11NameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.yoho.yhorder.invoice.helper.SSLUtilities;
import com.yoho.yhorder.invoice.model.SoapReq;
import com.yoho.yhorder.invoice.webservice.SoapClient;
import com.yoho.yhorder.invoice.webservice.helper.FPReqXmlHelper;
import com.yoho.yhorder.invoice.webservice.xmlbean.CommonFPKJProject;
import com.yoho.yhorder.invoice.webservice.xmlbean.CommonFPKJReq;
import com.yoho.yhorder.invoice.webservice.xmlbean.ProjectDetail;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sun.misc.BASE64Encoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import java.util.List;

/**
 * Created by chenchao on 2016/5/30.
 */
@Service
public class CXFClient implements SoapClient{
    private static final Logger LOGGER = LoggerFactory.getLogger(CXFClient.class);
    /**
     * 电子发票服务地址WSDL(http)
     */
    @Value("${ws.fp.url}")
    public String DZFPSERVICE_URL_HTTP;

    @Value("${fp.sec.keyStore.file}")
    private String sslStore;

    @Value("${fp.sec.keyStore.sslPwd}")
    private String sslPwd;

    /**
     * 电子发票服务地址WSDL(https)
     */
    @Value("${ws.fp.url}")
    public String DZFPSERVICE_URL_HTTPS;

    @Value("${invoice.appid}")
    private String appid;

    @Value("${invoice.soap.receiveTimeout:5000}")
    private long receiveTimeout;

    @Value("${invoice.soap.connectionTimeout:60000}")
    private long connectionTimeout ;
    /**
     * CXF客户端请求http服务
     *
     * @throws Exception
     */
    public void httpRequest() throws Exception {
        JaxWsDynamicClientFactory clientFactory = JaxWsDynamicClientFactory.newInstance();
        Client client = clientFactory.createClient(DZFPSERVICE_URL_HTTP);
        String content = "<REQUEST_COMMON_FPCX class='REQUEST_COMMON_FPCX'><FPQQLSH>123451234512345</FPQQLSH><XSF_NSRSBH>440300568519737</XSF_NSRSBH></REQUEST_COMMON_FPCX>";
        SoapReq req = new SoapReq();
        req.setAppId(appid);
        req.setInterfaceCode("DFXJ1004");
        req.setContent(new BASE64Encoder().encodeBuffer(content.getBytes("UTF-8")));
        Object params = FPReqXmlHelper.getCommonXml(req);
        String operationName = "doService";
        Object[] results = client.invoke(operationName, params);
        System.out.println(results[0].toString());
    }

    /**
     * CXF客户端请求https服务
     *
     * @throws Exception
     */
    public String httpsRequest(String interfaceCode,String content) throws Exception {
        LOGGER.info("in httpsRequest sslStore {}, appid {} ", sslStore, appid);
        /*
         * 创建client，绕过证书
         */
        SSLUtilities.trustAllHostnames();
        SSLUtilities.trustAllHttpsCertificates();
        JaxWsDynamicClientFactory clientFactory = JaxWsDynamicClientFactory.newInstance();
        Client client = clientFactory.createClient(DZFPSERVICE_URL_HTTPS);
        //time out
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();

        httpClientPolicy.setConnectionTimeout(connectionTimeout);
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setReceiveTimeout(receiveTimeout);
        conduit.setClient(httpClientPolicy);
        TLSClientParameters tlsClientParameters = conduit.getTlsClientParameters();
        if(tlsClientParameters == null){
            tlsClientParameters = new TLSClientParameters();
        }
        //域名和URL校验
        tlsClientParameters.setHostnameVerifier((String urlHostName, SSLSession session) -> {
            return true;
        });
        //jdk 信任管理器
        tlsClientParameters.setTrustManagers(new SSLUtilities()._trustManagers);
        conduit.setTlsClientParameters(tlsClientParameters);

        //make params
        SoapReq req = new SoapReq();
        req.setAppId(appid);
        req.setInterfaceCode(interfaceCode);
        req.setContent(new BASE64Encoder().encodeBuffer(content.getBytes("UTF-8")));
        String params = FPReqXmlHelper.getCommonXml(req);
        String operationName = "doService";
        long beginTime = System.currentTimeMillis();
        Object[] results = client.invoke(operationName, params);
        LOGGER.info("invoke invoice vendor interfaceCode {} cost {} ms", interfaceCode, System.currentTimeMillis()-beginTime);
        client.destroy();
        return results[0].toString();
    }

    public static void main(String[] args) throws Exception {
        //String interfaceCode = InterfaceCode.QUERY;
        //String content = "<REQUEST_COMMON_FPCX class='REQUEST_COMMON_FPCX'><FPQQLSH>123451234512345</FPQQLSH><XSF_NSRSBH>440300568519737</XSF_NSRSBH></REQUEST_COMMON_FPCX>";

        //String result = new CXFClient().httpsRequest(interfaceCode, content);
        //System.out.println(result);
        /**/
        XStream stream = new XStream(new XppDriver(new NoNameCoder()));
        stream.autodetectAnnotations(true);
        stream.alias(ProjectDetail.class.getSimpleName().toLowerCase(), ProjectDetail.class);
        stream.aliasSystemAttribute(null, "class"); // 去掉 class 属性

        CommonFPKJReq fpkjReq = new CommonFPKJReq();
        fpkjReq.setFpReqSeriseNum("wer230002");
        fpkjReq.setOpenid("what");
        fpkjReq.setBackup("");
        CommonFPKJProject fpkjProject = new CommonFPKJProject();
        ProjectDetail projectDetail = new ProjectDetail();
        projectDetail.setProjectName("ProjectName-a");
        List<ProjectDetail> projectDetailList = Lists.newArrayList();
        projectDetailList.add(projectDetail);
        projectDetail = new ProjectDetail();
        projectDetail.setProjectName("ProjectName-b");
        projectDetailList.add(projectDetail);
        fpkjProject.setProjectDetails(projectDetailList);
        fpkjReq.setProject(fpkjProject);
        System.out.println("CommonFPKJReq xml is " +stream.toXML(fpkjReq));

        //System.out.println("InterfaceReq xml is : " + stream.toXML(new InterfaceReq()));

        //System.out.println("build InterfaceReq xml is : " + stream.toXML(new InterfaceReq.InterfaceReqBuilder().build()));

    }


}

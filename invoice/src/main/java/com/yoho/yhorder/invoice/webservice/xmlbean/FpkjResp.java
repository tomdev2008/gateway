package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <RESPONSE>
 <FPQQLSH>发票请求流水号</FPQQLSH>
 <FP_DM>发票代码</FP_DM>
 <FP_HM>发票号码</FP_HM>
 <JYM>校验码</JYM>
 <KPRQ>开票日期</KPRQ>
 <PDF_URL>PDF下载地址</PDF_URL>
 </RESPONSE>

 * Created by chenchao on 2016/6/17.
 */
@XStreamAlias("RESPONSE")
public class FpkjResp {
    /**
     * <FPQQLSH>发票请求流水号</FPQQLSH>
     */
    @XStreamAlias("FPQQLSH")
    private String seriesNum;

    /**
     *<FP_DM>发票代码</FP_DM>
     */
    @XStreamAlias("FP_DM")
    private String invoiceCode;

    /**
     *<FP_HM>发票号码</FP_HM>
     */
    @XStreamAlias("FP_HM")
    private String invoiceNum;

    /**
     *<JYM>校验码</JYM>
     */
    @XStreamAlias("JYM")
    private String validateCode;

    /**
     *<KPRQ>开票日期</KPRQ>
     */
    @XStreamAlias("KPRQ")
    private String issueDate;

    /**
     *<PDF_URL>PDF下载地址</PDF_URL>
     */
    @XStreamAlias("PDF_URL")
    private String pdfUrl;

    @XStreamOmitField
    private boolean issueSuccess = false;

    @XStreamOmitField
    private ReturnStateInfo returnStateInfo;

    public boolean getIssueSuccess() {
        return issueSuccess;
    }

    public void setIssueSuccess(boolean issueSuccess) {
        this.issueSuccess = issueSuccess;
    }

    public String getSeriesNum() {
        return seriesNum;
    }

    public void setSeriesNum(String seriesNum) {
        this.seriesNum = seriesNum;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public String getInvoiceNum() {
        return invoiceNum;
    }

    public void setInvoiceNum(String invoiceNum) {
        this.invoiceNum = invoiceNum;
    }

    public String getValidateCode() {
        return validateCode;
    }

    public void setValidateCode(String validateCode) {
        this.validateCode = validateCode;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public ReturnStateInfo getReturnStateInfo() {
        return returnStateInfo;
    }

    public void setReturnStateInfo(ReturnStateInfo returnStateInfo) {
        this.returnStateInfo = returnStateInfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("seriesNum", seriesNum)
                .append("invoiceCode", invoiceCode)
                .append("invoiceNum", invoiceNum)
                .append("validateCode", validateCode)
                .append("issueDate", issueDate)
                .append("pdfUrl", pdfUrl)
                .append("issueSuccess", issueSuccess)
                .append("returnStateInfo", returnStateInfo)
                .toString();
    }

    public static void main(String[] args) {
        FpkjResp resp = new FpkjResp();
        resp.setIssueSuccess(true);
        System.out.println(resp);
    }
}

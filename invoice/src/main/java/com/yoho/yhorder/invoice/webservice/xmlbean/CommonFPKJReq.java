package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**电子发票开具
 * Created by chenchao on 2016/6/6.
 */
@XStreamAlias("REQUEST_COMMON_FPKJ")
@XStreamInclude({CommonFPKJProject.class})
public class CommonFPKJReq {

    @XStreamAsAttribute
    @XStreamAlias("class")
    protected String reqClass = "REQUEST_COMMON_FPKJ";

    /**
     * 发票请求流水号
     */
    @XStreamAlias("FPQQLSH")
    private String fpReqSeriseNum="";

    /**
     *<KPLX>开票类型</KPLX>
     */
    @XStreamAlias("KPLX")
    private String kpType="";

    /**
     *<XSF_NSRSBH>销售方纳税人识别号</XSF_NSRSBH>
     */
    @XStreamAlias("XSF_NSRSBH")
    private String salesTaxpayerId="";

    /**
     *<XSF_MC>销售方名称</XSF_MC>
     */
    @XStreamAlias("XSF_MC")
    private String salesName="";

    /**
     *<XSF_DZDH>销售方地址、电话</XSF_DZDH>
     */
    @XStreamAlias("XSF_DZDH")
    private String salesAddrTel="";

    /**
     *<XSF_YHZH>销售方银行账号</XSF_YHZH>
     */
    @XStreamAlias("XSF_YHZH")
    private String salesBankAccount="";

    /**
     *<GMF_NSRSBH>购买方纳税人识别号</GMF_NSRSBH>
     */
    @XStreamAlias("GMF_NSRSBH")
    private String buyerTaxpayerId="";

    /**
     *<GMF_MC>购买方名称</GMF_MC>
     */
    @XStreamAlias("GMF_MC")
    private String buyerName="";

    /**
     *<GMF_DZDH>购买方地址、电话</GMF_DZDH>
     */
    @XStreamAlias("GMF_DZDH")
    private String buyerAddrTel="";

    /**
     *<GMF_YHZH>购买方银行账号</GMF_YHZH>
     */
    @XStreamAlias("GMF_YHZH")
    private String buyerBankAccount="";

    /**
     *<GMF_SJH>购买方手机号</GMF_SJH>
     */
    @XStreamAlias("GMF_SJH")
    private String buyerTel="";

    /**
     *<GMF_DZYX>购买方电子邮箱</GMF_DZYX>
     */
    @XStreamAlias("GMF_DZYX")
    private String buyerEmail="";

    /**
     *<FPT_ZH>发票通账户</FPT_ZH>
     */
    @XStreamAlias("FPT_ZH")
    private String invoiceCommonAccount="";

    /**
     *<WX_OPENID>微信openId</WX_OPENID>
     */
    @XStreamAlias("WX_OPENID")
    private String openid="";


    /**
     *<KPR>开票人</KPR>
     */
    @XStreamAlias("KPR")
    private String biller="";

    /**
     *<SKR>收款人</SKR>
     */
    @XStreamAlias("SKR")
    private String payee="";

    /**
     *<FHR>复核人</FHR>
     */
    @XStreamAlias("FHR")
    private String reviewer="";

    /**
     *<YFP_DM>原发票代码</YFP_DM>
     */
    @XStreamAlias("YFP_DM")
    private String originalInvoiceCode="";

    /**
     *<YFP_HM>原发票号码</YFP_HM>
     */
    @XStreamAlias("YFP_HM")
    private String originalInvoiceNum="";

    /**
     *<JSHJ>价税合计</JSHJ>
     */
    @XStreamAlias("JSHJ")
    private double totalValoremTax;

    /**
     *<HJJE>合计金额</HJJE>
     */
    @XStreamAlias("HJJE")
    private double combinedAmount;

    /**
     *<HJSE>合计税额</HJSE>
     */
    @XStreamAlias("HJSE")
    private double combinedTax;

    /**
     *<BZ>备注</BZ>
     */
    @XStreamAlias("BZ")
    private String backup="";

    /**
     *<HYLX>行业类型</HYLX>
     */
    @XStreamAlias("HYLX")
    private String industryType="";

    /**
     *<BY1>备用字段1</BY1>
     */
    @XStreamAlias("BY1")
    private String backup1="";

    /**
     *<BY2>备用字段2</BY2>
     */
    @XStreamAlias("BY2")
    private String backup2="";

    /**
     *<BY3>备用字段3</BY3>
     */
    @XStreamAlias("BY3")
    private String backup3="";

    /**
     *<BY4>备用字段4</BY4>
     */
    @XStreamAlias("BY4")
    private String backup4="";

    /**
     *<BY5>备用字段5</BY5>
     */
    @XStreamAlias("BY5")
    private String backup5="";

    /**
     *<BY6>备用字段6</BY6>
     */
    @XStreamAlias("BY6")
    private String backup6="";

    /**
     *<BY7>备用字段7</BY7>
     */
    @XStreamAlias("BY7")
    private String backup7="";

    /**
     *<BY8>备用字段8</BY8>
     */
    @XStreamAlias("BY8")
    private String backup8="";

    /**
     *<BY9>备用字段9</BY9>
     */
    @XStreamAlias("BY9")
    private String backup9="";

    /**
     *备用字段10
     */
    @XStreamAlias("BY10")
    private String backup10="";

    @XStreamAlias("COMMON_FPKJ_XMXXS")
    private CommonFPKJProject project;


    public String getReqClass() {
        return reqClass;
    }

    public void setReqClass(String reqClass) {
        this.reqClass = reqClass;
    }

    public String getFpReqSeriseNum() {
        return fpReqSeriseNum;
    }

    public void setFpReqSeriseNum(String fpReqSeriseNum) {
        this.fpReqSeriseNum = fpReqSeriseNum;
    }

    public String getKpType() {
        return kpType;
    }

    public void setKpType(String kpType) {
        this.kpType = kpType;
    }

    public String getSalesTaxpayerId() {
        return salesTaxpayerId;
    }

    public void setSalesTaxpayerId(String salesTaxpayerId) {
        this.salesTaxpayerId = salesTaxpayerId;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getSalesAddrTel() {
        return salesAddrTel;
    }

    public void setSalesAddrTel(String salesAddrTel) {
        this.salesAddrTel = salesAddrTel;
    }

    public String getSalesBankAccount() {
        return salesBankAccount;
    }

    public void setSalesBankAccount(String salesBankAccount) {
        this.salesBankAccount = salesBankAccount;
    }

    public String getBuyerTaxpayerId() {
        return buyerTaxpayerId;
    }

    public void setBuyerTaxpayerId(String buyerTaxpayerId) {
        this.buyerTaxpayerId = buyerTaxpayerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerAddrTel() {
        return buyerAddrTel;
    }

    public void setBuyerAddrTel(String buyerAddrTel) {
        this.buyerAddrTel = buyerAddrTel;
    }

    public String getBuyerBankAccount() {
        return buyerBankAccount;
    }

    public void setBuyerBankAccount(String buyerBankAccount) {
        this.buyerBankAccount = buyerBankAccount;
    }

    public String getBuyerTel() {
        return buyerTel;
    }

    public void setBuyerTel(String buyerTel) {
        this.buyerTel = buyerTel;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getInvoiceCommonAccount() {
        return invoiceCommonAccount;
    }

    public void setInvoiceCommonAccount(String invoiceCommonAccount) {
        this.invoiceCommonAccount = invoiceCommonAccount;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getBiller() {
        return biller;
    }

    public void setBiller(String biller) {
        this.biller = biller;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getOriginalInvoiceCode() {
        return originalInvoiceCode;
    }

    public void setOriginalInvoiceCode(String originalInvoiceCode) {
        this.originalInvoiceCode = originalInvoiceCode;
    }

    public String getOriginalInvoiceNum() {
        return originalInvoiceNum;
    }

    public void setOriginalInvoiceNum(String originalInvoiceNum) {
        this.originalInvoiceNum = originalInvoiceNum;
    }

    public double getTotalValoremTax() {
        return totalValoremTax;
    }

    public void setTotalValoremTax(double totalValoremTax) {
        this.totalValoremTax = totalValoremTax;
    }

    public double getCombinedAmount() {
        return combinedAmount;
    }

    public void setCombinedAmount(double combinedAmount) {
        this.combinedAmount = combinedAmount;
    }

    public double getCombinedTax() {
        return combinedTax;
    }

    public void setCombinedTax(double combinedTax) {
        this.combinedTax = combinedTax;
    }

    public String getBackup() {
        return backup;
    }

    public void setBackup(String backup) {
        this.backup = backup;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getBackup1() {
        return backup1;
    }

    public void setBackup1(String backup1) {
        this.backup1 = backup1;
    }

    public String getBackup2() {
        return backup2;
    }

    public void setBackup2(String backup2) {
        this.backup2 = backup2;
    }

    public String getBackup3() {
        return backup3;
    }

    public void setBackup3(String backup3) {
        this.backup3 = backup3;
    }

    public String getBackup4() {
        return backup4;
    }

    public void setBackup4(String backup4) {
        this.backup4 = backup4;
    }

    public String getBackup5() {
        return backup5;
    }

    public void setBackup5(String backup5) {
        this.backup5 = backup5;
    }

    public String getBackup6() {
        return backup6;
    }

    public void setBackup6(String backup6) {
        this.backup6 = backup6;
    }

    public String getBackup7() {
        return backup7;
    }

    public void setBackup7(String backup7) {
        this.backup7 = backup7;
    }

    public String getBackup8() {
        return backup8;
    }

    public void setBackup8(String backup8) {
        this.backup8 = backup8;
    }

    public String getBackup9() {
        return backup9;
    }

    public void setBackup9(String backup9) {
        this.backup9 = backup9;
    }

    public String getBackup10() {
        return backup10;
    }

    public void setBackup10(String backup10) {
        this.backup10 = backup10;
    }

    public CommonFPKJProject getProject() {
        return project;
    }

    public void setProject(CommonFPKJProject project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("reqClass", reqClass)
                .append("fpReqSeriseNum", fpReqSeriseNum)
                .append("kpType", kpType)
                .append("salesTaxpayerId", salesTaxpayerId)
                .append("salesName", salesName)
                .append("salesAddrTel", salesAddrTel)
                .append("salesBankAccount", salesBankAccount)
                .append("buyerTaxpayerId", buyerTaxpayerId)
                .append("buyerName", buyerName)
                .append("buyerAddrTel", buyerAddrTel)
                .append("buyerBankAccount", buyerBankAccount)
                .append("buyerTel", buyerTel)
                .append("buyerEmail", buyerEmail)
                .append("invoiceCommonAccount", invoiceCommonAccount)
                .append("openid", openid)
                .append("biller", biller)
                .append("payee", payee)
                .append("reviewer", reviewer)
                .append("originalInvoiceCode", originalInvoiceCode)
                .append("originalInvoiceNum", originalInvoiceNum)
                .append("totalValoremTax", totalValoremTax)
                .append("combinedAmount", combinedAmount)
                .append("combinedTax", combinedTax)
                .append("backup", backup)
                .append("industryType", industryType)
                .append("backup1", backup1)
                .append("backup2", backup2)
                .append("backup3", backup3)
                .append("backup4", backup4)
                .append("backup5", backup5)
                .append("backup6", backup6)
                .append("backup7", backup7)
                .append("backup8", backup8)
                .append("backup9", backup9)
                .append("backup10", backup10)
                .append("project", project)
                .toString();
    }
}

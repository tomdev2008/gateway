package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by chenchao on 2016/6/6.
 */
@XStreamAlias("COMMON_FPKJ_XMXX")
public class ProjectDetail {


  /**
   *<FPHXZ>发票行性质</FPHXZ>
   */
  @XStreamAlias("FPHXZ")
  private int invoiceLineNature;

  /**
   *<XMMC>项目名称</XMMC>
   */
  @XStreamAlias("XMMC")
  private String projectName="";

  /**
   *<GGXH>规格型号</GGXH>
   */
  @XStreamAlias("GGXH")
  private String specificateNum="";

  /**
   *<DW>单位</DW>
   */
  @XStreamAlias("DW")
  private String unit="";

  /**
   *<XMSL>项目数量</XMSL>
   */
  @XStreamAlias("XMSL")
  private Integer amount;

  /**
   *<XMDJ>项目单价</XMDJ>
   */
  @XStreamAlias("XMDJ")
  private Double unitPrice;

  /**
   *<XMJE>项目金额</XMJE>
   */
  @XStreamAlias("XMJE")
  private Double projectAmount;

  /**
   *<SL>税率</SL>
   */
  @XStreamAlias("SL")
  private Double taxRate;


  /**
   *<SE>税额</SE>
   */
  @XStreamAlias("SE")
  private Double taxAmount;


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

 public int getInvoiceLineNature() {
  return invoiceLineNature;
 }

 public void setInvoiceLineNature(int invoiceLineNature) {
  this.invoiceLineNature = invoiceLineNature;
 }

 public String getProjectName() {
  return projectName;
 }

 public void setProjectName(String projectName) {
  this.projectName = projectName;
 }

 public String getSpecificateNum() {
  return specificateNum;
 }

 public void setSpecificateNum(String specificateNum) {
  this.specificateNum = specificateNum;
 }

 public String getUnit() {
  return unit;
 }

 public void setUnit(String unit) {
  this.unit = unit;
 }

 public Integer getAmount() {
  return amount;
 }

 public void setAmount(Integer amount) {
  this.amount = amount;
 }

 public Double getUnitPrice() {
  return unitPrice;
 }

 public void setUnitPrice(Double unitPrice) {
  this.unitPrice = unitPrice;
 }

 public Double getProjectAmount() {
  return projectAmount;
 }

 public void setProjectAmount(Double projectAmount) {
  this.projectAmount = projectAmount;
 }

 public Double getTaxRate() {
  return taxRate;
 }

 public void setTaxRate(Double taxRate) {
  this.taxRate = taxRate;
 }

 public Double getTaxAmount() {
  return taxAmount;
 }

 public void setTaxAmount(Double taxAmount) {
  this.taxAmount = taxAmount;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("invoiceLineNature", invoiceLineNature)
                .append("projectName", projectName)
                .append("specificateNum", specificateNum)
                .append("unit", unit)
                .append("amount", amount)
                .append("unitPrice", unitPrice)
                .append("projectAmount", projectAmount)
                .append("taxRate", taxRate)
                .append("taxAmount", taxAmount)
                .append("backup1", backup1)
                .append("backup2", backup2)
                .append("backup3", backup3)
                .append("backup4", backup4)
                .append("backup5", backup5)
                .toString();
    }
}

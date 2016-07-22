package com.yoho.yhorder.dal.model;

public class ExpressCompany extends Base{
    private static final long serialVersionUID = -5366782719733664520L;
    /**
     * 物流公司ID
     */
    private Integer id;

    /**
     * 物流公司名称
     */
    private String companyName;

    /**
     * 物流公司首字母
     */
    private String companyAlif;

    /**
     * 物流公司编码
     */
    private String companyCode;

    /**
     * 添加时间
     */
    private Integer createTime;

    /**
     * 物流公司启用状态 1开启，0关闭
     */
    private Byte status;

    /**
     * 物流公司logo
     */
    private String companyLogo;

    /**
     * 物流公司网址
     */
    private String companyUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName == null ? null : companyName.trim();
    }

    public String getCompanyAlif() {
        return companyAlif;
    }

    public void setCompanyAlif(String companyAlif) {
        this.companyAlif = companyAlif == null ? null : companyAlif.trim();
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode == null ? null : companyCode.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo == null ? null : companyLogo.trim();
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public void setCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl == null ? null : companyUrl.trim();
    }
}
package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**电子发票查询
 * Created by chenchao on 2016/6/6.
 */
@XStreamAlias("REQUEST_COMMON_FPCX")
public class CommonFPCXRequest {
    /**
     * 发票请求流水号
     */
    @XStreamAlias("FPQQLSH")
    private String FPQQLSH;
    /**
     * 销售方纳税人识别号
     */
    @XStreamAlias("XSF_NSRSBH")
    private String XSF_NSRSBH;

    public String getFPQQLSH() {
        return FPQQLSH;
    }

    public void setFPQQLSH(String FPQQLSH) {
        this.FPQQLSH = FPQQLSH;
    }

    public String getXSF_NSRSBH() {
        return XSF_NSRSBH;
    }

    public void setXSF_NSRSBH(String XSF_NSRSBH) {
        this.XSF_NSRSBH = XSF_NSRSBH;
    }
}

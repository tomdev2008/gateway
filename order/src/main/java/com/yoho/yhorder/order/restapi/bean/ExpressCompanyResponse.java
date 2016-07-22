package com.yoho.yhorder.order.restapi.bean;

import java.util.List;

/**
 * 物流公司信息类
 * Created by sunjiexiang on 2015/11/27.
 */
public class ExpressCompanyResponse {

    private String caption;

    private String url;

    private String logo;

    private String is_support;

    private String express_number;

    private List<InnerDetail> express_detail;

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getIs_support() {
        return is_support;
    }

    public void setIs_support(String is_support) {
        this.is_support = is_support;
    }

    public String getExpress_number() {
        return express_number;
    }

    public void setExpress_number(String express_number) {
        this.express_number = express_number;
    }

    public List<InnerDetail> getExpress_detail() {
        return express_detail;
    }

    public void setExpress_detail(List<InnerDetail> express_detail) {
        this.express_detail = express_detail;
    }

    class InnerDetail{
        private String acceptTime;
        private String accept_address;

        public String getAcceptTime() {
            return acceptTime;
        }

        public void setAcceptTime(String acceptTime) {
            this.acceptTime = acceptTime;
        }

        public String getAccept_address() {
            return accept_address;
        }

        public void setAccept_address(String accept_address) {
            this.accept_address = accept_address;
        }
    }
}

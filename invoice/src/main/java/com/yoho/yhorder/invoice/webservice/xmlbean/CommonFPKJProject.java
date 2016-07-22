package com.yoho.yhorder.invoice.webservice.xmlbean;

import com.thoughtworks.xstream.annotations.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * <COMMON_FPKJ_XMXXS class="COMMON_FPKJ_XMXX" size="1">
 * Created by chenchao on 2016/6/6.
 */
@XStreamAlias("COMMON_FPKJ_XMXXS")
@XStreamInclude({ProjectDetail.class})
public class CommonFPKJProject {

    @XStreamAlias("class")
    @XStreamAsAttribute
    protected String targetClass = "COMMON_FPKJ_XMXX";

    @XStreamAlias("size")
    @XStreamAsAttribute
    protected int size = 0;
    /**
     * <COMMON_FPKJ_XMXX>
     */
    @XStreamImplicit(itemFieldName = "COMMON_FPKJ_XMXX")
    private List<ProjectDetail> projectDetails;

    public List<ProjectDetail> getProjectDetails() {
        return projectDetails;
    }

    public void setProjectDetails(List<ProjectDetail> projectDetails) {
        this.projectDetails = projectDetails;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("targetClass", targetClass)
                .append("size", size)
                .append("projectDetails", projectDetails)
                .toString();
    }
}

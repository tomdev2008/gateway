package com.yoho.yhorder.common.request;

import java.io.Serializable;


public class BaseRequest  implements Serializable {

    private boolean isLoadAll;

    private int pageNum;


    public boolean isLoadAll() {
        return isLoadAll;
    }

    public void setIsLoadAll(boolean isLoadAll) {
        this.isLoadAll = isLoadAll;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
}

package com.yoho.yhorder.common.page;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对分页的基本数据进行一个简单的封装
 */
public class Page<T> {

    public static final Page newInstance(Integer pageNo, Integer pageSize, Object o) {
        Page page = new Page();
        page.setPageNo((pageNo != null && pageNo > 0) ? pageNo - 1 : 0);
        page.setPageSize((pageSize != null && pageSize > 0) ? pageSize : 15);
        page.setStart(page.getPageNo() * page.getPageSize());
        try {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                //过滤内容为空的
                if (field.get(o) == null) {
                    continue;
                }
                page.getParams().put(field.getName(), field.get(o));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return page;
    }

    private Integer start = 0;
    private Integer pageNo = 1;//页码，默认是第一页
    private Integer pageSize = 20;//每页显示的记录数，默认是20
    private Integer totalRecord;//总记录数
    private Integer totalPage;//总页数

    private List<T> results;//对应的当前页记录
    private Map<String, Object> params = new HashMap<String, Object>();//其他的参数我们把它分装成一个Map对象

    private boolean isLoadAll = false;


    public boolean isLoadAll() {
        return isLoadAll;
    }

    public void setIsLoadAll(boolean isLoadAll) {
        this.isLoadAll = isLoadAll;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
        //在设置总页数的时候计算出对应的总页数，在下面的三目运算中加法拥有更高的优先级，所以最后可以不加括号。
        int totalPage = totalRecord % pageSize == 0 ? totalRecord / pageSize : totalRecord / pageSize + 1;
        this.setTotalPage(totalPage);
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Page [pageNo=").append(pageNo).append(", pageSize=")
                .append(pageSize).append(", results=").append(results).append(
                ", totalPage=").append(totalPage).append(
                ", totalRecord=").append(totalRecord).append("]");
        return builder.toString();
    }

}
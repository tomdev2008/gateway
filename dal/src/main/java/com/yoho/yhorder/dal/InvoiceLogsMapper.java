package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.InvoiceLogs;

import java.util.List;

public interface InvoiceLogsMapper {


    int deleteByPrimaryKey(Integer id);

    int insert(InvoiceLogs record);

    int insertSelective(InvoiceLogs record);

    List<InvoiceLogs> selectByOrderId(Integer orderId);

    InvoiceLogs selectByPrimaryKey(Integer id);

}
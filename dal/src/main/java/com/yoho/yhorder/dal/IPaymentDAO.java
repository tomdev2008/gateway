package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.Payment;

import java.util.List;

public interface IPaymentDAO {
    int deleteByPrimaryKey(Short id);

    int insert(Payment record);

    int insertSelective(Payment record);

    Payment selectByPrimaryKey(Short id);

    List<Payment> selectByStatus();

    int updateByPrimaryKeySelective(Payment record);

    int updateByPrimaryKey(Payment record);
}
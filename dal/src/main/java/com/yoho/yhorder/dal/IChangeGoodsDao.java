package com.yoho.yhorder.dal;


import com.yoho.yhorder.dal.domain.ChangeGoodsMainInfo;

public interface IChangeGoodsDao {

    int insert(ChangeGoodsMainInfo record);

    int insertSelective(ChangeGoodsMainInfo record);

    ChangeGoodsMainInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ChangeGoodsMainInfo record);

    int updateByPrimaryKey(ChangeGoodsMainInfo record);

    /**
     * 通过id获取换货记录
     */
    ChangeGoodsMainInfo selectById(Integer id);

}












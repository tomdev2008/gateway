package com.yoho.yhorder.dal;


import com.yoho.yhorder.dal.domain.ChangeGoodsMainInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IChangeGoodsMainMapper {
    int updateDelFlagByChangeGoods(Integer id);

    int insert(ChangeGoodsMainInfo record);

    int insertSelective(ChangeGoodsMainInfo record);

    ChangeGoodsMainInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ChangeGoodsMainInfo record);

    int updateByPrimaryKey(ChangeGoodsMainInfo record);

    /**
     * 获取换货商品
     * @param changeIds
     * @return
     */
    List<ChangeGoodsMainInfo> selectExchange(@Param("changeIds") List<Integer> changeIds);

    /**
     * 根据订单号，获取换货申请
     */
    ChangeGoodsMainInfo selectByOrderCode(long orderCode);

    /**
     * 根据 ERP Change ID，更新换货申请
     */
    ChangeGoodsMainInfo selectByErpChangeGoodsId(int erpChangeId);
}
package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.LogisticsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ILogisticsInfoDao {
    int deleteByPrimaryKey(Integer id);

    int insert(LogisticsInfo record);

    int insertSelective(LogisticsInfo record);

    LogisticsInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LogisticsInfo record);

    int updateByPrimaryKey(LogisticsInfo record);

    List<LogisticsInfo> selectByOrderCodeAndState(@Param("orderCode") Long orderCode, @Param("state") Byte state);

    List<LogisticsInfo> selectByOrderCode(@Param("orderCode") Long orderCode);

    List<LogisticsInfo> selectByOrderCodeAndWaybillCode(@Param("orderCode") Long orderCode, @Param("waybillCode") String waybillCode);

    int selectCountByOrdercodeAddressTime(@Param("orderCode") Long orderCode, @Param("acceptAddress") String acceptAddress, @Param("createTime") Integer createTime);

}
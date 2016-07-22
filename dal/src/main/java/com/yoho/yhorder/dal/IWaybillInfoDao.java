package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.WaybillInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IWaybillInfoDao {
    int deleteByPrimaryKey(Integer id);

    int insert(WaybillInfo record);

    int insertSelective(WaybillInfo record);

    WaybillInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WaybillInfo record);

    int updateByPrimaryKeyWithBLOBs(WaybillInfo record);

    int updateByPrimaryKey(WaybillInfo record);

    List<WaybillInfo> selectByLogisticsTypeAndWaybillCode(@Param("logisticsType") Byte logisticsType, @Param("waybillCode") String waybillCode);

    List<WaybillInfo> selectByOrderCodeAndLogisticsType(@Param("orderCode") Long orderCode, @Param("logisticsType") Byte logisticsType);

}
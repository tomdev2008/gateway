package com.yoho.yhorder.dal;

import org.apache.ibatis.annotations.Param;

import com.yoho.yhorder.dal.model.SysConfig;

public interface ISysConfigMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(SysConfig record);

	int insertSelective(SysConfig record);

	SysConfig selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(SysConfig record);

	int updateByPrimaryKey(SysConfig record);

	SysConfig selectByConfigKey(@Param("configKey") String configKey);
}
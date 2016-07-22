package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ExpressCompany;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IExpressCompanyDao {
    int deleteByPrimaryKey(Integer id);

    int insert(ExpressCompany record);

    int insertSelective(ExpressCompany record);

    ExpressCompany selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ExpressCompany record);

    int updateByPrimaryKey(ExpressCompany record);

    /**
     * 获取物流公司列表
     */
    List<ExpressCompany> selectAll(@Param("status")Byte status);

    /**
     *  根据物流公司名称获取物流公司
     * @param companyName
     * @return
     */
    ExpressCompany selectByCompanyName(String companyName);
}















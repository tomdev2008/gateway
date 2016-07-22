package com.yoho.yhorder.dal;


import com.yoho.yhorder.dal.model.Express;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IExpressMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Express record);

    int insertSelective(Express record);

    Express selectByPrimaryKey(Integer id);

    List<Express> selectByOrderCode(String orderCode);

    int updateByPrimaryKeySelective(Express record);

    int updateByPrimaryKey(Express record);

    int selectCountByRouteIdAndOrderCode(@Param("routeId") Integer routeId, @Param("orderCode") Long orderCode);
}
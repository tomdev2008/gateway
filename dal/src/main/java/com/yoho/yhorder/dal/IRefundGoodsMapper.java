package com.yoho.yhorder.dal;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 退货mapper
 *
 * @author CaoQi
 * @Time 2015/11/20
 */
public interface IRefundGoodsMapper {


    int selectCountByUidAndStatusLessThan(@Param("uid") Integer uid, @Param("status") Byte status);

}

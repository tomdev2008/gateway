package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ReturnedGoodsList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IReturnedGoodsListDAO {

    List<ReturnedGoodsList> selectReturnedList(@Param("initOrderCode") Long initOrderCode,@Param("status") Integer status,@Param("changePurchaseId") Integer changePurchaseId);
}
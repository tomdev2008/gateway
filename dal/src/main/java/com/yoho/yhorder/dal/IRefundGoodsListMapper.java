package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.RefundGoodsCnt;
import com.yoho.yhorder.dal.model.RefundGoodsList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IRefundGoodsListMapper {

    List<RefundGoodsList> selectByReturnRequestIds(@Param("returnRequestIds") List<Integer> returnRequestIds);

    List<RefundGoodsCnt> selectRefundGoodsCnt(RefundGoodsCnt refundGoodsCnt);

}
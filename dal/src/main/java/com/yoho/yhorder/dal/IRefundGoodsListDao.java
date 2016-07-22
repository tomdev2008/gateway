package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.domain.RefundNumber;
import com.yoho.yhorder.dal.domain.RefundNumberStatistics;
import com.yoho.yhorder.dal.model.RefundGoodsList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRefundGoodsListDao {
    int deleteByPrimaryKey(Integer id);

    int insert(RefundGoodsList record);

    RefundGoodsList selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RefundGoodsList record);

    int updateByPrimaryKey(RefundGoodsList record);
    
    /**
     * 根据returnRequestId更新退货商品表的status
     */
    int updateByReturned(RefundGoodsList record);
    
    /**
     * 申请单ID
     *
     * @param returnRequestId
     * @return
     */
    List<RefundGoodsList> selectByRequestId(Integer returnRequestId);

    /**
     * 批量获取退货商品数量
     */
    List<RefundNumberStatistics> selectRefundNumberStatistics(@Param("orderCode") Long orderCode, @Param("refundNumberList") List<RefundNumber> refundNumberList);

    /**
     * 批量获取退货商品数量
     */
    List<RefundNumberStatistics> selectRefundNumberStatisticsByOrderCodes(@Param("orderCodes") List<Long> orderCodes);

    /**
     * 添加退货申请商品
     */
    int insertRefundGoods(RefundGoodsList refundGoodsList);
    
    /**
     * 批量获取退货商品数量--不包含换货生成的退货
     */
    List<RefundNumberStatistics> selectPureRefundNumberStatistics(@Param("orderCode") Long orderCode, @Param("numList") List<Integer> numList);

}
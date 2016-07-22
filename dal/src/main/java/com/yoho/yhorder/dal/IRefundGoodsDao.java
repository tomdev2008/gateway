package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.RefundGoods;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IRefundGoodsDao {
    int deleteByPrimaryKey(Integer id);

    int insert(RefundGoods record);

    /**
     * 创建退货单
     *
     * @return
     */
    int insertRefund(RefundGoods refundGoods);

    RefundGoods selectByPrimaryKey(Integer id);

    /**
     * 根据erpRefundId获取退货记录
     *
     * @param erpRefundId
     */
    RefundGoods selectByErpRefundId(Integer erpRefundId);

    int updateByPrimaryKeySelective(RefundGoods refundGoods);

    int updateByPrimaryKey(RefundGoods record);

    /**
     * 通过id和uid获取退货记录
     */
    RefundGoods selectByIdAndUid(@Param("id") Integer id, @Param("uid") Integer uid);

    /**
     * 获取退货订单数量
     *
     * @param uid
     * @return
     */
    int selectCountByUid(Integer uid);


    /**
     * 根据用户id获取退货订单列表
     *
     * @param uid
     * @param offset
     * @param size
     * @return
     */
    List<RefundGoods> selectListByUid(@Param("uid") Integer uid, @Param("offset") Integer offset, @Param("size") Integer size);


    RefundGoods selectByUidAndChangePurchaseId(@Param("uid") int uid, @Param("changePurchaseId") int changePurchaseId);

    List<RefundGoods> selectByOrderCode(Long orderCode);
}
















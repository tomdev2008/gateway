package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.domain.ChangeGoods;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 换货
 *
 * @author CaoQi
 * @Time 2015/11/20
 */
public interface IChangeGoodsMapper {

    /**
     * 获取退换货的商品数量
     *
     * @param sourceOrderCode
     * @param productSku
     * @param goodsType
     * @return
     */
    int selectChangeGoodsNum(@Param("sourceOrderCode") String sourceOrderCode,
                             @Param("productSku") String productSku, @Param("goodsType") String goodsType);

    /**
     * 获取退换货的商品数量
     *
     * @return
     */
    List<Map<String, ?>> selectChangeGoodsNumBatch(@Param("paramsList") List<Map<String, String>> paramsList);


    List<ChangeGoods> selectChangeGoodsList(ChangeGoods changeGoods);

    int insert(List<ChangeGoods> changeGoods);

    //删除换货数据
    int updateDelFlagByChangeGoods(@Param("changePurchaseId") Integer changePurchaseId);

    /**
     * 根据 申请表 ID, 批量更新订单状态
     */
    int updateStatusByChangePurchaseId(@Param("changePurchaseId") int applyId, @Param("status") byte status);


}

package com.yoho.yhorder.dal;

import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.dal.model.OrderPromotionSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by wujiexiang on 16/4/22.
 */
public interface IOrderPromotionSnapshotDao {

    /**
     * 新增订单使用的促销
     * @param list
     * @return
     */
    int insertOrderPromotionSnapshot(@Param("list") List<OrderPromotionSnapshot>  list);

    /**
     * 查询订单使用的快照
     * @param orderCode
     * @return
     */
    List<PromotionInfo> selectOrderPromotionSnapshot(long orderCode);
}

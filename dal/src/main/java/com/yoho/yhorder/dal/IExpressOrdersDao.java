package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ExpressOrders;
import com.yoho.yhorder.dal.model.ExpressOrdersKey;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface IExpressOrdersDao {
    int deleteByPrimaryKey(ExpressOrdersKey key);

    int insert(ExpressOrders record);

    int insertSelective(ExpressOrders record);

    ExpressOrders selectByPrimaryKey(ExpressOrdersKey key);

    int updateByPrimaryKeySelective(ExpressOrders record);

    int updateByPrimaryKey(ExpressOrders record);

    /**
     * 根据快递单号和快递公司更新状态和次数
     *
     * @return
     */
    int updateExpressNum(@Param("expressNumber") String expressNumber, @Param("expressId")Integer expressId, @Param("flag") byte flag);
}
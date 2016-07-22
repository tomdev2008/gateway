package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.HistoryOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IHistoryOrderDao {
    int deleteByPrimaryKey(Integer id);

    int insert(HistoryOrder record);

    int insertSelective(HistoryOrder record);

    HistoryOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HistoryOrder record);

    int updateByPrimaryKeyWithBLOBs(HistoryOrder record);

    int updateByPrimaryKey(HistoryOrder record);

    int selectCountByUid(Integer uid);

    List<HistoryOrder> selectByUid(@Param("uid") Integer uid ,@Param("start") int start, @Param("limit") int limit);
}
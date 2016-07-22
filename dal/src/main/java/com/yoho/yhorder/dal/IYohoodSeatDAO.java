package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.YohoodSeat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IYohoodSeatDAO {
    int deleteByPrimaryKey(Integer id);

    int insert(YohoodSeat record);

    int insertSelective(YohoodSeat record);

    YohoodSeat selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(YohoodSeat record);

    int updateSeatToUse(YohoodSeat record);

    List<YohoodSeat> selectByTicketCodes(@Param("ticketCodes") List<Long> ticketCodes);
}
package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.YohoodTickets;
import com.yoho.yhorder.dal.model.YohoodTicketsKey;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface YohoodTicketsMapper {
    int deleteByPrimaryKey(YohoodTicketsKey key);

    int insert(YohoodTickets record);

    int insertSelective(YohoodTickets record);

    YohoodTickets selectByPrimaryKey(YohoodTicketsKey key);

    int updateByPrimaryKeySelective(YohoodTickets record);

    int updateByPrimaryKey(YohoodTickets record);

	YohoodTickets selectByUidAndDate(@Param("uid") Integer uid, @Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

	YohoodTickets selectByStatusAndTicketCode(@Param("status") Short status, @Param("ticketCode") Long ticketCode);

    int selectCountByOrderCode(@Param("orderCode") long orderCode);

    List<YohoodTickets> selectByOrderCode(@Param("orderCode") Long orderCode);
}
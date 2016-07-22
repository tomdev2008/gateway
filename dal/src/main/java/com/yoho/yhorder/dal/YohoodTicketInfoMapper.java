package com.yoho.yhorder.dal;

import org.apache.ibatis.annotations.Param;

import com.yoho.yhorder.dal.model.YohoodTicketInfo;

import java.util.List;

public interface YohoodTicketInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(YohoodTicketInfo record);

    int insertSelective(YohoodTicketInfo record);

    YohoodTicketInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(YohoodTicketInfo record);

    int updateByPrimaryKey(YohoodTicketInfo record);

	YohoodTicketInfo selectByStatusAndEmployCode(@Param("status") int status, @Param("employCode") String employCode);

    List<YohoodTicketInfo> selectByTicketCodes(@Param("ticketCodes") List<Long> ticketCodes);
}
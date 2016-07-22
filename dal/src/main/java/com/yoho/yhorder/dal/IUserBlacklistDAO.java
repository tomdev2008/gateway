package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.UserBlacklist;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by JXWU on 2016/1/27.
 * 黑名单
 */
public interface IUserBlacklistDAO {

    UserBlacklist selectByUid(@Param("uid") int uid);

    List<UserBlacklist> selectByUidAndIP(@Param("uid") int uid, @Param("ip") long ip);

}

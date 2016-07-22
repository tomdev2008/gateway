package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.YHPushCenterTempalte;
import org.apache.ibatis.annotations.Param;

/**
 * Created by JXWU on 2016/2/24.
 */
public interface IYHPushCenterTempalteDAO {

    YHPushCenterTempalte selectPushCenterTempalte(@Param("sceneName") String sceneName, @Param("pushType") String pushType);
}

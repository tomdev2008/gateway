package com.yoho.yhorder.dal;

import java.util.List;
import java.util.Map;

/**
 *  promotion dao
 * Created by chunhua.zhang@yoho.cn on 2015/12/17.
 */
public interface IPromotionDAO {


    /**
     * 查询所有活跃，可用的促销信息
     * @return
     */
    List<Map> selectActivePromotions();
}

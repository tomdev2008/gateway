package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.OrderCodeQueue;

/**
 * Created by JXWU on 2015/11/30.
 */
public interface IOrderCodeQueueDAO {

    /**
     * 插入uid，获取自增主键
     *
     * @param orderCodeQueue
     * @return
     */
    int insertUid(OrderCodeQueue orderCodeQueue);

    Integer selectUidById(Integer id);
}

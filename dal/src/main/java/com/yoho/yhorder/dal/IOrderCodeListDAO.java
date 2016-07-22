package com.yoho.yhorder.dal;

/**
 * Created by JXWU on 2015/11/30.
 */
public interface IOrderCodeListDAO {
    /**
     * 获取订单code
     *
     * @param id
     * @return
     */
    Long selectOrderCodeById(Long id);

    Integer selectIdByOrderCode(Long orderCode);

}

package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ShoppingTag;
import org.apache.ibatis.annotations.Param;

/**
 * Created by JXWU on 2015/11/19.
 */
public interface IShoppingTagDAO {

    /**
     * 添加购物车校验key
     */
    public void insertShoppingTag(ShoppingTag shoppingTag);


    public ShoppingTag selectShoppingTag(@Param("uid") Integer uid, @Param("shoppingTagKey") String shoppingTagKey);


    public int updateShoppingTagToUse(@Param("uid") Integer uid);
}

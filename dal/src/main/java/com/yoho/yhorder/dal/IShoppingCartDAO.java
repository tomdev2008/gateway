package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ShoppingCart;
import org.apache.ibatis.annotations.Param;

/**
 * Created by JXWU on 2015/11/17.
 */
public interface IShoppingCartDAO {

    public ShoppingCart selectShoppingCartById(@Param("id") Integer id, @Param("uid") Integer uid);

    public ShoppingCart selectShoppingCartByUid(Integer uid);

    public ShoppingCart selectShoppingCartByShoppingKey(@Param("shoppingKey") String shoppingKey, @Param("uid") Integer uid);

    public void insertShoppingCart(ShoppingCart shoppingCart);

    /**
     * 删除购物车
     * 已知场景：合并时删除临时购物车
     *
     * @param id
     * @param uid
     * @return
     */
    int delShoppingCartById(@Param("id") Integer id, @Param("uid") Integer uid);

    @Deprecated
    int setUidByShoppingKey(ShoppingCart shoppingCart);

    int setShoppingKeyById(ShoppingCart shoppingCart);
}

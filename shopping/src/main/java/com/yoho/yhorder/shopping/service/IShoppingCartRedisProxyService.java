/*
 * Copyright (C), 2016-2016, yoho
 * FileName: IShoppingCartRedisProxyService.java
 * Author:   maelk_liu
 * Date:     2016年4月26日 下午12:23:55
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.shopping.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.yoho.yhorder.dal.IShoppingCartDAO;
import com.yoho.yhorder.dal.IShoppingCartItemsDAO;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.dal.model.ShoppingCartItems;

/**
 * 代理类：主要代理对象：
 * 
 * IShoppingCartDAO
 * IShoppingCartItemsDAO
 * 
 * @author maelk_liu
 */
public interface IShoppingCartRedisProxyService{
    
    /**
     * 所有IShoppingCartDAO,IShoppingCartItemsDAO接口新增的方法，必须在IShoppingCartRedisProxyService重新申明一遍!
     * 
     * 后面可能采用字节码Javasist操作 ，优化。目前先采用这种最粗暴的做法
     * 
     */

    /**
     * begin copy method from 
     * {@link IShoppingCartDAO}
     */
    public ShoppingCart selectShoppingCartByUid(Integer uid);

    public ShoppingCart selectShoppingCartByShoppingKey(String shoppingKey);

    public void insertShoppingCart(ShoppingCart shoppingCart);
    
    int delShoppingCartById(Integer id);
    
    int setUidByShoppingKey(ShoppingCart shoppingCart);
    
    int setShoppingKeyById(ShoppingCart shoppingCart);
    /**
     * end copy method from 
     * {@link IShoppingCartDAO}
     */
    //***********************************************************************************************************
    
    /**
     * begin copy method from 
     * {@link IShoppingCartItemsDAO}
     */
    List<ShoppingCartItems> selectByCartId(Integer shoppingCartId);

    int changeCart(ShoppingCartItems record);

    int batchUpdateStatusTo_0(Map<String, String> map);

    /**
     * 更新购物车中的记录状态为0
     *
     * @param shoppingCartId
     * @param uid
     * @param promotionId
     * @param rowNum
     * @param status         记录状态，1有效 0无效
     * @return
     */
    int updateCartGoodsByPromotionID(@Param("shopping_cart_id") String shoppingCartId, @Param("uid") String uid,
                                     @Param("promotion_id") String promotionId, @Param("row_num") int rowNum,
                                     @Param("status") String status);


    /**
     * 更新购物车中的记录状态为0
     *
     * @param shoppingCartId
     * @param  skuId
     * @param status         记录状态，1有效 0无效
     * @return upload
     */
    int updateCartGoodsBySKU(@Param("shopping_cart_id") int shoppingCartId,    @Param("sku_id") int skuId,
                                     @Param("status") int status,@Param("promotionId") Integer promotionId);
    /**
     * 商品种类个数
     *
     * @param shoppingCartId
     * @return
     */
    int countShoppingCartGoods(Integer shoppingCartId);

    /**
     * SKU商品数量
     *
     * @param shoppingCartId
     * @param productSku
     * @param promotionId
     * @return
     */
    int sumShoppingCartGoodsByProductSku(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku, @Param("promotionId") Integer promotionId);

    /**
     * 商品总量
     *
     * @param shoppingCartId
     * @return
     */
    int sumShoppingCartGoods(Integer shoppingCartId);

    /**
     * 商品总量,如果传入了UID 需要根据UID来count
     *
     * @param shoppingCartId
     * @return
     */
    int sumShoppingCartGoodsByUid(@Param(value="shoppingCartId")int shoppingCartId, @Param(value="uid") int uid);


    /**
     * 插入商品
     *
     * @param uid
     * @param shoppingCartId
     * @param productSkn
     * @param productSku
     * @param buyNumber
     * @param promotionId
     * @param selected
     * @param status
     */
    int insertShoppingCartGoods(@Param("uid") Integer uid, @Param("shoppingCartId") Integer shoppingCartId, @Param("productSkn") Integer productSkn, @Param("productSku") Integer productSku, @Param("buyNumber") Integer buyNumber, @Param("promotionId") Integer promotionId, @Param("selected") String selected, @Param("status") int status);


    /**
     * 更新商品数量
     *
     * @param productSku
     * @param shoppingCartId
     * @param buyNumber
     */
    int updateShoppingCartGoodsNumber(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku, @Param("buyNumber") Integer buyNumber,@Param("promotionId") Integer promotionId);


    List<ShoppingCartItems> selectShoppingCartGoods(@Param("shoppingCartId") Integer shoppingCartId, @Param("selected") String selected);

    /**
     * 获取sku数量
     *
     * @param shoppingCartId
     * @param productSku
     * @return
     */
    int countShoppingCartGoodsByProductSku(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku,@Param("promotionId") Integer promotionId);

    /**
     * 逻辑删除，更改status状态
     *
     * @param shoppingCartId
     * @param productSku
     */
    int updateShoppingCartGoodsStatusByProductSku(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku,@Param("promotionId") Integer promotionId);


    int updateShoppingCartGoodsWithOld(@Param("shoppingCartId") Integer shoppingCartId, @Param("newProductSku") Integer newProductSku, @Param("oldProductSku") Integer oldProductSku, @Param("buyNumber") Integer buyNumber);

    /**
     * 更新商品选中状态
     *
     * @param shoppingCartId
     * @param productSku
     * @param selected
     */
    int updateShoppingCartGoodsSelectedStatus(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku, @Param("selected") String selected);


    /**
     * 逻辑商品
     *
     * @param uid
     * @param deleteIds
     * @return
     */
    int updateShoppingCartGoodsStatusByCartID(@Param("uid") Integer uid, @Param("deleteIds") String deleteIds);


    /**
     * SKU商品数量
     * @param shoppingCartId
     * @param productSku
     * @param promotionId 可能为null
     * @return
     */
    int sumGoodsBuyNumberByProductSku(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku,@Param("promotionId") Integer promotionId);


    /**
     * 减少商品数量
     * @param shoppingCartId
     * @param productSku
     * @param buyNumber
     * @param promotionId 可能为null
     * @return
     */
    int decreaseShoppingCartGoodsNumber(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku, @Param("buyNumber") Integer buyNumber,@Param("promotionId") Integer promotionId);


    /**
     * 批量更新商品选中状态
     */
    int batchUpdateShoppingCartGoodsSelectedStatus(@Param("list") List<ShoppingCartItems> list);


    /**
     * 批量插入
     * @param list
     * @return
     */
    int batchInsertShoppingCartItems(@Param("list") List<ShoppingCartItems> list);

    /**
     * 逻辑删除，更改status状态
     * @param list
     * @return
     */
    int disableStatusBySkuAndPromotionId(@Param("list") List<ShoppingCartItems> list);


    List<ShoppingCartItems> selectItemsBySkuAndPromotionId(@Param("list") List<ShoppingCartItems> list);

    /**
     * 批量更新商品购买数量
     * @param list
     */
    int updateNumInBatch(@Param("list") List<ShoppingCartItems> list);


    /**
     * 设置shoppingCartId的购物车列表item的uid为uid
     * @param shoppingCartId
     * @param uid
     * @return
     */
    int updateCartGoodsUserIdById(@Param(value="shoppingCartId")int shoppingCartId, @Param(value="uid") int uid);

    /**
     * end copy method from 
     * {@link IShoppingCartItemsDAO}
     */
    //******************************************************************************************************************************
    //下面是其他method
    
    
}

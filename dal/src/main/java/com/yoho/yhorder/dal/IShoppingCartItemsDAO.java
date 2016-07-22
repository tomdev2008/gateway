package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ShoppingCartItems;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IShoppingCartItemsDAO {

    List<ShoppingCartItems> selectByCartId(@Param("shoppingCartId") Integer shoppingCartId, @Param("uid") Integer uid);

    @Deprecated
    int changeCart(ShoppingCartItems record);

    /**
     * 商品种类个数
     *
     * @param shoppingCartId
     * @return
     */
    int countShoppingCartGoods(@Param("shoppingCartId") Integer shoppingCartId, @Param("uid") Integer uid);

    /**
     * SKU商品数量
     *
     * @param shoppingCartId
     * @param productSku
     * @param promotionId
     * @return
     */
    int sumShoppingCartGoodsByProductSku(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku, @Param("promotionId") Integer promotionId, @Param("uid") Integer uid);

    /**
     * 商品总量
     *
     * @param shoppingCartId
     * @return
     */
    int sumShoppingCartGoods(@Param("shoppingCartId") Integer shoppingCartId, @Param("uid") Integer uid);


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
    int insertShoppingCartGoods(@Param("uid") Integer uid, @Param("shoppingCartId") Integer shoppingCartId, @Param("productSkn") Integer productSkn,
                                @Param("productSku") Integer productSku, @Param("buyNumber") Integer buyNumber, @Param("promotionId") Integer promotionId,
                                @Param("selected") String selected, @Param("status") int status,@Param("shoppingKey") String shoppingKey);


    /**
     * 更新商品数量 buyNumber=buyNumber_old+buyNumber_add
     *
     * @param productSku
     * @param shoppingCartId
     * @param buyNumber
     */
    int updateShoppingCartGoodsNumber(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku,
                                      @Param("buyNumber") Integer buyNumber, @Param("promotionId") Integer promotionId, @Param("uid") Integer uid);


    List<ShoppingCartItems> selectShoppingCartGoods(@Param("shoppingCartId") Integer shoppingCartId, @Param("selected") String selected, @Param("uid") Integer uid);

    /**
     * 获取sku数量
     *
     * @param shoppingCartId
     * @param productSku
     * @return
     */
    int countShoppingCartGoodsByProductSku(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku, @Param("promotionId") Integer promotionId, @Param("uid") Integer uid);


    int updateShoppingCartGoodsWithOld(@Param("shoppingCartId") Integer shoppingCartId, @Param("newProductSku") Integer newProductSku, @Param("oldProductSku") Integer oldProductSku,
                                       @Param("buyNumber") Integer buyNumber, @Param("uid") Integer uid);

    /**
     * 减少商品数量
     *
     * @param shoppingCartId
     * @param productSku
     * @param buyNumber
     * @param promotionId    可能为null
     * @return
     */
    int decreaseShoppingCartGoodsNumber(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku,
                                        @Param("buyNumber") Integer buyNumber, @Param("promotionId") Integer promotionId, @Param("uid") Integer uid);


    //TODO uid使用ShoppingCartItems中的行不行
    /**
     * 批量更新商品选中状态
     */
    int batchUpdateShoppingCartGoodsSelectedStatus(@Param("list") List<ShoppingCartItems> list, @Param("uid") Integer uid);


    /**
     * 批量插入
     *
     * @param list
     * @return
     */
    int batchInsertShoppingCartItems(@Param("list") List<ShoppingCartItems> list);


    List<ShoppingCartItems> selectItemsBySkuAndPromotionId(@Param("list") List<ShoppingCartItems> list, @Param("uid") Integer uid);

    //TODO 需不需要uid
    /**
     * 批量更新商品购买数量
     * ShoppingCartItems里面有uid
     *
     * @param list
     */
    int updateNumInBatch(@Param("list") List<ShoppingCartItems> list,@Param("uid") Integer uid);


    /**
     * 批量删除购物车items
     *
     * @param list
     */
    int deleteItemsBySkuAndPromotionId(@Param("list") List<ShoppingCartItems> list,@Param("uid") Integer uid);


    /**
     * 物理删除
     *
     * @param shoppingCartId
     * @param productSku
     */
    int deleteShoppingCartGoodsByProductSku(@Param("shoppingCartId") Integer shoppingCartId, @Param("productSku") Integer productSku,
                                            @Param("promotionId") Integer promotionId, @Param("uid") Integer uid);


    /**
     * 提交购物车时删除items中商品
     *
     * @param uid
     * @param deleteIds
     * @return
     */
    int deleteShoppingCartGoodsByCartID(@Param("uid") Integer uid, @Param("deleteIds") String deleteIds);

    int deleteCartGoodsBySKU(@Param("shopping_cart_id") int shoppingCartId, @Param("sku_id") int skuId,
                             @Param("promotionId") Integer promotionId, @Param("uid") Integer uid);


    /**
     * 删除购物车中的多余促销商品
     *
     * @param shoppingCartId
     * @param uid
     * @param promotionId
     * @param rowNum
     * @return
     */
    int deleteCartGoodsByPromotionID(@Param("shopping_cart_id") String shoppingCartId, @Param("uid") String uid,
                                     @Param("promotion_id") String promotionId, @Param("row_num") int rowNum);


    /**
     * 设置shoppingCartId的购物车列表item的uid为uid
     * 不再使用!!!!!!!!!!!!!!!!!!!!!
     * @param shoppingCartId
     * @param uid
     * @return
     */
    @Deprecated
    int updateCartGoodsUserIdById(@Param(value = "shoppingCartId") int shoppingCartId, @Param(value = "uid") int uid);


    int batchDeleteByIds(Map<String, String> map);


    /**
     * 删除购物车内商品
     * 已知场景：合并时删除临时购物车
     * @param shoppingCartId
     * @param uid
     * @return
     */
    int deleteShoppingCartItemsByCartId(@Param(value = "shoppingCartId") int shoppingCartId, @Param(value = "uid") int uid);
}
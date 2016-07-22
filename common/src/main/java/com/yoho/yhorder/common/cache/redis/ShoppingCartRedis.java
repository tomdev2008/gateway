/*
 * Copyright (C), 2016-2016, yoho
 * FileName: ShoppingCartRedis.java
 * Author:   maelk_liu
 * Date:     2016年4月26日 下午2:22:13
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <description>
 * 修改人姓名             修改时间            版本号                  描述
 */
package com.yoho.yhorder.common.cache.redis;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yoho.core.redis.YHHashOperations;
import com.yoho.core.redis.YHRedisTemplate;
import com.yoho.core.redis.YHValueOperations;

import javax.annotation.Resource;


/**
 * 购物车shoppingCart以及shopingCartItem表之redis操作类
 * 购物车计算结果redis缓存
 *
 * @author maelk_liu
 */
@Component
public class ShoppingCartRedis {

    private Logger logger = LoggerFactory.getLogger(ShoppingCartRedis.class);

    @Autowired
    YHHashOperations<String, String, String> hashOperations;

    @Autowired
    YHValueOperations<String, String> valueOperations;

    @Resource(name = "yhRedisTemplate")
    YHRedisTemplate<String, String> redisTemplate;

    /**
     * uid与shopping_key的缓存前缀,后缀为uid  表：shopping_cart
     * 1对1
     */
    public final static String SHOPPING_CART_UID_AND_SHOPPINGKEY_PREFIX_KEY = "yh:order:shoppingCart:uid:";

    /**
     * shopping_key与表shopping_cart记录的缓存前缀,后缀为shopping_key
     * 1对1
     */
    public final static String SHOPPING_CART_SHOPPINGKEY_PREFIX_KEY = "yh:order:shoppingCart:shoppingkey:";

    /**
     * shoppingCartId与  表 ：shopping_cart_items   记录前缀.后缀为shoppingCartId
     */
    public final static String SHOPPING_CART_ITEMS_SHOPPINGCARTID_PREFIX_KEY = "yh:order:shoppingCartItems:shoppingCartId:";
    
    /**
     * 购物车列表缓存key:根据uid查
     */
    public final static String SHOPPONH_CART_COMPUTE_RESULT_PREFIX_UID_KEY = "yh:order:shoppingCartComputeResult:uid:";
    
    /**
     * 购物车列表缓存key:根据shopping-key查
     */
    public final static String SHOPPONH_CART_COMPUTE_RESULT_PREFIX_SHOPKEY_KEY = "yh:order:shoppingCartComputeResult:shoppingkey:";
    
    /**
     * 根据Key获取redis中的值
     * 
     * @param key 
     * @param type   0表示shoppingkey  1表示uid
     */
    public String getShoppingQueryResponse(String key,int type){
        String redisKey;
        String result=null;
        //0表示根据shoppingkey查
        if(type==0){
            redisKey = SHOPPONH_CART_COMPUTE_RESULT_PREFIX_SHOPKEY_KEY+key;
            try {
                result = redisTemplate.opsForValue().get(redisKey);
            } catch (Exception e) {
                logger.warn("get redis result fail,key is : {}",redisKey,e);
                return null;
            }
            return result;
        }
        redisKey = SHOPPONH_CART_COMPUTE_RESULT_PREFIX_UID_KEY+key;
        try {
            result =valueOperations.get(redisKey);
        } catch (Exception e) {
            logger.warn("get redis result fail,key is : {}",redisKey,e);
            return null;
        }
        return result;
    }
    
    /**
     * 设置redis中的值:shoppingQueryResult
     * 
     * @param key 
     * @param type   0表示shoppingkey  1表示uid
     * @param shoppingQueryResult 
     */
    public void setShoppingQueryResponse(String key,int type,String shoppingQueryResult){
        String redisKey;
        //0表示根据shoppingkey查
        if(type==0){
            redisKey = SHOPPONH_CART_COMPUTE_RESULT_PREFIX_SHOPKEY_KEY+key;
            try {
                valueOperations.set(redisKey, shoppingQueryResult);
                redisTemplate.longExpire(redisKey, 2, TimeUnit.MINUTES);
            } catch (Exception e) {
                try {
                    redisTemplate.delete(redisKey);
                    logger.info("seldom face ,rediskey is{}",redisKey);
                } catch (Exception e1) {
                    logger.warn("fatal warn: redis key is {}",redisKey);
                }
                logger.info("set redis result,key is :"+redisKey+" fail {}",e);
            }
            return;
        }
        redisKey = SHOPPONH_CART_COMPUTE_RESULT_PREFIX_UID_KEY+key;
        try {
            valueOperations.set(redisKey, shoppingQueryResult);
            redisTemplate.longExpire(redisKey, 2, TimeUnit.MINUTES);
        } catch (Exception e) {
            try {
                redisTemplate.delete(redisKey);
                logger.info("seldom face ,rediskey is",redisKey);
            } catch (Exception e1) {
                logger.warn("fatal warn: redis key is {}",redisKey);
            }
            logger.info("set redis result,key is :"+redisKey+" fail {}",e);
        }
    }
    
    /**
     * 根据Key清除redis值
     * 
     * @param uid 
     * @param shoppingKey  
     */
    public void killShoppingQueryResponse(int uid,String shoppingKey){
        if(uid>0){
            try {
                redisTemplate.delete(SHOPPONH_CART_COMPUTE_RESULT_PREFIX_UID_KEY+uid);
            } catch (Exception e) {
                logger.warn("delete"+SHOPPONH_CART_COMPUTE_RESULT_PREFIX_UID_KEY+uid+" fail");
            }  
        }
        if(shoppingKey!=null&&!shoppingKey.equals("")){
            try {
                redisTemplate.delete(SHOPPONH_CART_COMPUTE_RESULT_PREFIX_SHOPKEY_KEY+shoppingKey);
            } catch (Exception e) {
                logger.warn("delete"+SHOPPONH_CART_COMPUTE_RESULT_PREFIX_SHOPKEY_KEY+shoppingKey+" fail");
            }
        }
    }
    
    /**
     * 根据Uid获取shopping_key
     * @param uid
     */
    public String getShoppingKeyByUid(String uid){
        String key = SHOPPING_CART_UID_AND_SHOPPINGKEY_PREFIX_KEY+uid;
        String result=valueOperations.get(key);
        return result;
    }
    
    /**
     * 根据Uid往redis中塞值ShoppingKey
     *
     * @param uid
     * @param ShoppingKey
     */
    public void setShoppingKeyByUid(String uid,String ShoppingKey){
        String key = SHOPPING_CART_UID_AND_SHOPPINGKEY_PREFIX_KEY+uid;
        valueOperations.setIfAbsent(key, ShoppingKey);
        redisTemplate.longExpire(key, 10, TimeUnit.MINUTES);
    }
    
    /**
     * 根据shopping_key获得
     * @param uid
     */
    public String getShoppingCartByKey(String shopping_key){
        String key = SHOPPING_CART_SHOPPINGKEY_PREFIX_KEY+shopping_key;
        String result=valueOperations.get(key);
        return result;
    }
    
    /**
     * 根据ShoppingKey往redis中塞值ShoppingCart
     *
     * @param uid
     * @param ShoppingKey
     */
    public void setShoppingCartByShoppingKey(String ShoppingKey,String ShoppingCart){
        String key = SHOPPING_CART_UID_AND_SHOPPINGKEY_PREFIX_KEY+ShoppingKey;
        valueOperations.setIfAbsent(key, ShoppingCart);
        redisTemplate.longExpire(key, 10, TimeUnit.MINUTES);
    }
    
    /**
     * 根据cartId以及ItemId获得单个ShoppingCartItem记录
     * 
     * @param cartId
     * @param ItemId
     */
    public String getSingleShoppingCartItemByCartIdAndItemId(String cartId,String ItemId){
        String key =SHOPPING_CART_ITEMS_SHOPPINGCARTID_PREFIX_KEY+cartId;
        return getHashField(key, ItemId);
    }
    
    /**
     * 根据cartId以及ItemId,shoppingCartItem往redis塞值
     * 
     * @param cartId
     * @param ItemId
     * @param shoppingCartItem
     */
    public void setSingleShoppingCartItemByCartIdAndItemId(String cartId,String ItemId,String shoppingCartItem){
        String key =SHOPPING_CART_ITEMS_SHOPPINGCARTID_PREFIX_KEY+cartId;
        putHashField(key, ItemId, shoppingCartItem);
        redisTemplate.longExpire(key, 10, TimeUnit.MINUTES);
    }
    
    /**
     * 根据cartId获得所有ShoppingCartItem记录
     * 
     * @param cartId
     * @param ItemId
     */
    public Map<String,String>getAllShoppingCartItemByCartIdAndItemId(String cartId){
        String key =SHOPPING_CART_ITEMS_SHOPPINGCARTID_PREFIX_KEY+cartId;
        return getAllFieldsMap(key);
    }
    
    /**
     * 批量塞值
     * 
     * @param cartId
     */
    public void setAllShoppingCartItemByCartIdAndMap(String cartId,Map<String,String>map){
        String key =SHOPPING_CART_ITEMS_SHOPPINGCARTID_PREFIX_KEY+cartId;
        hashOperations.putAll(key, map);
        redisTemplate.longExpire(key, 10, TimeUnit.MINUTES);
    }

    private String getHashField(String key, String fieldKey) {
        try {
            return hashOperations.get(key, fieldKey);
        } catch (Exception ex) {
            logger.warn("call redis hash get error,key is {},fieldKey is {}", key, fieldKey, ex);
        }
        return null;
    }

    private void putHashField(String key, String fieldKey, String fieldValue) {
        try {
            hashOperations.put(key, fieldKey, fieldValue);
        } catch (Exception ex) {
            logger.warn("call redis hash put error,key is {},fieldKey is {},fieldValue is {}", key, fieldKey, fieldValue, ex);
        }
    }

    private Map<String, String> getAllFieldsMap(String key) {
        try {
            return hashOperations.entries(key);
        } catch (Exception ex) {
            logger.warn("call redis hash entries error,key is {}", key, ex);
            return null;
        }
    }
}

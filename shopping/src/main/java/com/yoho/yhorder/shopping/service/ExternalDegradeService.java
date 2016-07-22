package com.yoho.yhorder.shopping.service;

import com.alibaba.fastjson.JSONArray;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.core.rest.client.hystrix.AsyncFuture;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.client.invoker.ProductServiceHandler;
import com.yoho.product.model.ProductGiftBo;
import com.yoho.product.model.StorageBo;
import com.yoho.product.model.promotion.AddCostProductBo;
import com.yoho.product.model.shopcart.ProductShopCartBo;
import com.yoho.product.request.AddCostProductRequest;
import com.yoho.product.request.BatchBaseRequest;
import com.yoho.service.model.promotion.ProductBuyLimitBo;
import com.yoho.service.model.promotion.UserCouponsListBO;
import com.yoho.service.model.promotion.request.PromotionBuyLimitReq;
import com.yoho.service.model.promotion.request.UserCouponDetailListReq;
import com.yoho.service.model.request.RedEnvelopesReqBO;
import com.yoho.service.model.request.UserAddressReqBO;
import com.yoho.service.model.request.UserVipReqBO;
import com.yoho.service.model.request.YohoCoinLogReqBO;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.service.model.response.YohoCoin;
import com.yoho.service.model.vip.VipInfo;
import com.yoho.service.model.vip.VipLevel;
import com.yoho.yhorder.common.cache.redis.CacheEnum;
import com.yoho.yhorder.common.cache.redis.RedisValueOperation;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.IUserBlacklistDAO;
import com.yoho.yhorder.dal.model.UserBlacklist;
import com.yoho.yhorder.shopping.model.OrderYohoCoin;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import com.yoho.yhorder.shopping.utils.VIPEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by wujiexiang on 16/6/29.
 * 支持降级
 */
@Component
public class ExternalDegradeService {

    private final Logger logger = LoggerFactory.getLogger("externalCallLog");

    //商品信息查询降级开关
    @Value("${degrade.product.query.enable:false}")
    private boolean queryProductInfoDegreadeEnable = false;

    //商品限购数量降级开关
    @Value("${degrade.buylimit.query.enable:false}")
    private boolean querySkuBuyLimitDegreadeEnable = false;

    //用户vip降级开关key
    @Value("${degrade.vip.query.enable:false}")
    private boolean queryUserVipDegreadeEnable = false;

    //yoho币降级开关
    @Value("${degrade.yohocoin.query.enable:false}")
    private boolean queryUserYohoCoinDegradeEnable = false;

    //红包降级开关
    @Value("${degrade.redenvelopes.query.enable:false}")
    private boolean queryUserRedenvelopesDegradeEnable = false;

    //获取用户未使用的限购码降级开关
    @Value("${degrade.coupons.queryNotUseCoupons.enable:false}")
    private boolean queryNotUseCouponsDegreadeEnable = false;

    //获取用户地址降级开关
    @Value("${degrade.address.query.enable:false}")
    private boolean queryUserAddressDegreadeEnable = false;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private ExternalService externalService;

    @Autowired
    private RedisValueOperation redisValueOperation;

    @Autowired
    private ProductServiceHandler productServiceHandler;


    @Autowired
    private IUserBlacklistDAO userBlacklistDAO;


    @Autowired
    protected IOrdersMapper ordersMapper;

    /**
     * 查询商品限购
     *
     * @param skns
     * @return
     */
    public ProductBuyLimitBo[] queryProductBuyLimitBySknids(List<Integer> skns) {
        String serviceName = ShoppingConfig.PROMOTION_QUERY_PRODUCTBUYLIMIT_BYSKNIDS_REST_URL;

        logger.info("call service {} to query skn buy limit,skn {},degrade enable {}", serviceName, skns, querySkuBuyLimitDegreadeEnable);

        PromotionBuyLimitReq promotionBuyLimitReq = new PromotionBuyLimitReq();
        promotionBuyLimitReq.setProductSkn(new ArrayList<>(skns));

        ProductBuyLimitBo[] result = null;

        if (querySkuBuyLimitDegreadeEnable) {
            ProductBuyLimitBo[] defaultValue = new ProductBuyLimitBo[0];
            result = serviceCaller.call(serviceName, promotionBuyLimitReq, ProductBuyLimitBo[].class, defaultValue);
        } else {
            serviceCaller.call(serviceName, promotionBuyLimitReq, ProductBuyLimitBo[].class);
        }
        logger.info("query skn buy limit,result size is {}", (result != null ? result.length : 0));
        return result;
    }


    /**
     * 获取用户红包
     *
     * @param uid
     * @return
     */
    public double queryUserRedEnvelopes(int uid) {
        logger.info("call service {} to query user Red Envelopes for uid {},degrade enable {}", ShoppingConfig.USERS_QUERY_USER_REDENVELOPESCOUNT_REST_URL, uid, queryUserRedenvelopesDegradeEnable);
        RedEnvelopesReqBO reqBO = new RedEnvelopesReqBO();
        reqBO.setUid(uid);
        BigDecimal result = null;
        if (queryUserRedenvelopesDegradeEnable) {
            //降级
            BigDecimal zero = BigDecimal.ZERO;
            result = serviceCaller.call(ShoppingConfig.USERS_QUERY_USER_REDENVELOPESCOUNT_REST_URL, reqBO, BigDecimal.class, zero);
        } else {
            result = serviceCaller.call(ShoppingConfig.USERS_QUERY_USER_REDENVELOPESCOUNT_REST_URL, reqBO, BigDecimal.class);
        }

        return result == null ? 0 : result.doubleValue();
    }


    /**
     * 查询可用用户yoho币,过滤掉logid=0的记录
     * 支持降级
     *
     * @param uid
     * @return
     */
    public OrderYohoCoin queryUsableYohoCoin(int uid) {
        logger.info("call service {} to query user usable yoho coin for uid {},degrade enable {}", ShoppingConfig.USERS_QUERY_USERYOHO_FILTERLOGID_REST_URL, uid, queryUserYohoCoinDegradeEnable);

        YohoCoinLogReqBO userYohoCoinReqBO = new YohoCoinLogReqBO();
        userYohoCoinReqBO.setUid(uid);

        YohoCoin yohoCoin = null;
        if (queryUserYohoCoinDegradeEnable) {
            YohoCoin defaultValue = new YohoCoin();
            defaultValue.setCoinNum(0);
            yohoCoin = serviceCaller.call(ShoppingConfig.USERS_QUERY_USERYOHO_FILTERLOGID_REST_URL, userYohoCoinReqBO, YohoCoin.class, defaultValue);
        } else {
            yohoCoin = serviceCaller.call(ShoppingConfig.USERS_QUERY_USERYOHO_FILTERLOGID_REST_URL, userYohoCoinReqBO, YohoCoin.class);
        }

        OrderYohoCoin orderYohoCoin = new OrderYohoCoin();
        if (yohoCoin != null) {
            if (yohoCoin.getCoinNum() != null) {
                orderYohoCoin.setYohoCoinNum(yohoCoin.getCoinNum());
            }
            orderYohoCoin.setCoinUnit(yohoCoin.getCoinUnit());
        }
        logger.info("result is {}", orderYohoCoin);
        return orderYohoCoin;
    }


    /**
     * 查询vip信息
     *
     * @param uid
     * @return
     */
    public VipInfo queryVipDetailInfo(int uid) throws ServiceException {
        logger.info("call service {} to query user vip for uid {},degrade enable {}",
                ShoppingConfig.USERS_QUERY_VIP_LEVEL_REST_URL, uid, queryUserVipDegreadeEnable);
        UserVipReqBO reqBO = new UserVipReqBO();
        reqBO.setUid(uid);

        try {
            return serviceCaller.call(ShoppingConfig.USERS_QUERY_VIP_LEVEL_REST_URL, reqBO, VipInfo.class);
        } catch (Exception ex) {
            if (queryUserVipDegreadeEnable) {
                logger.warn("start degradation policy to query user vip from local,uid is {}", uid);
                /**
                 * vip 等级很重要,涉及到vip折扣,
                 * 若获取vip等级的服务超时了,取本地的缓存,
                 *    本地缓存没有,默认普通会员等级
                 */
                VipInfo vipInfo = getLocalVipInfo(uid);
                logger.info("get local vip level,result is {}", vipInfo.getCurVipInfo().getCurLevel());
                return vipInfo;
            }

            throw ex;
        }
    }


    /**
     * 异步获取用户未使用的限购码
     * @param uid
     * @return
     */
    public AsyncFuture<UserCouponsListBO> createQueryUserNotUseCouponsAsyncFuture(int uid) {
        logger.info("call service {} to query user not use coupons for uid {},degrade enable {}",
                ShoppingConfig.PROMOTION_QUERY_USERNOUSEDCOUPONS_REST_URL, uid, queryNotUseCouponsDegreadeEnable);

        UserCouponDetailListReq userCouponDetailListReq = new UserCouponDetailListReq();
        userCouponDetailListReq.setUid(uid);

        AsyncFuture<UserCouponsListBO> asyncFuture = null;

        if (queryNotUseCouponsDegreadeEnable) {
            UserCouponsListBO defaultValue = new UserCouponsListBO();
            asyncFuture = serviceCaller.asyncCall(ShoppingConfig.PROMOTION_QUERY_USERNOUSEDCOUPONS_REST_URL,
                    userCouponDetailListReq, UserCouponsListBO.class, defaultValue);
        } else {
            asyncFuture = serviceCaller.asyncCall(ShoppingConfig.PROMOTION_QUERY_USERNOUSEDCOUPONS_REST_URL,
                    userCouponDetailListReq, UserCouponsListBO.class);
        }

        logger.info("create query user coupon asyncFuture success,request is {},asyncFuture is {}", userCouponDetailListReq, asyncFuture);

        return asyncFuture;
    }


    /**
     * 批量查询sku信息，包括库存、价格等
     *
     * @param skus
     * @return
     */
    public ProductShopCartBo[] queryProductShoppingCartBySkuids(List<Integer> skus) {
        logger.info("query product info,degrade enable {}", queryProductInfoDegreadeEnable);
        if (queryProductInfoDegreadeEnable) {
            List<ProductShopCartBo> list = productServiceHandler.queryProductShopCartBySkuIds(skus);
            return listToArray(list,ProductShopCartBo.class);
        } else {
            return externalService.queryProductShoppingCartBySkuids(skus);
        }
    }

    /**
     * 查询赠品和加价购商品信息
     * @param skns
     * @return
     */
    public AddCostProductBo[] queryAddCostProducts(List<Integer> skns) {
        if (queryProductInfoDegreadeEnable) {
            List<AddCostProductBo> list = productServiceHandler.queryAddCostProducts(skns);
            return listToArray(list, AddCostProductBo.class);
        } else {
            AddCostProductRequest addCostProductRequest = new AddCostProductRequest();
            addCostProductRequest.setProductSknIds(skns);
            return serviceCaller.call("product.queryAddCostProducts", addCostProductRequest, AddCostProductBo[].class);
        }
    }

    /**
     * 赠品信息
     * @param skns
     * @return
     */
    public ProductGiftBo[] queryProductGiftBySkns(List<Integer> skns) {
        if (queryProductInfoDegreadeEnable) {
            List<ProductGiftBo> list = productServiceHandler.queryProductGiftBySkns(skns);
            return listToArray(list, ProductGiftBo.class);
        } else {
            BatchBaseRequest<Integer> request = new BatchBaseRequest<>();
            request.setParams(skns);
            return serviceCaller.call("product.queryProductGiftBySkns", request, ProductGiftBo[].class);
        }
    }


    /**
     * 批量查询库存
     * @param skuList
     * @return
     */
    public StorageBo[] queryStorageBySkuIds(List<Integer> skuList) {

        if (queryProductInfoDegreadeEnable) {
            List<StorageBo> list = productServiceHandler.queryStorageBySkuIds(skuList);
            return listToArray(list, StorageBo.class);
        } else {
            //批量查询商品库存
            BatchBaseRequest<Integer> request = new BatchBaseRequest<>();
            request.setParams(skuList);
            return serviceCaller.call(ShoppingConfig.PRODUCT_BATCHQUERY_STORAGEBYSKUIDS_REST_URL, request, StorageBo[].class);
        }
    }

    /**
     * 查询用户黑名单 老erp可以拉黑的功能
     * 后面放在users模块
     * @param uid
     * @param ip
     * @return
     */
    public UserBlacklist[] queryUserBlacklist(int uid, long ip) {

        logger.info("query user black list,uid is {},ip is {}", uid, ip);

        String postKey = uid + "-" + ip;

        boolean hasKey = redisValueOperation.hasKey(CacheEnum.USER_BACKLIST, postKey);
        if (!hasKey) {
            //缓存中没有
            List<UserBlacklist> blacklists = executeAndGetFuture(new Callable<List<UserBlacklist>>() {
                public List<UserBlacklist> call() {
                    return userBlacklistDAO.selectByUidAndIP(uid, ip);
                }
            });
            if (CollectionUtils.isEmpty(blacklists)) {
                blacklists = new ArrayList<>();
            }
            redisValueOperation.put(CacheEnum.USER_BACKLIST, postKey, JSONArray.toJSONString(blacklists), false);
        }
        UserBlacklist[] userBlacklistArray = redisValueOperation.get(CacheEnum.USER_BACKLIST, postKey, UserBlacklist[].class);
        return userBlacklistArray;
    }

    /**
     * 查询用户当月web端的订单的数量
     * 防止刷单
     * @param uid
     * @return
     */
    public int queryCurrentMonthOrderCountFromWebClient(int uid) {
        logger.info("query current month order count from web client,uid is {}", uid);
        /**
         * 存放缓存需要同步更新,暂时先查询数据库
         */

        Integer count = executeAndGetFuture(new Callable<Integer>() {
            public Integer call() {
                return ordersMapper.selectCurrentMonthOrderCount(uid);
            }
        });

        return count == null ? 0 : count;
    }


    /**
     * 查询用户默认地址
     *
     * @param uid
     * @return
     */
    public UserAddressRspBO queryUserDefaultAddress(int uid) {
        String serviceName = ShoppingConfig.USERS_QUERY_USERDEFAULTADRESS_REST_URL;
        logger.info("call service {} to query default address, uid {},degrade enable {}", serviceName, uid, queryUserAddressDegreadeEnable);

        UserAddressReqBO userAddressReqBO = new UserAddressReqBO();
        userAddressReqBO.setUid(uid);

        UserAddressRspBO userAddressRspBO = null;
        if (queryUserAddressDegreadeEnable) {
            UserAddressRspBO defaultValue = new UserAddressRspBO();
            userAddressRspBO = serviceCaller.call(serviceName, userAddressReqBO, UserAddressRspBO.class, defaultValue);
        } else {
            userAddressRspBO = serviceCaller.call(serviceName, userAddressReqBO, UserAddressRspBO.class);

        }

        logger.info("user {} has default address {}", uid, userAddressRspBO);

        return userAddressRspBO;
    }


    /**
     * 查询用户地址
     * @param uid
     * @param addressId
     * @param defaultValue
     * @return
     */
    public UserAddressRspBO getUserAddressByUidAndAddressId(Integer uid, Integer addressId, UserAddressRspBO defaultValue) {
        String serviceName = ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL;
        logger.info("call service {} for uid {},addressId {},default value {},degrade enable {}", serviceName, uid, addressId, defaultValue, queryUserAddressDegreadeEnable);
        UserAddressReqBO userAddressReqBO = new UserAddressReqBO();
        userAddressReqBO.setUid(uid);
        userAddressReqBO.setId(addressId);

        UserAddressRspBO userAddressBO = null;
        if (queryUserAddressDegreadeEnable) {
            userAddressBO = serviceCaller.call(ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL, userAddressReqBO, UserAddressRspBO.class, defaultValue);
        } else {
            userAddressBO = serviceCaller.call(ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL, userAddressReqBO, UserAddressRspBO.class);
        }

        logger.info("call service {} for uid {},result is {}", serviceName, uid, userAddressBO);

        return userAddressBO;
    }

    private <T> T executeAndGetFuture(Callable<T> callable) {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            FutureTask<T> futureTask = new FutureTask<T>(callable);
            executor.execute(futureTask);

            //在这里可以做别的任何事情
            try {
                //取得结果，同时设置超时执行时间为1秒。同样可以用future.get()，不设置执行超时时间取得结果
                return futureTask.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                futureTask.cancel(true);
            } catch (ExecutionException e) {
                futureTask.cancel(true);
            } catch (TimeoutException e) {
                logger.warn("execute future timeout");
                futureTask.cancel(true);
            } finally {
                executor.shutdown();
            }

        } catch (Exception ex) {
            logger.error("executeAndGetFuture error", ex);
        }

        return null;
    }

    /**
     * 获取本地的vip等级缓存,默认为普通会员
     *
     * @param uid
     * @return
     */
    private VipInfo getLocalVipInfo(int uid) {
        VipInfo vipInfo = new VipInfo();
        String cachedValue = redisValueOperation.get(CacheEnum.USER_VIP, String.valueOf(uid), String.class);
        VipLevel vipLevel = new VipLevel();
        vipLevel.setCurLevel(VIPEnum.VIP_0.curLevel + "");
        if (StringUtils.isNotEmpty(cachedValue) && NumberUtils.isNumber(cachedValue)) {
            vipLevel.setCurLevel(cachedValue);
        }
        vipInfo.setCurVipInfo(vipLevel);
        return vipInfo;
    }

    private <T> T[] listToArray(List<T> list,Class cla) {
        if (CollectionUtils.isNotEmpty(list)) {
            T[] array = (T[]) Array.newInstance(cla, list.size());

            return list.toArray(array);
        } else {
            return null;
        }
    }

}

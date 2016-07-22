package com.yoho.yhorder.shopping.service;

import com.yoho.core.common.restbean.ResponseBean;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.core.rest.client.hystrix.AsyncFuture;
import com.yoho.product.model.StorageBo;
import com.yoho.product.model.shopcart.ProductShopCartBo;
import com.yoho.product.request.*;
import com.yoho.product.response.VoidResponse;
import com.yoho.service.model.inbox.request.InboxReqBO;
import com.yoho.service.model.order.model.audit.AuditGoods;
import com.yoho.service.model.order.request.AuditRequest;
import com.yoho.service.model.order.request.IssueTicketRequest;
import com.yoho.service.model.order.request.OrderRequest;
import com.yoho.service.model.order.response.audit.AuditCodPayResponse;
import com.yoho.service.model.promotion.LimitCodeUserBo;
import com.yoho.service.model.promotion.ProductBuyLimitBo;
import com.yoho.service.model.promotion.UserCouponsListBO;
import com.yoho.service.model.promotion.request.LimitCodeOrderReq;
import com.yoho.service.model.promotion.request.PromotionBuyLimitReq;
import com.yoho.service.model.promotion.request.UserCouponDetailListReq;
import com.yoho.service.model.request.*;
import com.yoho.service.model.response.*;
import com.yoho.service.model.vip.VipInfo;
import com.yoho.yhorder.shopping.model.OrderYohoCoin;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JXWU on 2016/2/3.
 * 调用外部服务，都在这边
 */
@Component
public class ExternalService {

    private final Logger logger = LoggerFactory.getLogger("externalCallLog");

    @Autowired
    private ServiceCaller serviceCaller;

    /**
     * 批量查询sku信息，包括库存、价格等
     *
     * @param skus
     * @return
     */
    public ProductShopCartBo[] queryProductShoppingCartBySkuids(List<Integer> skus) {
        logger.debug("call service {} to query product info for shopping cart ,skus are {}", ShoppingConfig.PRODUCT_QUERY_PRODUCTSHOPCART_BYSKUIDS_REST_URL, skus);
        BatchBaseRequest request = new BatchBaseRequest();
        request.setParams(skus);
        ProductShopCartBo[] productShopCartBos = serviceCaller.call(ShoppingConfig.PRODUCT_QUERY_PRODUCTSHOPCART_BYSKUIDS_REST_URL, request, ProductShopCartBo[].class);
        logger.debug("query product info for shopping cart,skus are {},result is {}", skus, productShopCartBos);
        return productShopCartBos;
    }


    /**
     * 查询vip信息
     *
     * @param uid
     * @return
     */
    public VipInfo queryVipDetailInfo(int uid) {
        UserVipReqBO reqBO = new UserVipReqBO();
        reqBO.setUid(uid);
        return serviceCaller.call(ShoppingConfig.USERS_QUERY_VIP_LEVEL_REST_URL, reqBO, VipInfo.class);
    }

    /**
     * 查询用户yoho币,包括logid=0
     * @param uid
     * @return
     */
    public OrderYohoCoin queryUserYohoCoin(int uid) {
        logger.info("call service {} to query user yoho coin for uid {}", ShoppingConfig.USERS_QUERY_USERYOHO_REST_URL, uid);
        YohoCoinLogReqBO userYohoCoinReqBO = new YohoCoinLogReqBO();
        userYohoCoinReqBO.setUid(uid);
        YohoCoin yohoCoin = serviceCaller.call(ShoppingConfig.USERS_QUERY_USERYOHO_REST_URL, userYohoCoinReqBO, YohoCoin.class);
        OrderYohoCoin orderYohoCoin = new OrderYohoCoin();
        if (yohoCoin != null) {
            if (yohoCoin.getCoinNum() != null) {
                orderYohoCoin.setYohoCoinNum(yohoCoin.getCoinNum());
            }
            orderYohoCoin.setCoinUnit(yohoCoin.getCoinUnit());
        }
        logger.info("result is {}",orderYohoCoin);
        return orderYohoCoin;
    }

    /**
     * 获取用户红包
     *
     * @param uid
     * @return
     */
    public double queryUserRedEnvelopes(int uid) {
        logger.info("call service {} to query user Red Envelopes for uid {}", ShoppingConfig.USERS_QUERY_USER_REDENVELOPESCOUNT_REST_URL, uid);
        RedEnvelopesReqBO reqBO = new RedEnvelopesReqBO();
        reqBO.setUid(uid);
        BigDecimal result = serviceCaller.call(ShoppingConfig.USERS_QUERY_USER_REDENVELOPESCOUNT_REST_URL, reqBO, BigDecimal.class);
        return result == null ? 0 : result.doubleValue();
    }


    /**
     * 查询商品限购
     *
     * @param skns
     * @return
     */
    public ProductBuyLimitBo[] queryProductBuyLimitBySknids(List<Integer> skns) {
        PromotionBuyLimitReq promotionBuyLimitReq = new PromotionBuyLimitReq();
        promotionBuyLimitReq.setProductSkn(new ArrayList<>(skns));
        return serviceCaller.call(ShoppingConfig.PROMOTION_QUERY_PRODUCTBUYLIMIT_BYSKNIDS_REST_URL, promotionBuyLimitReq, ProductBuyLimitBo[].class);
    }


    /**
     * 查询addressid对应的地址
     *
     * @param uid
     * @param addressId
     * @return
     */
    public UserAddressRspBO queryUserAddressByUidAndAddressId(Integer uid, Integer addressId) {
        logger.debug("call service {} to query user address,uid {},addressid {}", ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL, uid, addressId);
        UserAddressReqBO userAddressReqBO = new UserAddressReqBO();
        userAddressReqBO.setUid(uid);
        userAddressReqBO.setId(addressId);
        UserAddressRspBO userAddressRspBO = serviceCaller.call(ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL, userAddressReqBO, UserAddressRspBO.class);
        logger.debug("call service {} for uid {},addressId {},result is {}", ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL, uid, addressId, userAddressRspBO);
        return userAddressRspBO;
    }

    /**
     * 查询用户默认地址
     *
     * @param uid
     * @return
     */
    public UserAddressRspBO queryUserDefaultAddress(int uid) {
        logger.debug("call service {} to query default address, uid {}", ShoppingConfig.USERS_QUERY_USERDEFAULTADRESS_REST_URL, uid);
        UserAddressReqBO userAddressReqBO = new UserAddressReqBO();
        userAddressReqBO.setUid(uid);
        UserAddressRspBO userAddressRspBO =   serviceCaller.call(ShoppingConfig.USERS_QUERY_USERDEFAULTADRESS_REST_URL, userAddressReqBO, UserAddressRspBO.class);
        logger.debug("user {} has default address {}", uid, userAddressRspBO);
        return userAddressRspBO;
    }

    public StorageBo[] queryStorageBySkus(List<Integer> skuList) {
        //批量查询商品库存
        BatchBaseRequest<Integer> request = new BatchBaseRequest<>();
        request.setParams(skuList);
        logger.debug("call service {} to query storages, skus: {}", ShoppingConfig.PRODUCT_BATCHQUERY_STORAGEBYSKUIDS_REST_URL, skuList);
        StorageBo[] storageBos = serviceCaller.call(ShoppingConfig.PRODUCT_BATCHQUERY_STORAGEBYSKUIDS_REST_URL, request, StorageBo[].class);

        logger.debug("call service {} to query storages, skus: {},result are {}",
                ShoppingConfig.PRODUCT_BATCHQUERY_STORAGEBYSKUIDS_REST_URL,
                skuList,
                storageBos);
        return storageBos;
    }

    /**
     * 校验限购码
     * @param uid
     * @param skn
     * @return
     */
    public LimitCodeUserBo queryLimitCodeUserBo(int uid, String skn,String sku,String limitProductCode) {
        LimitCodeOrderReq req = new LimitCodeOrderReq();
        req.setUid(uid);
        req.setProductSkn(skn);
        req.setProductSku(sku);
        req.setLimitProductCode(limitProductCode);
        logger.info("call service {} to query limit code,request is {}", ShoppingConfig.PROMOTION_CHECKLIMITCODE_REST_URL, req);
        LimitCodeUserBo limitCodeUserBo = serviceCaller.call(ShoppingConfig.PROMOTION_CHECKLIMITCODE_REST_URL, req, LimitCodeUserBo.class);
        logger.info("call service {} to query limit code,req is {},result is {}", ShoppingConfig.PROMOTION_CHECKLIMITCODE_REST_URL, req, limitCodeUserBo);
        return limitCodeUserBo;
    }


    public void addLimitCodeUseRecord(int uid, String limitCode, int skn, Long orderCode)
    {
        LimitCodeOrderReq req = new LimitCodeOrderReq();
        req.setUid(uid);
        req.setLimitCode(limitCode);
        req.setProductSkn(String.valueOf(skn));
        req.setOrderCode(String.valueOf(orderCode));
        logger.info("call service {} to use limit code,request is {}", ShoppingConfig.PROMOTION_ADDLIMITCODEUSERECORD_REST_URL, req);
        ResponseBean responseBean = serviceCaller.call(ShoppingConfig.PROMOTION_ADDLIMITCODEUSERECORD_REST_URL, req, ResponseBean.class);
        logger.info("call service {} to use limit code,result is {}", ShoppingConfig.PROMOTION_ADDLIMITCODEUSERECORD_REST_URL, responseBean);

    }

    /**
     * 使用yoho币
     * @param uid
     * @param orderCode
     * @param yohoCoinNum
     * @param type
     * @param params
     * @return
     */
    public YohoCurrencyRspBO useYohoCoin(int uid, long orderCode, int yohoCoinNum, int type, String params) {
        YohoCoinReqBO reqBO = new YohoCoinReqBO();
        reqBO.setUid(uid);
        reqBO.setNum(yohoCoinNum);
        reqBO.setOrder_code(orderCode);
        reqBO.setType(type);
        reqBO.setParams(params);
        logger.info("call service {} to use yoho coin,request is {}", ShoppingConfig.USERS_CHANGEYOHOCOIN_REST_URL, reqBO);
        YohoCurrencyRspBO rspBO = serviceCaller.call(ShoppingConfig.USERS_CHANGEYOHOCOIN_REST_URL, reqBO, YohoCurrencyRspBO.class);
        logger.info("call service {} to use yoho coin,result is {}", ShoppingConfig.USERS_CHANGEYOHOCOIN_REST_URL, rspBO);
        return rspBO;
    }


    /**
     * 使用红包
     * @param uid
     * @param orderCode
     * @param amount
     * @param type
     * @param founder
     * @param remark
     * @param activeId
     * @return
     */
    public BigDecimal useRedenvelopes(int uid,  long orderCode,double amount,int type, String founder, String remark, int activeId) {
        RedEnvelopesReqBO reqBO = new RedEnvelopesReqBO();
        reqBO.setUid(uid);
        reqBO.setOrderCode(orderCode);
        reqBO.setType(type);
        reqBO.setFounder(founder);
        reqBO.setRemark(remark);
        reqBO.setActiveId(activeId);
        reqBO.setAmount(amount);
        logger.info("call service {} to use redenvelopes,request is {}", ShoppingConfig.USERS_USEREDENVELOPES_REST_URL, reqBO);
        BigDecimal afterValue = serviceCaller.call(ShoppingConfig.USERS_USEREDENVELOPES_REST_URL, reqBO, BigDecimal.class);
        logger.info("call service {} to use redenvelopes,result is {}", ShoppingConfig.USERS_USEREDENVELOPES_REST_URL, afterValue);
        return afterValue;
    }

    /**
     * 批量收藏
     * @param uid
     * @param skuIds
     * @return
     */
    public int  batchAddFavorite(int uid,List<Integer> skuIds)
    {
        FavoriteRequest request = new FavoriteRequest();
        request.setUid(uid);
        request.setType("product");
        request.setSkuIds(skuIds);
        logger.info("call service {} to batchAddFavorite,request is {}", ShoppingConfig.PRODUCT_BATCHADDFAVORITE_REST_URL, request);
        Integer changedSknCount = serviceCaller.call(ShoppingConfig.PRODUCT_BATCHADDFAVORITE_REST_URL, request, Integer.class);
        logger.info("call service {} to batchAddFavorite,result is {}", ShoppingConfig.PRODUCT_BATCHADDFAVORITE_REST_URL, changedSknCount);
        return changedSknCount;
    }

    /**
     * 批量减少库存
     * {"updateStorageRequest":[{"skuId":102000,"storageNum":5},{"skuId":102001,"storageNum":2}]}
     * @param updateStorageRequest
     */
    public void batchDecreaseStorageBySkuId(List<UpdateStorageRequest> updateStorageRequest)
    {
        BatchUpdateStorageRequest request = new BatchUpdateStorageRequest();
        request.setUpdateStorageRequest(updateStorageRequest);
        logger.info("call service {} for {}", ShoppingConfig.PRODUCT_BATCHDECREASESTORAGEBYSKUID_REST_URL, request);
        serviceCaller.call(ShoppingConfig.PRODUCT_BATCHDECREASESTORAGEBYSKUID_REST_URL, request, VoidResponse .class);
    }

    /**
     * 站内信
     * @param inboxReqBO
     * @return
     */
    public ResponseBean saveInbox(InboxReqBO inboxReqBO)
    {
        logger.info("call service {} for {}", ShoppingConfig.MESSAGE_SAVEINBOX_REST_URL, inboxReqBO);
        return  serviceCaller.call(ShoppingConfig.MESSAGE_SAVEINBOX_REST_URL, inboxReqBO, ResponseBean .class);
    }

    public Integer queryStorageNumBySkuId(Integer skuId) {
        BaseRequest request = new BaseRequest();
        request.setParam(skuId);
        Integer storageNum = serviceCaller.call(ShoppingConfig.PRODUCT_QUERYSTORAGENUMBYSKUID_REST_URL, request, Integer.class);
        logger.info("call service {} for {},result is {}", ShoppingConfig.PRODUCT_QUERYSTORAGENUMBYSKUID_REST_URL, request, storageNum);
        return storageNum;
    }

    /**
     * 异步调用货到付款的稽核
     * @param uid
     * @param goodsList
     * @param amount
     * @return
     */
    public AsyncFuture<AuditCodPayResponse> createAuditCodPayAsyncFuture(int uid, List<AuditGoods> goodsList, double amount) {
        AuditRequest request = new AuditRequest();
        request.setUid(uid);
        request.setGoodsList(goodsList);
        request.setAmount(amount);

        logger.info("create audit codpay asyncFuture,request is {}", request);

        AsyncFuture<AuditCodPayResponse> auditCodPayAsyncFuture = serviceCaller.asyncCall(ShoppingConfig.ORDER_AUDIT_CODPAY_REST_URL, request, AuditCodPayResponse.class, new AuditCodPayResponse());

        logger.info("create audit codpay asyncFuture success,request is {},asyncFuture is {}", request, auditCodPayAsyncFuture);

        return auditCodPayAsyncFuture;
    }

    /**
     * 获取货到付款的稽核结果
     * @param asyncFuture
     * @param waitingSeconds
     * @param defaultResponse
     * @return
     */
    public AuditCodPayResponse getAuditCodPayResponseFrom(AsyncFuture<AuditCodPayResponse> asyncFuture, int waitingSeconds, AuditCodPayResponse defaultResponse) {
        if (asyncFuture == null) {
            logger.info("asyncFuture is null");
            return defaultResponse;
        }
        try {
            AuditCodPayResponse response = asyncFuture.get(waitingSeconds);
            logger.info("return response from asyncFuture,response is {}", response);
            return response;
        } catch (Exception ex) {
            logger.warn("get response from asyncFuture error,asyncFuture is {}", asyncFuture, ex);
            return defaultResponse;
        }
    }

    /**
     * 异步获取用户未使用的限购码
     * @param uid
     * @return
     */
    public AsyncFuture<UserCouponsListBO> createQueryUserNotUseCouponsAsyncFuture(int uid) {
        UserCouponDetailListReq userCouponDetailListReq = new UserCouponDetailListReq();
        userCouponDetailListReq.setUid(uid);

        logger.info("create query user coupon asyncFuture,request is {}", userCouponDetailListReq);

        AsyncFuture<UserCouponsListBO> asyncFuture = serviceCaller.asyncCall(ShoppingConfig.PROMOTION_QUERY_USERNOUSEDCOUPONS_REST_URL,
                userCouponDetailListReq, UserCouponsListBO.class);

        logger.info("create query user coupon asyncFuture success,request is {},asyncFuture is {}", userCouponDetailListReq, asyncFuture);

        return asyncFuture;
    }


    public Object getAsyncResponseFrom(AsyncFuture asyncFuture, int waitingSeconds) {
        if (asyncFuture == null) {
            logger.info("asyncFuture is null");
            return null;
        }
        Object t;
        try {
            t = asyncFuture.get(waitingSeconds);
            logger.info("return response from asyncFuture,response is {}", t);
            return t;
        } catch (Exception ex) {
            logger.warn("get response from asyncFuture error,asyncFuture is {}", asyncFuture, ex);
            return null;
        }
    }

    public Object getAsyncResponseFrom(AsyncFuture asyncFuture) {
        return getAsyncResponseFrom(asyncFuture,1);
    }


    /**
     * jit拆单
     * @param uid
     * @param orderCode
     * @return
     */
    public int splitMultiPackage(int uid, long orderCode) {
        logger.info("split order,uid is {},order code", uid, orderCode);
        OrderRequest request = new OrderRequest();
        request.setUid(uid);
        request.setOrderCode(orderCode);
        Integer subOrderNum = serviceCaller.call("order.splitOrder", request, Integer.class);
        logger.info("call service order.splitOrder to split order success,request is {},response is {}", request, subOrderNum);
        return subOrderNum;
    }

    /**
     * 返yoho币
     * @param uid
     * @param orderCode
     * @param yohoCoinNum
     */
    public YohoCurrencyRspBO refundYohoCoin(int uid, long orderCode, int yohoCoinNum) {
        logger.info("refund user yoho coin,uid is {},order code is {},yoho coin num is {}", uid, orderCode, yohoCoinNum);
        if (yohoCoinNum > 0) {
            String serviceName = "users.refundYohoCoin";
            YohoCoinReqBO yohoCoinReqBO = new YohoCoinReqBO();
            yohoCoinReqBO.setNum(yohoCoinNum);
            yohoCoinReqBO.setOrder_code(orderCode);
            //2 订单取消退还
            yohoCoinReqBO.setType(2);
            yohoCoinReqBO.setUid(uid);
            yohoCoinReqBO.setPid(0);
            YohoCurrencyRspBO res = serviceCaller.call(serviceName, yohoCoinReqBO, YohoCurrencyRspBO.class);
            logger.info("call service {},request is {},after call currency is {}", serviceName, yohoCoinReqBO, res == null ? "error,res is null" : res.getCurrency());
            return res;
        }

        return null;
    }


    /**
     * 返红包
     * @param uid
     * @param orderCode
     * @param useRedEnvelopes
     */
    public void refundRedEnvelopes(int uid, long orderCode, double useRedEnvelopes) {
        logger.info("refund user Red Envelopes,uid is {},order code is {},useRedEnvelopes is {}", uid, orderCode, useRedEnvelopes);
        if (useRedEnvelopes > 0) {
            String serviceName = "users.cancelReturnRedenvelopes";
            RedEnvelopesReqBO red = new RedEnvelopesReqBO();
            red.setType(3);
            red.setRemark("取消订单加红包");
            red.setUid(uid);
            red.setAmount(useRedEnvelopes);
            red.setFounder(String.valueOf(uid));
            red.setOrderCode(orderCode);
            red.setActiveId(2);
            RedEnvelopesCancelRspBO res = serviceCaller.call(serviceName, red, RedEnvelopesCancelRspBO.class);
            logger.info("call service {},request is {},after call currency is {}", serviceName, red, res == null ? "error,res is null" : res.getResult());
        }
    }

    /**
     * 查询订单使用的红包记录,包括下单,回退
     * @param uid
     * @param orderCode
     * @return
     */
    public RedenvelopesResponseBO[] queryOrderRedenvelopes(int uid, long orderCode) {
        logger.info("query user order RedEnvelopes,uid is {},order code is {}", uid, orderCode);
        RedEnvelopesReqBO reqBO = new RedEnvelopesReqBO();
        reqBO.setUid(uid);
        reqBO.setOrderCode(orderCode);
        RedenvelopesResponseBO[] responseBOs = serviceCaller.call("users.queryOrderRedenvelopes", reqBO, RedenvelopesResponseBO[].class);
        logger.info("query user order RedEnvelopes,response is {}", responseBOs);
        return responseBOs;
    }

    /**
     * 查询可用用户yoho币,过滤掉logid=0的记录
     * @param uid
     * @return
     */
    public OrderYohoCoin queryUsableYohoCoin(int uid) {
        logger.info("call service {} to query user usable yoho coin for uid {}", ShoppingConfig.USERS_QUERY_USERYOHO_FILTERLOGID_REST_URL, uid);
        YohoCoinLogReqBO userYohoCoinReqBO = new YohoCoinLogReqBO();
        userYohoCoinReqBO.setUid(uid);
        YohoCoin yohoCoin = serviceCaller.call(ShoppingConfig.USERS_QUERY_USERYOHO_FILTERLOGID_REST_URL, userYohoCoinReqBO, YohoCoin.class);
        OrderYohoCoin orderYohoCoin = new OrderYohoCoin();
        if (yohoCoin != null) {
            if (yohoCoin.getCoinNum() != null) {
                orderYohoCoin.setYohoCoinNum(yohoCoin.getCoinNum());
            }
            orderYohoCoin.setCoinUnit(yohoCoin.getCoinUnit());
        }
        logger.info("result is {}",orderYohoCoin);
        return orderYohoCoin;
    }

    /**
     * 发行门票
     * @param uid
     * @param orderCode
     */
    public void issueTicket(int uid, long orderCode) {
        String serviceName = "order.issueTicket";
        logger.info("call service {} to issue ticket,uid is {},order code is {}", serviceName, uid, orderCode);
        IssueTicketRequest request = new IssueTicketRequest();
        request.setUid(uid);
        request.setOrderCode(orderCode);
        serviceCaller.call(serviceName,request,Void.class);
    }
}

package com.yoho.yhorder.order.service.impl;

import static com.yoho.yhorder.common.utils.YHPreconditions.checkArgument;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.helpers.ImagesHelper;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.GoodsBo;
import com.yoho.product.model.ProductBo;
import com.yoho.product.model.RefundProductBo;
import com.yoho.product.request.BatchBaseRequest;
import com.yoho.product.request.BatchProductOrderRequest;
import com.yoho.product.request.ProductOrderRequest;
import com.yoho.service.model.order.OrderStatusDesc;
import com.yoho.service.model.order.constants.InvoiceType;
import com.yoho.service.model.order.constants.OrderStatus;
import com.yoho.service.model.order.constants.OrdersMateKey;
import com.yoho.service.model.order.model.RefundGoodsBO;
import com.yoho.service.model.order.model.RefundGoodsListBO;
import com.yoho.service.model.order.model.RefundOrder;
import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;
import com.yoho.service.model.order.model.refund.Goods;
import com.yoho.service.model.order.model.refund.Payment;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.order.response.PageResponse;
import com.yoho.service.model.promotion.CouponsBo;
import com.yoho.service.model.promotion.request.CouponsLogReq;
import com.yoho.service.model.promotion.request.CouponsReq;
import com.yoho.service.model.request.AreaReqBO;
import com.yoho.service.model.request.YohoCoinReqBO;
import com.yoho.service.model.response.AreaRspBo;
import com.yoho.service.model.response.YohoCurrencyRspBO;
import com.yoho.yhorder.common.cache.redis.UserOrderCache;
import com.yoho.yhorder.common.convert.BeanConvert;
import com.yoho.yhorder.common.utils.CalendarUtils;
import com.yoho.yhorder.common.utils.OrderGoodsTypeUtils;
import com.yoho.yhorder.common.utils.OrderYmlUtils;
import com.yoho.yhorder.common.utils.RefundUtils;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.domain.ChangeGoodsMainInfo;
import com.yoho.yhorder.dal.domain.RefundNumber;
import com.yoho.yhorder.dal.domain.RefundNumberStatistics;
import com.yoho.yhorder.dal.model.*;
import com.yoho.yhorder.invoice.service.InvoiceService;
import com.yoho.yhorder.invoice.webservice.constant.InvoiceSoapErrorCode;
import com.yoho.yhorder.order.config.Constant;
import com.yoho.yhorder.order.service.IErpService;
import com.yoho.yhorder.order.service.IOrderMqService;
import com.yoho.yhorder.order.service.IRefundService;
import com.yoho.yhorder.order.service.IYohoOrderService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * qianjun 2015/12/3
 */
@Service("refundService")
public class RefundServiceImpl implements IRefundService {

    private static final Logger logger = LoggerFactory.getLogger(RefundServiceImpl.class);

    @Autowired
    private IRefundGoodsDao refundGoodsDao;

    @Autowired
    private IRefundGoodsListDao refundGoodsListDao;

    @Autowired
    private IExpressOrdersDao expressOrdersDao;

    @Autowired
    private OrdersCouponsMapper ordersCouponsMapper;

    @Autowired
    private IApplyGoodsImagesDao applyGoodsImagesDao;

    @Autowired
    private IChangeGoodsMainMapper changeGoodsMainMapper;

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    private IRefundGoodsListMapper refundGoodsListMapper;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private IYohoOrderService yohoOrderService;

    @Autowired
    @Qualifier("erpService")
    private IErpService erpService;

    @Autowired
    @Qualifier("mqErpService")
    private IErpService mqErpService;

    @Value("CALL")
    private String erpMessageSyncType;

    @Value("${erp.order.refund.sync.type:RPC}")
    private String erpOrderRefundSyncType;

    @Value("${erp.order.express.sync.type:RPC}")
    private String erpOrderExpressSyncType;

    @Autowired
    private YHValueOperations<String, String> valueOperations;

    @Autowired
    private BeanConvert beanConvert;

    @Autowired
    private UserOrderCache userOrderCache;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Autowired
    private IOrderMqService orderMqService;

    private final static int SEVEN_DAYS_RETURN = 7 * 24 * 60 * 60;

    /**
     * 根据退货订单总数返回结果
     */
    private PageResponse<RefundGoodsBO> returnResultByRefundCnt(Integer pageNum, Integer pageSize, Integer totalPage, Integer count, List<RefundGoodsBO> refundGoodsBOList) {
        /**
         * 定义结果返回对象
         */
        PageResponse<RefundGoodsBO> resultResponse = new PageResponse<>();

        /**
         * 封装分页及数据到结果返回对象中
         */
        resultResponse.setPageNo(pageNum);
        resultResponse.setPageSize(pageSize);
        resultResponse.setTotalPage(totalPage);
        resultResponse.setTotalCount(count);
        resultResponse.setList(refundGoodsBOList);

        return resultResponse;
    }

    @Override
    public RefundOrder findRefundOrderById(Integer id) {
        if (id == null || id == 0) {
            return null;
        }
        logger.info("find refund order {}.", id);
        RefundGoods refundGoods = refundGoodsDao.selectByPrimaryKey(id);
        if (refundGoods == null) {
            logger.info("find refund order {} fail, can not find change order.", id);
            return null;
        } else {
            RefundOrder refundOrder = new RefundOrder();
            BeanUtils.copyProperties(refundGoods, refundOrder);
            logger.info("find refund order {} success.", id);
            return refundOrder;
        }
    }

    @Override
    public PageResponse<RefundGoodsBO> getListByUid(Integer uid, Integer pageNum, Integer pageSize) {
        logger.info("beign getListByUid uid:{},pageNum:{},pageSize:{}", uid, pageNum, pageSize);

        // 校验必要参数
        if (uid == null || uid <= 0) {
            logger.warn("getListByUid fail, because the uid is empty");
            throw new ServiceException(ServiceError.ORDER_UID_IS_EMPTY);
        }

        // 获取退货订单总数
        int count = refundGoodsDao.selectCountByUid(uid);
        // 定义返回数据的总页数
        int totalPage = 0;
        // 定义退货商品集合
        List<RefundGoodsBO> refundGoodsBOList = new ArrayList<>();

        /**
         * 如果退货订单数量为0，就直接返回空数据分页对象 空数据分页对象 ：分页对象数据正常返回、数据结果为空集合
         */
        if (count <= 0) {
            logger.info("getListByUid return, because the data count is 0");
            return returnResultByRefundCnt(pageNum, pageSize, totalPage, count, refundGoodsBOList);
        }

        // 如果查询范围大于总数，则使用总数替换查询范围
        if (pageSize > count) {
            pageSize = count;
        }

        // 定义并计算分页查询的偏移数量
        Integer offset = (pageNum - 1) * pageSize;
        // 根据uid获取订单退货商品集合
        List<RefundGoods> refundGoodsList = refundGoodsDao.selectListByUid(uid, offset, pageSize);
        /**
         * 如果退货商品集合为空，就直接返回空数据分页对象 空数据分页对象 ：分页对象数据正常返回、数据结果为空集合
         */
        if (CollectionUtils.isEmpty(refundGoodsList)) {
            logger.info("getListByUid return, because the refundGoodsList is empty");
            return returnResultByRefundCnt(pageNum, pageSize, totalPage, count, refundGoodsBOList);
        }
        // 设置数据分页总页数
        totalPage = ((Double) Math.ceil(count / pageSize.doubleValue())).intValue();

        // 定义查询换货商品批量查询条件
        List<Integer> changePurchaseIds = new ArrayList<>();
        // 定义订单信息批量查询条件
        List<Map<String, String>> orderParams = new ArrayList<>();
        // 定义退货商品批量查询条件
        List<Integer> returnRequestIds = new ArrayList<>();

        /**
         * 循环退货商品集合，获取批处理查询条件
         */
        for (RefundGoods refundGoods : refundGoodsList) {
            if (Objects.nonNull(refundGoods.getChangePurchaseId()) && refundGoods.getChangePurchaseId() > 0) {
                // 设置换货商品批量查询条件
                changePurchaseIds.add(refundGoods.getChangePurchaseId());
            }

            // 设置订单批量查询条件
            Map<String, String> orderParam = new HashMap<>();
            orderParam.put("uid", uid + "");
            orderParam.put("orderCode", refundGoods.getSourceOrderCode() + "");
            orderParams.add(orderParam);

            // 设置退货商品批量查询条件
            returnRequestIds.add(refundGoods.getId());
        }

        // 换货商品信息
        List<ChangeGoodsMainInfo> changeGoodsMainInfoList = null;
        // 订单商品信息
        List<OrdersGoods> ordersGoodsList = null;
        // 商品信息
        List<GoodsBo> goodsBoList = null;
        List<ProductBo> productBoList = null;
        // 定义订单商品批量查询条件
        List<Map<String, String>> orderGoodsParams;
        List<Orders> ordersList = null;

        // 执行批量查询，获取退货商品集合
        List<RefundGoodsList> refundGoodsLists = refundGoodsListMapper.selectByReturnRequestIds(returnRequestIds);

        if (CollectionUtils.isNotEmpty(refundGoodsLists)) {
            // 执行批量查询，获取订单集合
            ordersList = ordersMapper.getBatchByOrdercode(orderParams);
            orderGoodsParams = getOrderGoodsParams(refundGoodsLists, ordersList);

            if (CollectionUtils.isNotEmpty(orderGoodsParams)) {
                // 执行批量查询，根据sku获取订单商品集合
                ordersGoodsList = ordersGoodsMapper.selectGoodsBySku(orderGoodsParams);
            }

            BatchBaseRequest<Integer> baseRequest = new BatchBaseRequest<>();
            // 设定商品批量查询条件
            baseRequest.setParams(getProductSnkList(refundGoodsLists));
            // 批量获取产品基本信息
            productBoList = Arrays.asList(serviceCaller.call("product.batchQueryProductBasicInfo", baseRequest, ProductBo[].class));

            if (CollectionUtils.isNotEmpty(ordersGoodsList)) {
                // 定义商品基本信息批量查询条件
                List<Integer> goodsIdList = new ArrayList<>();
                for (OrdersGoods ordersGoods : ordersGoodsList) {
                    goodsIdList.add(ordersGoods.getGoodsId());
                }
                // 执行服务批量查询，根据商品编号获取商品基本信息
                baseRequest = new BatchBaseRequest<>();
                baseRequest.setParams(goodsIdList);
                goodsBoList = Arrays.asList(serviceCaller.call("product.batchQueryGoodsById", baseRequest, GoodsBo[].class));
            }
        }

        if (CollectionUtils.isNotEmpty(changePurchaseIds)) {
            // 执行批量查询，获取换货商品集合
            changeGoodsMainInfoList = changeGoodsMainMapper.selectExchange(changePurchaseIds);
        }

        for (RefundGoods refundGoods : refundGoodsList) {
            RefundGoodsBO refundGoodsBO = new RefundGoodsBO();
            refundGoodsBO.setGoods(new ArrayList<>());
            refundGoodsBO.setOrderCode(refundGoods.getOrderCode());
            refundGoodsBO.setCreateTime(refundGoods.getCreateTime());
            refundGoodsBO.setSourceOrderCode(refundGoods.getSourceOrderCode());
            refundGoodsBO.setOrderCreateTime(getOrderCreateTime(refundGoodsBO, ordersList));

            // 状态
            if (Objects.nonNull(refundGoods.getChangePurchaseId()) && refundGoods.getChangePurchaseId() > 0) {
                refundGoodsBO.setId(refundGoods.getChangePurchaseId());
                refundGoodsBO.setRefundType(Byte.parseByte("2"));
                refundGoodsBO.setStatusName(OrderYmlUtils.getOrderConfig("changeStatus").get("other") + "");

                if (CollectionUtils.isNotEmpty(changeGoodsMainInfoList)) {
                    setRefundGoodsBO(changeGoodsMainInfoList, refundGoods, refundGoodsBO);
                }
            } else {
                String statusName = OrderYmlUtils.getOrderConfig("returnsStatus").get(OrderYmlUtils.converKeyToYAML(refundGoods.getStatus() + "")) + "";
                refundGoodsBO.setStatusName(StringUtils.isNotEmpty(statusName) ? statusName : OrderYmlUtils.getOrderConfig("returnsStatus").get("other") + "");

                refundGoodsBO.setId(refundGoods.getId());
                refundGoodsBO.setRefundType(Byte.parseByte("1"));
                refundGoodsBO.setStatus(refundGoods.getStatus());
                //退货在审核之后可以取消,填写物流信息之后不可以取消
                if (refundGoods.getStatus() > 10) {
                    refundGoodsBO.setCanCancel("N");
                } else {
                    refundGoodsBO.setCanCancel("Y");
                }
            }

            if (CollectionUtils.isNotEmpty(refundGoodsLists)) {
                refundGoodsBO.setGoods(getOrdersGoodses(ordersList, refundGoods, refundGoodsLists, ordersGoodsList, goodsBoList, productBoList));
            }
            refundGoodsBOList.add(refundGoodsBO);
        }

        logger.info("exit getListByUid.");
        // 返回分页参数与集合数据
        return returnResultByRefundCnt(pageNum, pageSize, totalPage, count, refundGoodsBOList);
    }

    /**
     * 从原始订单集合中找到当前的退货订单并返回原始订单的创建时间
     *
     * @param refundGoodsBO
     * @param ordersList
     * @return
     */
    private Integer getOrderCreateTime(RefundGoodsBO refundGoodsBO, List<Orders> ordersList) {
        Integer orderCreateTime = null;
        if (CollectionUtils.isEmpty(ordersList)) {
            return orderCreateTime;
        }

        for (Orders orders : ordersList) {
            if (!refundGoodsBO.getSourceOrderCode().equals(orders.getOrderCode())) {
                continue;
            }

            orderCreateTime = orders.getCreateTime();
            break;
        }

        return orderCreateTime;
    }

    private List<OrdersGoods> getOrdersGoodses(List<Orders> ordersList, RefundGoods refundGoods, List<RefundGoodsList> refundGoodsLists, List<OrdersGoods> ordersGoodsList, List<GoodsBo> goodsBoList,
                                               List<ProductBo> productBoList) {
        Integer orderId = null;

        if (CollectionUtils.isNotEmpty(ordersList)) {
            // 循环找出与当前订单相关的订单编号
            for (Orders orders : ordersList) {
                if (!orders.getOrderCode().equals(refundGoods.getSourceOrderCode())) {
                    continue;
                }
                orderId = orders.getId();
                break;
            }
        }

        List<OrdersGoods> ordersGoodses = new ArrayList<>();
        // 循环找出与当前订单相关的商品sku
        for (RefundGoodsList goodsList : refundGoodsLists) {
            if (!goodsList.getReturnRequestId().equals(refundGoods.getId())) {
                continue;
            }
            ordersGoodses.add(getOrdersGoods(ordersGoodsList, goodsBoList, productBoList, goodsList, orderId));
        }
        return ordersGoodses;
    }

    private OrdersGoods getOrdersGoods(List<OrdersGoods> ordersGoodsList, List<GoodsBo> goodsBoList, List<ProductBo> productBoList, RefundGoodsList goodsList, Integer orderId) {
        OrdersGoods responseOrderGoods = new OrdersGoods();
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            if (!goodsList.getProductSku().equals(ordersGoods.getErpSkuId()) || !ordersGoods.getOrderId().equals(orderId)) {
                continue;
            }

            responseOrderGoods.setGoodsId(ordersGoods.getGoodsId());
            responseOrderGoods.setProductId(ordersGoods.getProductId());
            responseOrderGoods.setProductSku(ordersGoods.getErpSkuId());
            responseOrderGoods.setSizeName(ordersGoods.getSizeName());
            responseOrderGoods.setColorName(ordersGoods.getColorName());
            responseOrderGoods.setSalesPrice(ordersGoods.getGoodsPrice().longValue());
            if (CollectionUtils.isEmpty(goodsBoList)) {
                break;
            }
            for (GoodsBo goodsBo : goodsBoList) {
                if (!ordersGoods.getGoodsId().equals(goodsBo.getId())) {
                    continue;
                }
                responseOrderGoods.setGoodsImage(goodsBo.getColorImage());
                break;
            }
            break;
        }

        responseOrderGoods.setGoodsType(goodsList.getGoodsType());
        responseOrderGoods.setGoodsTypeDESC(OrderGoodsTypeUtils.getOrderGoodsTypeMap(OrderYmlUtils.converKeyToYAML(goodsList.getGoodsType() + "")).get("en") + "");

        if (CollectionUtils.isEmpty(productBoList)) {
            return responseOrderGoods;
        }

        for (ProductBo productBo : productBoList) {
            if (!goodsList.getProductSkn().equals(productBo.getErpProductId())) {
                continue;
            }

            responseOrderGoods.setCnAlphabet(productBo.getCnAlphabet());
            responseOrderGoods.setProductSkn(productBo.getErpProductId());
            responseOrderGoods.setProductName(productBo.getProductName());
            break;
        }
        return responseOrderGoods;
    }

    private void setRefundGoodsBO(List<ChangeGoodsMainInfo> changeGoodsMainInfoList, RefundGoods refundGoods, RefundGoodsBO refundGoodsBO) {
        for (ChangeGoodsMainInfo change : changeGoodsMainInfoList) {
            if (!change.getId().equals(refundGoods.getChangePurchaseId())) {
                continue;
            }

            String statusName;

            if (change.getExchangeMode() == 20 && change.getStatus() == 20) {
                statusName = "上门取货";
            } else {
                //通过yml操作工具类获取key为changeStatus的定义数据，然后根据换货状态获取配置内容
                statusName = OrderYmlUtils.getOrderConfig("changeStatus").get(OrderYmlUtils.converKeyToYAML(change.getStatus() + "")) + "";
            }

            if (StringUtils.isNotEmpty(statusName)) {
                refundGoodsBO.setStatusName(statusName);
            }
            refundGoodsBO.setStatus(change.getStatus());
            //寄回换货,审核之后(0,10)可以取消换货,     上门换货,只有申请之后审核之前可以取消(0)
            setCancelChangeStatus(refundGoodsBO, change.getStatus(), change.getExchangeMode());
            break;
        }
    }

    private void setCancelChangeStatus(RefundGoodsBO refundGoodsBO, Byte status, Byte exchangeMode) {
        if (status > 10) {
            refundGoodsBO.setCanCancel("N");
            return;
        }
        //申请状态下都可以取消
        if (status == 0) {
            refundGoodsBO.setCanCancel("Y");
            return;
        }
        if (status == 10) {
            //审核状态下,20是上门换货,不能取消
            if (exchangeMode == 20) {
                refundGoodsBO.setCanCancel("N");
                return;
            }
            refundGoodsBO.setCanCancel("Y");
            return;
        }
        refundGoodsBO.setCanCancel("N");
    }

    private List<Integer> getProductSnkList(List<RefundGoodsList> refundGoodsLists) {
        List<Integer> productSknList = new ArrayList<>();
        for (RefundGoodsList refundGoods : refundGoodsLists) {
            productSknList.add(refundGoods.getProductSkn());
        }
        return productSknList;
    }

    private List<Map<String, String>> getOrderGoodsParams(List<RefundGoodsList> refundGoodsLists, List<Orders> ordersList) {
        List<Map<String, String>> orderGoodsParams = new ArrayList<>();
        for (RefundGoodsList refundGoods : refundGoodsLists) {
            if (CollectionUtils.isEmpty(ordersList)) {
                continue;
            }

            for (Orders orders : ordersList) {
                if (!orders.getOrderCode().equals(refundGoods.getOrderCode())) {
                    continue;
                }

                Map<String, String> orderGoodsParam = new HashMap<>();
                orderGoodsParam.put("erpSkuId", refundGoods.getProductSku() + "");
                orderGoodsParam.put("orderId", orders.getId() + "");
                orderGoodsParams.add(orderGoodsParam);
                break;
            }
        }
        return orderGoodsParams;
    }

    /**
     * 获取退货订单商品列表
     */
    @Override
    public Map<String, Object> goodsList(Integer uid, Long orderCode) {
        logger.info("goods list by uid [{}] and orderCode [{}].", uid, orderCode);
        // 验证退货订单商品列表请求参数
        validateGoodsListRequest(uid, orderCode);
        // 获取订单
        Orders orders = ordersMapper.selectByCodeAndUid(String.valueOf(orderCode), String.valueOf(uid));
        // 验证订单参数
        validateOrderParameter(orders);
        // 订单商品
        List<OrdersGoods> ordersGoodsList = ordersGoodsMapper.selectOrderGoodsByOrderId(Collections.singletonList(orders.getId()));
        //如果商品不存在,直接抛出异常
        if (ordersGoodsList == null || ordersGoodsList.size() == 0) {
            throw new ServiceException(ServiceError.ORDER_ORDERS_GOODS_IS_EMPTY);
        }
        Set<Integer> skns = buildSknsByGoodsList(ordersGoodsList);
        BatchBaseRequest<Integer> batchBaseRequest = new BatchBaseRequest<Integer>();
        batchBaseRequest.setParams(new ArrayList<Integer>(skns));
        //调商品服务查询skn列表是否有退换货限制
        Map<Integer, Integer> limitMap = serviceCaller.call("product.batchQueryIsSupportRefundExchange", batchBaseRequest, Map.class);
        // 获取该订单使用的优惠券信息
        OrdersCoupons ordersCoupons = ordersCouponsMapper.selectByOrderId(orders.getId());
        BigDecimal couponAmount = new BigDecimal("0.00");
        if (ordersCoupons != null) {
            // 获取该订单使用的优惠券数量
            couponAmount = getcouponAmount(ordersCoupons);
        }
        // 获取退货商品数量
        List<RefundNumberStatistics> refundNumberStatisticsList = getRefundNumberStatistics(orderCode, ordersGoodsList);
        // 获取产品和商品信息
        List<Map<String, Object>> goodsList = gainProductAndGoods(orderCode, ordersGoodsList, refundNumberStatisticsList, limitMap);
        // 退款方式
        List<Map<String, Object>> paymentList = gainPaymentWay(orders);
        // 退款原因
        List<Map<String, Object>> reasonList = gainRefundReason("exchangeType");
        Map<String, Object> goodsListData = new LinkedHashMap<>();
        goodsListData.put("goods_list", goodsList);
        goodsListData.put("return_reason", reasonList);
        goodsListData.put("special_return_reason", gainRefundReason("specialRefundType"));
        goodsListData.put("special_notice", getSpecialNotice());
        goodsListData.put("return_amount_mode", paymentList);
        goodsListData.put("coupon_amount", formatRefundData(couponAmount));
        goodsListData.put("yoho_coin_num", formatRefundData(new BigDecimal(String.valueOf(orders.getYohoCoinNum() / 100.0))));
        // 获取退款金额信息
        String returnAmount = gainReturnAmount(orders, ordersCoupons, couponAmount);
        goodsListData.put("return_amount_info", returnAmount);
        return goodsListData;
    }

    /**
     * 获取特殊商品提示内容
     */
    private Map<String, String> getSpecialNotice() {
        Map<String, String> notice = new HashMap<String, String>();
        notice.put("title", "该商品暂不支持7天无理由退换");
        notice.put("remark", "1.考虑到个人卫生，例如内衣、内裤、袜子等贴身塑身类商品，不支持无理由退换货   2.香水、香薰、化妆品等特殊商品，无质量问题，不支持无理由退换货");
        return notice;
    }

    /**
     * 功能描述: 获得退货商品所有skn列表
     * 〈功能详细描述〉
     *
     * @param ordersGoodsList
     */
    private Set<Integer> buildSknsByGoodsList(List<OrdersGoods> ordersGoodsList) {
        //这里不需要判断是否为空,判断逻辑已经有过了
        Set<Integer> sknSet = new HashSet<Integer>();
        for (OrdersGoods goods : ordersGoodsList) {
            sknSet.add(goods.getProductSkn());
        }
        return sknSet;
    }

    /**
     * 验证退货订单商品列表请求参数
     */
    private void validateGoodsListRequest(Integer uid, Long orderCode) {
        if (uid == null || uid < 1) {
            logger.warn("goodsList fail, invalid user ID is [{}].", uid);
            throw new ServiceException(ServiceError.ORDER_INVALID_USER_ID);
        }
        if (orderCode == null) {
            logger.warn("goodsList fail, invalid order code is [{}].", orderCode);
            throw new ServiceException(ServiceError.ORDER_ORDER_NUMBER_ERROR);
        }
    }

    /**
     * 验证订单参数
     */
    private void validateOrderParameter(Orders orders) {
        if (orders == null) {
            logger.warn("goodsList fail, the order does not exist");
            throw new ServiceException(ServiceError.ORDER_DOES_NOT_EXIST);
        }
        // 验证订单是否被删除
        if (orders.getOrdersStatus() != 1) {
            logger.warn("goodsList fail, the order has been deleted,orderCode {}", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_HAS_BEEN_DELETED);
        }
        // 验证订单状态
        if (orders.getStatus() != 6) {
            logger.warn("goodsList fail, the order not confirmed receipt and cannot be returned,orderCode {}", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_NOT_CONFIRMED_CANNOT_RETURNED);
        }
        // 退货时间验证
        if ((int) (System.currentTimeMillis() / 1000) - orders.getUpdateTime() > SEVEN_DAYS_RETURN) {
            logger.warn("goodsList fail, your order has exceeded the return period,orderCode {}", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_EXCEEDED_RETURN_PERIOD);
        }
    }

    /**
     * 获取该订单使用的优惠券数量
     */
    private BigDecimal getcouponAmount(OrdersCoupons ordersCoupons) {
        BigDecimal couponAmount = new BigDecimal("0.00");
        // 获得一条优惠券
        CouponsReq couponsReq = new CouponsReq();
        ArrayList<Integer> couponsIds = new ArrayList<>();
        couponsIds.add(ordersCoupons.getCouponsId());
        couponsReq.setIds(couponsIds);
        List<CouponsBo> couponsBoList = Arrays.asList(serviceCaller.call("promotion.queryCouponList", couponsReq, CouponsBo[].class));
        if (CollectionUtils.isNotEmpty(couponsBoList)) {
            CouponsBo couponsBo = couponsBoList.get(0);
            couponAmount = couponsBo.getCouponAmount();
        }
        return couponAmount;
    }

    /**
     * 批量获取退货商品数量
     */
    private List<RefundNumberStatistics> getRefundNumberStatistics(Long orderCode, List<OrdersGoods> ordersGoodsList) {
        List<RefundNumber> refundNumberList = new ArrayList<>();
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            RefundNumber refundNumber = new RefundNumber();
            refundNumber.setProductSku(ordersGoods.getErpSkuId());
            refundNumber.setLastPrice(ordersGoods.getGoodsPrice());
            refundNumberList.add(refundNumber);
        }
        // 执行批量查询，获取批量退货商品数量
        return refundGoodsListDao.selectRefundNumberStatistics(orderCode, refundNumberList);
    }

    /**
     * @param refundNumberStatisticsList
     * @return
     */
    private MultiKeyMap refundNumberStatisticsList2Map(List<RefundNumberStatistics> refundNumberStatisticsList) {
        MultiKeyMap refundMap = new MultiKeyMap();
        for (RefundNumberStatistics refundNumberStatistics : refundNumberStatisticsList) {
            if (refundMap.containsKey(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice())) {
                Integer num = (Integer) refundMap.get(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice());
                refundMap.put(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice(), num + refundNumberStatistics.getNumber());
            } else {
                refundMap.put(refundNumberStatistics.getOrderCode(), refundNumberStatistics.getProductSku(), refundNumberStatistics.getLastPrice(), refundNumberStatistics.getNumber());
            }
        }
        return refundMap;
    }

    /**
     * 获取产品和商品信息
     */
    private List<Map<String, Object>> gainProductAndGoods(Long orderCode, List<OrdersGoods> ordersGoodsList, List<RefundNumberStatistics> refundNumberStatisticsList,
                                                          Map<Integer, Integer> limitMap) {
        List<RefundProductBo> refundProductBoList = queryRefundProductByIds(ordersGoodsList);
        List<Map<String, Object>> goodsList = new ArrayList<>();
        MultiKeyMap refundMap = refundNumberStatisticsList2Map(refundNumberStatisticsList);

        for (OrdersGoods ordersGoods : ordersGoodsList) {
            int orderGoodsNum = ordersGoods.getNum();
            //能退的商品数量
            int availableRefundNumber = 0;
            int alreadyRefundNumer = 0;
            if (refundMap.containsKey(orderCode, ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice())) {
                alreadyRefundNumer = (Integer) refundMap.get(orderCode, ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice());
            }
            //当前商品数量 比 已经退的商品数量小
            if (orderGoodsNum <= alreadyRefundNumer) {
                alreadyRefundNumer = alreadyRefundNumer - orderGoodsNum;
                refundMap.put(orderCode, ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice(), alreadyRefundNumer);
                continue;
            } else {
                availableRefundNumber = orderGoodsNum - alreadyRefundNumer;
                alreadyRefundNumer = 0;
                refundMap.put(orderCode, ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice(), alreadyRefundNumer);
            }
            // 产品信息
            ProductBo productBo = getProductBoByProductId(ordersGoods.getProductId(), refundProductBoList);
            if (productBo == null) {
                continue;
            }
            GoodsBo goodsBo = getGoodsBoByGoodsIdAndProductId(ordersGoods.getGoodsId(), ordersGoods.getProductId(), refundProductBoList);
            if (goodsBo == null) {
                continue;
            }
            // 产品图片
            String goodsImage = goodsBo.getColorImage();
            int hasShoes = 0;
            if (productBo.getMaxSortId() == 6) {
                hasShoes = 1;
            }
            for (int i = 0; i < availableRefundNumber; i++) {
                Map<String, Object> goodsMap = new LinkedHashMap<>();
                goodsMap.put("product_name", productBo.getProductName());
                goodsMap.put("product_skn", productBo.getErpProductId());
                //增加商品skn是否是特殊商品,需要限制其退换货
                if (limitMap == null || limitMap.get(productBo.getErpProductId()) == null) {
                    goodsMap.put("is_limit_skn", "N");//1表明无限制,0表示有限制
                } else {
                    goodsMap.put("is_limit_skn", limitMap.get(productBo.getErpProductId()) == 0 ? "Y" : "N");
                }
                goodsMap.put("product_sku", ordersGoods.getErpSkuId());
                goodsMap.put("product_skc", goodsBo.getProductSkc());
                goodsMap.put("size_name", ordersGoods.getSizeName());
                goodsMap.put("color_name", ordersGoods.getColorName());
                goodsMap.put("goods_id", ordersGoods.getGoodsId());
                goodsMap.put("product_id", ordersGoods.getProductId());
                goodsMap.put("goods_image", goodsImage);
                goodsMap.put("hasShoes", hasShoes);
                goodsMap.put("goods_type_id", ordersGoods.getGoodsType());
                String goodsType = ordersGoods.getGoodsType() == null ? "" : (String) ((LinkedHashMap) RefundUtils.getRefundMap("orderGoodsType").get(
                        RefundUtils.convertKeyToYAML(String.valueOf(ordersGoods.getGoodsType())))).get(RefundUtils.convertKeyToYAML("en"));
                goodsMap.put("goods_type", goodsType);
                goodsMap.put("last_price", formatRefundData(ordersGoods.getGoodsPrice()));
                goodsList.add(goodsMap);
            }
        }
        return goodsList;
    }

    /**
     * 查询商品和产品信息
     */
    private List<RefundProductBo> queryRefundProductByIds(List<OrdersGoods> ordersGoodsList) {
        BatchProductOrderRequest batchProductOrderRequest = new BatchProductOrderRequest();
        List<ProductOrderRequest> productOrderRequestList = new ArrayList<>();
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            ProductOrderRequest productOrderRequest = new ProductOrderRequest();
            productOrderRequest.setGoodsId(ordersGoods.getGoodsId());
            productOrderRequest.setProductId(ordersGoods.getProductId());
            productOrderRequestList.add(productOrderRequest);
        }
        batchProductOrderRequest.setProductOrderRequest(productOrderRequestList);
        // 执行批量查询，获取商品和产品信息
        return Arrays.asList(serviceCaller.call("product.queryRefundProductByIds", batchProductOrderRequest, RefundProductBo[].class));
    }

    /**
     * 退款方式
     */
    private List<Map<String, Object>> gainPaymentWay(Orders orders) {
        List<Map<String, Object>> paymentList;
        if (orders.getPaymentType() == 1) {
            paymentList = new ArrayList<>();
            Map<String, Object> paymentMap = new HashMap<>();
            paymentMap.put("id", 1);
            paymentMap.put("name", "原卡返还");
            paymentMap.put("is_default", "Y");
            paymentList.add(paymentMap);
        } else {
            paymentList = new ArrayList<>();
            Map<String, Object> paymentMap = new HashMap<>();
            paymentMap.put("id", 2);
            paymentMap.put("name", "银行卡");
            paymentMap.put("is_default", "Y");
            paymentList.add(paymentMap);
            paymentMap = new HashMap<>();
            paymentMap.put("id", 3);
            paymentMap.put("name", "支付宝");
            paymentMap.put("is_default", "N");
            paymentList.add(paymentMap);
        }
        return paymentList;
    }

    /**
     * 退款原因"exchangeType"
     * 特殊原因"specialRefundType"
     *
     * @add by maelk-liu
     */
    private List<Map<String, Object>> gainRefundReason(String key) {
        List<Map<String, Object>> reasonList = new ArrayList<>();
        // 退货类型 通过yml操作工具类获取key为exchangeType的yml配置内容
        Map<String, Object> exchangeType = RefundUtils.getRefundMap(key);
        for (Map.Entry<String, Object> exchangeTypeEntry : exchangeType.entrySet()) {
            Map<String, Object> reasonMap = new LinkedHashMap<>();
            // 对于数字，yml返回的Key会自动加上[]中括号
            String exchangeTypeKey = exchangeTypeEntry.getKey();
            String exchangeTypeRealKey = exchangeTypeKey.substring(1, exchangeTypeKey.lastIndexOf(']'));
            reasonMap.put("id", Integer.parseInt(exchangeTypeRealKey));
            reasonMap.put("name", exchangeTypeEntry.getValue());
            reasonList.add(reasonMap);
        }
        return reasonList;
    }

    /**
     * 退货数据格式化
     */
    private String formatRefundData(BigDecimal bigDecimal) {
        BigDecimal scaleBigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        DecimalFormat df = new DecimalFormat();
        String style = "0.00";
        df.applyPattern(style);
        return df.format(scaleBigDecimal);
    }

    /**
     * 获取退款金额信息
     */
    private String gainReturnAmount(Orders orders, OrdersCoupons ordersCoupons, BigDecimal couponAmount) {
        String text = "";
        if (orders.getYohoCoinNum() != null && orders.getYohoCoinNum() != 0) {
            text += "￥" + formatRefundData(new BigDecimal(String.valueOf(orders.getYohoCoinNum() / 100.0))) + "YOHO币";
        }
        if (ordersCoupons != null) {
            if (StringUtils.isNotEmpty(text)) {
                text += ",面值" + formatRefundData(couponAmount) + "元的优惠券";
            } else {
                text += "面值" + formatRefundData(couponAmount) + "元的优惠券";
            }
        }
        String returnAmount = "";
        if (StringUtils.isNotEmpty(text)) {
            returnAmount = "该订单已经使用" + StringUtils.trim(text);
        }
        return returnAmount;
    }

    /**
     * 提交退货申请
     */
    public Map<String, Object> submit(Long orderCode, Integer uid, String areaCode, List<Goods> goodsList, Payment payment) {
        logger.info("submit by orderCode [{}],uid [{}],areaCode [{}],goodsList [{}], and payment [{}].", orderCode, uid, areaCode, goodsList, payment);
        // 验证退货申请参数
        validateSubmitRequest(orderCode, uid, goodsList, payment);
        // 查询可以退货的订单
        Orders orders = findCanSubmitOrdersByOrderCodeAndUid(orderCode, uid);
        // 验证退货商品数量
        validateSubmitRefundGoodsNumbers(orders, goodsList);
        // 验证提交退货申请的重复提交
        validateSubmitDuplicate(orderCode);
        // 创建退货订单
        JSONObject refundOrder = createRefundOrder(orderCode, uid, areaCode, payment);
        // 创建退货订单列表
        List<Map<String, Object>> refundGoodsList = createRefundGoodsList(orderCode, goodsList);
        if (IErpService.ERP_SERVICE_TYPE_MQ.equals(erpOrderRefundSyncType)) {
            refundOrder.put("erpRefundId", 0);
            refundOrder.put("status", 0);
            refundOrder.put("returnAmount", 0);
            refundOrder.put("isReturnCoupon", "N");
            refundOrder.put("returnYohoCoin", 0);
            // 保存退货订单
            Map<String, Object> refundResultMap = saveRefundOrder(orderCode, uid, refundOrder, refundGoodsList);
            refundOrder.put("order_returned_id", refundResultMap.get("apply_id"));
            //发送消息给Mq
            JSONObject res = refundGoods(refundOrder, refundGoodsList, false);
            if ("fail".equals(res.getString("sendResult"))) {
                logger.warn("send mq message fail,service:Refund-submit.params is :orderCode :{} ,uid [{}],areaCode [{}],and payment [{}]", orderCode, uid, areaCode, payment);
            }
            return refundResultMap;
        }
        // ERP系统保存退货订单
        JSONObject res = refundGoods(refundOrder, refundGoodsList, true);
        if (res.getIntValue("code") == 200) {
            refundOrder.put("erpRefundId", res.getJSONObject("data").get("returned_id"));
            refundOrder.put("status", res.getJSONObject("data").get("returned_status"));
            refundOrder.put("returnAmount", res.getJSONObject("data").get("real_returned_amount"));
            refundOrder.put("isReturnCoupon", res.getJSONObject("data").get("is_return_coupon"));
            refundOrder.put("returnYohoCoin", res.getJSONObject("data").get("return_yoho_coin"));
            // 保存退货订单
            return saveRefundOrder(orderCode, uid, refundOrder, refundGoodsList);
        } else {
            logger.warn("submit fail, don't create erp credit order");
            String message = res.getString("message");
            ServiceException serviceException = new ServiceException(ServiceError.ORDER_SUBMIT_CALL_ERP_RESULT);
            serviceException.setParams(message);
            throw serviceException;
        }
    }

    /**
     * 根据erp回传的mq消息同步前台表 创建退货信息
     *
     * @param order_returned_id 前台表退货订单唯一主键
     * @param erpRefundId       erp退货订单唯一主键
     * @param status            erp退货状态
     * @param returnAmount      退金额
     * @param isReturnCoupon    是否退红包
     * @param returnYohoCoin    退有货币
     * @author maelk-liu
     */
    public void syncRefundStatus(int order_returned_id, int erpRefundId, int status, double returnAmount, String isReturnCoupon, int returnYohoCoin) {
        logger.info("syncRefundStatus order_returned_id[{}], erpRefundId [{}],status [{}],returnAmount [{}],isReturnCoupon [{}], and returnYohoCoin [{}].",
                order_returned_id, erpRefundId, status, returnAmount, isReturnCoupon, returnYohoCoin);
        //根据主键查refund_goods表
        RefundGoods refg = refundGoodsDao.selectByPrimaryKey(order_returned_id);
        if(refg.getErpRefundId()!=null&&refg.getErpRefundId().intValue()>0){
            //防止出现erp返回多次消息,导致前台退货单被取消
            logger.warn("syncRefundStatus fail, because this refund has updated,order_returned_id:{},alreadyErpId:{},nowErpId:{}", order_returned_id,
                    refg.getErpRefundId(),erpRefundId);
            return;
        }
        if (refg == null) {
            logger.warn("syncRefundStatus fail, beacuse queryRefundGoodsById is null,order_returned_id:{}", order_returned_id);
            throw new ServiceException(ServiceError.ORDER_REFUND_QUERY_ORDER_GOODS_NULL);
        }
        //这里说明是顾客已经主动取消了,所以需要发送一个mq消息给erp
        if (refg.getStatus() == 91) {
            logger.info("user:uid {} has cancel refund request, refundId {},orderCode {}", refg.getUid(), order_returned_id, refg.getOrderCode());
            JSONObject obj = new JSONObject();
            obj.put("id", refg.getErpRefundId());
            obj.put("status", 900);
            obj.put("type", 1);//1表示退货
            orderMqService.sendChangeRefundCancelMessage(obj);
            return;
        }
        //更新refund_goods表
        refg.setErpRefundId(erpRefundId);
        refg.setStatus((byte) status);
        refg.setReturnAmount(new BigDecimal(returnAmount));
        refg.setIsReturnCoupon(isReturnCoupon);
        refg.setReturnYohoCoin(returnYohoCoin);
        refundGoodsDao.updateByPrimaryKey(refg);
        //更新refund_goods_list表
        updateRefundGoodsListStatus(refg.getId(), status);
    }

    /**
     * 根据erp回传的mq消息对前台表 更新退货状态
     *
     * @param erpRefundId erp退货订单唯一主键
     * @param status      erp退货状态,同步给前台用
     * @author maelk-liu
     */
    public void syncRefundStatus(int erpRefundId, int status, double returnAmount, String isReturnCoupon, int returnYohoCoin) {
        logger.warn("sync refund status, erpRefundId is {} status is {}.", erpRefundId, status);
        RefundGoods refundGoods = refundGoodsDao.selectByErpRefundId(erpRefundId);
        if (refundGoods == null) {
            logger.warn("sync refund status fail, beacuse can not find refund goods by erpRefundId {}.", erpRefundId);
            throw new ServiceException(ServiceError.ORDER_REFUND_QUERY_ORDER_GOODS_NULL);
        }
        logger.info("sync refund status {} start, erpRefundId is {} status is {}.", refundGoods.getOrderCode(), erpRefundId, status);
        if (refundGoods.getChangePurchaseId() > 0) {
            logger.info("sync refund status {} fail, because this is change order, erpRefundId is {} status is {}.", refundGoods.getOrderCode(), erpRefundId, status);
            return;
        }
        if (refundGoods.getStatus() == 40) {
            logger.warn("sync refund status {} fail, because this order has refunded, erpRefundId is {} status is {}.", refundGoods.getOrderCode(), erpRefundId, status);
            return;
        }
        if (refundGoods.getStatus() == 91) {
            logger.warn("sync refund status {} fail, because this order has reject, erpRefundId is {} status is {}.", refundGoods.getOrderCode(), erpRefundId, status);
            return;
        }
        // 更新换货单
        refundGoods.setStatus((byte) status);
        refundGoods.setReturnAmount(new BigDecimal(returnAmount));
        refundGoods.setIsReturnCoupon(isReturnCoupon);
        refundGoods.setReturnYohoCoin(returnYohoCoin);
        refundGoodsDao.updateByPrimaryKey(refundGoods);
        // 更新换货商品
        updateRefundGoodsListStatus(refundGoods.getId(), status);
        //erp同步状态为已退款并且前台表中状态不为40,才能退
        if (status == 40) {
            Long initOrderCode = getInitOrderCode(refundGoods.getOrderCode());
            Orders orders = ordersMapper.selectByOrderCode(initOrderCode.toString());
            //退优惠券
            if ("Y".equals(isReturnCoupon)) {
                logger.info("sync refund status {},  return coupon", orders.getOrderCode(), orders.getParentOrderCode());
                if (orders.getParentOrderCode() != null && orders.getParentOrderCode() > 0) {
                    orders = ordersMapper.selectByOrderCode(String.valueOf(orders.getParentOrderCode()));
                }
                refundOrderCouponUse(orders);
            }
            //退有货币
            if (returnYohoCoin > 0) {
                refundYohoCoin(orders, returnYohoCoin);
            }
            //冲红发票以及开具新的发票
            redbuildInvoice(orders.getId(), orders.getOrderCode(), returnAmount, refundGoods.getId());
        }
        logger.info("sync refund status {} success, erpRefundId is {} status is {}.", refundGoods.getOrderCode(), erpRefundId, status);
    }

    /**
     * 冲红并开新发票
     *
     * @param id
     * @param orderCode
     * @param amount
     * @param refundId
     */
    private void redbuildInvoice(int id, long orderCode, double amount, int refundId) {
        logger.info("begin build-red-Invoice,orderCode:{} ", orderCode);
        OrdersMeta meta = ordersMetaDAO.selectByOrdersIdAndMetaKey(id, OrdersMateKey.ELECTRONIC_INVOICE);
        //meta没数据,说明该笔订单不需要开电子发票
        if (null == meta || null == meta.getMetaValue()) {
            logger.info("this order need not buildInvoice,orderCode:{}", orderCode);
            return;
        }
        InvoiceBo bo = JSON.parseObject(meta.getMetaValue(), InvoiceBo.class);
        //电子发票
        if (bo.getType() == InvoiceType.electronic.getIntVal()) {
            OrderInvoiceBo orderInvoice = new OrderInvoiceBo();
            orderInvoice.setRefoundAmount(amount);
            orderInvoice.setOrderId(id);
            orderInvoice.setGoodsItemList(buildGoodsItems(refundId));
            //设置类别为退货
            orderInvoice.setOrderStatus(OrderStatus.refund);

            logger.info("{}.redbuildInvoice begin red Invoice, orderInvoice {}",getClass().getSimpleName(),orderInvoice);
            //冲红
            InvoiceBo invoiceBo = invoiceService.redInvoice((OrderInvoiceBo)orderInvoice.clone());
            logger.info("{}.redbuildInvoice end red Invoice, invoiceBo {}",getClass().getSimpleName(), invoiceBo);
            //开新发票
            if (invoiceBo != null && InvoiceSoapErrorCode.SUCCESS_KP.equalsIgnoreCase(invoiceBo.getReturnCode())){
                invoiceBo = invoiceService.issueInvoice(orderInvoice);
                logger.info("{}.redbuildInvoice do blue Invoice, invoiceBo {}",getClass().getSimpleName(), invoiceBo);
            }
        }
    }

    /**
     * 返回skn+num
     *
     * @param refundId
     */
    private List<GoodsItemBo> buildGoodsItems(int refundId) {
        List<RefundGoodsList> refundGoodsList = refundGoodsListDao.selectByRequestId(refundId);
        if (refundGoodsList == null || refundGoodsList.size() == 0) {
            return null;
        }
        List<GoodsItemBo> goodsItemList = new ArrayList<GoodsItemBo>();
        //numsMap存放skn和数量Num
        Map<Integer, Integer> numsMap = new HashMap<Integer, Integer>();
        for (RefundGoodsList goods : refundGoodsList) {
            if (numsMap.get(goods.getProductSkn()) == null) {
                numsMap.put(goods.getProductSkn(), 1);
            } else {
                //累加skn下的数量
                numsMap.put(goods.getProductSkn(), numsMap.get(goods.getProductSkn()) + 1);
            }
        }
        for (Integer key : numsMap.keySet()) {
            GoodsItemBo bo = new GoodsItemBo();
            bo.setSkn(key);
            bo.setBuyNumber(numsMap.get(key));
            goodsItemList.add(bo);
        }
        return goodsItemList;
    }

    /**
     * 退货 ：退有货币
     *
     * @param orders
     */
    private void refundYohoCoin(Orders orders, int returnYohoCoin) {
        logger.info("syncRefundStatus refundYohoCoin ,uid {} ,orderCode {},returnYohoCoinNum {},originalYohocoin {}",
                orders.getUid(), orders.getOrderCode(), returnYohoCoin, orders.getYohoCoinNum());
        if (orders.getYohoCoinNum() != null && orders.getYohoCoinNum() > 0) {
            YohoCoinReqBO yohoCoinReqBO = new YohoCoinReqBO();
            yohoCoinReqBO.setNum(returnYohoCoin);
            //加入jit拆单优化,如果OrderCode是子订单号,需要找到原订单号
            if (orders.getParentOrderCode() != null && orders.getParentOrderCode() > 0) {
                yohoCoinReqBO.setOrder_code(orders.getParentOrderCode());
            } else {
                yohoCoinReqBO.setOrder_code(orders.getOrderCode());
            }
            //退货类别
            //1初始化 2取消订单返回 3活动奖励 4充值 5签到奖励 6抽奖活动奖励  7调研问卷奖励 8完善资料奖励 9下单使用 10退货退换  11活动赠送 12抽奖使用 13购买商品赠送 14晒单奖励 15补差价 16积分过期 17兑换礼品卡
            yohoCoinReqBO.setType(10);
            yohoCoinReqBO.setUid(orders.getUid());
            yohoCoinReqBO.setPid(0);
            //调用远程服务
            serviceCaller.call("users.refundYohoCoin", yohoCoinReqBO, YohoCurrencyRspBO.class);
        }
    }

    /**
     * 退货 返回优惠券
     * 这里退优惠券退的是父订单的优惠券
     *
     * @param orders
     */
    private void refundOrderCouponUse(Orders orders) {
        OrdersCoupons ordersCoupons = ordersCouponsMapper.selectByOrderId(orders.getId());
        if (ordersCoupons == null) {
            logger.info("user {} not use coupons in order {},orderCode {}.", orders.getUid(), orders.getId(), orders.getOrderCode());
            return;
        }
        CouponsLogReq couponsLogReq = new CouponsLogReq();
        couponsLogReq.setUid(orders.getUid());
        couponsLogReq.setOrderCode(orders.getOrderCode());
        boolean result = serviceCaller.call("promotion.cancelOrderCouponUse", couponsLogReq, Boolean.class);
        if (result) {
            logger.info("refundOrderCouponUse success by order code {}, uid {}.", orders.getOrderCode(), orders.getUid());
        } else {
            logger.info("refundOrderCouponUse fail by order code {}, uid {}.", orders.getOrderCode(), orders.getUid());
        }
    }

    private Long getInitOrderCode(Long orderCode) {
        ChangeGoodsMainInfo changeGoods = changeGoodsMainMapper.selectByOrderCode(orderCode);
        if (changeGoods == null) {
            return orderCode;
        } else {
            logger.info("this is change order refund goods, order code is {} init order code is {}.", orderCode, changeGoods.getInitOrderCode());
            return changeGoods.getInitOrderCode();
        }

    }

    private void updateRefundGoodsListStatus(int id, int status) {
        RefundGoodsList record = new RefundGoodsList();
        record.setStatus((byte) status);
        record.setReturnRequestId(id);
        refundGoodsListDao.updateByReturned(record);
    }

    /**
     * 验证退货申请参数
     */
    private void validateSubmitRequest(Long orderCode, Integer uid, List<Goods> goodsList, Payment payment) {
        // 验证请求参数
        if (orderCode == null) {
            logger.warn("submit fail, invalid order code is [{}].", orderCode);
            throw new ServiceException(ServiceError.ORDER_NUMBER_ERROR);
        }
        //验证UID
        if (uid == null || uid < 1) {
            logger.warn("submit fail, invalid user ID is [{}].", uid);
            throw new ServiceException(ServiceError.ORDER_USER_ID_ERROR);
        }
        //验证退货商品
        if (CollectionUtils.isEmpty(goodsList)) {
            logger.warn("submit fail, goods list is empty.");
            throw new ServiceException(ServiceError.ORDER_SELECT_GOODS_TO_RETURN);
        }
        //验证退款设置
        if (payment == null) {
            logger.warn("submit fail, don't set refund payment.");
            throw new ServiceException(ServiceError.ORDER_SELECT_RETURN_SET);
        }
        //验证退款方式
        if (payment.getReturnAmountMode() == null) {
            logger.warn("submit fail, don't select refund mode.");
            throw new ServiceException(ServiceError.ORDER_SELECT_RETURN_METHOD);
        }
        //验证退款设置
        if (payment.getReturnAmountMode() == 2) {
            if (StringUtils.isEmpty(payment.getBankName()) || StringUtils.isEmpty(payment.getPayeeName()) || StringUtils.isEmpty(payment.getBankCard())) {
                logger.warn("submit fail, don't set up your bank card information.");
                throw new ServiceException(ServiceError.ORDER_SETUP_BANK_CARD_INFORMATION);
            }
        } else if (payment.getReturnAmountMode() == 3) {
            if (StringUtils.isEmpty(payment.getAlipayAccount()) || StringUtils.isEmpty(payment.getAlipayName())) {
                logger.warn("submit fail, don't set up alipay account");
                throw new ServiceException(ServiceError.ORDER_SETUP_ALIPAY_ACCOUNT);
            }
        }
    }

    /**
     * 查询可以退货的订单
     */
    private Orders findCanSubmitOrdersByOrderCodeAndUid(Long orderCode, Integer uid) {
        // 验证订单是否存在
        Orders orders = ordersMapper.selectByCodeAndUid(String.valueOf(orderCode), String.valueOf(uid));
        if (orders == null) {
            logger.warn("submit fail, because of the order {} does not exist.", orderCode);
            throw new ServiceException(ServiceError.ORDER_RETURN_ORDER_NOT_EXIST);
        }
        // 验证是否被删除
        if (orders.getOrdersStatus() == 0) {
            logger.warn("submit fail, because of the order {} has been deleted.", orderCode);
            throw new ServiceException(ServiceError.ORDER_ORDER_DELETE);
        }
        return orders;
    }

    /**
     * 验证退货商品数量
     */
    private void validateSubmitRefundGoodsNumbers(Orders orders, List<Goods> goodsList) {
        // 根据sku获得订单商品信息
        List<OrdersGoods> ordersGoodsList = getOrderGoodsBySku(orders.getId(), goodsList);
        List<RefundNumberStatistics> refundNumberStatisticsList = getRefundNumberStatistics(orders.getOrderCode(), ordersGoodsList);
        MultiKeyMap refundGoodsNumber = new MultiKeyMap();
        for (Goods goods : goodsList) {
            if (goods.getProductSkn() == null || goods.getProductSkc() == null || goods.getProductSku() == null) {
                logger.warn("submit fail, don't select a return goods");
                throw new ServiceException(ServiceError.ORDER_SELECT_RETURN_GOODS);
            }
            if (goods.getReturnedReason() == null || "all".equalsIgnoreCase(String.valueOf(goods.getReturnedReason()))) {
                logger.warn("submit fail, don't select the reason for return");
                throw new ServiceException(ServiceError.ORDER_SELECT_RETURN_REASON);
            }
            if ((goods.getReturnedReason() == 4 || goods.getReturnedReason() == 6 || goods.getReturnedReason() == 8) && CollectionUtils.isEmpty(goods.getEvidenceImages())) {
                logger.warn("submit fail, don't upload a real shot pictures");
                throw new ServiceException(ServiceError.ORDER_UPLOAD_REAL_PICTURE);
            }
            if ((goods.getReturnedReason() == 4 || goods.getReturnedReason() == 6 || goods.getReturnedReason() == 8) && StringUtils.isEmpty(goods.getRemark())) {
                logger.warn("submit fail, don't fill out the return instructions");
                throw new ServiceException(ServiceError.ORDER_FILLOUT_RETURN_INSTRUCTION);
            }
            OrdersGoods ordersGoods = getOrdersGoodsBySkuAndPrice(goods.getProductSku(), goods.getLastPrice(), ordersGoodsList);
            if (ordersGoods == null) {
                continue;
            }
            //本次退货商品数量
            if (refundGoodsNumber.containsKey(goods.getProductSku(), goods.getLastPrice())) {
                refundGoodsNumber.put(goods.getProductSku(), goods.getLastPrice(), (Integer) refundGoodsNumber.get(goods.getProductSku(), goods.getLastPrice()) + 1);
            } else {
                refundGoodsNumber.put(goods.getProductSku(), goods.getLastPrice(), 1);
            }
            //订单内商品总数 sku & price
            int ordersGoodsNum = countOrdersGoodsBySkuAndPrice(goods.getProductSku(), goods.getLastPrice(), ordersGoodsList);
            //已经退货商品数量 sku & price
            int alreadyRefundNum = getRefundNumber(orders.getOrderCode(), goods.getProductSku(), goods.getLastPrice(), refundNumberStatisticsList);

            int currentRefundNum = (Integer) refundGoodsNumber.get(goods.getProductSku(), goods.getLastPrice());
            //退货数量校验
            if ((ordersGoodsNum - alreadyRefundNum) < currentRefundNum) {
                logger.warn("submit fail, return amount exceeds , sku:{} ,ordersGoodsNum : {},alreadyRefundNum : {}",
                        goods.getProductSku(), ordersGoodsNum, alreadyRefundNum);
                throw new ServiceException(ServiceError.ORDER_RETURN_AMOUNT_EXCEED);
            }
        }
    }

    /**
     * 根据sku获取订单商品信息
     */
    private List<OrdersGoods> getOrderGoodsBySku(Integer ordersId, List<Goods> goodsList) {
        // 遍历获取批处理查询参数
        List<Map<String, String>> orderGoodsParams = new ArrayList<>();
        for (Goods goods : goodsList) {
            Map<String, String> orderGoodsParam = new HashMap<>();
            orderGoodsParam.put("erpSkuId", String.valueOf(goods.getProductSku()));
            orderGoodsParam.put("orderId", String.valueOf(ordersId));
            orderGoodsParams.add(orderGoodsParam);
        }
        // 执行批量查询，根据sku获取订单商品信息
        return ordersGoodsMapper.selectGoodsBySku(orderGoodsParams);
    }

    /**
     * 获取退货商品数量
     */
    private int getRefundNumber(Long orderCode, Integer erpSkuId, BigDecimal goodsPrice, List<RefundNumberStatistics> refundNumberStatisticsList) {
        for (RefundNumberStatistics refundNumberStatistics : refundNumberStatisticsList) {
            if (equals(orderCode, refundNumberStatistics.getOrderCode()) && equals(erpSkuId, refundNumberStatistics.getProductSku()) && equals(goodsPrice, refundNumberStatistics.getLastPrice())) {
                return refundNumberStatistics.getNumber();
            }
        }
        return 0;
    }


    /**
     * 获取退货商品数量by sku
     */
    private int getRefundNumberBySku(Long orderCode, Integer erpSkuId, List<RefundNumberStatistics> refundNumberStatisticsList) {
        int num = 0;
        for (RefundNumberStatistics refundNumberStatistics : refundNumberStatisticsList) {
            if (equals(orderCode, refundNumberStatistics.getOrderCode()) && equals(erpSkuId, refundNumberStatistics.getProductSku())) {
                num += refundNumberStatistics.getNumber();
            }
        }
        return num;
    }


    /**
     * 比较相等工具类
     */
    private boolean equals(Object expect, Object actual) {
        if (expect == null || actual == null) {
            return false;
        } else {
            return expect.equals(actual);
        }
    }

    /**
     * 根据ProductId获取产品信息
     */
    private ProductBo getProductBoByProductId(Integer productId, List<RefundProductBo> refundProductBoList) {
        for (RefundProductBo refundProductBo : refundProductBoList) {
            if (refundProductBo.getProductBo().getId().equals(productId)) {
                return refundProductBo.getProductBo();
            }
        }
        return null;
    }

    /**
     * 根据goodsId和ProductId获取商品信息
     */
    private GoodsBo getGoodsBoByGoodsIdAndProductId(Integer goodsId, Integer productId, List<RefundProductBo> refundProductBoList) {
        for (RefundProductBo refundProductBo : refundProductBoList) {
            if (refundProductBo.getGoodsBo().getId().equals(goodsId) && refundProductBo.getProductBo().getId().equals(productId)) {
                return refundProductBo.getGoodsBo();
            }
        }
        return null;
    }

    /**
     * 保存退货订单
     */
    private Map<String, Object> saveRefundOrder(Long orderCode, Integer uid, JSONObject refundOrder, List<Map<String, Object>> refundGoodsList) {
        try {
            // 保存退货订单
            Integer applyId = setRefund(orderCode, uid, refundOrder);
            if (applyId == null) {
                logger.warn("submit fail, don't create local credit order");
                throw new ServiceException(ServiceError.ORDER_FAILTO_LOCAL_CREDIT_ORDER);
            }
            // 保存退货订单列表
            saveRefundGoodsList(refundOrder, refundGoodsList, applyId);
            // 返回保存退货订单成功信息
            Map<String, Object> applyIdData = new HashMap<>();
            applyIdData.put("apply_id", applyId.toString());
            // 退货成功后，清除订单各类统计缓存
            userOrderCache.clearOrderCountCache(uid);
            return applyIdData;
        } catch (Exception e) {
            logger.warn("submit fail, don't create local credit order");
            throw new ServiceException(ServiceError.ORDER_FAILTO_CREDIT_ORDER);
        }
    }

    /**
     * 保存退货订单列表
     */
    private void saveRefundGoodsList(JSONObject refundOrder, List<Map<String, Object>> refundGoodsLists, Integer applyId) {
        List<RefundGoodsList> multiRefundGoodsList = new ArrayList<>();
        Map<RefundGoodsList, List<String>> temp = new HashMap<>();
        for (Map<String, Object> refundGoodsListMap : refundGoodsLists) {
            RefundGoodsList refundGoodsList = new RefundGoodsList();
            refundGoodsList.setReturnRequestId(applyId);
            refundGoodsList.setOrderCode((Long) refundGoodsListMap.get("order_code"));
            refundGoodsList.setProductSkn((Integer) refundGoodsListMap.get("product_skn"));
            refundGoodsList.setProductSkc((Integer) refundGoodsListMap.get("product_skc"));
            refundGoodsList.setProductSku((Integer) refundGoodsListMap.get("product_sku"));
            refundGoodsList.setGoodsType((Byte) refundGoodsListMap.get("goods_type"));
            refundGoodsList.setLastPrice(new BigDecimal((String) refundGoodsListMap.get("last_price")));
            refundGoodsList.setReturnedReason((Byte) refundGoodsListMap.get("returned_reason"));
            refundGoodsList.setRemark((String) refundGoodsListMap.get("remark"));
            refundGoodsList.setStatus(((Integer) refundOrder.get("status")).byteValue());
            refundGoodsListDao.insertRefundGoods(refundGoodsList);
            temp.put(refundGoodsList, (List) refundGoodsListMap.get("evidence_images"));
            multiRefundGoodsList.add(refundGoodsList);
        }
        List<ApplyGoodsImages> batchApplyGoodsImages = new ArrayList<>();
        for (RefundGoodsList refundGoodsList : multiRefundGoodsList) {
            Integer applyGoodsId = refundGoodsList.getId();
            List<String> imagePaths = (List) temp.get(refundGoodsList);
            if (CollectionUtils.isEmpty(imagePaths)) {
                continue;
            }
            for (String imagePath : imagePaths) {
                if (applyId < 1 || applyGoodsId < 1 || StringUtils.isEmpty(imagePath)) {
                    continue;
                }
                ApplyGoodsImages applyGoodsImages = new ApplyGoodsImages();
                applyGoodsImages.setApplyId(applyId);
                applyGoodsImages.setApplyGoodsId(applyGoodsId);
                applyGoodsImages.setImageType((byte) 1);
                applyGoodsImages.setImagePath(imagePath);
                batchApplyGoodsImages.add(applyGoodsImages);
            }
        }

        if (CollectionUtils.isNotEmpty(batchApplyGoodsImages)) {
            applyGoodsImagesDao.insertBatch(batchApplyGoodsImages);
        }
    }

    /**
     * 创建退货单
     */
    public int setRefund(Long orderCode, Integer uid, JSONObject refundOrder) {
        // 验证refundOrder必要参数
        String[] mustInputFilters = {"returnAmountMode", "returnMode", "returnShippingCost", "returnAmount", "sourceOrderCode", "erpRefundId"};
        for (String mustInputFilter : mustInputFilters) {
            if (refundOrder.get(mustInputFilter) == null) {
                ServiceException serviceException = new ServiceException(ServiceError.ORDER_MISSING_REQUIRED_FIELD);
                serviceException.setParams("退货订单数据包缺少 " + mustInputFilter + "字段");
                throw serviceException;
            }
        }
        RefundGoods refundGoods = JSONObject.parseObject(refundOrder.toJSONString(), RefundGoods.class);
        refundGoods.setOrderCode(orderCode);
        refundGoods.setUid(uid);
        refundGoods.setCreateTime((int) (System.currentTimeMillis() / 1000));
        refundGoodsDao.insertRefund(refundGoods);
        return refundGoods.getId();
    }

    /**
     * 验证提交退货申请的重复提交
     */
    private void validateSubmitDuplicate(Long orderCode) {
        boolean isFirstSubmit;
        try {
            isFirstSubmit = valueOperations.get(Constant.CACHE_ORDER_REFUND_CODE + orderCode) == null;
            if (isFirstSubmit) {
                valueOperations.set(Constant.CACHE_ORDER_REFUND_CODE + orderCode, String.valueOf(orderCode), 15, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            isFirstSubmit = true;
        }
        if (!isFirstSubmit) {
            logger.warn("submit fail, don't submit duplicate");
            throw new ServiceException(ServiceError.ORDER_NO_DUPLICATE_SUBMIT);
        }
    }

    /**
     * 创建退货订单
     */
    private JSONObject createRefundOrder(Long orderCode, Integer uid, String areaCode, Payment payment) {
        // 驼峰命名和后面必须参数对应
        JSONObject refundOrder = new JSONObject();
        refundOrder.put("uid", uid);
        refundOrder.put("initOrderCode", orderCode);
        refundOrder.put("sourceOrderCode", orderCode);
        refundOrder.put("changePurchaseId", 0);
        refundOrder.put("returnAmountMode", payment.getReturnAmountMode());
        // 寄回退货
        refundOrder.put("returnMode", 10);
        refundOrder.put("returnShippingCost", 0);
        refundOrder.put("returnAmount", 0);
        refundOrder.put("isReturnCoupon", "N");
        refundOrder.put("returnYohoCoin", 0);
        refundOrder.put("remark", payment.getRemark() == null ? "" : payment.getRemark());
        refundOrder.put("payeeName", StringUtils.isEmpty(payment.getPayeeName()) ? "" : payment.getPayeeName());
        refundOrder.put("areaCode", StringUtils.isEmpty(payment.getAreaCode()) ? 0 : payment.getAreaCode());
        AreaRspBo county = null;
        AreaRspBo city = null;
        AreaRspBo province = null;
        // 省市区
        if (StringUtils.isNotEmpty(areaCode)) {
            AreaReqBO areaReqBO = new AreaReqBO();
            areaReqBO.setCode(Integer.valueOf(areaCode));
            county = serviceCaller.call("users.getAreaByCode", areaReqBO, AreaRspBo.class);
            if (county != null) {
                city = county.getParent();
                if (city != null) {
                    province = city.getParent();
                }
            }
        }
        refundOrder.put("province", province == null ? "未知" : province.getCaption());
        refundOrder.put("city", city == null ? "" : city.getCaption());
        refundOrder.put("county", county == null ? "" : county.getCaption());
        refundOrder.put("bankName", StringUtils.isEmpty(payment.getBankName()) ? "" : payment.getBankName());
        refundOrder.put("bankCard", StringUtils.isEmpty(payment.getBankCard()) ? "" : payment.getBankCard());
        refundOrder.put("alipayAccount", StringUtils.isEmpty(payment.getAlipayAccount()) ? "" : payment.getAlipayAccount());
        refundOrder.put("alipayName", StringUtils.isEmpty(payment.getAlipayName()) ? "" : payment.getAlipayName());
        refundOrder.put("expressCompany", "");
        refundOrder.put("expressNumber", "");
        // 1为驳回
        refundOrder.put("reject", 0);
        refundOrder.put("erpRefundId", 0);
        refundOrder.put("status", 0);
        return refundOrder;
    }

    /**
     * 创建退货订单列表
     */
    private List<Map<String, Object>> createRefundGoodsList(Long orderCode, List<Goods> goodsList) {
        List<Map<String, Object>> refundGoodsList = new ArrayList<>();
        for (Goods goods : goodsList) {
            Map<String, Object> refundGoodsData = new LinkedHashMap<>();
            refundGoodsData.put("order_code", orderCode);
            refundGoodsData.put("product_skn", goods.getProductSkn());
            refundGoodsData.put("product_skc", goods.getProductSkc());
            refundGoodsData.put("product_sku", goods.getProductSku());
            refundGoodsData.put("goods_type", goods.getGoodsType());
            // 成交价
            refundGoodsData.put("last_price", formatRefundData(goods.getLastPrice()));
            refundGoodsData.put("returned_reason", goods.getReturnedReason());
            refundGoodsData.put("remark", StringUtils.isEmpty(goods.getRemark()) ? "" : goods.getRemark());
            refundGoodsData.put("evidence_images", CollectionUtils.isEmpty(goods.getEvidenceImages()) ? Collections.emptyList() : goods.getEvidenceImages());
            refundGoodsList.add(refundGoodsData);
        }
        return refundGoodsList;
    }

    /**
     * 向ERP系统提交退货数据
     */
    private JSONObject refundGoods(JSONObject refundOrder, List<Map<String, Object>> refundGoodsList, Boolean submitType) {
        // 构建请求参数
        JSONObject refundData = new JSONObject();
        refundData.put("order_returned_id", refundOrder.get("order_returned_id"));
        refundData.put("order_code", refundOrder.get("sourceOrderCode"));
        refundData.put("uid", refundOrder.get("uid"));
        //0、无  1、原卡返回 2、银行 3、支付宝 4、YOHO币
        refundData.put("return_amount_mode", refundOrder.get("returnAmountMode"));
        refundData.put("remark", refundOrder.get("remark"));
        refundData.put("payee_name", refundOrder.get("payeeName"));
        refundData.put("area_code", refundOrder.get("areaCode"));
        refundData.put("province", refundOrder.get("province"));
        refundData.put("city", refundOrder.get("city"));
        refundData.put("county", refundOrder.get("county"));
        refundData.put("bank_name", refundOrder.get("bankName"));
        refundData.put("bank_card", refundOrder.get("bankCard"));
        refundData.put("alipay_account", refundOrder.get("alipayAccount"));
        refundData.put("alipay_name", refundOrder.get("alipayName"));
        List<Map<String, Object>> returnedGoodsList = new ArrayList<>();
        for (Map<String, Object> refundGoodsMap : refundGoodsList) {
            Map<String, Object> returnedGoodsMap = new LinkedHashMap<>();
            returnedGoodsMap.put("product_sku", refundGoodsMap.get("product_sku"));
            returnedGoodsMap.put("goods_type", refundGoodsMap.get("goods_type"));
            returnedGoodsMap.put("last_price", refundGoodsMap.get("last_price"));
            returnedGoodsMap.put("returned_reason", refundGoodsMap.get("returned_reason"));
            returnedGoodsMap.put("remark", refundGoodsMap.get("remark"));
            returnedGoodsMap.put("evidence_images", refundGoodsMap.get("evidence_images"));
            returnedGoodsList.add(returnedGoodsMap);
        }
        refundData.put("returned_goods_list", returnedGoodsList);
        if (submitType) {
            //在erp系统提交退货申请
            JSONObject retData = erpService.refundGoods(refundData);
            return retData;
        }
        //发mq消息至erp
        JSONObject retData = mqErpService.refundGoods(refundData);
        return retData;
    }

    /**
     * 退货详情
     */
    public Map<String, Object> detail(Integer id, Integer uid) {
        logger.info("refund detail by id [{}] and uid [{}].", id, uid);
        // 验证退货详情请求参数
        validateRefundDetailRequest(id, uid);
        // 获取退货记录
        RefundGoodsBO refundGoodsBO = getByIdAndUid(id, uid);
        if (refundGoodsBO == null) {
            logger.warn("detail fail, refund request record does not exist");
            throw new ServiceException(ServiceError.ORDER_REFUND_REQUEST_NOT_EXIST);
        }
        // 获取订单
        Orders orders = yohoOrderService.getOrderByCode(refundGoodsBO.getSourceOrderCode().toString());

        if (orders == null) {
            logger.warn("detail fail, without a refund order goods");
            throw new ServiceException(ServiceError.ORDER_NO_REFUND_GOODS);
        }
        // 获取退货商品
        List<RefundGoodsListBO> refundGoodsListBOList = getByRequestId(uid, refundGoodsBO.getId());
        if (CollectionUtils.isEmpty(refundGoodsListBOList)) {
            logger.warn("detail fail, without refund request goods");
            throw new ServiceException(ServiceError.ORDER_NO_REFUND_REQUEST_GOODS);
        }
        Byte status = refundGoodsBO.getStatus();
        // 获取退货状态列表
        List<Map<String, String>> statusList = getStatusList(status);
        String createTime = CalendarUtils.parsefomatSeconds(refundGoodsBO.getCreateTime(), CalendarUtils.LONG_FORMAT_LINE);
        // 根据sku获得订单商品信息
        List<OrdersGoods> ordersGoodsList = getOrderGoodsBySku(orders.getId().toString(), refundGoodsListBOList);
        // 根据产品skn获取产品信息
        List<ProductBo> productBoList = getProductBoByProductSkn(refundGoodsListBOList);
        // 获取商品ID集合
        List<Integer> goodsIds = getGoodsIds(orders, refundGoodsListBOList, ordersGoodsList);
        // 通过商品ID获取商品集合
        List<GoodsBo> goodsBoList = getGoodsByGoodsIds(goodsIds);
        // 获取退货商品列表
        List<Map<String, Object>> goodsList = gainRefundGoodsList(refundGoodsListBOList, ordersGoodsList, productBoList, goodsBoList);
        // 获取该订单使用的优惠券
        OrdersCoupons ordersCoupons = ordersCouponsMapper.selectByOrderId(orders.getId());
        BigDecimal couponAmount = new BigDecimal("0.00");
        if (ordersCoupons != null) {
            // 获取该订单使用的优惠券数量
            couponAmount = getcouponAmount(ordersCoupons);
        }
        // 退款方式
        Map<String, Object> refundType = RefundUtils.getRefundMap("refundType");
        // 退货状态
        Map<String, Object> refundStatus = RefundUtils.getRefundMap("refundStatus");
        Map<String, Object> refundGoodsMap = new LinkedHashMap<>();
        OrderStatusDesc orderStatusDesc = OrderStatusDesc.valueOf(orders.getPaymentType().intValue(), orders.getStatus());
        refundGoodsMap.put("source_payment_type", orders.getPaymentType());
        refundGoodsMap.put("source_payment_type_desc", orderStatusDesc.getPaymentTypeDesc());
        refundGoodsMap.put("source_order_code", refundGoodsBO.getSourceOrderCode());
        refundGoodsMap.put("use_yoho_coin_num", formatRefundData(new BigDecimal(String.valueOf(orders.getYohoCoinNum() / 100.0))));
        refundGoodsMap.put("status", status);
        String statusName;
        if (refundStatus.get(RefundUtils.convertKeyToYAML(String.valueOf(status))) == null) {
            statusName = "";
        } else {
            statusName = (String) refundStatus.get(RefundUtils.convertKeyToYAML(String.valueOf(status)));
        }
        refundGoodsMap.put("status_name", statusName);
        refundGoodsMap.put("statusList", statusList);
        refundGoodsMap.put("create_time", createTime);
        String returnAmountModeName = refundGoodsBO.getReturnAmountMode() == null ? "" : (String) refundType.get(RefundUtils.convertKeyToYAML(String.valueOf(refundGoodsBO.getReturnAmountMode())));
        refundGoodsMap.put("return_amount_mode_name", returnAmountModeName);
        refundGoodsMap.put("return_amount_mode", refundGoodsBO.getReturnAmountMode());
        refundGoodsMap.put("return_yoho_coin", formatRefundData(new BigDecimal(String.valueOf(refundGoodsBO.getReturnYohoCoin() / 100.0))));
        refundGoodsMap.put("goods_list", goodsList);
        // 获取退换货状态提示内容
        refundGoodsMap.put("notice", getRefundNotice("refund", status, refundGoodsBO.getExpressCompany(), refundGoodsBO.getExpressNumber(), refundGoodsBO.getExpressId()));
        refundGoodsMap.put("return_amount", formatRefundData(refundGoodsBO.getReturnAmount()));
        refundGoodsMap.put("return_coupon_amount", refundGoodsBO.getIsReturnCoupon().equalsIgnoreCase("Y") ? formatRefundData(couponAmount) : formatRefundData(new BigDecimal("0.00")));
        refundGoodsMap.put("express_number", refundGoodsBO.getExpressNumber());
        String returnAmountTotal = formatRefundData(refundGoodsBO.getReturnAmount().add(new BigDecimal(String.valueOf(refundGoodsBO.getReturnYohoCoin() / 100.0)))
                .add(refundGoodsBO.getIsReturnCoupon().equalsIgnoreCase("Y") ? couponAmount : new BigDecimal("0.00")));
        refundGoodsMap.put("return_amount_total", returnAmountTotal);
        // 获取退款金额信息
        String returnAmount = gainReturnAmount(orders, ordersCoupons, couponAmount);
        refundGoodsMap.put("return_amount_info", returnAmount);
        // 银行卡信息
        gainPaymentInfo(refundGoodsBO, refundGoodsMap);
        //返回id,uid 用于前端可能存在的取消操作   id:退货表Id 
        refundGoodsMap.put("id", id);
        refundGoodsMap.put("uid", uid);
        //退货在审核之后可以取消,填写物流信息之后不可以取消
        if (status > 10) {
            refundGoodsMap.put("canCancel", "N");
        } else {
            refundGoodsMap.put("canCancel", "Y");
        }
        return refundGoodsMap;
    }

    /**
     * 验证退货详情请求参数
     */
    private void validateRefundDetailRequest(Integer id, Integer uid) {
        if (id == null || id < 1) {
            logger.warn("detail fail, invalid refund request ID is [{}].", id);
            throw new ServiceException(ServiceError.ORDER_REFUND_REQUEST_ID_ERROR);
        }
        if (uid == null || uid < 1) {
            logger.warn("detail fail, invalid user ID is [{}].", uid);
            throw new ServiceException(ServiceError.ORDER_UID_ERROR);
        }
    }

    /**
     * 通过id和uid获取退货记录
     */
    private RefundGoodsBO getByIdAndUid(Integer id, Integer uid) {
        if (id < 1) {
            return null;
        }
        RefundGoods refundGoods = refundGoodsDao.selectByIdAndUid(id, uid);
        RefundGoodsBO refundGoodsBO = null;
        if (refundGoods != null) {
            refundGoodsBO = new RefundGoodsBO();
            beanConvert.convertFrom(refundGoods, refundGoodsBO, RefundGoodsBO.class);
        }
        return refundGoodsBO;
    }

    /**
     * 通过申请单ID获取退货商品
     */
    private List<RefundGoodsListBO> getByRequestId(Integer uid, Integer returnRequestId) {
        if (uid < 1 || returnRequestId < 1) {
            return new ArrayList<>();
        }

        List<RefundGoodsList> refundGoodsLists = refundGoodsListDao.selectByRequestId(returnRequestId);
        List<RefundGoodsListBO> refundGoodsListBOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(refundGoodsLists)) {
            beanConvert.convertFromBatch(refundGoodsLists, refundGoodsListBOList, RefundGoodsListBO.class);
        }
        return refundGoodsListBOList;
    }

    /**
     * 获取退货状态列表
     */
    private List<Map<String, String>> getStatusList(Byte status) {
        List<Map<String, String>> statusList = new ArrayList<>();
        if (status == 91) {
            Map<String, String> statusMap = new LinkedHashMap<>();
            statusMap.put("name", "申请提交");
            statusMap.put("act", "Y");
            statusList.add(statusMap);
            statusMap = new LinkedHashMap<>();
            statusMap.put("name", "已取消");
            statusMap.put("act", "Y");
            statusList.add(statusMap);
            return statusList;
        }
        // 退货状态 通过yml操作工具类获取key为refundStatus的yml配置内容
        Map<String, Object> returnsStatus = RefundUtils.getRefundMap("refundStatus");
        for (Map.Entry<String, Object> returnStatusEntry : returnsStatus.entrySet()) {
            // 对于数字，yml返回的Key会自动加上[]中括号
            String returnStatusKey = returnStatusEntry.getKey();
            String returnStatusRealKey = returnStatusKey.substring(1, returnStatusKey.lastIndexOf(']'));
            if (Integer.parseInt(returnStatusRealKey) == 91) {
                continue;
            }
            Map<String, String> statusMap = new LinkedHashMap<>();
            statusMap.put("name", (String) returnStatusEntry.getValue());
            if (Byte.parseByte(returnStatusRealKey) <= status) {
                statusMap.put("act", "Y");
            } else {
                statusMap.put("act", "N");
            }
            statusList.add(statusMap);
        }
        return statusList;
    }

    /**
     * 根据sku获得订单商品集合
     */
    private List<OrdersGoods> getOrderGoodsBySku(String ordersId, List<RefundGoodsListBO> refundGoodsListBOList) {
        // 遍历获取批处理查询参数
        List<Map<String, String>> orderGoodsParams = new ArrayList<>();
        for (RefundGoodsListBO refundGoodsListBO : refundGoodsListBOList) {
            Map<String, String> orderGoodsParam = new HashMap<>();
            orderGoodsParam.put("erpSkuId", String.valueOf(refundGoodsListBO.getProductSku()));
            orderGoodsParam.put("orderId", ordersId);
            orderGoodsParams.add(orderGoodsParam);
        }
        // 执行批量查询，根据sku获取订单商品信息
        return ordersGoodsMapper.selectGoodsBySku(orderGoodsParams);
    }

    /**
     * 根据产品skn获取产品集合
     */
    private List<ProductBo> getProductBoByProductSkn(List<RefundGoodsListBO> refundGoodsListBOList) {
        BatchBaseRequest<Integer> requestSkns = new BatchBaseRequest<>();
        List<Integer> skns = new ArrayList<>();
        for (RefundGoodsListBO refundGoodsListBO : refundGoodsListBOList) {
            skns.add(refundGoodsListBO.getProductSkn());
        }
        requestSkns.setParams(skns);
        return Arrays.asList(serviceCaller.call("product.batchQueryProductBasicInfo", requestSkns, ProductBo[].class));
    }

    /**
     * 获取商品ID集合
     */
    private List<Integer> getGoodsIds(Orders orders, List<RefundGoodsListBO> refundGoodsListBOList, List<OrdersGoods> ordersGoodsList) {
        List<Integer> goodsIds = new ArrayList<>();
        for (RefundGoodsListBO refundGoodsListBO : refundGoodsListBOList) {
            for (OrdersGoods ordersGoods : ordersGoodsList) {
                if (ordersGoods.getErpSkuId().equals(refundGoodsListBO.getProductSku()) && ordersGoods.getOrderId().equals(orders.getId())) {
                    Integer goodsId = ordersGoods.getGoodsId() == null ? 0 : ordersGoods.getGoodsId();
                    goodsIds.add(goodsId);
                }
            }
        }
        return goodsIds;
    }

    /**
     * 通过商品ID获取商品集合
     */
    private List<GoodsBo> getGoodsByGoodsIds(List<Integer> goodsIds) {
        if (goodsIds.isEmpty()) {
            return Collections.emptyList();
        }
        BatchBaseRequest<Integer> requestGoodsIds = new BatchBaseRequest<>();
        requestGoodsIds.setParams(goodsIds);
        return Arrays.asList(serviceCaller.call("product.batchQueryGoodsById", requestGoodsIds, GoodsBo[].class));
    }

    /**
     * 获取退货商品列表
     */
    private List<Map<String, Object>> gainRefundGoodsList(List<RefundGoodsListBO> refundGoodsListBOList, List<OrdersGoods> ordersGoodsList, List<ProductBo> productBoList, List<GoodsBo> goodsBoList) {
        List<Map<String, Object>> goodsList = new ArrayList<>();
        for (RefundGoodsListBO refundGoodsListBO : refundGoodsListBOList) {
            // 获取产品信息
            ProductBo productBo = getProductBoByProductSkn(refundGoodsListBO.getProductSkn(), productBoList);
            if (productBo == null) {
                continue;
            }
            // 获取订单商品信息
            OrdersGoods ordersGoods = getOrdersGoodsBySkuAndPrice(refundGoodsListBO.getProductSku(), refundGoodsListBO.getLastPrice(), ordersGoodsList);
            if (ordersGoods == null) {
                continue;
            }
            // 获取商品信息
            GoodsBo goodsBo = getGoodsBoByGoodsId(ordersGoods.getGoodsId(), goodsBoList);
            if (goodsBo == null) {
                continue;
            }
            // 获取退货商品信息
            goodsList.add(gainRefundGoodsInfo(refundGoodsListBO, productBo, ordersGoods, goodsBo));
        }
        return goodsList;
    }

    /**
     * 获取退货商品信息
     */
    private Map<String, Object> gainRefundGoodsInfo(RefundGoodsListBO refundGoodsListBO, ProductBo productBo, OrdersGoods ordersGoods, GoodsBo goodsBo) {
        Map<String, Object> refundGoodsInfo = new LinkedHashMap<>();
        // 退货类型 通过yml操作工具类获取key为exchangeType的yml配置内容
        Map<String, Object> exchangeType = RefundUtils.getRefundMap("exchangeType");
        List<String> images = new ArrayList<>();
        List<Byte> bytes = new ArrayList<>();
        bytes.add((byte) 4);
        bytes.add((byte) 6);
        bytes.add((byte) 8);
        if (bytes.contains(refundGoodsListBO.getReturnedReason())) {
            // 获得图片
            List<ApplyGoodsImages> applyGoodsImagesList = getImages(refundGoodsListBO.getReturnRequestId(), refundGoodsListBO.getId());
            for (ApplyGoodsImages applyGoodsImages : applyGoodsImagesList) {
                // 缩略图模板
                String imagePath = applyGoodsImages.getImagePath();
                // 兼容性 "/2016/01/13/10/0259d4a9b1b09ddfc3b90d188b3f9193dc.jpg"
                // /2013/10/12/11/02e820ace2740b0a80e55e205e8dc2f0be.jpg
                if (imagePath.startsWith("\"") && imagePath.endsWith("\"")) {
                    // 去掉前后多余""
                    imagePath = imagePath.substring(imagePath.indexOf('\"') + 1, imagePath.lastIndexOf('\"'));
                }
                images.add(ImagesHelper.template2(imagePath, ImagesHelper.SYS_BUCKET.get("evidenceImages"), "center", "YmxhY2s="));
            }
        }
        BigDecimal price = refundGoodsListBO.getGoodsType() == 2 ? new BigDecimal("0.00") : ordersGoods.getGoodsPrice();
        refundGoodsInfo.put("product_skn", productBo.getErpProductId());
        refundGoodsInfo.put("product_name", productBo.getProductName());
        refundGoodsInfo.put("goods_image", goodsBo.getColorImage());
        refundGoodsInfo.put("size_name", ordersGoods.getSizeName());
        refundGoodsInfo.put("color_name", ordersGoods.getColorName());
        refundGoodsInfo.put("sales_price", formatRefundData(price));
        refundGoodsInfo.put("reason", refundGoodsListBO.getReturnedReason());
        refundGoodsInfo.put("remark", refundGoodsListBO.getRemark());
        String reasonName = refundGoodsListBO.getReturnedReason() == null ? "" : (String) exchangeType.get(RefundUtils.convertKeyToYAML(String.valueOf(refundGoodsListBO.getReturnedReason())));
        refundGoodsInfo.put("reason_name", reasonName);
        refundGoodsInfo.put("evidence_images", images);
        String goodsType = refundGoodsListBO.getGoodsType() == null ? "" : (String) ((LinkedHashMap) RefundUtils.getRefundMap("orderGoodsType").get(
                RefundUtils.convertKeyToYAML(String.valueOf(refundGoodsListBO.getGoodsType())))).get(RefundUtils.convertKeyToYAML("en"));
        refundGoodsInfo.put("goods_type", goodsType);
        return refundGoodsInfo;
    }

    /**
     * 获得图片
     */
    private List<ApplyGoodsImages> getImages(Integer applyId, Integer applyGoodsId) {
        if (applyId < 1 || applyGoodsId < 1) {
            return new ArrayList<>();
        }
        return applyGoodsImagesDao.selectImages(applyId, applyGoodsId);
    }

    /**
     * 获取产品信息
     */
    private ProductBo getProductBoByProductSkn(Integer productSkn, List<ProductBo> productBoList) {
        for (ProductBo product : productBoList) {
            if (product.getErpProductId().equals(productSkn)) {
                return product;
            }
        }
        return null;
    }

    /**
     * 获取订单商品信息
     */
    private OrdersGoods getOrdersGoodsBySkuAndPrice(Integer productSku, BigDecimal price, List<OrdersGoods> ordersGoodsList) {
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            if (ordersGoods.getErpSkuId().equals(productSku) && equals(price, ordersGoods.getGoodsPrice())) {
                return ordersGoods;
            }
        }
        return null;
    }

    /**
     * 获取订单中指定sku商品数量
     *
     * @param productSku
     * @param ordersGoodsList
     * @return
     */
    private int countOrdersGoodsByProductSku(Integer productSku, List<OrdersGoods> ordersGoodsList) {
        int num = 0;
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            if (ordersGoods.getErpSkuId().equals(productSku)) {
                num += ordersGoods.getNum();
            }
        }
        return num;
    }

    /**
     * 获取订单中指定sku 与 price的商品数量
     *
     * @param productSku
     * @param ordersGoodsList
     * @return
     */
    private int countOrdersGoodsBySkuAndPrice(Integer productSku, BigDecimal price, List<OrdersGoods> ordersGoodsList) {
        int num = 0;
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            if (ordersGoods.getErpSkuId().equals(productSku) && equals(price, ordersGoods.getGoodsPrice())) {
                num += ordersGoods.getNum();
            }
        }
        return num;
    }


    /**
     * 获取商品信息
     */
    private GoodsBo getGoodsBoByGoodsId(Integer goodsId, List<GoodsBo> goodsBoList) {
        for (GoodsBo goodsBo : goodsBoList) {
            if (goodsBo.getId().equals(goodsId)) {
                return goodsBo;
            }
        }
        return null;
    }

    /**
     * 获取退换货状态提示内容
     */
    private Map<String, Object> getRefundNotice(String type, Byte status, String expressCompany, String expressNumber, Integer expressId) {
        Map<String, Object> refundNotice;
        if (status == 20) {
            refundNotice = new LinkedHashMap<>();
            refundNotice.put("id", 20);
            refundNotice.put("title", "商品寄回物流信息");
            refundExpress(expressCompany, expressNumber, expressId, refundNotice);
            return refundNotice;
        }
        Map<String, Object> notice;
        if (type.equalsIgnoreCase("refund")) {
            // 退货状态提醒
            notice = RefundUtils.getRefundMap("refundNotice");
        } else {
            // 换货提醒内容
            notice = RefundUtils.getRefundMap("changeNotice");
        }
        if (notice.get(RefundUtils.convertKeyToYAML(String.valueOf(status))) == null) {
            return new LinkedHashMap<>();
        } else
            refundNotice = (Map) notice.get(RefundUtils.convertKeyToYAML(String.valueOf(status)));
        if (status == 30 || status == 40) {
            refundExpress(expressCompany, expressNumber, expressId, refundNotice);
            return refundNotice;
        }
        return refundNotice;
    }

    /**
     * 退货快递信息
     */
    private void refundExpress(String expressCompany, String expressNumber, Integer expressId, Map<String, Object> refundNotice) {
        refundNotice.put("express_company", expressCompany);
        refundNotice.put("express_number", expressNumber);
        refundNotice.put("express_id", expressId);
    }

    /**
     * 获取银行卡信息
     */
    private void gainPaymentInfo(RefundGoodsBO refundGoodsBO, Map<String, Object> refundGoodsMap) {
        Map<String, Object> returnAmountMode;
        if (refundGoodsBO.getReturnAmountMode() == 2) {
            returnAmountMode = new LinkedHashMap<>();
            returnAmountMode.put("bank_name", refundGoodsBO.getBankName());
            String bankCard = refundGoodsBO.getBankCard();
            if (StringUtils.isNotBlank(bankCard) && bankCard.length() > 10) {
                String replaceBankCard = bankCard.replace(bankCard.substring(4, bankCard.length() - 4), "******");
                returnAmountMode.put("bank_card", replaceBankCard);
            } else {
                returnAmountMode.put("bank_card", StringUtils.EMPTY);
            }
            returnAmountMode.put("payee_name", refundGoodsBO.getPayeeName());
            refundGoodsMap.put("return_amount_mode_info", returnAmountMode);
        } else if (refundGoodsBO.getReturnAmountMode() == 3) {
            returnAmountMode = new LinkedHashMap<>();
            returnAmountMode.put("alipay_account", refundGoodsBO.getAlipayAccount());
            returnAmountMode.put("alipay_name", refundGoodsBO.getAlipayName());
            refundGoodsMap.put("return_amount_mode_info", returnAmountMode);
        } else {
            returnAmountMode = new LinkedHashMap<>();
            refundGoodsMap.put("return_amount_mode_info", returnAmountMode);
        }
    }

    /**
     * 保存快递信息
     */
    public void setExpress(Integer id, Integer uid, String expressCompany, String expressNumber, Integer expressId) {
        logger.info("set express by id [{}] ,uid [{}],expressCompany [{}],expressNumber [{}] and expressId [{}].", id, uid, expressCompany, expressNumber, expressId);
        // 验证请求参数
        validateSetExpressRequest(id, uid, expressCompany, expressNumber, expressId);
        // 查询退货商品
        RefundGoods refundGoods = refundGoodsDao.selectByIdAndUid(id, uid);
        // 验证保存快递信息的退货商品
        validateSetExpressRefundGoods(refundGoods);
        // 验证保存快递信息的重复提交
        validateSetExpressDuplicate(refundGoods.getSourceOrderCode(), expressNumber);
        // 向ERP系统提交物流数据
        setRefundExpressData(refundGoods.getId(), refundGoods.getErpRefundId(), expressCompany, expressNumber, expressId);
        // 保存或更新快递订单
        // 更新退货商品快递信息
        saveOrUpdateExpressOrders(expressNumber, expressId, refundGoods);
        refundGoods.setExpressCompany(expressCompany);
        refundGoods.setExpressNumber(expressNumber);
        refundGoods.setExpressId(expressId);
        refundGoods.setStatus((byte) 20);
        refundGoodsDao.updateByPrimaryKey(refundGoods);
    }

    /**
     * 验证请求参数
     */
    private void validateSetExpressRequest(Integer id, Integer uid, String expressCompany, String expressNumber, Integer expressId) {
        if (id == null || id < 1) {
            logger.warn("setExpress fail, don't select to set refund request of logistics number");
            throw new ServiceException(ServiceError.ORDER_SET_REFUND_REQUEST);
        }
        if (StringUtils.isEmpty(expressCompany)) {
            logger.warn("setExpress fail, don't select a logistics company");
            throw new ServiceException(ServiceError.ORDER_LOGISTICS_COMPANY);
        }
        if (StringUtils.isEmpty(expressNumber)) {
            logger.warn("setExpress fail, don't fill out the express order");
            throw new ServiceException(ServiceError.ORDER_FILL_OUT_EXPRESS_ORDER);
        }
        //校验快递单是否有效 ,字母,下划线,数字
        checkArgument(expressNumber.matches("\\w+"), ServiceError.ORDER_EXPRESS_NOT_VALIDATE);
        if (uid == null || uid < 1) {
            logger.warn("setExpress fail, don't log in first");
            throw new ServiceException(ServiceError.ORDER_LOGIN_FIRST);
        }
        if (expressId == null || expressId < 1) {
            logger.warn("setExpress fail, don't select a logistics company,expressId does not exist");
            throw new ServiceException(ServiceError.ORDER_LOGISTICS_COMPANY);
        }
    }

    /**
     * 验证保存快递信息的退货商品
     */
    private void validateSetExpressRefundGoods(RefundGoods refundGoods) {
        if (refundGoods == null) {
            logger.warn("setExpress fail, refundGoods was not found");
            throw new ServiceException(ServiceError.ORDER_NO_REFUND_REQUEST);
        }
        if (refundGoods.getStatus() > 20) {
            logger.warn("setExpress fail, refundGoods don't send back , because of refundStatus [{}]", refundGoods.getStatus());
            throw new ServiceException(ServiceError.ORDER_FAIL_EXPRESS_ORDER);
        }
        if (refundGoods.getSourceOrderCode() < 1) {
            logger.warn("setExpress fail, sourceOrderCode don't exist , because of sourceOrderCode [{}]", refundGoods.getSourceOrderCode());
            throw new ServiceException(ServiceError.ORDER_FAIL_EXPRESS_ORDER);
        }
        if (refundGoods.getCreateTime() == null) {
            logger.warn("setExpress fail, createTime is null");
            throw new ServiceException(ServiceError.ORDER_FAIL_EXPRESS_ORDER);
        }
    }

    /**
     * 验证保存快递信息的重复提交
     */
    private void validateSetExpressDuplicate(long orderCode, String expressNumber) {
        boolean isFirstSubmit;
        try {
            isFirstSubmit = valueOperations.get(Constant.CACHE_ORDER_SET_EXPRESS_CODE + orderCode + "-" + expressNumber) == null;
            if (isFirstSubmit) {
                valueOperations.set(Constant.CACHE_ORDER_SET_EXPRESS_CODE + orderCode + "-" + expressNumber, String.valueOf(orderCode) + expressNumber, 10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            isFirstSubmit = true;
        }
        if (!isFirstSubmit) {
            logger.warn("setExpress fail, don't submit duplicate");
            throw new ServiceException(ServiceError.ORDER_NO_DUPLICATE_SUBMIT);
        }
    }

    /**
     * 向ERP系统提交物流数据
     */
    private void setRefundExpressData(Integer id, Integer erpApplyId, String expressCompany, String expressNumber, Integer expressId) {
        // 构建请求参数
        JSONObject refundExpressData = new JSONObject();
        //begin 加入退货申请单id  add by maelk-liu 2016/4/12 快递信息前后台分离
        refundExpressData.put("order_returned_id", id);
        //end
        refundExpressData.put("apply_id", erpApplyId);
        refundExpressData.put("express_number", expressNumber);
        refundExpressData.put("express_name", expressCompany);
        refundExpressData.put("express_id", expressId);
        //增加类别标示,业务类型为退货。这里是为了向mq发送消息时跟换货订阅同一个topic时,以便于erp系统处理
        refundExpressData.put("type", "refund");
        // 提交订单状态到ERP系统
        if (IErpService.ERP_SERVICE_TYPE_MQ.equalsIgnoreCase(erpOrderExpressSyncType)) {
            mqErpService.setRefundExpressData(refundExpressData);
        } else {
            erpService.setRefundExpressData(refundExpressData);
        }
    }

    /**
     * 保存或更新快递订单
     */
    private void saveOrUpdateExpressOrders(String expressNumber, Integer expressId, RefundGoods refundGoods) {
        // 查询物流订单
        ExpressOrdersKey expressOrdersKey = new ExpressOrdersKey();
        expressOrdersKey.setOrderCode(refundGoods.getSourceOrderCode());
        expressOrdersKey.setExpressNumber(expressNumber);
        ExpressOrders expressOrders = expressOrdersDao.selectByPrimaryKey(expressOrdersKey);
        if (expressOrders == null) {
            // 添加物流订单
            expressOrders = new ExpressOrders();
            expressOrders.setOrderCode(refundGoods.getSourceOrderCode());
            expressOrders.setExpressNumber(expressNumber);
            expressOrders.setExpressId(expressId.byteValue());
            expressOrders.setOrderCreateTime(refundGoods.getCreateTime());
            expressOrders.setNum((byte) 0);
            expressOrders.setFlag((byte) 0);
            expressOrders.setSmsType((byte) 2);
            expressOrders.setCreateTime(System.currentTimeMillis() / 1000);
            expressOrdersDao.insertSelective(expressOrders);
        } else if (ExpressOrders.SMS_TYPE_REFUND.equals(expressOrders.getSmsType())) {
            // 更新物流订单
            expressOrders.setExpressId(expressId.byteValue());
            expressOrdersDao.updateByPrimaryKeySelective(expressOrders);
        }
    }

    /**
     * 根据uid获取退换货总数
     */
    @Override
    public int getCountByUid(Integer uid) {
        if (uid == null) {
            logger.warn("getCountByUid fail, don't log in first");
            throw new ServiceException(ServiceError.ORDER_LOGIN_FIRST);
        }

        return refundGoodsDao.selectCountByUid(uid);
    }

    @Override
    public void cancelRefund(Integer id, Integer uid) {
        logger.info("cancelRefund begin by id [{}] and uid [{}].", id, uid);
        if (id == null || id < 1) {
            logger.warn("cancelRefund fail, invalid refund request ID is [{}].", id);
            throw new ServiceException(ServiceError.ORDER_REFUND_REQUEST_ID_ERROR);
        }
        if (uid == null || uid < 1) {
            logger.warn("cancelRefund fail, invalid user ID is [{}].", uid);
            throw new ServiceException(ServiceError.ORDER_UID_ERROR);
        }
        //根据主键查refund_goods表
        RefundGoods refg = refundGoodsDao.selectByPrimaryKey(id);
        if (refg == null) {
            logger.warn("cancelRefund fail, beacuse queryRefundGoodsById is null,order_returned_id:{}", id);
            throw new ServiceException(ServiceError.ORDER_REFUND_QUERY_ORDER_GOODS_NULL);
        }
        /*后台没有退货记录
        if (refundGoodsBO.getErpRefundId() == null||refundGoodsBO.getErpRefundId()==0) {
            logger.warn("cancelRefund fail, refund request record does not exist");
            throw new ServiceException(ServiceError.ORDER_REFUND_REQUEST_NOT_EXIST);
        }*/
        // 获取退货商品
        List<RefundGoodsListBO> refundGoodsListBOList = getByRequestId(uid, refg.getId());
        if (CollectionUtils.isEmpty(refundGoodsListBOList)) {
            logger.warn("detail fail, without refund request goods");
            throw new ServiceException(ServiceError.ORDER_NO_REFUND_REQUEST_GOODS);
        }
        Byte status = refg.getStatus();
        //退货状态在 审核状态：10之后(不包含)就不允许取消了
        if (status > 10) {
            logger.warn("cancelRefund fail, status {} is not allowed ,refundId {},uid {},orderCode {}!", status, id, uid, refg.getSourceOrderCode());
            throw new ServiceException(ServiceError.ORDER_REFUND_CANCAEL_STATUS_ILLEGAL);
        }
        //开始取消，更新refund_goods表字段
        refg.setStatus((byte) 91);
        refundGoodsDao.updateByPrimaryKey(refg);
        //更新refund_goods_list表
        updateRefundGoodsListStatus(refg.getId(), 91);
        if (refg.getErpRefundId() == 0) {
            //表明此时erp还没有收到消息,不能取消.为了防止erp在用户取消之后消费了创建退货消息,
            //需要在后期erp返回退货单消息时,判断当前退货信息是否取消
            logger.info("cancelRefund not send mq to erp,because refundId {} erp not handle ,uid {}.", id, uid);
            //@see ErpCreateRefundOrderMessageConsumer
        } else {
            //发送mq消息给erp 
            JSONObject obj = new JSONObject();
            obj.put("id", refg.getErpRefundId());
            obj.put("status", 900);
            obj.put("type", 1);//1表示退货
            orderMqService.sendChangeRefundCancelMessage(obj);
        }
    }
}
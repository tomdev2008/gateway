package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.helpers.ImagesHelper;
import com.yoho.core.redis.YHValueOperations;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.model.ChangeGoodsBo;
import com.yoho.product.model.ChangeProductBo;
import com.yoho.product.model.GoodsBo;
import com.yoho.product.model.ProductBo;
import com.yoho.product.request.*;
import com.yoho.product.response.VoidResponse;
import com.yoho.service.model.order.model.*;
import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.order.response.orderChange.*;
import com.yoho.service.model.request.AreaReqBO;
import com.yoho.service.model.request.UserAddressReqBO;
import com.yoho.service.model.request.UserVipReqBO;
import com.yoho.service.model.response.AreaRspBo;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.service.model.vip.VipInfo;
import com.yoho.service.model.vip.VipLevel;
import com.yoho.yhorder.common.cache.redis.UserOrderCache;
import com.yoho.yhorder.common.utils.*;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.contants.DeliveryType;
import com.yoho.yhorder.dal.domain.ChangeGoods;
import com.yoho.yhorder.dal.domain.ChangeGoodsMainInfo;
import com.yoho.yhorder.dal.domain.RefundNumberStatistics;
import com.yoho.yhorder.dal.model.*;
import com.yoho.yhorder.order.config.*;
import com.yoho.yhorder.order.model.ChangeGoodsDetailMidBo;
import com.yoho.yhorder.order.model.OrderChangeGoodsApplyErpRsp;
import com.yoho.yhorder.order.service.IChangeGoodsService;
import com.yoho.yhorder.order.service.IErpService;
import com.yoho.yhorder.order.service.IOrderCancelService;
import com.yoho.yhorder.order.service.IOrderMqService;
import com.yoho.yhorder.order.service.IRefundService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yoho.yhorder.common.utils.YHPreconditions.*;

/**
 * 类的描述
 *
 * @author lijian
 * @Time 2015/11/20
 */
@Service
public class ChangeGoodsServiceImpl implements IChangeGoodsService {

    Logger logger = LoggerFactory.getLogger(ChangeGoodsServiceImpl.class);


    @Autowired
    private IChangeGoodsMapper iChangeGoodsMapper;

    //换货主表dao请求
    @Autowired
    private IChangeGoodsMainMapper iChangeGoodsMainMapper;

    //换货订单
    @Autowired
    IChangeGoodsDao iChangeGoodsDao;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    IOrdersGoodsMapper iOrdersGoodsMapper;

    @Autowired
    IOrdersMapper iOrdersMapper;

    @Autowired
    IRefundGoodsListMapper iRefundGoodsListMapper;

    @Autowired
    IApplyGoodsImagesDao iApplyGoodsImagesDao;

    @Autowired
    @Qualifier("mqErpService")
    private IErpService mqErpService;

    @Autowired
    private IOrderCodeQueueDAO orderCodeQueueDAO;

    @Autowired
    private IOrderCodeListDAO orderCodeListDAO;

    @Autowired
    IRefundService iRefundService;

    @Autowired
    private YHValueOperations<String, String> valueOperations;


    @Autowired
    private IRefundGoodsDao refundGoodsDao;

    @Autowired
    private IRefundGoodsListDao refundGoodsListDao;

    @Autowired
    private IExpressOrdersDao expressOrdersDao;

    @Autowired
    private UserOrderCache userOrderCache;

    @Autowired
    private IOrderCancelService orderCancelService;

    @Autowired
    private IOrderMqService orderMqService;

    @Override
    public ChangeOrder findChangeOrderById(Integer id) {
        if (id == null || id == 0) {
            return null;
        }
        logger.info("find change order {}.", id);
        ChangeGoodsMainInfo changeGoods = iChangeGoodsDao.selectByPrimaryKey(id);
        if (changeGoods == null) {
            logger.info("find change order {} fail, can not find change order.", id);
            return null;
        } else {
            ChangeOrder changeOrder = new ChangeOrder();
            BeanUtils.copyProperties(changeGoods, changeOrder);
            logger.info("find change order {} success.", id);
            return changeOrder;
        }
    }

    /**
     * 加载换货方法，如果是 VIP 加载上门换货方式
     * 1. 寄回换货
     * 2. 上门取货(VIP)
     *
     * @see IChangeGoodsService#getChangeDeliveryList(OrderChangeDeliveryReq)
     */
    @Override
    public List<OrderChangeDeliveryRsp> getChangeDeliveryList(OrderChangeDeliveryReq request) {
        // 验证请求参数
        if (request == null) {
            logger.warn("GetChangeDeliveryList fail, request is null");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (request.getUid() == null) {
            logger.warn("GetChangeDeliveryList fail, request uid is empty");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        //封装返回结果
        List<OrderChangeDeliveryRsp> response = new ArrayList<OrderChangeDeliveryRsp>();
        //设置寄回换货信息
        OrderChangeDeliveryRsp sendChangeDeliveryType = new OrderChangeDeliveryRsp();
        sendChangeDeliveryType.setId(DeliveryType.TYPE_SEND.getId());
        sendChangeDeliveryType.setName(DeliveryType.TYPE_SEND.getType());
        sendChangeDeliveryType.setIsDefault(DeliveryType.TYPE_SEND.getDefaultType());
        response.add(sendChangeDeliveryType);
        //设置上门换货(白金会员专享服务)
        OrderChangeDeliveryRsp doorChangeDeliveryType = createDoorChangeDeliveryType(request);
        if (doorChangeDeliveryType != null) {
            response.add(doorChangeDeliveryType);
        }
        return response;

    }

    /**
     * 设置上门换货(白金会员专享服务)
     */
    private OrderChangeDeliveryRsp createDoorChangeDeliveryType(OrderChangeDeliveryReq request) {
        // 验证用户是否是白金会员
        if (!isPlatinumVipByUid(request.getUid())) {
            return null;
        }
        request.setAreaCode(StringUtils.isEmpty(request.getAreaCode()) ? "0" : request.getAreaCode());
        // 查询区域信息
        AreaReqBO areaReqBO = new AreaReqBO();
        areaReqBO.setCode(Integer.valueOf(request.getAreaCode()));
        // yh_passport.area
        AreaRspBo areaRspBo = serviceCaller.call(ServerURL.USERS_GET_AREA_INFO_BY_CODE, areaReqBO, AreaRspBo.class);
        if (areaRspBo == null) {
            logger.info("can not find AreaRspBo by code {}. uid {}", request.getAreaCode(), request.getUid());
            return null;
        }
        // 如果该地区支持上门换货
        if (Constants.YES.equals(areaRspBo.getIs_delivery())) {
            OrderChangeDeliveryRsp doorChangeDeliveryType = new OrderChangeDeliveryRsp();
            doorChangeDeliveryType.setId(DeliveryType.TYPE_DOOR.getId());
            doorChangeDeliveryType.setName(DeliveryType.TYPE_DOOR.getType());
            doorChangeDeliveryType.setIsDefault(DeliveryType.TYPE_DOOR.getDefaultType());
            return doorChangeDeliveryType;
        } else {
            logger.info("the area {} is not support door server. uid {}", areaReqBO.getCaption(), request.getUid());
            return null;
        }
    }

    //验证用户是否是白金会员
    private boolean isPlatinumVipByUid(Integer uid) {
        VipLevel vipLevel = getUserVipLevel(uid);
        if (vipLevel == null || !"3".equals(vipLevel.getCurLevel())) {
            logger.info("user {} is not Platinum Vip", uid);
            return false;
        } else {
            return true;
        }
    }

    private VipLevel getUserVipLevel(int uid) {
        UserVipReqBO userVipReqBO = new UserVipReqBO();
        userVipReqBO.setUid(uid);
        VipInfo result = serviceCaller.call(ServerURL.USERS_GET_VIP_INFO, userVipReqBO, VipInfo.class);
        if (result == null) {
            logger.info("can not find VipInfo by uid {}", uid);
            return null;
        }
        return result.getCurVipInfo();
    }

    /**
     * @see IChangeGoodsService#saveExpressInfo(OrderChangeExpressReq)
     */
    @Override
    public void saveExpressInfo(OrderChangeExpressReq orderChangeExpressReq) {
        // 验证参数[id, uid, expressCompany, expressNumber || expressId]
        saveExpressInfoValidate(orderChangeExpressReq);
        // 加载换货商品信息
        ChangeGoodsMainInfo changeGoodsMainInfo = iChangeGoodsMainMapper.selectByPrimaryKey(orderChangeExpressReq.getId());
        if (changeGoodsMainInfo == null) {
            logger.warn("setExpress fail, changeGoods don't send back , because of changeGoodsMainInfo is NULL");
            throw new ServiceException(ServiceError.ORDER_SAVE_EXPRESS_FAIL);
        }
        // 0: 提交申请 10: 审核通过 20: 商品寄回 30: 商品入库 40: 换货发出 50: 换货完成 91: 已取消
        if (changeGoodsMainInfo.getStatus() > 20) {
            logger.warn("setExpress fail, changeGoods don't send back , because of refundStatus [{}]", changeGoodsMainInfo.getStatus());
            throw new ServiceException(ServiceError.ORDER_FAIL_EXPRESS_ORDER);
        }
        // Express -> ERP
        saveExpressToErp(changeGoodsMainInfo.getErpExchangeId(), orderChangeExpressReq);
        // 保存 || 更新 物流信息
        saveOrUpdateExpressOrders(orderChangeExpressReq, changeGoodsMainInfo);
        // 保存换货商品信息
        updateChangeGoods(orderChangeExpressReq);

    }

    /**
     * 保存换货信息前的验证
     *
     * @param orderChangeExpressReq [id, expressCompany, expressId, uid]
     */
    private void saveExpressInfoValidate(OrderChangeExpressReq orderChangeExpressReq) {
        if (orderChangeExpressReq.getId() == null || orderChangeExpressReq.getId() == 0) {
            throw new ServiceException(ServiceError.ORDER_CHANGE_NO_ID);
        }
        if (StringUtils.isEmpty(orderChangeExpressReq.getExpressCompany()) || NumUtil.checkNumIsZero(orderChangeExpressReq.getExpressId())) {
            throw new ServiceException(ServiceError.ORDER_CHANGE_NO_EXPRESS_COMPANY);
        }

        if (StringUtils.isEmpty(orderChangeExpressReq.getExpressNumber())) {
            throw new ServiceException(ServiceError.ORDER_CHANGE_NO_EXPRESS_NUM);
        }
        // 检查一下快递号的长度
        checkArgument(orderChangeExpressReq.getExpressNumber().length() <= 50, ServiceError.ORDER_EXPRESS_NOT_VALIDATE);
        if (orderChangeExpressReq.getUid() == null || orderChangeExpressReq.getUid() == 0) {
            throw new ServiceException(ServiceError.ORDER_CHANGE_NO_LOGIN);
        }
        //校验快递单是否有效 ,字母,下划线,数字
        checkArgument(orderChangeExpressReq.getExpressNumber().matches("\\w+"), ServiceError.ORDER_EXPRESS_NOT_VALIDATE);
    }

    /**
     * 保存换货商品信息
     *
     * @param orderChangeExpressReq [id, uid, expressNumber, expressId, expressCompany]
     */
    private void updateChangeGoods(OrderChangeExpressReq orderChangeExpressReq) {
        ChangeGoodsMainInfo changeGoods = new ChangeGoodsMainInfo();
        changeGoods.setUid(orderChangeExpressReq.getUid());
        changeGoods.setId(orderChangeExpressReq.getId());
        changeGoods.setExpressNumber(orderChangeExpressReq.getExpressNumber());
        changeGoods.setExpressId(Integer.valueOf((orderChangeExpressReq.getExpressId())));
        changeGoods.setExpressCompany(orderChangeExpressReq.getExpressCompany());
        changeGoods.setStatus((byte) 20);
        int result = iChangeGoodsMainMapper.updateByPrimaryKeySelective(changeGoods);
        if (result < 1) {
            logger.warn("saveExpressInfo updateByPrimaryKeySelective   ERROR is {}", JSONObject.toJSONString(changeGoods));
            throw new ServiceException(ServiceError.ORDER_SAVE_EXPRESS_FAIL);
        }
    }

    /**
     * 向 Erp 中，写入物流信息
     *
     * @param orderChangeExpressReq [id, expressNumber, expressCompany, expressId]
     */
    private void saveExpressToErp(Integer erpChangeId, OrderChangeExpressReq orderChangeExpressReq) {
        Map<String, Object> express = new HashMap<String, Object>();
        express.put("id", orderChangeExpressReq.getId());
        express.put("apply_id", erpChangeId);
        express.put("express_number", orderChangeExpressReq.getExpressNumber());
        express.put("express_name", orderChangeExpressReq.getExpressCompany());
        express.put("express_id", orderChangeExpressReq.getExpressId());
        express.put("type", "change");
        //调用erp存储数据
        mqErpService.updateChangeOrderExpressInfo(express);
    }

    /**
     * 保存，更新物流订单
     *
     * @param orderChangeExpressReq [expressNumber, orderCode]
     */
    private void saveOrUpdateExpressOrders(OrderChangeExpressReq orderChangeExpressReq, ChangeGoodsMainInfo changeGoodsMainInfo) {
        // 查询表中的 ExpressOrders
        ExpressOrders expressOrders = new ExpressOrders();
        expressOrders.setExpressNumber(orderChangeExpressReq.getExpressNumber());
        expressOrders.setOrderCode(changeGoodsMainInfo.getSourceOrderCode());
        ExpressOrders expOrders = expressOrdersDao.selectByPrimaryKey(expressOrders);
        logger.info("saveExpressInfo expOrders is {}", JSONObject.toJSONString(expOrders));
        // if create else update
        if (expOrders == null) {
            ExpressOrders expressOrder = new ExpressOrders();
            expressOrder.setExpressId(orderChangeExpressReq.getExpressId());
            expressOrder.setOrderCode(changeGoodsMainInfo.getSourceOrderCode());
            expressOrder.setExpressNumber(orderChangeExpressReq.getExpressNumber());
            expressOrder.setOrderCreateTime(changeGoodsMainInfo.getCreateTime());
            expressOrder.setSmsType((byte) 3);
            expressOrder.setFlag((byte) 0);
            expressOrder.setNum((byte) 0);
            expressOrder.setCreateTime(DateUtil.toSecond(new Date()));
            int res = expressOrdersDao.insert(expressOrder);
            if (res < 1) {
                logger.warn("saveExpressInfo SAVE EXPRESS ERROR is {}", JSONObject.toJSONString(expressOrder));
                throw new ServiceException(ServiceError.ORDER_SAVE_EXPRESS_FAIL);
            }
        } else if (ExpressOrders.SMS_TYPE_CHANGE.equals(expOrders.getSmsType())) {
            // 更新物流订单
            expressOrders.setExpressId(orderChangeExpressReq.getExpressId());
            expressOrdersDao.updateByPrimaryKeySelective(expressOrders);
        }
    }

    /**
     * 获取换货订单商品
     *
     * @param orderChangeGoodsReq [Uid, orderCode]
     * @return :
     */
    @Override
    public ChangeGoodsListBO getChangeGoodsList(OrderChangeGoodsReq orderChangeGoodsReq) {
        // 验证请求参数
        validateGetChangeGoodsListRequest(orderChangeGoodsReq);
        // 查询符合条件的订单
        Orders orders = findCanChangeOrdersByOrderCodeAndUid(orderChangeGoodsReq);
        ChangeGoodsListBO changeGoodsListBO = new ChangeGoodsListBO();
        // 查询订单中的可换货商品
        List<ChangeGoodsDetailBo> changeGoodsDetailBoList = findChangeGoodsDetailBoByOrders(orders);
        if (changeGoodsDetailBoList.isEmpty()) {
            logger.warn("GetChangeGoodsList fail, the order {} can not find change goods.", orderChangeGoodsReq.getOrderCode());
            return changeGoodsListBO;
        } else {
            // 设置可换货的商品
            changeGoodsListBO.setGoodsList(changeGoodsDetailBoList);
            // 设置区域信息
            setAddress(orders, changeGoodsListBO);
            // 设置换货原因信息
            setExchangeReason(changeGoodsListBO);
            // 设置特殊商品换货原因信息
            setSpecialExchangeReason(changeGoodsListBO);
            // 设置特殊商品换货提醒
            setSpecialNotice(changeGoodsListBO);
            return changeGoodsListBO;
        }
    }

    private void setSpecialNotice(ChangeGoodsListBO changeGoodsListBO) {
        SpecialNoticeBo sn = new SpecialNoticeBo();
        sn.setRemark("1.考虑到个人卫生，例如内衣、内裤、袜子等贴身塑身类商品，不支持无理由退换货   2.香水、香薰、化妆品等特殊商品，无质量问题，不支持无理由退换货");
        sn.setTitle("该商品暂不支持7天无理由退换");
        changeGoodsListBO.setSpecialNoticeBo(sn);
    }

    private void validateGetChangeGoodsListRequest(OrderChangeGoodsReq orderChangeGoodsReq) {
        if (orderChangeGoodsReq == null) {
            logger.warn("GetChangeGoodsList fail, request is null.");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (orderChangeGoodsReq.getUid() == null) {
            logger.warn("GetChangeGoodsList fail, request uid is null.");
            throw new ServiceException(ServiceError.ORDER_UID_IS_EMPTY);
        }
        if (orderChangeGoodsReq.getOrderCode() == null) {
            logger.warn("GetChangeGoodsList fail, request order code is null.");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
    }

    private Orders findCanChangeOrdersByOrderCodeAndUid(OrderChangeGoodsReq orderChangeGoodsReq) {
        Orders orders = iOrdersMapper.selectByCodeAndUid(orderChangeGoodsReq.getOrderCode().toString(), orderChangeGoodsReq.getUid().toString());
        if (orders == null) {
            logger.warn("GetChangeGoodsList fail, can not find order by order code {} and uid {}.", orderChangeGoodsReq.getOrderCode(), orderChangeGoodsReq.getUid());
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        // 是否删除
        if (orders.getOrdersStatus() != 1) {
            logger.warn("GetChangeGoodsList fail, the orders {} is deleted.", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_HAS_DELETE);
        }
        // 必须是已完成订单？
        if (orders.getStatus() != 6) {
            logger.warn("GetChangeGoodsList fail, the orders {} has not confirm. the status is {}.", orders.getOrderCode(), orders.getStatus());
            throw new ServiceException(ServiceError.ORDER_CHANGE_ORDER_NO_CONFIRM);
        }
        Long currentTimeSeconds = DateUtil.toSecond(new Date());
        if (currentTimeSeconds - orders.getUpdateTime() > Constant.ORDER_EXCHANGE_LIMIT_DAY) {
            logger.warn("GetChangeGoodsList fail, the orders {} is greater than exchange limit day.currentTimeSeconds {} - updateTime {} > exchange limit day {}.", orders.getOrderCode(), currentTimeSeconds, orders.getUpdateTime(), Constant.ORDER_EXCHANGE_LIMIT_DAY);
            throw new ServiceException(ServiceError.ORDER_CHANGE_ORDER_EXPIRED);
        }
        return orders;
    }

    // 查询订单中的可换货商品
    private List<ChangeGoodsDetailBo> findChangeGoodsDetailBoByOrders(Orders orders) {
        ArrayList<Integer> orderId = new ArrayList<Integer>();
        orderId.add(orders.getId());
        List<OrdersGoods> ordersGoodsList = iOrdersGoodsMapper.selectOrderGoodsByOrderId(orderId);
        if (ordersGoodsList.isEmpty()) {
            logger.info("the orders {} has no goods.", orders.getId());
            return Collections.emptyList();
        } else {
            return findChangeGoodsDetailBoByOrdersAndOrdersGoods(orders, ordersGoodsList);
        }
    }

    private Set<Integer> buildSknsByGoodsList(List<OrdersGoods> ordersGoodsList) {
        //这里不需要判断是否为空,判断逻辑已经有过了
        Set<Integer> sknSet = new HashSet<Integer>();
        for (OrdersGoods goods : ordersGoodsList) {
            sknSet.add(goods.getProductSkn());
        }
        return sknSet;
    }

    /**
     * 这个方式主要的工作
     * <pre>
     *     1. 对订单中加载过来的商品设置一些值【包括库存】
     *     2. 调用 product 项目，查询一些商品值设置到列表
     *     3. 返回糅合的数据
     * </pre>
     */
    private List<ChangeGoodsDetailBo> findChangeGoodsDetailBoByOrdersAndOrdersGoods(Orders orders, List<OrdersGoods> ordersGoodsList) {
        //如果商品不存在,直接抛出异常
        if (ordersGoodsList == null || ordersGoodsList.size() == 0) {
            throw new ServiceException(ServiceError.ORDER_ORDERS_GOODS_IS_EMPTY);
        }
        Set<Integer> skns = buildSknsByGoodsList(ordersGoodsList);
        BatchBaseRequest<Integer> batchBaseRequest = new BatchBaseRequest<Integer>();
        batchBaseRequest.setParams(new ArrayList<Integer>(skns));
        //调商品服务查询skn列表是否有退换货限制
        Map<Integer, Integer> limitMap = serviceCaller.call("product.batchQueryIsSupportRefundExchange", batchBaseRequest, Map.class);

        // 筛选是否是换货业务数据
        List<ChangeGoodsDetailMidBo> changeGoodsDetailMidBos = BeanTool.copyList(ordersGoodsList, ChangeGoodsDetailMidBo.class);
        for (ChangeGoodsDetailMidBo changeGoodsDetailMidBo : changeGoodsDetailMidBos) {
            changeGoodsDetailMidBo.setGoodsTypeId(Integer.valueOf(changeGoodsDetailMidBo.getGoodsType()));
        }
        List<ChangeGoodsDetailBo> ordersGoodsChangeBoList = BeanTool.copyList(changeGoodsDetailMidBos, ChangeGoodsDetailBo.class);
        // 更新换货数量信息【判断换货数据与退货数据是否正确】
        updateChangeNumForChangeGoodsDetailBo(orders, ordersGoodsChangeBoList);
        //执行批量查询，根据skn获取商品集合
        //定义商品批量查询条件
        List<ProductOrderRequest> productOrderRequest = new ArrayList<ProductOrderRequest>();
        for (ChangeGoodsDetailBo ordersGoodsChangeBo : ordersGoodsChangeBoList) {
            if (ordersGoodsChangeBo != null && ordersGoodsChangeBo.getIsChangeFlag() != null
                    && ordersGoodsChangeBo.getIsChangeFlag().intValue() == 1) {

                ProductOrderRequest productOrder = new ProductOrderRequest();
                productOrder.setGoodsId(ordersGoodsChangeBo.getGoodsId());
                productOrder.setProductId(ordersGoodsChangeBo.getProductId());
                productOrderRequest.add(productOrder);
            }
        }
        if (productOrderRequest.isEmpty()) {
            logger.info("the orders {} has no goods isChangeFlag equals 1.", orders.getOrderCode());
            return Collections.emptyList();
        }
        BatchProductOrderRequest batchProductOrderRequest = new BatchProductOrderRequest();
        batchProductOrderRequest.setProductOrderRequest(productOrderRequest);
        ChangeProductBo[] changeProductBos = serviceCaller.call(ServerURL.PRODUCT_BATCH_QUERY_CHANGE_PRODUCT_SKC_INFO, batchProductOrderRequest, ChangeProductBo[].class);
        if (changeProductBos == null || changeProductBos.length == 0) {
            logger.info("the orders {} has no ChangeProductBo BatchProductOrderRequest {}.", orders.getOrderCode(), JSON.toJSONString(productOrderRequest));
            return Collections.emptyList();
        }
        List<ChangeGoodsDetailBo> changeGoodsDetailBoList = new ArrayList<ChangeGoodsDetailBo>();
        for (ChangeGoodsDetailBo ordersGoodsChangeBo : ordersGoodsChangeBoList) {
            if (ordersGoodsChangeBo.getIsChangeFlag() != null && ordersGoodsChangeBo.getIsChangeFlag().intValue() == 1 && changeProductBos.length > 0) {
                for (ChangeProductBo changeProductBo : changeProductBos) {
                    ProductBo productBo = changeProductBo.getProductBo();
                    GoodsBo goodsBo = changeProductBo.getGoodsBo();
                    int hasShoes = 0;
                    if (productBo.getMiddleSortId() == 6) {
                        hasShoes = 1;
                    }
                    if (changeProductBo != null && productBo != null && productBo.getId().intValue() == ordersGoodsChangeBo.getProductId()
                            && goodsBo != null && goodsBo.getId().intValue() == ordersGoodsChangeBo.getGoodsId()) {
                        int changeNum = ordersGoodsChangeBo.getChangeNum();
                        for (int i = 0; i < changeNum; i++) {
                            ChangeGoodsDetailBo changeGoodsDetailBo = new ChangeGoodsDetailBo();
                            BeanUtils.copyProperties(ordersGoodsChangeBo, changeGoodsDetailBo);
                            changeGoodsDetailBo.setProductName(productBo.getProductName());
                            changeGoodsDetailBo.setProductSkn(productBo.getErpProductId());
                            changeGoodsDetailBo.setProductSkc(goodsBo.getProductSkc());
                            changeGoodsDetailBo.setHasShoes(hasShoes);
                            changeGoodsDetailBo.setProductSku(changeGoodsDetailBo.getErpSkuId());
                            changeGoodsDetailBo.setProduct_skc(String.valueOf(changeGoodsDetailBo.getProductSkc()));
                            changeGoodsDetailBo.setProduct_sku(String.valueOf(changeGoodsDetailBo.getProductSku()));
                            changeGoodsDetailBo.setProduct_skn(String.valueOf(changeGoodsDetailBo.getProductSkn()));
                            //增加商品skn是否是特殊商品,需要限制其退换货
                            if (limitMap == null || limitMap.get(productBo.getErpProductId()) == null) {
                                changeGoodsDetailBo.setIs_limit_skn("N");//1表明没有限制
                            } else {
                                changeGoodsDetailBo.setIs_limit_skn(limitMap.get(productBo.getErpProductId()) == 0 ? "Y" : "N");
                            }
                            //产品系统已经处理图片
                            // changeGoodsDetailBo.setGoodsImg(ImagesHelper.template2(defaultImg.getImageUrl(), OrderConstant.IMG_CHANGE_BUCKET, OrderConstant.IMG_POSITION, OrderConstant.IMG_CHANGE_BACKGROUND));
                            changeGoodsDetailBo.setGoodsImg(goodsBo.getColorImage());
                            changeGoodsDetailBo.setGoodsTypeName(OrderGoodsTypeUtils.getOrderGoodsTypeMap(changeGoodsDetailBo.getGoodsType() + "").get("en") + "");
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            if (changeGoodsDetailBo.getSalesPrice() != null) {
                                changeGoodsDetailBo.setLastPrice(decimalFormat.format(changeGoodsDetailBo.getGoodsPrice()));
                            }
                            changeGoodsDetailBoList.add(changeGoodsDetailBo);
                        }
                    }
                }
            }

        }
        return changeGoodsDetailBoList;
    }


    /**
     * @param refundGoodsCntList
     * @return
     */
    private MultiKeyMap refundNumberStatisticsList2Map(List<RefundGoodsCnt> refundGoodsCntList) {
        MultiKeyMap refundMap = new MultiKeyMap();
        for (RefundGoodsCnt refundGoodsCnt : refundGoodsCntList) {
            if (refundMap.containsKey(refundGoodsCnt.getProductSku(), refundGoodsCnt.getLastPrice())) {
                Integer num = (Integer) refundMap.get(refundGoodsCnt.getProductSku(), refundGoodsCnt.getLastPrice());
                refundMap.put(refundGoodsCnt.getProductSku(), refundGoodsCnt.getLastPrice(), num + refundGoodsCnt.getCnt());
            } else {
                refundMap.put(refundGoodsCnt.getProductSku(), refundGoodsCnt.getLastPrice(), refundGoodsCnt.getCnt());
            }
        }
        return refundMap;
    }

    /**
     * 设置可换货信息
     *
     * @param orders
     * @param ordersGoodsChangeBoList
     */
    private void updateChangeNumForChangeGoodsDetailBo(Orders orders, List<ChangeGoodsDetailBo> ordersGoodsChangeBoList) {
        // 查询是否有退货
        RefundGoodsCnt refundGoodsCnt = new RefundGoodsCnt();
        refundGoodsCnt.setOrderCode(orders.getOrderCode());
        List<RefundGoodsCnt> refundGoodsCntList = iRefundGoodsListMapper.selectRefundGoodsCnt(refundGoodsCnt);
        MultiKeyMap exchangeMap = refundNumberStatisticsList2Map(refundGoodsCntList);
        for (ChangeGoodsDetailBo ordersGoodsChangeBo : ordersGoodsChangeBoList) {
            // 默认可以换货
            ordersGoodsChangeBo.setIsChangeFlag(1);
            ordersGoodsChangeBo.setChangeNum(ordersGoodsChangeBo.getNum());

            if (exchangeMap.containsKey(ordersGoodsChangeBo.getErpSkuId(), ordersGoodsChangeBo.getGoodsPrice())) {
                int alreadyExchangeNum = (Integer) exchangeMap.get(ordersGoodsChangeBo.getErpSkuId(), ordersGoodsChangeBo.getGoodsPrice());
                if (ordersGoodsChangeBo.getNum() > alreadyExchangeNum) {
                    ordersGoodsChangeBo.setChangeNum((ordersGoodsChangeBo.getNum() - alreadyExchangeNum));
                    exchangeMap.put(ordersGoodsChangeBo.getErpSkuId(), ordersGoodsChangeBo.getGoodsPrice(), 0);
                } else {
                    ordersGoodsChangeBo.setIsChangeFlag(0);
                    ordersGoodsChangeBo.setChangeNum(0);
                    exchangeMap.put(ordersGoodsChangeBo.getErpSkuId(), ordersGoodsChangeBo.getGoodsPrice(), alreadyExchangeNum - ordersGoodsChangeBo.getNum());
                }
            }
        }
    }

    private void setAddress(Orders orders, ChangeGoodsListBO changeGoodsListBO) {
        AddressBO addressBO = new AddressBO();
        AreaReqBO areaReqBO = new AreaReqBO();
        areaReqBO.setCode(orders.getAreaCode());
        AreaRspBo areaRspBo = serviceCaller.call(ServerURL.USERS_GET_AREA_INFO_BY_CODE, areaReqBO, AreaRspBo.class);
        if (areaRspBo != null) {
            AreaRspBo city = areaRspBo.getParent();
            if (city != null) {
                AreaRspBo province = city.getParent();
                if (province != null) {
                    addressBO.setArea(province.getCaption() + " " + city.getCaption() + " " + areaRspBo.getCaption());
                } else {
                    addressBO.setArea(city.getCaption() + " " + areaRspBo.getCaption());
                }
            } else {
                addressBO.setArea(areaRspBo.getCaption());
            }
        } else {
            logger.info("the orders {} can not find AreaRspBo by code {}.", orders.getOrderCode(), areaReqBO.getCode());
        }
        addressBO.setConsignee(orders.getUserName());
        addressBO.setMobile(orders.getMobile());
        addressBO.setAddress(orders.getAddress());
        addressBO.setZipCode(orders.getZipCode().toString());
        addressBO.setAreaCode(orders.getAreaCode().toString());
        addressBO.setAddressId(0);
        changeGoodsListBO.setAddress(addressBO);
    }

    private void setExchangeReason(ChangeGoodsListBO changeGoodsListBO) {
        // 通过yml操作工具类获取key为changeStatus的定义数据，然后根据换货状态获取配置内容
        LinkedHashMap<String, Object> changeTypeMap = OrderYmlUtils.getOrderConfig(OrderConstant.YML_CHANGE_TYPE);
        List<ChangeTypeBo> changeTypeBoList = new ArrayList<ChangeTypeBo>();
        Iterator it = changeTypeMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            ChangeTypeBo changeTypeBo = new ChangeTypeBo();
            changeTypeBo.setId(Integer.valueOf(StringUtils.replace(StringUtils.replace(key, "[", ""), "]", "")));
            changeTypeBo.setName((String) changeTypeMap.get(key));
            changeTypeBoList.add(changeTypeBo);
        }
        changeGoodsListBO.setExchangeReason(changeTypeBoList);
    }

    /**
     * 功能描述: 商品特殊原因
     *
     * @param changeGoodsListBO
     */
    private void setSpecialExchangeReason(ChangeGoodsListBO changeGoodsListBO) {
        // 通过yml操作工具类获取key为changeStatus的定义数据，然后根据换货状态获取配置内容
        LinkedHashMap<String, Object> changeTypeMap = OrderYmlUtils.getOrderConfig("changeSpecialType");
        List<ChangeTypeBo> changeTypeBoList = new ArrayList<ChangeTypeBo>();
        Iterator it = changeTypeMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            ChangeTypeBo changeTypeBo = new ChangeTypeBo();
            changeTypeBo.setId(Integer.valueOf(StringUtils.replace(StringUtils.replace(key, "[", ""), "]", "")));
            changeTypeBo.setName((String) changeTypeMap.get(key));
            changeTypeBoList.add(changeTypeBo);
        }
        changeGoodsListBO.setSpecialExchangeReason(changeTypeBoList);
    }

    /**
     * @see IChangeGoodsService#getChangeGoodsDetail(OrderChangeGoodsReq)
     */
    @Override
    public ChangeGoodsBO getChangeGoodsDetail(OrderChangeGoodsReq orderChangeGoodsReq) {
        // 验证获取换货详情请求[id, uid]
        validateGetChangeGoodsDetaillRequest(orderChangeGoodsReq);
        // 查询换货商品信息
        ChangeGoodsMainInfo changeGoods = findChangeGoodsById(orderChangeGoodsReq.getId());
        // 查找原订单
        Orders srcOrders = findSrcOrdersByOrderCodeAndUid(changeGoods.getSourceOrderCode(), orderChangeGoodsReq.getUid());
        // 查找新订单
        Orders newOrders = findNewOrdersByOrderCodeAndUid(changeGoods.getOrderCode(), orderChangeGoodsReq.getUid());
        // 封装返回信息
        ChangeGoodsBO changeGoodsBO = new ChangeGoodsBO();
        // 从换货商品中设置部分属性
        setOrderChangeProperties(changeGoods, changeGoodsBO);
        // 从新订单中设置部分属性[consigneeName, mobile]
        setOrderChangeProperties(newOrders, changeGoodsBO);
        // 设置换货区域信息
        setOrderChangeAreaProperties(newOrders, changeGoodsBO);
        // 设置换货提示信息
        setOrderChangeNotice(changeGoods, changeGoodsBO);
        // 设置换货状态信息
        setOrderChangeStatus(changeGoods, changeGoodsBO);
        // 设置换货商品信息
        setOrderChangeGoods(changeGoods, srcOrders, newOrders, changeGoodsBO);
        //设置是否能取消换货
        setCancelChangeStatus(changeGoodsBO, changeGoods.getStatus(), changeGoods.getExchangeMode());
        return changeGoodsBO;
    }

    private void setCancelChangeStatus(ChangeGoodsBO changeGoods, Byte status, Byte exchangeMode) {
        if (status > 10) {
            changeGoods.setCanCancel("N");
            return;
        }
        //申请状态下都可以取消
        if (status == 0) {
            changeGoods.setCanCancel("Y");
            return;
        }
        if (status == 10) {
            //审核状态下,20是上门换货,不能取消
            if (exchangeMode == 20) {
                changeGoods.setCanCancel("N");
                return;
            }
            changeGoods.setCanCancel("Y");
            return;
        }
        changeGoods.setCanCancel("N");
    }

    /**
     * 验证获取换货详情请求
     */
    private void validateGetChangeGoodsDetaillRequest(OrderChangeGoodsReq orderChangeGoodsReq) {
        if (orderChangeGoodsReq.getId() == null) {
            logger.warn("GetChangeGoodsDetaill fail, because of request id is null");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (orderChangeGoodsReq.getUid() == null) {
            logger.warn("GetChangeGoodsDetaill fail, because of request uid is null");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
    }

    /**
     * 查询换货商品信息
     */
    private ChangeGoodsMainInfo findChangeGoodsById(Integer id) {
        ChangeGoodsMainInfo changeGoods = iChangeGoodsDao.selectById(id);
        if (changeGoods == null) {
            logger.warn("GetChangeGoodsDetaill fail, because of can not find change_goods by id {}.", id);
            throw new ServiceException(ServiceError.ORDER_CHANGE_ORDER_NO_EXISTS);
        }
        return changeGoods;
    }

    /**
     * 查找原订单
     */
    private Orders findSrcOrdersByOrderCodeAndUid(Long orderCode, Integer uid) {
        Orders srcOrders = iOrdersMapper.selectByCodeAndUidNoStatus(orderCode.toString(), uid.toString());
        if (srcOrders == null) {
            logger.warn("GetChangeGoodsDetaill fail, because of can not find src orders by order code {} and uid {}.", orderCode, uid);
            throw new ServiceException(ServiceError.ORDER_CHANGE_SOURCE_NO_EXISTS);
        }
        return srcOrders;
    }

    /**
     * 查找新订单
     */
    private Orders findNewOrdersByOrderCodeAndUid(Long orderCode, Integer uid) {
        Orders newOrders = iOrdersMapper.selectByCodeAndUidNoStatus(orderCode.toString(), uid.toString());
        if (newOrders == null) {
            logger.warn("GetChangeGoodsDetaill fail, because of can not find new orders by order code {} and uid {}.", orderCode, uid);
            throw new ServiceException(ServiceError.ORDER_CHANGE_NO_EXISTS);
        }
        return newOrders;
    }

    /**
     * 从换货商品中设置部分属性
     */
    private void setOrderChangeProperties(ChangeGoodsMainInfo changeGoods, ChangeGoodsBO changeGoodsBO) {
        changeGoodsBO.setStatus(Integer.valueOf((changeGoods.getStatus())));
        // 通过yml操作工具类获取key为changeStatus的定义数据，然后根据换货状态获取配置内容
        String statusName = OrderYmlUtils.getOrderConfig("changeStatus").get(OrderYmlUtils.converKeyToYAML(String.valueOf(changeGoods.getStatus()))) + "";
        if (StringUtils.isNotEmpty(statusName)) {
            changeGoodsBO.setStatusName(statusName);
        }
        changeGoodsBO.setCreateTime((DateUtil.getSecond2DateStr(Long.valueOf(changeGoods.getCreateTime()))));
        changeGoodsBO.setSourceOrderCode(changeGoods.getSourceOrderCode());
        changeGoodsBO.setOrderCode(String.valueOf(changeGoods.getOrderCode()));
        changeGoodsBO.setDeliveryTpye(String.valueOf(changeGoods.getExchangeMode()));
        changeGoodsBO.setDeliveryTpyeName(changeGoods.getExchangeMode() == OrderConfig.SEND_TYPE_ID.intValue() ? OrderConfig.SEND_TYPE : OrderConfig.DOOR_TYPE);
        changeGoodsBO.setExpressNumber(changeGoods.getExpressNumber());
        changeGoodsBO.setExpressCompany(changeGoods.getExpressCompany());
    }

    /**
     * 从新订单中设置部分属性
     */
    private void setOrderChangeProperties(Orders newOrders, ChangeGoodsBO changeGoodsBO) {
        changeGoodsBO.setConsigneeName(newOrders.getUserName());
        changeGoodsBO.setMobile(newOrders.getMobile());
    }

    /**
     * 设置换货区域信息
     */
    private void setOrderChangeAreaProperties(Orders newOrders, ChangeGoodsBO changeGoodsBO) {
        AreaReqBO areaReqBO = new AreaReqBO();
        areaReqBO.setCode(newOrders.getAreaCode());
        AreaRspBo areaRspBo = serviceCaller.call(ServerURL.USERS_GET_AREA_INFO_BY_CODE, areaReqBO, AreaRspBo.class);
        if (areaReqBO != null) {
            changeGoodsBO.setCounty(areaRspBo.getCaption());
            AreaRspBo city = areaRspBo.getParent();
            if (city != null) {
                changeGoodsBO.setCity(city.getCaption());
                AreaRspBo province = city.getParent();
                if (province != null) {
                    changeGoodsBO.setProvince(province.getCaption());
                } else {
                    logger.info("GetChangeGoodsDetaill: can not find province by order {}, area code {}.", newOrders.getOrderCode(), newOrders.getAreaCode());
                }
            } else {
                logger.info("GetChangeGoodsDetaill: can not find city by order {}, area code {}.", newOrders.getOrderCode(), newOrders.getAreaCode());
            }
        } else {
            logger.info("GetChangeGoodsDetaill: can not find AreaRspBo by order {}, area code {}.", newOrders.getOrderCode(), newOrders.getAreaCode());
        }
        changeGoodsBO.setAddress(newOrders.getAddress());
    }

    /**
     * 设置换货提示信息
     */
    private void setOrderChangeNotice(ChangeGoodsMainInfo changeGoods, ChangeGoodsBO changeGoodsBO) {
        OrderDeliveryTypeBo orderDeliveryTypeBo = createOrderChangeNotice(changeGoods.getStatus().intValue(), changeGoods.getExpressCompany(), changeGoods.getExpressNumber(), changeGoods.getExpressId());
        // 重新赋值提示信息
        if ("20".equals(changeGoodsBO.getDeliveryTpye())) {
            if (changeGoodsBO.getStatus() == OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_10.getId()) {
                orderDeliveryTypeBo.setId(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_10.getId());
                orderDeliveryTypeBo.setTitle(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_10.getTitle());
                orderDeliveryTypeBo.setRemark(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_10.getRemark());
            } else if (changeGoodsBO.getStatus() == OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_20.getId()) {
                orderDeliveryTypeBo.setId(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_20.getId());
                orderDeliveryTypeBo.setTitle(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_20.getTitle());
                orderDeliveryTypeBo.setRemark(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_20.getRemark());
            } else if (changeGoodsBO.getStatus() == OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_50.getId()) {
                orderDeliveryTypeBo.setId(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_50.getId());
                orderDeliveryTypeBo.setTitle(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_50.getTitle());
                orderDeliveryTypeBo.setRemark(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_50.getRemark());
            } else if (changeGoodsBO.getStatus() == OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_30.getId()) {
                orderDeliveryTypeBo.setId(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_30.getId());
                orderDeliveryTypeBo.setTitle(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_30.getTitle());
                orderDeliveryTypeBo.setRemark(OrderChangeNewDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_30.getRemark());
            }

        }
        changeGoodsBO.setNotice(orderDeliveryTypeBo);
    }

    // 获取通知信息
    private OrderDeliveryTypeBo createOrderChangeNotice(int status, String expressCompany, String expressNumber, int expressId) {
        if (status == 20) {
            OrderDeliveryTypeBo orderDeliveryType = new OrderDeliveryTypeBo();
            orderDeliveryType.setId(OrderDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_20.getId());
            orderDeliveryType.setTitle(OrderDeliveryType.ORDER_CHANGE_DELIVERY_TYPE_20.getTitle());
            orderDeliveryType.setExpressCompany(expressCompany);
            orderDeliveryType.setExpressNumber(expressNumber);
            orderDeliveryType.setExpressId(expressId);
            return orderDeliveryType;
        }
        OrderDeliveryTypeBo orderDeliveryType = new OrderDeliveryTypeBo();
        for (OrderDeliveryType s : OrderDeliveryType.values()) {
            if (s.getId() == status) {
                orderDeliveryType.setId(s.getId());
                orderDeliveryType.setTitle(s.getTitle());
                orderDeliveryType.setRemark(s.getRemark());
                if (status == 30 || status == 40 || status == 50) {
                    orderDeliveryType.setExpressCompany(expressCompany);
                    orderDeliveryType.setExpressNumber(expressNumber);
                    orderDeliveryType.setExpressId(expressId);
                }
            }

        }
        return orderDeliveryType;
    }

    /**
     * 设置换货状态信息
     */
    private void setOrderChangeStatus(ChangeGoodsMainInfo changeGoods, ChangeGoodsBO changeGoodsBO) {
        List<OrderChangeStatusBo> orderChangeStatusBoList = new ArrayList<OrderChangeStatusBo>();
        // 换货单已取消
        if (changeGoods.getStatus().intValue() == 91) {
            // 提交申请
            OrderChangeStatusBo submitStatus = new OrderChangeStatusBo();
            submitStatus.setName(OrderConfig.getChangeStatu().get(0));
            submitStatus.setAct(Constants.YES);
            submitStatus.setKey(0);
            // 已取消
            OrderChangeStatusBo cancelStatus = new OrderChangeStatusBo();
            cancelStatus.setName(OrderConfig.getChangeStatu().get(91));
            cancelStatus.setAct(Constants.YES);
            cancelStatus.setKey(91);
            orderChangeStatusBoList.add(submitStatus);
            orderChangeStatusBoList.add(cancelStatus);
        } else {
            Map<Integer, String> changeStatus = OrderConfig.getChangeStatu();
            for (Map.Entry<Integer, String> returnStatusEntry : changeStatus.entrySet()) {
                // 已取消或换货发出
                if (returnStatusEntry.getKey() == 91 || returnStatusEntry.getKey() == 40) {
                    continue;
                }
                OrderChangeStatusBo orderChangeStatusBo = new OrderChangeStatusBo();
                if (returnStatusEntry.getKey() == 20 && "20".equals(changeGoodsBO.getDeliveryTpye())) {
                    orderChangeStatusBo.setName("上门取货");
                } else {
                    orderChangeStatusBo.setName(returnStatusEntry.getValue());
                }
                orderChangeStatusBo.setAct(returnStatusEntry.getKey().intValue() <= changeGoods.getStatus().intValue() ? Constants.YES : Constants.NO);
                orderChangeStatusBo.setKey(returnStatusEntry.getKey());
                orderChangeStatusBoList.add(orderChangeStatusBo);
            }
        }
        Collections.sort(orderChangeStatusBoList);
        changeGoodsBO.setStatusList(orderChangeStatusBoList);
    }

    /**
     * 设置换货商品信息
     */
    private void setOrderChangeGoods(ChangeGoodsMainInfo changeGoods, Orders srcOrders, Orders newOrders, ChangeGoodsBO changeGoodsBO) {
        ChangeGoods changeGoodsQuery = new ChangeGoods();
        changeGoodsQuery.setChangePurchaseId(changeGoods.getId());
        List<ChangeGoods> changeGoodsList = iChangeGoodsMapper.selectChangeGoodsList(changeGoodsQuery);
        if (changeGoodsList.isEmpty()) {
            logger.warn("GetChangeGoodsDetaill {}, can not find change_goods_list by change_purchase_id {}.", changeGoods.getOrderCode(), changeGoods.getId());
            changeGoodsBO.setGoodsList(Collections.emptyList());
        } else {
            // 封装换货信息
            List<OrderChangeOrderDetailRsp> orderDetailRsp = wrapOrderChangeGoods(changeGoodsList);
            // 更新商品信息
            setOrderChangeGoodsProductInfo(changeGoodsList, orderDetailRsp);
            // 更新源订单商品信息
            setOrderChangeGoodsSrcGoodsInfo(srcOrders, changeGoodsList, orderDetailRsp);
            // 更新新订单商品信息
            setOrderChangeGoodsNewGoodsInfo(newOrders, changeGoodsList, orderDetailRsp);
            // 更新并封装图片信息
            setOrderChangeGoodsEvidenceImages(changeGoods, changeGoodsList, orderDetailRsp);
            changeGoodsBO.setGoodsList(orderDetailRsp);
        }

    }


    private List<OrderChangeOrderDetailRsp> wrapOrderChangeGoods(List<ChangeGoods> changeGoodsList) {
        List<OrderChangeOrderDetailRsp> orderDetailRsp = new ArrayList<OrderChangeOrderDetailRsp>();
        //换货类型  通过yml操作工具类获取key为exchangeType的yml配置内容
        Map<String, Object> exchangeType = RefundUtils.getRefundMap(OrderConstant.YML_CHANGE_REASON_TYPE);
        for (ChangeGoods goods : changeGoodsList) {
            OrderChangeOrderDetailRsp orderChangeOrderDetailRsp = new OrderChangeOrderDetailRsp();
            orderChangeOrderDetailRsp.setProduct_skc(goods.getSourceProductSkc().toString());
            orderChangeOrderDetailRsp.setProduct_sku(goods.getSourceProductSku().toString());
            orderChangeOrderDetailRsp.setNew_product_skc(goods.getProductSkc().toString());
            orderChangeOrderDetailRsp.setNew_product_skn(goods.getProductSkn().toString());
            orderChangeOrderDetailRsp.setNew_product_sku(goods.getProductSku().toString());
            orderChangeOrderDetailRsp.setReason(goods.getExchangeReason().toString());
            orderChangeOrderDetailRsp.setReason_name((String) exchangeType.get(RefundUtils.convertKeyToYAML(goods.getExchangeReason().toString())));
            orderChangeOrderDetailRsp.setRemark(goods.getRemark());
            orderChangeOrderDetailRsp.setGoods_type(OrderGoodsTypeUtils.getOrderGoodsTypeMap(goods.getGoodsType() + "").get("en") + "");
            //后面辅助用
            orderChangeOrderDetailRsp.setGoods_type_id(Integer.valueOf(goods.getGoodsType()));
            orderChangeOrderDetailRsp.setId(goods.getId());
            orderDetailRsp.add(orderChangeOrderDetailRsp);
        }
        return orderDetailRsp;
    }


    private void setOrderChangeGoodsNewGoodsInfo(Orders newOrders, List<ChangeGoods> changeGoodsList, List<OrderChangeOrderDetailRsp> orderDetailRsp) {
        List<OrderGoodsRequest> newOrderGoodsRequestList = new ArrayList<OrderGoodsRequest>();
        for (ChangeGoods goods : changeGoodsList) {
            OrderGoodsRequest orderGoodsRequestNew = new OrderGoodsRequest();
            orderGoodsRequestNew.setOrderId(newOrders.getId().toString());
            orderGoodsRequestNew.setErpSkuId(goods.getProductSku().toString());
            newOrderGoodsRequestList.add(orderGoodsRequestNew);
        }
        List<OrdersGoods> newOrderGoods = iOrdersGoodsMapper.selectOrderGoodsByOrderList(newOrderGoodsRequestList);
        //换后数据封装
        for (OrdersGoods ordersGoods : newOrderGoods) {
            for (OrderChangeOrderDetailRsp orderChangeOrderDetailRsp : orderDetailRsp) {
                if (orderChangeOrderDetailRsp.getNew_product_sku().equals(String.valueOf(ordersGoods.getErpSkuId()))) {
                    orderChangeOrderDetailRsp.setNew_size_name(ordersGoods.getSizeName());
                    orderChangeOrderDetailRsp.setNew_color_name(ordersGoods.getColorName());
                }
            }
        }
    }


    private void setOrderChangeGoodsProductInfo(List<ChangeGoods> changeGoodsList, List<OrderChangeOrderDetailRsp> orderDetailRsp) {
        //执行批量查询，根据skn获取商品集合
        List<Integer> productSknList = new ArrayList<Integer>();
        for (ChangeGoods goods : changeGoodsList) {
            productSknList.add(goods.getProductSkn());
        }
        BatchBaseRequest<Integer> baseRequest = new BatchBaseRequest<>();
        baseRequest.setParams(productSknList);
        List<ProductBo> productBoList = Arrays.asList(serviceCaller.call(ServerURL.PRODUCT_BATCH_QUERY_PRODUCT_BASIC_INFO, baseRequest, ProductBo[].class));
        for (ProductBo productBo : productBoList) {
            for (OrderChangeOrderDetailRsp orderChangeOrderDetailRsp : orderDetailRsp) {
                if (orderChangeOrderDetailRsp.getNew_product_skn().equals(String.valueOf(productBo.getErpProductId()))) {
                    orderChangeOrderDetailRsp.setProduct_skn(productBo.getErpProductId().toString());
                    orderChangeOrderDetailRsp.setProduct_name(productBo.getProductName());
                }
            }
        }
    }

    private void setOrderChangeGoodsSrcGoodsInfo(Orders srcOrders, List<ChangeGoods> changeGoodsList, List<OrderChangeOrderDetailRsp> orderDetailRsp) {
        // 批量查询源订单商品信息
        List<OrderGoodsRequest> orderGoodsRequestList = new ArrayList<OrderGoodsRequest>();
        for (ChangeGoods goods : changeGoodsList) {
            OrderGoodsRequest orderGoodsRequest = new OrderGoodsRequest();
            orderGoodsRequest.setOrderId(srcOrders.getId().toString());
            orderGoodsRequest.setErpSkuId(goods.getSourceProductSku().toString());
            orderGoodsRequestList.add(orderGoodsRequest);
        }
        List<OrdersGoods> srcOrderGoods = iOrdersGoodsMapper.selectOrderGoodsByOrderList(orderGoodsRequestList);
        for (OrdersGoods ordersGoods : srcOrderGoods) {
            for (OrderChangeOrderDetailRsp orderChangeOrderDetailRsp : orderDetailRsp) {
                if (orderChangeOrderDetailRsp.getProduct_sku().equals(String.valueOf(ordersGoods.getErpSkuId()))) {
                    orderChangeOrderDetailRsp.setSrc_product_id(ordersGoods.getProductId());
                    orderChangeOrderDetailRsp.setSrc_goods_id(ordersGoods.getGoodsId());
                    orderChangeOrderDetailRsp.setSize_name(ordersGoods.getSizeName());
                    orderChangeOrderDetailRsp.setColor_name(ordersGoods.getColorName());
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    orderChangeOrderDetailRsp.setSales_price((orderChangeOrderDetailRsp.getGoods_type_id() == 2 ? decimalFormat.format(0) : decimalFormat.format(ordersGoods.getSalesPrice())));
                }
            }
        }
        // 封装源订单查询参数批量查询订单信息
        setOrderChangeGoodsImages(srcOrderGoods, orderDetailRsp);
    }

    private void setOrderChangeGoodsImages(List<OrdersGoods> srcOrderGoods, List<OrderChangeOrderDetailRsp> orderDetailRsp) {
        List<ProductOrderRequest> productOrderRequest = new ArrayList<ProductOrderRequest>();
        for (OrdersGoods ordersGoods : srcOrderGoods) {
            ProductOrderRequest productOrder = new ProductOrderRequest();
            productOrder.setGoodsId(ordersGoods.getGoodsId());
            productOrder.setProductId(ordersGoods.getProductId());
            productOrderRequest.add(productOrder);
        }
        //封装查询参数
        BatchProductOrderRequest batchProductOrderRequest = new BatchProductOrderRequest();
        batchProductOrderRequest.setProductOrderRequest(productOrderRequest);
        //skc信息 product 信息
        //封装商品图片信息
        ChangeProductBo[] changeProductBos = serviceCaller.call(ServerURL.PRODUCT_BATCH_QUERY_CHANGE_PRODUCT_SKC_INFO, batchProductOrderRequest, ChangeProductBo[].class);
        for (ChangeProductBo changeProductBo : changeProductBos) {
            ProductBo productBo = changeProductBo.getProductBo();
            GoodsBo goodsBo = changeProductBo.getGoodsBo();
            for (OrderChangeOrderDetailRsp orderChangeOrderDetailRsp : orderDetailRsp) {
                if (productBo != null && productBo.getId().intValue() == orderChangeOrderDetailRsp.getSrc_product_id()
                        && goodsBo != null && goodsBo.getId().intValue() == orderChangeOrderDetailRsp.getSrc_goods_id()) {
                    //产品已经处理
                    orderChangeOrderDetailRsp.setGoods_image(goodsBo.getColorImage());
                }
            }
        }
    }

    private void setOrderChangeGoodsEvidenceImages(ChangeGoodsMainInfo changeGoods, List<ChangeGoods> changeGoodsList, List<OrderChangeOrderDetailRsp> orderDetailRsp) {
        List<ApplyGoodsImages> applyGoodsImagesList = new ArrayList<ApplyGoodsImages>();
        for (ChangeGoods goods : changeGoodsList) {
            if (OrderConstant.CHANGE_REASON_TYPE.contains(goods.getExchangeReason())) {
                ApplyGoodsImages applyGoodsImages = new ApplyGoodsImages();
                applyGoodsImages.setApplyGoodsId(goods.getId());
                applyGoodsImages.setApplyId(changeGoods.getId());
                applyGoodsImagesList.add(applyGoodsImages);
            }
        }
        // 获得图片并封装
        List<ApplyGoodsImages> applyGoodsImagesResult;
        if (applyGoodsImagesList.isEmpty()) {
            applyGoodsImagesResult = Collections.emptyList();
        } else {
            applyGoodsImagesResult = iApplyGoodsImagesDao.selectImagesBatch(applyGoodsImagesList);
        }
        for (OrderChangeOrderDetailRsp orderChangeOrderDetailRsp : orderDetailRsp) {
            List<String> imgs = new ArrayList<String>();
            for (ApplyGoodsImages applyGoodsImages : applyGoodsImagesResult) {
                if (orderChangeOrderDetailRsp.getId() != null &&
                        orderChangeOrderDetailRsp.getId().intValue() == applyGoodsImages.getApplyGoodsId().intValue()) {
                    imgs.add(ImagesHelper.template2(applyGoodsImages.getImagePath(), OrderConstant.IMG_BUCKET, OrderConstant.IMG_POSITION, OrderConstant.IMG_BACKGROUND));
                }
            }
            orderChangeOrderDetailRsp.setEvidence_images(imgs);
        }
    }


    /**
     * 换货申请
     *
     * @see IChangeGoodsService#saveChangeGoodsApply(OrderChangeGoodsApplyReq)
     */
    @Override
    public OrderChangeGoodsApplyRsp saveChangeGoodsApply(OrderChangeGoodsApplyReq applyReq) {
        /** 数据处理 */
        // 验证换货申请请求参数
        validateSaveChangeGoodsApplyRequest(applyReq);
        long source_order_code = applyReq.getOrder_code();
        logger.info("SaveChangeGoodsApply for order {}, request is {}", source_order_code, JSONObject.toJSONString(applyReq));
        // 查询可以换货的订单
        Orders orders = findCanChangeGoodsOrdersByOrderCodeAndUid(applyReq.getOrder_code().toString(), applyReq.getUid().toString());
        // 更新skc并验证用户请求的新商品
        List<GoodsBo> newGoodsList = this.getGoodsByGoodsIds(applyReq.getGoods());

        checkNotEmpty(newGoodsList, ServiceError.ORDER_CHANGE_APPLY_NO_GOODS);
        // update skc
        List<OrderChangeGoodsDetailApplyReq> validGoodList = updateSkcAndValidateSaveChangeGoodsApplyRequestNewGoodses(applyReq.getGoods(), newGoodsList);
        checkNotEmpty(validGoodList, ServiceError.ORDER_CHANGE_APPLY_NO_GOODS);
        // 查询源订单商品
        List<OrdersGoods> srcGoodsList = getSrcOrdersGoodses(orders, validGoodList);
        checkNotEmpty(srcGoodsList, ServiceError.ORDER_CHANGE_APPLY_CHANGE_NUM_WRONG);

        // 验证数量【用户购买数量与已换货数据比较】
        validateChangeGoodsNumber(orders, applyReq.getGoods(), validGoodList, srcGoodsList);

        // 验库存
        validateStorage(validGoodList);

        //验证重复提交
        validateSubmitDuplicate(applyReq.getOrder_code());

        logger.info("SaveChangeGoodsApply for order {}, validate input params success!", source_order_code);

        // 更新地址信息【把 area_code 转换成具体地址，设置到 applyReq 里面】
        updateAddress(applyReq, orders);

        //部分参数重新封装
        applyReq.setExchange_mode(applyReq.getDelivery_tpye() == (byte) 20 ? (byte) 20 : (byte) 10);
        applyReq.setReceipt_time(StringUtils.isEmpty(applyReq.getReceipt_time()) ? "3" : applyReq.getReceipt_time());
        applyReq.setRemark(OrderConfig.CHANGE_REMARK);
        applyReq.setEmail("");
        applyReq.setExchange_goods_list(validGoodList);

        /** 数据保存 */
        long newOrderCode = getOrderCode(applyReq.getUid());
        long initOrderCode = getInitOrderCode(applyReq.getOrder_code());
        applyReq.setNew_order_code(newOrderCode);
        applyReq.setInit_order_code(initOrderCode);
        logger.info("SaveChangeGoodsApply for order {}, pre process MQ success!", source_order_code);

        //保存换货申请 [save changeGoodsApply(change_goods) change_goods_list ApplyGoodsImages]
        Integer changeOrdID = this.saveChangeApplyInfo(newOrderCode, initOrderCode, applyReq, validGoodList);

        logger.info("SaveChangeGoodsApply for order {}, save change apply[id={}] info success!", source_order_code, changeOrdID);

        // 保存 RefundGoods RefundGoodsList
        int refundId = this.saveRefundInfo(initOrderCode, changeOrdID, applyReq, validGoodList);
        logger.info("SaveChangeGoodsApply for order {}, save refund[id={}] info success!", source_order_code, refundId);
        //插入失败 回滚
        if (refundId < 1) {
            iChangeGoodsMapper.updateDelFlagByChangeGoods(changeOrdID);
            iChangeGoodsMainMapper.updateDelFlagByChangeGoods(changeOrdID);
            logger.warn("SaveChangeGoodsApply for order {}, saveRefundInfo db error!!! roll back ", source_order_code);
            throw new ServiceException(ServiceError.ORDER_CHANGE_CREATE_LOCAL_ERROR);
        }
        //创建订单 [Orders]
        createOrder(applyReq, validGoodList, newOrderCode, orders);

        logger.info("SaveChangeGoodsApply for order {}, create order[order_code = {}] success!", source_order_code, newOrderCode);

        /** MQ 通知 ERP 申请换货 */
        notifyERPByMQ(applyReq, refundId);
        // 锁库存，这个用异步 CALL
        decreaseStorage(applyReq);
        logger.info("SaveChangeGoodsApply for order {}, notice ERP success!", source_order_code);

        // 封装返回信息
        OrderChangeGoodsApplyRsp result = new OrderChangeGoodsApplyRsp();
        result.setApply_id(String.valueOf(changeOrdID));
        //换货后，清除订单各类统计信息缓存
        userOrderCache.clearOrderCountCache(orders.getUid());
        logger.info("SaveChangeGoodsApply for order {}, process success.", applyReq.getOrder_code());
        return result;
    }

    /**
     * 创建新订单
     */
    private void createOrder(OrderChangeGoodsApplyReq applyReq, List<OrderChangeGoodsDetailApplyReq> validGoodList, long newOrderCode, Orders sourceOrders) {
        ChangeOrdersBo order = new ChangeOrdersBo();
        order.setUid(applyReq.getUid());
        order.setOrderCode(newOrderCode);
        order.setUserName(applyReq.getConsignee_name());
        order.setMobile(applyReq.getMobile());
        order.setAreaCode(Integer.valueOf(applyReq.getArea_code()));
        order.setAddress(applyReq.getAddress());
        if (StringUtils.isNotBlank(applyReq.getZip_code())) {
            try {
                order.setZipCode(Integer.valueOf((applyReq.getZip_code())));
            } catch (Exception e) {
                logger.warn("SaveChangeGoodsApply for order {}, got a fail zip code {} uid is {}.", applyReq.getOrder_code(), applyReq.getZip_code(), applyReq.getUid());
                order.setZipCode(0);
            }
        } else {
            logger.warn("SaveChangeGoodsApply for order {}, can not find zip code uid is {}.", applyReq.getOrder_code(), applyReq.getZip_code(), applyReq.getUid());
            order.setZipCode(0);
        }
        order.setReceivingTime(Integer.valueOf(applyReq.getReceipt_time()));
        order.setReceiptTime(applyReq.getReceipt_time());
        order.setExchangeGoodsList(validGoodList);
        order.setPaymentType(sourceOrders.getPaymentType());
        //封装参数入库
        int res = this.saveChangeOrder(order);
        if (res <= 0) {
            logger.warn("SaveChangeGoodsApply for order {}, saveChangeGoodsApply !!!create order db error ", applyReq.getOrder_code());
            throw new ServiceException(ServiceError.ORDER_CHANGE_CREATE_LOCAL_ERROR);
        }
    }

    /**
     * 通知 ERP
     */
    private void notifyERPByMQ(OrderChangeGoodsApplyReq applyReq, int refundId) {
        JSONObject request = new JSONObject();
        request.put("id", refundId);
        request.put("city", applyReq.getCity());
        request.put("area_code", applyReq.getArea_code());
        request.put("address", applyReq.getAddress());
        request.put("consignee_name", applyReq.getConsignee_name());
        request.put("county", applyReq.getCounty());
        request.put("delivery_tpye", applyReq.getDelivery_tpye());
        request.put("email", applyReq.getEmail());
        request.put("exchange_mode", applyReq.getExchange_mode());
        request.put("mobile", applyReq.getMobile());
        request.put("order_code", applyReq.getNew_order_code());
        request.put("init_order_code", applyReq.getInit_order_code());
        request.put("source_order_code", applyReq.getOrder_code());
        request.put("province", applyReq.getProvince());
        request.put("receipt_time", applyReq.getReceipt_time());
        request.put("remark", applyReq.getRemark());
        request.put("uid", applyReq.getUid());
        request.put("zip_code", applyReq.getZip_code());
        request.put("goods", applyReq.getGoods());
        mqErpService.createChangeOrder(request);
    }

    private void decreaseStorage(OrderChangeGoodsApplyReq applyReq) {
        BatchUpdateStorageRequest batchUpdateStorageRequest = new BatchUpdateStorageRequest();
        List<UpdateStorageRequest> updateStorageList = new ArrayList<>();
        List<OrderChangeGoodsDetailApplyReq> goods = applyReq.getGoods();
        for (OrderChangeGoodsDetailApplyReq good : goods) {
            UpdateStorageRequest storage = new UpdateStorageRequest();
            storage.setSkuId(good.getNew_product_sku());
            storage.setStorageNum(1);
            updateStorageList.add(storage);
        }
        batchUpdateStorageRequest.setUpdateStorageRequest(updateStorageList);
        try {
            serviceCaller.asyncCall("product.batchDecreaseStorageBySkuId", batchUpdateStorageRequest, VoidResponse.class);
        } catch (Exception e) {
            // ignore
        }
    }


    /**
     * 根据uid获取一个订单号
     */
    private Long getOrderCode(Integer uid) {
        checkID(uid, ServiceError.SHOPPING_UID_IS_NULL);
        OrderCodeQueue orderCodeQueue = new OrderCodeQueue();
        orderCodeQueue.setUid(uid);
        orderCodeQueueDAO.insertUid(orderCodeQueue);
        Long orderCode = orderCodeListDAO.selectOrderCodeById(orderCodeQueue.getId());
        //获取订单号失败！
        checkArgument(orderCode > 0, ServiceError.SHOPPING_CART_GET_ORDERCODE_ERROR);
        int twoDigitsYear = LocalDate.now().getYear() - 2000;
        return orderCode < 10000000 ? new Long(twoDigitsYear + "0" + orderCode) : new Long(twoDigitsYear + "" + orderCode);
    }

    /**
     * 获取换货的原始订单
     */
    private long getInitOrderCode(long orderCode) {
        // 查看有没有申请记录
        ChangeGoodsMainInfo apply = iChangeGoodsMainMapper.selectByOrderCode(orderCode);
        return apply == null ? orderCode : apply.getInitOrderCode();
    }

    /**
     * MQ 消费者，更新退货相关表信息
     * {
     * "id":1,
     * "init_order_code":"111",
     * "source_order_code":1111,
     * "new_order_code":1111,
     * "exchange_id":111,
     * "returned_id":111,
     * "exchange_status":10,
     * "returned_status":10
     * }
     */
    @Override

    public void asyncUpdateChangeGoodsApply(OrderChangeGoodsApplyErpRsp erpRsp) {
        validateApplyErpRsp(erpRsp);

        // 更新退货申请表
        int refundedStatus = erpRsp.getReturned_status();
        RefundGoods refundGoods = refundGoodsDao.selectByPrimaryKey(erpRsp.getId());
        checkNotNull(refundGoods, ServiceError.ORDER_CHANGE_APPLY_ERP_ERROR);

        //这里说明是顾客已经主动取消了,所以需要发送一个mq消息给erp
        if (refundGoods.getStatus() == 91) {
            logger.info("user:uid {} has cancel change request, refundId {},orderCode {}", erpRsp.getUid(), erpRsp.getId(), erpRsp.getInit_order_code());
            //发送mq消息给erp
            JSONObject obj = new JSONObject();
            obj.put("id", erpRsp.getExchange_id());
            obj.put("status", 900);
            obj.put("type", 2);//2表示换货
            orderMqService.sendChangeRefundCancelMessage(obj);
            return;
        }
        refundGoods.setErpRefundId(erpRsp.getReturned_id());
        refundGoods.setStatus((byte) refundedStatus);
        refundGoodsDao.updateByPrimaryKeySelective(refundGoods);
        // 更新退货申请表
        RefundGoodsList record = new RefundGoodsList();
        record.setStatus((byte) refundedStatus);
        record.setReturnRequestId(refundGoods.getId());
        refundGoodsListDao.updateByReturned(record);
        // 更新换货申请表
        int exchangeStatus = erpRsp.getExchange_status();
        int changeOrderApplyId = refundGoods.getChangePurchaseId();
        ChangeGoodsMainInfo apply = new ChangeGoodsMainInfo();
        apply.setId(changeOrderApplyId);
        apply.setErpExchangeId(erpRsp.getExchange_id());
        apply.setStatus((byte) exchangeStatus);
        iChangeGoodsMainMapper.updateByPrimaryKeySelective(apply);
        // 更新换货商品表
        iChangeGoodsMapper.updateStatusByChangePurchaseId(changeOrderApplyId, (byte) exchangeStatus);
    }

    /**
     * {"id":1,"status":40,"type":"change"}
     */
    public void syncChangeGoodsStatus(Integer id, Integer status) {
        if (id == null || id <= 0 || status == null) {
            return;
        }
        ChangeGoodsMainInfo apply = iChangeGoodsMainMapper.selectByErpChangeGoodsId(id);
        if (apply == null) {
            logger.warn("change apply id is {} not exist", id);
            return;
        }
        // 审核通过
        if (status == 10) {
            RefundGoods refundGoods = refundGoodsDao.selectByUidAndChangePurchaseId(apply.getUid(), apply.getId());
            if (refundGoods != null) {
                updateRefundStatus(refundGoods, (byte) 10);
            } else {
                logger.warn("Not find refundGoods by change id {}", apply.getId());
            }
        }
        // 商品入库（仅商品寄回发该状态）
        else if (status == 30) {
            RefundGoods refundGoods = refundGoodsDao.selectByUidAndChangePurchaseId(apply.getUid(), apply.getId());
            if (refundGoods != null) {
                updateRefundStatus(refundGoods, (byte) 30);
            } else {
                logger.warn("Not find refundGoods by change id {}", apply.getId());
            }
        }
        // 上门换货（仅上门换货发该状态）
        else if (status == 40) { // 上门换货
            Byte exchangeMode = apply.getExchangeMode();
            if (exchangeMode == null || DeliveryType.TYPE_DOOR.getId() == exchangeMode.intValue()) {
                logger.info("change apply id is {}, change status 40 to 20");
                status = 20; // 商品寄回
            } else {
                logger.warn("change apply status error, exchange mode {} but erp back status {}", apply.getExchangeMode(), status);
                return;
            }
        }
        // 换货完成
        else if (status == 50) {
            RefundGoods refundGoods = refundGoodsDao.selectByUidAndChangePurchaseId(apply.getUid(), apply.getId());
            if (refundGoods != null) {
                updateRefundStatus(refundGoods, (byte) 40);
            } else {
                logger.warn("Not find refundGoods by change id {}", apply.getId());
            }
        }
        // 客服驳回
        else if (status == 91) {
            // 取消订单
            orderCancelService.updateOrderStatus(apply.getOrderCode(), 901, 0, "");
            // 取消退货单
            RefundGoods refundGoods = refundGoodsDao.selectByUidAndChangePurchaseId(apply.getUid(), apply.getId());
            if (refundGoods != null) {
                updateRefundStatus(refundGoods, (byte) 91);
            } else {
                logger.warn("Not find refundGoods by change id {}", apply.getId());
            }
        }
        // 更新换货申请表
        apply.setStatus(status.byteValue());
        iChangeGoodsMainMapper.updateByPrimaryKeySelective(apply);
        logger.info("change apply id is {} success now status is {}", id, status);
        // 更新换货商品表
        iChangeGoodsMapper.updateStatusByChangePurchaseId(apply.getId(), status.byteValue());
        logger.info("change apply id is {}  success now status is {}", id, status);
    }

    private void updateRefundStatus(RefundGoods refundGoods, byte status) {
        logger.info("update refund status for {}, status is {}", refundGoods.getChangePurchaseId(), status);
        refundGoods.setStatus(status);
        refundGoodsDao.updateByPrimaryKey(refundGoods);
        RefundGoodsList refundGoodsList = new RefundGoodsList();
        refundGoodsList.setStatus(status);
        refundGoodsList.setReturnRequestId(refundGoods.getId());
        refundGoodsListDao.updateByReturned(refundGoodsList);
        logger.info("update refund status for {} success, status is {}", refundGoods.getChangePurchaseId(), status);
    }

    private void validateApplyErpRsp(OrderChangeGoodsApplyErpRsp erpRsp) {
        checkID(erpRsp.getId(), ServiceError.ORDER_CHANGE_APPLY_ERP_ERROR);
    }

    /**
     * 验证商品库存
     * 注意：
     * 用户可能选同一种商品
     *
     * @param goods
     */
    private void validateStorage(List<OrderChangeGoodsDetailApplyReq> goods) {
        // 获取所有商品 SKU
        Map<Integer, Integer> skus = new HashMap<>();
        for (OrderChangeGoodsDetailApplyReq good : goods) {
            Integer num = skus.get(good.getNew_product_sku());
            if (num == null) {
                skus.put(good.getNew_product_sku(), 1);
            } else {
                skus.put(good.getNew_product_sku(), num + 1);
            }
        }
        BatchBaseRequest<Integer> request = new BatchBaseRequest<>();
        request.setParams(new ArrayList<>(skus.keySet()));
        // 批量查询库存
        List<JSONObject> storageList = null;
        try {
            storageList = serviceCaller.call("product.queryStorageBySkuIds", request, List.class);
        } catch (Exception e) {
            // 页面有判断，这里如果报错就不管了
            return;
        }
        checkNotEmpty(storageList, ServiceError.ORDER_CHANGE_CREATE_STORAGE_NOT_ENOUGH);
        logger.info("Find storage back json of {}", storageList);
        // 查询库存是否充足【理论上我们传过去的 sku 都应该可以查到库存】
        for (JSONObject jsonObj : storageList) {
            int num = jsonObj.getIntValue("storageNum");
            int sku = jsonObj.getIntValue("erpSkuId");
            if (skus.get(sku) == null) {
                continue;
            }
            if (skus.get(sku) > num) {
                throw new ServiceException(ServiceError.ORDER_CHANGE_CREATE_STORAGE_NOT_ENOUGH);
            }
        }

    }

    /**
     * 验证是否重复提交
     *
     * @param orderCode 订单 CODE
     */
    private void validateSubmitDuplicate(Long orderCode) {
        boolean isFirstSubmit;
        try {
            isFirstSubmit = valueOperations.get(Constant.CACHE_ORDER_EXCHANGE_CODE + orderCode) == null;
            if (isFirstSubmit) {
                valueOperations.set(Constant.CACHE_ORDER_EXCHANGE_CODE + orderCode, orderCode + "", Constant.CHANGE_SUBMIT_TIM_SECOND, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            isFirstSubmit = true;
        }
        if (!isFirstSubmit) {
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_SUBMIT_REPEAT);
        }
    }

    /**
     * 更新区域信息
     */
    private void updateAddress(OrderChangeGoodsApplyReq applyReq, Orders orders) {

        /** 根据 address_id 查询用户常用地址 */
        AreaRspBo areaRspBo = null;
        logger.info("Change apply of order {}, update address[{}] begin~", applyReq.getOrder_code(), applyReq.getAddress_id());
        if (applyReq.getAddress_id() != null && applyReq.getAddress_id() > 0) {
            UserAddressReqBO addressReq = new UserAddressReqBO();
            addressReq.setId(applyReq.getAddress_id());
            UserAddressRspBO addressRspBO = serviceCaller.call("users.getAddress", addressReq, UserAddressRspBO.class);
            logger.info("Change apply of order {}, call address success result is {}", applyReq.getOrder_code(), addressRspBO);
            if (addressRspBO != null) {
                applyReq.setConsignee_name(addressRspBO.getAddresseeName());
                applyReq.setAddress(addressRspBO.getAddress());
                applyReq.setMobile(addressRspBO.getMobile());
                applyReq.setZip_code(addressRspBO.getZipCode());
                areaRspBo = addressRspBO.getArea();
            }
        }

        /** 处理 PC 段带 **** 手机号码，如果用户没有修改就用原来的 */
        if (applyReq.getMobile() == null || applyReq.getMobile().contains("*")) {
            applyReq.setMobile(orders.getMobile());
        }

        /** 如果没有 address id, 用老数据，老方法 */
        if (areaRspBo == null) {
            /** 更新区域信息 */
            AreaReqBO areaReqBO = new AreaReqBO();
            areaReqBO.setCode(Integer.valueOf(applyReq.getArea_code()));
            areaRspBo = serviceCaller.call(ServerURL.USERS_GET_AREA_INFO_BY_CODE, areaReqBO, AreaRspBo.class);
            logger.info("Change apply of order {}, call getAreaByCode success result is {}", applyReq.getOrder_code(), areaRspBo);
        }

        logger.info("Change apply of order {}, area info is {}", applyReq.getOrder_code(), areaRspBo);
        updateAreaInfo(applyReq, areaRspBo);
    }

    /**
     * 更新省市区信息
     *
     * @param applyReq
     * @param areaRspBo
     */
    private void updateAreaInfo(OrderChangeGoodsApplyReq applyReq, AreaRspBo areaRspBo) {
        if (areaRspBo == null) {
            logger.warn("Change apply of order {}, can not find AreaRspBo by code {}", applyReq.getOrder_code(), applyReq.getArea_code());
            return;
        }
        applyReq.setCounty(areaRspBo.getCaption());
        AreaRspBo city = areaRspBo.getParent();
        if (city == null) {
            logger.warn("Change apply of order {}, can't find city of area code {}", applyReq.getOrder_code(), applyReq.getArea_code());
            return;
        }
        applyReq.setCity(city.getCaption());
        AreaRspBo province = city.getParent();
        if (province == null) {
            logger.warn("Change apply of order {}, can't find province of area code {}", applyReq.getOrder_code(), applyReq.getArea_code());
            return;
        }
        String address = applyReq.getAddress();
        applyReq.setProvince(province.getCaption());
        address = address.replace(province.getCaption(), "");
        applyReq.setAddress(address);
    }

    private void validateChangeGoodsNumber(Orders orders, List<OrderChangeGoodsDetailApplyReq> changeGoodsList, List<OrderChangeGoodsDetailApplyReq> validGoodList, List<OrdersGoods> srcOrderGoods) {
        // 查询订单中商品的退货情况
        RefundGoodsCnt refundGoodsCnt = new RefundGoodsCnt();
        refundGoodsCnt.setOrderCode(orders.getOrderCode());
        List<RefundGoodsCnt> refundGoodsCntList = iRefundGoodsListMapper.selectRefundGoodsCnt(refundGoodsCnt);
        logger.info("saveChangeGoodsApply refundGoodsCntList Result is {}", JSONObject.toJSONString(refundGoodsCntList));
        logger.info("saveChangeGoodsApply result is {}", JSONObject.toJSONString(srcOrderGoods));
        //校验 是否您申请的换货商品中存在换货数量大于可以进行换货的商品的数
        HashMap<String, OrderChangeApplyProdCheckReq> checkReqHashMap = getOrderChangeApplyProdCheckReqHashMap(changeGoodsList);
        for (Map.Entry<String, OrderChangeApplyProdCheckReq> entry : checkReqHashMap.entrySet()) {
            OrderChangeApplyProdCheckReq mapChange = entry.getValue();
            for (OrdersGoods ordersGoods : srcOrderGoods) {
                if (ordersGoods.getErpSkuId().intValue() == mapChange.getProduct_sku()
                        && ordersGoods.getGoodsPrice().compareTo(new BigDecimal(Double.valueOf(mapChange.getLast_price()))) == 0) {
                    int currentOrdersGoodNum = countOrdersGoodsNumBySkuAndPrice(ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice(), srcOrderGoods);
                    if (mapChange.getNum() + this.getRefundNumByRefundList(refundGoodsCntList, mapChange) > currentOrdersGoodNum) {
                        logger.warn("change goods num check error : sku {},,price {}, OrdersGoodNum {},changeGoods num:{},already changeNum:{} ",
                                ordersGoods.getErpSkuId(), ordersGoods.getGoodsPrice(), mapChange.getNum(), getRefundNumByRefundList(refundGoodsCntList, mapChange));
                        throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_CHANGE_NUM_WRONG);
                    }
                }

            }
        }
    }

    //根据退货list和换货申请，查询对应的退货商品数量
    private int getRefundNumByRefundList(List<RefundGoodsCnt> refundGoodsCntList, OrderChangeApplyProdCheckReq orderChangeApplyProdCheckReq) {
        if (refundGoodsCntList == null || refundGoodsCntList.size() == 0 || orderChangeApplyProdCheckReq == null) {
            return 0;
        }
        for (RefundGoodsCnt refundGoodsCnt : refundGoodsCntList) {
            if (refundGoodsCnt.getProductSku() != null
                    && refundGoodsCnt.getProductSku().intValue() == Integer.valueOf(orderChangeApplyProdCheckReq.getProduct_sku())
                    && refundGoodsCnt.getLastPrice().compareTo(new BigDecimal(Double.valueOf(orderChangeApplyProdCheckReq.getLast_price()))) == 0) {
                return refundGoodsCnt.getCnt();
            }
        }
        return 0;
    }

    //查询对应的订单商品中 ，指定sku和price的 商品数量
    private int countOrdersGoodsNumBySkuAndPrice(Integer sku, BigDecimal price, List<OrdersGoods> ordersGoodsList) {
        int count = 0;
        if (ordersGoodsList == null || ordersGoodsList.size() == 0 || sku == null || price == null) {
            return count;
        }
        for (OrdersGoods ordersGoods : ordersGoodsList) {
            if (ordersGoods.getErpSkuId() != null
                    && ordersGoods.getErpSkuId().equals(sku)
                    && ordersGoods.getGoodsPrice().equals(price)) {
                count += ordersGoods.getNum();
            }
        }
        return count;
    }


    /**
     * 查询源订单商品信息
     *
     * @param orders
     * @param validGoodList
     * @return
     */
    private List<OrdersGoods> getSrcOrdersGoodses(Orders orders, List<OrderChangeGoodsDetailApplyReq> validGoodList) {
        List<OrderGoodsRequest> orderGoodsRequestList = new ArrayList<OrderGoodsRequest>();
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : validGoodList) {
            OrderGoodsRequest orderGoodsRequest = new OrderGoodsRequest();
            orderGoodsRequest.setErpSkuId(String.valueOf(orderChangeGoodsDetailApplyReq.getProduct_sku()));
            orderGoodsRequest.setOrderId(orders.getId().toString());
            orderGoodsRequestList.add(orderGoodsRequest);
        }
        if (orderGoodsRequestList.size() > 0) {
            return iOrdersGoodsMapper.selectOrderGoodsByOrderList(orderGoodsRequestList);
        } else {
            return Collections.emptyList();
        }
    }

    // 更新skc并验证用户请求的新商品
    private List<OrderChangeGoodsDetailApplyReq> updateSkcAndValidateSaveChangeGoodsApplyRequestNewGoodses(List<OrderChangeGoodsDetailApplyReq> changeGoodsList, List<GoodsBo> goodsBoList) {
        // 获取用户请求的新商品
        List<OrderChangeGoodsDetailApplyReq> validGoodList = new ArrayList<>();
        // 获取新商品的skc
        logger.info("saveChangeGoodsApply goodsBoList Result is {}", JSONObject.toJSONString(goodsBoList));
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : changeGoodsList) {
            for (GoodsBo goodsBo : goodsBoList) {
                if (goodsBo != null && goodsBo.getId().intValue() == orderChangeGoodsDetailApplyReq.getNew_goods_id()) {
                    //筛选有效数据o
                    orderChangeGoodsDetailApplyReq.setNew_product_skc(goodsBo.getProductSkc());
                    orderChangeGoodsDetailApplyReq.setColor_id(goodsBo.getColorId());
                    orderChangeGoodsDetailApplyReq.setColor_name(goodsBo.getColorName());
                    validGoodList.add(orderChangeGoodsDetailApplyReq);
                    break;
                }
            }
        }
        return validGoodList;
    }

    private List<OrderChangeGoodsDetailApplyReq> getSaveChangeGoodsApplyRequestNewGoodses(List<OrderChangeGoodsDetailApplyReq> changeGoodsList) {
        List<OrderChangeGoodsDetailApplyReq> newGoodList = new ArrayList<>();
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : changeGoodsList) {
            if (NumUtil.checkNumIsZero(orderChangeGoodsDetailApplyReq.getProduct_skc())) {
                continue;
            }
            if (NumUtil.checkNumIsZero(orderChangeGoodsDetailApplyReq.getProduct_sku())) {
                continue;
            }
            if (NumUtil.checkNumIsZero(orderChangeGoodsDetailApplyReq.getNew_goods_id())) {
                continue;
            }
            if (NumUtil.checkNumIsZero(orderChangeGoodsDetailApplyReq.getNew_product_sku())) {
                continue;
            }
            if (orderChangeGoodsDetailApplyReq.getRemark() == null) {
                orderChangeGoodsDetailApplyReq.setRemark(StringUtils.EMPTY);
            }
            newGoodList.add(orderChangeGoodsDetailApplyReq);
        }
        return newGoodList;
    }

    private HashMap<String, OrderChangeApplyProdCheckReq> getOrderChangeApplyProdCheckReqHashMap(List<OrderChangeGoodsDetailApplyReq> changeGoodsList) {
        HashMap<String, OrderChangeApplyProdCheckReq> checkReqHashMap = new HashMap<String, OrderChangeApplyProdCheckReq>();
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : changeGoodsList) {
            if (NumUtil.checkNumIsZero(orderChangeGoodsDetailApplyReq.getProduct_skc())) {
                continue;
            }

            if (NumUtil.checkNumIsZero(orderChangeGoodsDetailApplyReq.getProduct_sku())) {
                continue;
            }
            String key = orderChangeGoodsDetailApplyReq.getProduct_sku() + "_" + orderChangeGoodsDetailApplyReq.getLast_price();
            if (checkReqHashMap.get(key) == null) {
                OrderChangeApplyProdCheckReq orderChangeApplyProdCheckReq = new OrderChangeApplyProdCheckReq();
                orderChangeApplyProdCheckReq.setProduct_sku((orderChangeGoodsDetailApplyReq.getProduct_sku()));
                orderChangeApplyProdCheckReq.setLast_price(orderChangeGoodsDetailApplyReq.getLast_price());
                orderChangeApplyProdCheckReq.setNum(1);
                checkReqHashMap.put(key, orderChangeApplyProdCheckReq);
            } else {
                OrderChangeApplyProdCheckReq orderChangeApplyProdCheckReq = checkReqHashMap.get(key);
                orderChangeApplyProdCheckReq.setNum(orderChangeApplyProdCheckReq.getNum() + 1);
                checkReqHashMap.put(key, orderChangeApplyProdCheckReq);
            }
        }
        return checkReqHashMap;
    }


    /**
     * 验证换货申请请求参数
     *
     * @param request
     */
    private void validateSaveChangeGoodsApplyRequest(OrderChangeGoodsApplyReq request) {
        if (request == null) {
            logger.warn("SaveChangeGoodsApply fail, request is not null.");
            throw new ServiceException(ServiceError.ORDER_REQUEST_PARM_IS_EMPTY);
        }
        if (request.getUid() == null || request.getUid().intValue() == 0) {
            logger.warn("SaveChangeGoodsApply fail, request uid is not null.");
            throw new ServiceException(ServiceError.ORDER_UID_IS_EMPTY);
        }
        if (request.getOrder_code() == null || request.getOrder_code().intValue() == 0) {
            logger.warn("SaveChangeGoodsApply fail, request order_code is not null.");
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        // 验证换货商品
        List<OrderChangeGoodsDetailApplyReq> goods = request.getGoods();
        if (CollectionUtils.isEmpty(goods)) {
            logger.warn("SaveChangeGoodsApply fail, request goods is not empty. uid is {}, order_code is {}", request.getUid(), request.getOrder_code());
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_GOODS);
        }
        for (OrderChangeGoodsDetailApplyReq good : goods) {
            if (StringUtils.isEmpty(good.getExchange_reason())) {
                logger.warn("SaveChangeGoodsApply fail, request goods {} exchange_reason is not empty. uid is {}, order_code is {}", good.getId(), request.getUid(), request.getOrder_code());
                throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_CHANGE_REASON);
            }
            if (OrderConstant.CHANGE_REASON_TYPE.contains(Byte.valueOf(good.getExchange_reason()))) {
                logger.info("SaveChangeGoodsApply fail, request goods {} exchange_reason {} in {}. uid is {}, order_code is {}", good.getId(), good.getExchange_reason(), OrderConstant.CHANGE_REASON_TYPE, request.getUid(), request.getOrder_code());
                if (!vaildImg(good.getEvidence_images())) {
                    logger.warn("SaveChangeGoodsApply fail, request goods {} evidence_images {} is not validate.", good.getId(), good.getEvidence_images());
                    throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_FULL_INFO);
                }
                if (StringUtils.isEmpty(good.getRemark())) {
                    logger.warn("SaveChangeGoodsApply fail, request goods {} remark {} is empty.", good.getId(), good.getEvidence_images());
                    throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_FULL_INFO);
                }
            }
            // 检测商品 key
            if (NumUtil.checkNumIsZero(good.getProduct_skc())) {
                logger.warn("SaveChangeGoodsApply fail, Product_skc is not validate!");
                throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_FULL_INFO);
            }
            if (NumUtil.checkNumIsZero(good.getProduct_sku())) {
                logger.warn("SaveChangeGoodsApply fail, Product_sku is not validate!");
                throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_FULL_INFO);
            }
            if (NumUtil.checkNumIsZero(good.getNew_goods_id())) {
                logger.warn("SaveChangeGoodsApply fail, New_goods_id is not validate!");
                throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_FULL_INFO);
            }
            if (NumUtil.checkNumIsZero(good.getNew_product_sku())) {
                logger.warn("SaveChangeGoodsApply fail, New_goods_id is not validate!");
                throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_FULL_INFO);
            }
            if (good.getRemark() == null) {
                good.setRemark(StringUtils.EMPTY);
            }
        }
        //
        if (StringUtils.isEmpty(request.getConsignee_name())) {
            logger.warn("SaveChangeGoodsApply fail, request consignee_name is not empty. uid is {}, order_code is {}", request.getUid(), request.getOrder_code());
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_RECEIVE_NAME);
        }
        if (StringUtils.isEmpty(request.getArea_code()) || "0".equals(request.getArea_code().trim())) {
            logger.warn("SaveChangeGoodsApply fail, request area_code is not empty. uid is {}, order_code is {}", request.getUid(), request.getOrder_code());
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_AREA);
        }
        if (StringUtils.isEmpty(request.getAddress())) {
            logger.warn("SaveChangeGoodsApply fail, request address is not empty. uid is {}, order_code is {}", request.getUid(), request.getOrder_code());
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_ADDRESS);
        }
        //手机校验
        if (request.getMobile() != null && !request.getMobile().contains("*")) {
            if (!PhoneUtil.mobileVerify(request.getMobile())) {
                logger.warn("SaveChangeGoodsApply fail, request mobile {} is not a mobile number. uid is {}, order_code is {}", request.getMobile(), request.getUid(), request.getOrder_code());
                throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_PHONE_WRONG);
            }
        }
        if (request.getDelivery_tpye() <= 0) {
            logger.warn("SaveChangeGoodsApply fail, request delivery_tpye is not empty. uid is {}, order_code is {}", request.getUid(), request.getOrder_code());
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_DEVELIVERY_TYPE);
        }
        request.setRemark(YHStringUtils.filterEmojiCharacter(request.getRemark()));
        request.setAddress(YHStringUtils.filterEmojiCharacter(request.getAddress()));
    }

    /**
     * 查询可以换货的订单
     *
     * @param orderCode
     * @param uid
     * @return
     */
    private Orders findCanChangeGoodsOrdersByOrderCodeAndUid(String orderCode, String uid) {
        Orders orders = iOrdersMapper.selectByCodeAndUid(orderCode, uid);
        if (orders == null) {
            logger.warn("SaveChangeGoodsApply for order {}, can not find order by code {} and uid {}.", orderCode, orderCode, uid);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        if (orders.getOrdersStatus() != null && orders.getOrdersStatus() == 0) {
            logger.warn("SaveChangeGoodsApply for order {}, i wish the order ordersStatus not equals 0.", orderCode);
            throw new ServiceException(ServiceError.ORDER_HAS_DELETE);
        }
        if (orders.getStatus() != null && orders.getStatus() != 6) {
            logger.warn("SaveChangeGoodsApply for order {}, i wish the order status equals 6,but it is {}.", orderCode, orders.getStatus());
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_NO_SIGN);
        }
        long now = DateUtil.toSecond(new Date());
        if ((now - orders.getUpdateTime()) > Constant.EXCHANGE_LIMIT_DAY) {
            logger.warn("SaveChangeGoodsApply for order {}, i wish the order not exceed exchange_limit_day ,but now {} - update_time {} > exchange_limit_day {}.", orderCode, now, orders.getUpdateTime(), Constant.EXCHANGE_LIMIT_DAY);
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_HAS_EXPIRED);
        }
        return orders;
    }

    private boolean vaildImg(List<String> evidenceImages) {
        if (evidenceImages == null || evidenceImages.size() == 0) {
            return false;
        }
        int i = 0;
        for (String evidenceImage : evidenceImages) {
            if (evidenceImage == null || "null".equals(evidenceImage) || "".equals(evidenceImage)) {
                i++;
            }
        }
        if (i == evidenceImages.size()) {
            return false;
        }
        return true;
    }

    //保存创建换货订单
    private Integer saveChangeOrder(ChangeOrdersBo orders) {
        logger.info("ChangeOrdersBo is {} ", JSON.toJSONString(orders));
        if (orders == null || NumUtil.checkNumIsZero(orders.getOrderCode()) || NumUtil.checkNumIsZero(orders.getAreaCode())
                || StringUtils.isEmpty(orders.getUserName())
                || StringUtils.isEmpty(orders.getAddress()) || orders.getExchangeGoodsList() == null || orders.getExchangeGoodsList().size() == 0) {
            return 0;
        }
        orders.setReceiptTime(orders.getReceivingTime() < 1 ? OrderConfig.getReceiptTime().get(2) : OrderConfig.getReceiptTime().get(orders.getReceivingTime()));
        orders.setActivitiesId((short) 0);
        orders.setIsNeedRapid(String.valueOf(0));
        orders.setIsPreContact("N");
        orders.setIsPrintPrice("N");
        orders.setShipmentTime(0);
        orders.setArriveTime(0);
        orders.setRefundStatus((byte) 0);
        orders.setExchangeStatus((byte) 0);
        orders.setCancelType((byte) 0);
        orders.setIsCancel("N");
        orders.setStatus((byte) 1);
        orders.setIsArrive("N");
        orders.setIsLock("N");
        orders.setExceptionStatus((byte) 0);
        orders.setOrderType((byte) 7);
        orders.setIsInvoice("N");
        orders.setInvoicesType("0");
        orders.setInvoicesPayable("");
        orders.setYohoCoinNum(0);
        orders.setAmount(BigDecimal.valueOf(0));
        orders.setIsPrintPrice("N");
        orders.setPaymentStatus("Y");
        orders.setShippingTypeId((byte) 0);
        orders.setShippingCost(BigDecimal.valueOf(0));
        orders.setExpressId((byte) 0);
        orders.setPhone("");
        orders.setRemark("换货订单");
        orders.setAttribute((byte) 1);
        orders.setExpressNumber("0");
        //TODO 待确定 php没传
        orders.setIsPayed("Y");
        orders.setUpdateTime(DateUtil.toSecond(new Date()).intValue());
        orders.setCreateTime(DateUtil.toSecond(new Date()).intValue());
        orders.setBankCode("");
        orders.setOrdersStatus((byte) 1);


        List<ChangeGoodsRequest> list = new ArrayList<ChangeGoodsRequest>();
        List<OrderChangeGoodsDetailApplyReq> exchangeGoods = orders.getExchangeGoodsList();
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : exchangeGoods) {
            ChangeGoodsRequest changeGoodsRequest = new ChangeGoodsRequest();
            changeGoodsRequest.setPruductSkn(orderChangeGoodsDetailApplyReq.getProduct_skn());
            changeGoodsRequest.setSkuId(orderChangeGoodsDetailApplyReq.getNew_product_sku());
            list.add(changeGoodsRequest);
        }

        //封装查询参数 查询产品库存型号等信息
        BatchChangeGoodsResqust batchChangeGoodsResqust = new BatchChangeGoodsResqust();
        batchChangeGoodsResqust.setList(list);
        logger.info("createChangeOrder BatchChangeGoodsResqust Request is {}", JSON.toJSONString(batchChangeGoodsResqust));
        //sku信息 product 信息
        ChangeGoodsBo[] changeGoodsBos = serviceCaller.call(ServerURL.PRODUCT_BATCH_QUERY_CHANGE_PRODUCT_SKU, batchChangeGoodsResqust, ChangeGoodsBo[].class);
        logger.info("createChangeOrder ChangeGoodsBo is {}", JSON.toJSONString(changeGoodsBos));


        List<OrdersGoodsBo> ordersGoodsBoList = new ArrayList<OrdersGoodsBo>();
        List<OrderChangeGoodsDetailApplyReq> exchangeGoodsList = orders.getExchangeGoodsList();
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : exchangeGoodsList) {
            OrdersGoodsBo goodsBo = new OrdersGoodsBo();
            goodsBo.setKey(orderChangeGoodsDetailApplyReq.getNew_product_sku() + "_" + orderChangeGoodsDetailApplyReq.getLast_price());
            goodsBo.setUid(orders.getUid());
            goodsBo.setOrderCode(orders.getOrderCode());
            goodsBo.setGoodsId(orderChangeGoodsDetailApplyReq.getNew_goods_id());
            goodsBo.setProductSkn(orderChangeGoodsDetailApplyReq.getProduct_skn());
            goodsBo.setGoodsType(Byte.valueOf((orderChangeGoodsDetailApplyReq.getGoods_type())));
            goodsBo.setErpSkuId(orderChangeGoodsDetailApplyReq.getNew_product_sku());
            goodsBo.setGoodsPrice(BigDecimal.valueOf(Double.valueOf(orderChangeGoodsDetailApplyReq.getLast_price())));
            goodsBo.setGoodsAmount(BigDecimal.valueOf(Double.valueOf(orderChangeGoodsDetailApplyReq.getLast_price())));
            goodsBo.setNum(1);
            goodsBo.setProductSkc(orderChangeGoodsDetailApplyReq.getNew_product_skc());
            // 产品数据
            if (changeGoodsBos != null && changeGoodsBos.length > 0) {
                for (ChangeGoodsBo changeGoodsBo : changeGoodsBos) {
                    if (orderChangeGoodsDetailApplyReq.getNew_product_sku().intValue() == changeGoodsBo.getErpSkuId()) {
                        goodsBo.setColorId(changeGoodsBo.getColorId().byteValue());
                        goodsBo.setColorName(changeGoodsBo.getColorName());
                        goodsBo.setSizeId(changeGoodsBo.getSizeId());
                        goodsBo.setSizeName(changeGoodsBo.getSizeName());
                        goodsBo.setProductId(changeGoodsBo.getProductId());
                        goodsBo.setSalesPrice((changeGoodsBo.getSalePrice().longValue()));
                        goodsBo.setBrandId(changeGoodsBo.getBrandId());
                    }
                }
            }
            ordersGoodsBoList.add(goodsBo);
        }

        //相同的去重并相加
        for (int i = 0; i < ordersGoodsBoList.size(); i++) {
            for (int j = 0; j < ordersGoodsBoList.size(); j++) {
                if (i == j) continue;
                if (ordersGoodsBoList.get(i) != null && ordersGoodsBoList.get(j) != null
                        && ordersGoodsBoList.get(i).getKey().equals(ordersGoodsBoList.get(j).getKey())) {
                    OrdersGoodsBo goodsBo = ordersGoodsBoList.get(i);
                    goodsBo.setNum(ordersGoodsBoList.get(j).getNum() + goodsBo.getNum());
                    goodsBo.setGoodsAmount(ordersGoodsBoList.get(j).getGoodsPrice().multiply(BigDecimal.valueOf(goodsBo.getNum())));
                    ordersGoodsBoList.set(j, null);
                }
            }
        }

        ordersGoodsBoList = ordersGoodsBoList.stream().filter(ordersGoodsBo -> ordersGoodsBo != null).collect(Collectors.toList());


        //入库操作
        Orders orderdb = BeanTool.copyObject(orders, Orders.class);
        logger.info("createChangeOrder orderdb is {} ", JSON.toJSONString(orderdb));
        int res = iOrdersMapper.insert(orderdb);
        logger.info("createChangeOrder orderdb return id is {} ", orderdb.getId());
        if (res > 0 && ordersGoodsBoList.size() > 0) {
            List<OrdersGoods> goodList = BeanTool.copyList(ordersGoodsBoList, OrdersGoods.class);
            logger.info("createChangeOrder goodList is {} ", JSON.toJSONString(goodList));
            if (goodList != null && goodList.size() > 0) {
                for (OrdersGoods ordersGoods : goodList) {
                    ordersGoods.setOrderId(orderdb.getId());
                }
            }
            int resGoods = iOrdersGoodsMapper.batchInsertOrderGoods(goodList);
            if (goodList != null && goodList.size() > 0) {
                res = resGoods;
            }
        }
        return res;
    }

    //保存换货订单信息在退货表
    private Integer saveRefundInfo(long initOrderCode, Integer changeOrdID, OrderChangeGoodsApplyReq applyReq, List<OrderChangeGoodsDetailApplyReq> validGoodList) {
        JSONObject refundOrder = new JSONObject();
        refundOrder.put("erpRefundId", 0); // 回调设置
        refundOrder.put("returnAmount", 0);
        refundOrder.put("returnAmountMode", 1);
        refundOrder.put("returnMode", (byte) (applyReq.getExchange_mode() == 20 ? 20 : 10));
        refundOrder.put("returnShippingCost", 0);
        refundOrder.put("sourceOrderCode", applyReq.getOrder_code());
        refundOrder.put("initOrderCode", initOrderCode); // 这个不一定准确，回调更新
        refundOrder.put("changePurchaseId", changeOrdID);
        refundOrder.put("isReturnCoupon", "N");
        refundOrder.put("returnYohoCoin", 0);
        refundOrder.put("remark", "用户申请换货的退货");

        logger.info("saveRefundInfo change refundOrder req   is {}", JSON.toJSONString(refundOrder));
        Integer refundApplyId = iRefundService.setRefund(applyReq.getOrder_code(), applyReq.getUid(), refundOrder);

        logger.info("save order[order_code = {}] refund[id = {}] finish.", initOrderCode, refundApplyId);

        if (refundApplyId < 1) {
            return 0;
        }
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : validGoodList) {
            RefundGoodsList refundGoodsListParameter = new RefundGoodsList();
            refundGoodsListParameter.setReturnRequestId(refundApplyId);
            refundGoodsListParameter.setOrderCode(applyReq.getOrder_code());
            refundGoodsListParameter.setProductSkn(orderChangeGoodsDetailApplyReq.getProduct_skn());
            refundGoodsListParameter.setProductSkc(orderChangeGoodsDetailApplyReq.getNew_product_skc());
            refundGoodsListParameter.setProductSku(orderChangeGoodsDetailApplyReq.getProduct_sku());
            refundGoodsListParameter.setGoodsType(Byte.valueOf(orderChangeGoodsDetailApplyReq.getGoods_type()));
            refundGoodsListParameter.setLastPrice(BigDecimal.valueOf(Double.valueOf(orderChangeGoodsDetailApplyReq.getLast_price())));
            refundGoodsListParameter.setReturnedReason(Byte.valueOf(orderChangeGoodsDetailApplyReq.getExchange_reason()));
            refundGoodsListParameter.setRemark(orderChangeGoodsDetailApplyReq.getRemark());
            refundGoodsListParameter.setStatus((byte) 0);
            int i = refundGoodsListDao.insertRefundGoods(refundGoodsListParameter);
            if (i < 1) {
                return 0;
            }
        }

        logger.info("save order[order_code = {}] refund[id = {}] goods finish.", initOrderCode, refundApplyId);

        return refundApplyId;
    }

    /**
     * 通过商品ID获取商品集合
     *
     * @param saveChangeGoodsApplyRequestNewGoods
     * @return
     */
    private List<GoodsBo> getGoodsByGoodsIds(List<OrderChangeGoodsDetailApplyReq> saveChangeGoodsApplyRequestNewGoods) {
        if (saveChangeGoodsApplyRequestNewGoods.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> newGoodsIds = new ArrayList<>();
        for (OrderChangeGoodsDetailApplyReq goods : saveChangeGoodsApplyRequestNewGoods) {
            newGoodsIds.add(goods.getNew_goods_id());
        }
        BatchBaseRequest<Integer> requestGoodsIds = new BatchBaseRequest<>();
        requestGoodsIds.setParams(newGoodsIds);
        logger.info("query prodcut sys getGoodsByGoodsIds req is {}", JSONObject.toJSONString(requestGoodsIds));
        return Arrays.asList(serviceCaller.call(ServerURL.PRODUCT_BATCH_QUERY_GOODS_INFO, requestGoodsIds, GoodsBo[].class));
    }

    //保存换货订单信息
    private Integer saveChangeApplyInfo(long newOrderCode, long initOrderCode, OrderChangeGoodsApplyReq applyReq, List<OrderChangeGoodsDetailApplyReq> validGoodList) {
        /** 保存换货申请 */
        ChangeGoodsMainInfo changeGoodsMainInfo = new ChangeGoodsMainInfo();
        changeGoodsMainInfo.setUid(applyReq.getUid());
        changeGoodsMainInfo.setErpExchangeId(0);
        changeGoodsMainInfo.setInitOrderCode(initOrderCode);
        changeGoodsMainInfo.setSourceOrderCode(applyReq.getOrder_code());
        changeGoodsMainInfo.setOrderCode(newOrderCode);
        // 这个等 ERP 返回后更新
        changeGoodsMainInfo.setStatus((byte) 0); // 0 申请

        changeGoodsMainInfo.setExchangeMode((byte) (applyReq.getExchange_mode() == 20 ? 20 : 10));
        changeGoodsMainInfo.setExchangeRequestType((byte) 1);
        changeGoodsMainInfo.setRemark(OrderConfig.SAVE_CHANGE_REMARK);
        changeGoodsMainInfo.setReject((byte) 0); // 这个表示有没有被驳回
        changeGoodsMainInfo.setExpressId(0);
        changeGoodsMainInfo.setCreateTime(DateUtil.toSecond(new Date()).intValue());

        logger.info("saveChangeApplyInfo changeGoodsMainInfo is {}", JSONObject.toJSONString(changeGoodsMainInfo));
        iChangeGoodsMainMapper.insert(changeGoodsMainInfo);
        checkID(changeGoodsMainInfo.getId(), ServiceError.ORDER_CHANGE_CREATE_LOCAL_ERROR);

        logger.info("save change goods apply[{}] success!", changeGoodsMainInfo.getId());

        /** 保存换货申请商品数据 */
        List<ChangeGoods> changeGoodsList = new ArrayList<>();
        for (OrderChangeGoodsDetailApplyReq orderChangeGoodsDetailApplyReq : validGoodList) {
            ChangeGoods changeGoods = new ChangeGoods();
            changeGoods.setRemark(orderChangeGoodsDetailApplyReq.getRemark());
            changeGoods.setProductSkc(orderChangeGoodsDetailApplyReq.getNew_product_skc());
            changeGoods.setProductSkn(orderChangeGoodsDetailApplyReq.getProduct_skn());
            changeGoods.setProductSku(orderChangeGoodsDetailApplyReq.getNew_product_sku());
            changeGoods.setSourceProductSkc(orderChangeGoodsDetailApplyReq.getProduct_skc());
            changeGoods.setSourceProductSku(orderChangeGoodsDetailApplyReq.getProduct_sku());
            changeGoods.setGoodsType(Byte.valueOf(orderChangeGoodsDetailApplyReq.getGoods_type()));
            changeGoods.setExchangeReason(Byte.valueOf(orderChangeGoodsDetailApplyReq.getExchange_reason()));
            changeGoods.setChangePurchaseId(changeGoodsMainInfo.getId());
            changeGoods.setCreateTime(DateUtil.toSecond(new Date()).intValue());
            // 这里是要从 ERP 更新的数据
            changeGoods.setInitOrderCode(initOrderCode);
            changeGoods.setSourceOrderCode(applyReq.getOrder_code());
            changeGoods.setOrderCode(newOrderCode);
            changeGoods.setStatus((byte) 0);
            changeGoodsList.add(changeGoods);
        }
        int result = iChangeGoodsMapper.insert(changeGoodsList);
        //插入失败 回滚
        if (result != changeGoodsList.size()) {
            iChangeGoodsMapper.updateDelFlagByChangeGoods(changeGoodsMainInfo.getId());
            iChangeGoodsMainMapper.updateDelFlagByChangeGoods(changeGoodsMainInfo.getId());
            logger.warn("saveChangeApplyInfo iChangeGoodsMapper.insert db error return back ");
            throw new ServiceException(ServiceError.ORDER_CHANGE_CREATE_LOCAL_ERROR);
        }

        logger.info("save change goods apply[{}] goods success!", changeGoodsMainInfo.getId());

        /** 保存换货图片 */

        // 查询刚刚插入的商品，这里应该是为了获取刚刚插入的 ID
        ChangeGoods query = new ChangeGoods();
        query.setChangePurchaseId(changeGoodsMainInfo.getId());
        List<ChangeGoods> changeGoodsRes = iChangeGoodsMapper.selectChangeGoodsList(query);
        if (CollectionUtils.isEmpty(changeGoodsRes)) {
            // 这里不可能为空啊，刚插入进去的
            return changeGoodsMainInfo.getId();
        }


        List<ApplyGoodsImages> applyGoodsImagesList = new ArrayList<>();
        for (OrderChangeGoodsDetailApplyReq changeGoodReq : validGoodList) {
            List<String> images = changeGoodReq.getEvidence_images();
            if (CollectionUtils.isEmpty(images)) continue;
            ChangeGoods insertedGoods = findChangeGoods(changeGoodReq.getProduct_sku(), changeGoodsRes);
            if (insertedGoods == null) continue; // 这个不应该触发
            for (String img : images) {
                ApplyGoodsImages applyGoodsImages = new ApplyGoodsImages();
                applyGoodsImages.setApplyId(changeGoodsMainInfo.getId());
                applyGoodsImages.setApplyGoodsId(insertedGoods.getId());
                applyGoodsImages.setImagePath(img);
                applyGoodsImages.setImageType((byte) 2);
                applyGoodsImagesList.add(applyGoodsImages);
            }
        }

        // 用户没传图片
        if (CollectionUtils.isNotEmpty(applyGoodsImagesList)) {
            iApplyGoodsImagesDao.insertBatch(applyGoodsImagesList);
        }

        logger.info("save change goods apply[{}] error picture success!", changeGoodsMainInfo.getId());

        return changeGoodsMainInfo.getId();
    }

    private ChangeGoods findChangeGoods(int sku, List<ChangeGoods> insertedChangeGoods) {
        for (ChangeGoods changeGoods : insertedChangeGoods) {
            if (sku == changeGoods.getSourceProductSku()) {
                return changeGoods;
            }
        }
        return null;
    }

    /**
     * 取消换货
     */
    public void changeCancel(OrderChangeGoodsReq orderChangeGoodsReq) {
        logger.info("changeCancel begin,param is id:{} ,uid:{}!", orderChangeGoodsReq.getId(),
                orderChangeGoodsReq.getUid());
        // 验证获取换货参数 请求[id, uid]
        validateGetChangeGoodsDetaillRequest(orderChangeGoodsReq);
        // 查询换货商品信息
        ChangeGoodsMainInfo changeGoods = findChangeGoodsById(orderChangeGoodsReq.getId());
        // 查询换货商品详情
        ChangeGoods changeGoodsQuery = new ChangeGoods();
        changeGoodsQuery.setChangePurchaseId(changeGoods.getId());
        List<ChangeGoods> changeGoodsList = iChangeGoodsMapper.selectChangeGoodsList(changeGoodsQuery);
        if (changeGoodsList.isEmpty()) {
            logger.warn("changeCancel fail {}, can not find change_goods_list by change_purchase_id {}.", changeGoods.getOrderCode(), changeGoods.getId());
            throw new ServiceException(ServiceError.ORDER_CHANGE_GOODS_NOT_EXISTS);
        }
        // 这里判断状态,上门换货审核后不能取消,寄回换货审核后可以,两者在填写完物流信息后都不能取消
        validateCancelChangeStatus(changeGoods.getStatus(), changeGoods.getExchangeMode(), orderChangeGoodsReq.getId());
        // 更新换货申请表
        changeGoods.setStatus((byte) 91);
        iChangeGoodsMainMapper.updateByPrimaryKeySelective(changeGoods);
        logger.info("changeCancel update changeGoods success,id: {}", changeGoods.getId());
        // 更新换货商品表
        iChangeGoodsMapper.updateStatusByChangePurchaseId(changeGoods.getId(), (byte) 91);
        logger.info("changeCancel update changeGoodsList success,id: {}", changeGoods.getId());
        // 更新换货的退货
        RefundGoods refundGoods = refundGoodsDao.selectByUidAndChangePurchaseId(orderChangeGoodsReq.getUid(), orderChangeGoodsReq.getId());
        if (refundGoods != null) {
            updateRefundStatus(refundGoods, (byte) 91);
        } else {
            logger.warn("Not find refundGoods by change id {}", orderChangeGoodsReq.getId());
        }
        //更新订单表的换货信息
        Orders changOrder = iOrdersMapper.selectByOrderCode(String.valueOf(changeGoods.getOrderCode()));
        changOrder.setIsCancel("Y");
        changOrder.setCancelType(Orders.CANCEL_TYPE_USER);
        iOrdersMapper.updateByPrimaryKeySelective(changOrder);

        if (changeGoods.getErpExchangeId().intValue() == 0) {
            //表明此时erp还没有收到消息,不能取消.为了防止erp在用户取消之后消费了创建换货消息,
            //需要在后期erp返回换货单消息时,判断当前换货信息是否取消
            logger.info("changeCancel not send mq to erp,because changeId {} erp not handle ,uid {}.", orderChangeGoodsReq.getId(), orderChangeGoodsReq.getUid());
            //@see ErpCreateChangeOrderMessageConsumer
        } else {
            //发送mq消息给erp
            JSONObject obj = new JSONObject();
            obj.put("id", changeGoods.getErpExchangeId());
            obj.put("status", 900);
            obj.put("type", 2);//2表示换货
            orderMqService.sendChangeRefundCancelMessage(obj);
        }
    }

    private void validateCancelChangeStatus(Byte status, Byte exchangeMode, int changeId) {
        switch (exchangeMode) {
            case 10:
                //寄回换货限制
                if (status > 10) {
                    logger.info("changeCancel fail,this exchangeMode{} can not cancel by status {},changeId {}", exchangeMode, status, changeId);
                    throw new ServiceException(ServiceError.ORDER_CHANGE_CANCAEL_STATUS_ILLEGAL);
                }
                break;
            case 20:
                //上门换货限制
                if (status > 0) {
                    logger.info("changeCancel fail,this exchangeMode{} can not cancel by status {},changeId {}", exchangeMode, status, changeId);
                    throw new ServiceException(ServiceError.ORDER_CHANGE_CANCAEL_STATUS_ILLEGAL);
                }
                break;
            default:
                logger.warn("cancelChangeStatus exchangeMode illegal, unknown type exchangeMode{},changeId {}", exchangeMode, changeId);
        }
    }

}

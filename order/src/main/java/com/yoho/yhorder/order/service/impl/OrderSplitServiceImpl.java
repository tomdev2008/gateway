package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.message.YhProducerTemplate;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.constants.OrdersMateKey;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.yhorder.common.model.Coupon;
import com.yoho.yhorder.common.model.ERPOrder;
import com.yoho.yhorder.common.model.ERPOrderGoods;
import com.yoho.yhorder.common.model.ERPPromotion;
import com.yoho.yhorder.common.utils.*;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.domain.DeliveryAddress;
import com.yoho.yhorder.dal.model.OrderCodeQueue;
import com.yoho.yhorder.dal.model.OrderPromotionInfo;
import com.yoho.yhorder.dal.model.OrdersMeta;
import com.yoho.yhorder.order.model.OrderAmountDetail;
import com.yoho.yhorder.order.model.OrderWrapper;
import com.yoho.yhorder.order.service.IOrderSplitService;
import com.yoho.yhorder.order.service.IOrdersDeliveryAddressRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.yoho.yhorder.common.utils.YHPreconditions.checkArgument;
import static com.yoho.yhorder.common.utils.YHPreconditions.checkID;

/**
 * Created by wujiexiang on 16/4/24.
 */
@Service
public class OrderSplitServiceImpl implements IOrderSplitService {

    private Logger logger = LoggerFactory.getLogger("orderSplitLog");

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    private OrderPromotionInfoMapper orderPromotionInfoMapper;

    @Autowired
    private IOrderCodeQueueDAO orderCodeQueueDAO;

    @Autowired
    private IOrderCodeListDAO orderCodeListDAO;

    @Resource
    private YhProducerTemplate producerTemplate;

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Autowired
    private IOrdersDeliveryAddressRepository ordersDeliveryAddressService;

    /**
     * 拆分订单,订单必须支付了
     *
     * @param orderCode
     */
    @Override
    @Database(ForceMaster = true)
    public int splitOrder(int uid, long orderCode) {
        logger.info("enter split order service,uid is {},order code is {}", uid, orderCode);

        //1.校验参数
        if (isInvalidOrderCode(orderCode)) {
            logger.info("order code {} is invalid", orderCode);
            return 0;
        }

        Orders parentOrder = ordersMapper.selectByCodeAndUid(String.valueOf(orderCode), String.valueOf(uid));
        if (parentOrder == null) {
            logger.warn("not find order by order code {}", orderCode);
            return 0;
        }
        logger.info("find order result: order code {},id {},status {},amount {},paymentStatus {},payment {}," +
                        "is cancel {},orderStatus {},shipping cost {},is multi package {},is jit already split {}",
                parentOrder.getOrderCode(),
                parentOrder.getId(),
                parentOrder.getStatus(),
                parentOrder.getAmount(),
                parentOrder.getPaymentStatus(),
                parentOrder.getPayment(),
                parentOrder.getIsCancel(),
                parentOrder.getOrdersStatus(),
                parentOrder.getShippingCost(),
                parentOrder.getIsMultiPackage(),
                parentOrder.getIsJitAlreadySplit());

        /**
         * 未支付或者预售或者非jit的不需要拆单
         */

        if (isAlreadySplit(parentOrder)) {
            logger.warn("order is already split,order code is {}", parentOrder.getOrderCode());
            return 0;
        }

        //2.判断是否已经支付
        if (isNotPaidOrder(parentOrder)) {
            logger.warn("order is not paid,order code is {}", parentOrder.getOrderCode());
            return 0;
        }

        //3.没有多包就不需要拆分
        if (isNotMultiPackage(parentOrder)) {
            logger.warn("order is not multi package,order code is {}", parentOrder.getOrderCode());
            return 0;
        }
        //4.拆分订单
        int subOrderNum = doSplitOrder(parentOrder);

        logger.info("exit split order service,uid is {},order code is {},sub order num is {}", uid, orderCode, subOrderNum);

        return subOrderNum;

    }

    private boolean isInvalidOrderCode(long orderCode) {
        return orderCode < 1;
    }

    private boolean isAlreadySplit(Orders order) {
        return "Y".equals(order.getIsJitAlreadySplit());
    }

    private boolean isNotPaidOrder(Orders order) {
        return !"Y".equals(order.getPaymentStatus());
    }

    private boolean isNotMultiPackage(Orders order) {
        return !"Y".equals(order.getIsMultiPackage());
    }


    public void checkSupplierAndSupplierGoods(long parentOrderCode, Map<Integer, List<OrdersGoods>> supplierGoodsMap) throws ServiceException {
        if (MapUtils.isEmpty(supplierGoodsMap)) {
            logger.warn("not find any supplier from order goods,parent order code is {}", parentOrderCode);
            throw new ServiceException(ServiceError.ORDER_SPLIT_ORDER_FAIL);
        }

        Set<Integer> suppliers = supplierGoodsMap.keySet();

        if (!OrderPackageUtils.canSplitSubOrder(suppliers.size())) {
            logger.warn("not split order,suppliers are {},parent order code is {}", suppliers, parentOrderCode);
            throw new ServiceException(ServiceError.ORDER_SPLIT_ORDER_FAIL);
        }

        for (Integer supplier : suppliers) {
            List<OrdersGoods> goodsList = supplierGoodsMap.get(supplier);
            if (CollectionUtils.isEmpty(goodsList)) {
                logger.warn("not find any goods,supplier is {},parent order code is {}", supplier, parentOrderCode);
                throw new ServiceException(ServiceError.ORDER_SPLIT_ORDER_FAIL);
            }
        }
    }

    private Map<Integer, List<OrdersGoods>> groupGoodsBySupplier(List<OrdersGoods> goodsList) {
        Map<Integer, List<OrdersGoods>> supplierGoodsMap = new TreeMap<>();
        if (CollectionUtils.isEmpty(goodsList)) {
            return supplierGoodsMap;
        }

        List<OrdersGoods> tmpList = null;
        for (OrdersGoods goods : goodsList) {
            Integer supplierId = 0;
            if ("Y".equals(goods.getIsJit())) {
                supplierId = goods.getSupplierId();
            }

            if (!supplierGoodsMap.containsKey(supplierId)) {
                tmpList = new ArrayList<>();
                supplierGoodsMap.put(supplierId, tmpList);
            }

            tmpList = supplierGoodsMap.get(supplierId);

            tmpList.add(goods);

        }

        return supplierGoodsMap;
    }

    private int doSplitOrder(Orders parentOrder) {

        List<OrdersGoods> goodsList = ordersGoodsMapper.selectOrderGoodsByOrder(parentOrder);

        if (CollectionUtils.isEmpty(goodsList)) {
            logger.warn("not find any goods by order,order code is {}", parentOrder.getOrderCode());
            return 0;
        }

        Map<Integer, List<OrdersGoods>> supplierGoodsMap = groupGoodsBySupplier(goodsList);

        //校验供应商及其商品列表不为空
        checkSupplierAndSupplierGoods(parentOrder.getOrderCode(), supplierGoodsMap);

        logger.info("find supplier for order,order code is {},supplier are {}", parentOrder.getOrderCode(), supplierGoodsMap.keySet());

        return splitOrderBySuppliers(parentOrder, supplierGoodsMap);
    }

    /**
     * 根据供应商拆分订单
     *
     * @param parentOrder
     * @param supplierGoodsMap
     * @return
     */
    private int splitOrderBySuppliers(Orders parentOrder, Map<Integer, List<OrdersGoods>> supplierGoodsMap) {

        List<OrderWrapper> subOrderList = splitAllSubOrder(parentOrder, supplierGoodsMap);
        //子订单入库
        addOrderAndOrderGoodsToDB(subOrderList);


        // 保存订单收货地址信息
        saveOrdersDeliveryAddresses(parentOrder, subOrderList);

        //标记父订单已拆分
        markParentOrderAlreadySplit(parentOrder);
        //发送给erp
        createERPOrderAndSendMQ(subOrderList);

        return subOrderList.size();
    }

    /**
     * 保存订单收货地址信息
     *
     * @param parentOrder
     * @param subOrderList
     */
    private void saveOrdersDeliveryAddresses(Orders parentOrder, List<OrderWrapper> subOrderList) {
        DeliveryAddress deliveryAddress = ordersDeliveryAddressService.select(parentOrder);
        List<Orders> orderses = new ArrayList<>(subOrderList.size());
        for (OrderWrapper subOrder : subOrderList) {
            orderses.add(subOrder.getOrder());
        }
        ordersDeliveryAddressService.insert(orderses, deliveryAddress);
    }

    private InvoiceBo queryByOrderId(int orderId) {
        InvoiceBo invoiceBo = null;
        if (orderId <= 0) {
            logger.error("in splitAllSubOrder.queryByOrderId param orderId {}", orderId);
            return invoiceBo;
        }
        OrdersMeta ordersMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orderId, OrdersMateKey.ELECTRONIC_INVOICE);
        if (ordersMeta != null && StringUtils.isNotBlank(ordersMeta.getMetaValue())) {
            invoiceBo = JSONObject.parseObject(ordersMeta.getMetaValue(), InvoiceBo.class);
        }
        return invoiceBo;
    }

    private List<OrderWrapper> splitAllSubOrder(Orders parentOrder, Map<Integer, List<OrdersGoods>> supplierGoodsMap) {
        Set<Integer> supplierIds = supplierGoodsMap.keySet();
        List<OrderWrapper> subOrderList = new ArrayList<>();
        InvoiceBo invoiceBo = queryByOrderId(parentOrder.getId());
        double yohoCoinNum4Goods = 0;
        for (Integer supplierId : supplierIds) {
            List<OrdersGoods> goodsList = supplierGoodsMap.get(supplierId);
            OrderWrapper subOrder = splitOrderForOneSupplier(parentOrder, supplierId, goodsList, invoiceBo);
            subOrderList.add(subOrder);
            yohoCoinNum4Goods = YHMath.add(yohoCoinNum4Goods, subOrder.getOrderAmoutDetail().getYohoCoinCutNum());
        }

        //已经在splitOrderForOneSupplier中完成累加
        //   splitRedenvelopes(subOrderList, parentOrder);

        double yohoCoinNum4Shipping = 0;
        if (yohoCoinNum4Goods < parentOrder.getYohoCoinNum()) {
            yohoCoinNum4Shipping = YHMath.sub(parentOrder.getYohoCoinNum(), yohoCoinNum4Goods);
        }
        //为每一个订单计算运费
        double yohoCoinAmount4Shipping = 0.0;
        try {
            yohoCoinAmount4Shipping = YHMath.div(yohoCoinNum4Shipping, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
        } catch (IllegalAccessException ex) {
            logger.warn("call YHMath.div error,m is {},d is {}", yohoCoinNum4Shipping, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO, ex);
        }

        splitOrderShippingCost(subOrderList, parentOrder.getShippingCost().doubleValue(), yohoCoinAmount4Shipping);

        //已经在splitOrderForOneSupplier中完成累加
        //分摊yoho币,按照每个子订单的金额
        //    splitYohoCoin(subOrderList, parentOrder);

        //保证子订单的支付金额等于父订单
//        adjustAllSubOrderAmount(subOrderList, parentOrder);

        return subOrderList;
    }

    /**
     * 更新支付时间
     *
     * @param orders
     */
    private void updatePayFinishedTime(Orders orders) {
        int time = (int) (System.currentTimeMillis() / 1000);
        OrdersMeta a = ordersMetaDAO.selectByOrdersIdAndMetaKey(orders.getId(), OrdersMeta.TIMES);
        if (a == null) {
            OrdersMeta record = new OrdersMeta();
            record.setUid(orders.getUid());
            record.setOrderCode(orders.getOrderCode());
            record.setOrdersId(orders.getId());
            record.setMetaKey(OrdersMeta.TIMES);
            JSONObject js = new JSONObject();
            js.put("pay_finish_time", time);
            record.setMetaValue(js.toString());
            ordersMetaDAO.insert(record);
        }
    }

    @Deprecated
    public void splitRedenvelopes(List<OrderWrapper> orderList, Orders parentOrder) {

        logger.info("split red envelope,parent order code is {}", parentOrder.getOrderCode());
        double parentRedEnvelopes = 0;
        OrderPromotionInfo orderPromotionInfo = orderPromotionInfoMapper.selectByOrderCode(parentOrder.getOrderCode());
        if (null != orderPromotionInfo && orderPromotionInfo.getOrderPromotion() != null) {
            JSONObject jobj = JSONObject.parseObject(orderPromotionInfo.getOrderPromotion());
            parentRedEnvelopes = jobj.getDoubleValue("use_red_envelopes");
        }

        logger.info("parent order use red envelope {},parent order code is {}", parentRedEnvelopes, parentOrder.getOrderCode());
        if (parentRedEnvelopes <= 0) {
            logger.info("parent order do not use red envelope,order code is {}", parentOrder.getOrderCode());
            return;
        }

        double totalLastOrderAmount = 0;
        for (OrderWrapper orderWrapper : orderList) {
            totalLastOrderAmount = YHMath.add(totalLastOrderAmount, orderWrapper.getOrderAmoutDetail().getLastOrderAmount());
        }

        double avg = 0;
        if (totalLastOrderAmount > 0) {
            try {
                avg = MathUtils.div(parentRedEnvelopes, totalLastOrderAmount);
            } catch (Exception ex) {
                logger.warn("call YHMath.div error,m is {},d is {}", parentRedEnvelopes, totalLastOrderAmount, ex);
            }
        }

        double remainRedEnvelopes = parentRedEnvelopes;
        for (int i = orderList.size() - 1; i >= 0; i--) {
            OrderWrapper orderWrapper = orderList.get(i);
            OrderAmountDetail orderAmountDetail = orderWrapper.getOrderAmoutDetail();
            double ratioRedenvelope = MathUtils.down(YHMath.mul(orderAmountDetail.getLastOrderAmount(), avg));
            double realRedenvelope = ratioRedenvelope;
            if (i == 0) {
                realRedenvelope = remainRedEnvelopes;
            }
            remainRedEnvelopes = YHMath.sub(remainRedEnvelopes, realRedenvelope);
            if (remainRedEnvelopes < 0) {
                remainRedEnvelopes = 0;
            }

            orderAmountDetail.setRedenvelopesCutAmount(realRedenvelope);
            orderAmountDetail.setLastOrderAmount(YHMath.sub(orderAmountDetail.getLastOrderAmount(), realRedenvelope));
            orderWrapper.getOrder().setAmount(BigDecimal.valueOf(orderAmountDetail.getLastOrderAmount()));
            logger.info("after calc order red envelope,order code is {},remain red envelope is {},order amount detail is {}", orderWrapper.getOrder().getOrderCode(), remainRedEnvelopes, orderAmountDetail);
        }

    }


    /**
     * 1. 运费分摊,
     * 2. 用分摊到运费的有货币逐个减免
     *
     * @param orderList
     * @param parentOrderShippingCost
     * @param yohoCoin4Shipping
     */
    private void splitOrderShippingCost(List<OrderWrapper> orderList, double parentOrderShippingCost, double yohoCoin4Shipping) {
        logger.info("parent order shipping cost is {}   yohoCoin for shipping is {} ", parentOrderShippingCost, yohoCoin4Shipping);
        int orderNum = orderList.size();
        double[] packageShippingCostArray = OrderPackageUtils.caclPackageShippingCost(parentOrderShippingCost, orderNum);

        double yohoCoinRemain = yohoCoin4Shipping;
        double yohoCoinPer = 0;
        for (int i = 0; i < orderNum; i++) {
            OrderWrapper orderWrapper = orderList.get(i);
            OrderAmountDetail orderAmountDetail = orderWrapper.getOrderAmoutDetail();
            orderAmountDetail.setShippingCost(packageShippingCostArray[i]);
            orderAmountDetail.setShoppingOrigCost(parentOrderShippingCost);
            orderAmountDetail.setLastOrderAmount(orderAmountDetail.getLastOrderAmount() + orderAmountDetail.getShippingCost());

            if (yohoCoinRemain > 0) {
                yohoCoinPer = (yohoCoinRemain > packageShippingCostArray[i]) ? packageShippingCostArray[i] : yohoCoinRemain;
                yohoCoinRemain = YHMath.sub(yohoCoinRemain, yohoCoinPer);
                //订单总额减去有货币优惠
                orderAmountDetail.setLastOrderAmount(YHMath.sub(orderAmountDetail.getLastOrderAmount(), yohoCoinPer));
                //设置有货币抵运费
                orderAmountDetail.setYohoCoinShippingCost(yohoCoinPer);
                int oldYohoCoinNum = orderWrapper.getOrder().getYohoCoinNum();
                //添加订单使用有货币数量为所有的
                orderWrapper.getOrder().setYohoCoinNum(oldYohoCoinNum + MathUtils.toInt(yohoCoinPer, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO));
            }
            orderWrapper.getOrder().setShippingCost(BigDecimal.valueOf(orderAmountDetail.getShippingCost()));
            orderWrapper.getOrder().setAmount(BigDecimal.valueOf(orderAmountDetail.getLastOrderAmount()));

            logger.info("after calc order shipping cost,order code is {},order amount detail is {} ", orderList.get(i).getOrderCode(), orderAmountDetail);
        }
    }

    @Deprecated
    public void splitYohoCoin(List<OrderWrapper> orderList, Orders parentOrder) {
        Integer parentYohoCoinNum = parentOrder.getYohoCoinNum();
        logger.info("parent order code {},parent yoho coin num {}", parentOrder.getOrderCode(), parentYohoCoinNum);
        if (parentYohoCoinNum == null || parentYohoCoinNum <= 0) {
            return;
        }

        double parentYohoCoinAmount = 0;
        try {
            parentYohoCoinAmount = MathUtils.div(parentYohoCoinNum, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
        } catch (Exception ex) {
            logger.warn("call YHMath.div error,m is {},d is {}", parentYohoCoinNum, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO, ex);
        }


        double totalLastOrderAmount = 0;
        for (OrderWrapper orderWrapper : orderList) {
            totalLastOrderAmount = YHMath.add(totalLastOrderAmount, orderWrapper.getOrderAmoutDetail().getLastOrderAmount());
        }

        double avg = 0;
        if (totalLastOrderAmount > 0) {
            try {
                avg = MathUtils.div(parentYohoCoinAmount, totalLastOrderAmount);
            } catch (Exception ex) {
                logger.warn("call YHMath.div error,m is {},d is {}", parentYohoCoinAmount, totalLastOrderAmount, ex);
            }
        }

        double remainYohoCoinAmount = parentYohoCoinAmount;
        for (int i = orderList.size() - 1; i >= 0; i--) {
            OrderWrapper orderWrapper = orderList.get(i);
            OrderAmountDetail orderAmountDetail = orderWrapper.getOrderAmoutDetail();
            double ratioYohoCoinAmount = YHMath.mul(orderAmountDetail.getLastOrderAmount(), avg);

            double realYohoCoinAmount = ratioYohoCoinAmount;
            if (i == 0) {
                realYohoCoinAmount = remainYohoCoinAmount;
            }
            remainYohoCoinAmount = YHMath.sub(remainYohoCoinAmount, realYohoCoinAmount);
            if (remainYohoCoinAmount < 0) {
                remainYohoCoinAmount = 0;
            }


            int usedYohoCoinNum = MathUtils.toInt(realYohoCoinAmount, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
            orderAmountDetail.setYohCoinCutAmount(realYohoCoinAmount);
            orderAmountDetail.setYohoCoinCutNum(usedYohoCoinNum);
            orderAmountDetail.setLastOrderAmount(YHMath.sub(orderAmountDetail.getLastOrderAmount(), realYohoCoinAmount));
            orderWrapper.getOrder().setAmount(BigDecimal.valueOf(orderAmountDetail.getLastOrderAmount()));
            orderWrapper.getOrder().setYohoCoinNum(usedYohoCoinNum);

            logger.info("after calc order yoho coin,order code is {},remain yoho coin amount is {},order amount detail is {}", orderWrapper.getOrder().getOrderCode(), remainYohoCoinAmount, orderAmountDetail);
        }
    }

    public void adjustAllSubOrderAmount(List<OrderWrapper> orderList, Orders parentOrder) {
        double totalLastOrderAmount = parentOrder.getAmount() == null ? 0 : parentOrder.getAmount().doubleValue();
        logger.info("try to adjust all sub order amount,parent order code is {},total last order amount is {}", parentOrder.getOrderCode(), totalLastOrderAmount);
        double remainLastOrderAmount = totalLastOrderAmount;
        for (OrderWrapper orderWrapper : orderList) {
            double realAdjustAmount = 0;
            double beforeLastOrderAmount = orderWrapper.getOrderAmoutDetail().getLastOrderAmount();
            if (beforeLastOrderAmount > remainLastOrderAmount) {
                //子订单的支付金额大于父订单的支付金额,需要做调整
                double expectedAdjustAmount = YHMath.sub(beforeLastOrderAmount, remainLastOrderAmount);
                //调整
                tryAdjustSubOrderAmount(orderWrapper, expectedAdjustAmount);

                double afterLastOrderAmount = orderWrapper.getOrderAmoutDetail().getLastOrderAmount();

                if (afterLastOrderAmount >= 0) {
                    realAdjustAmount = YHMath.sub(beforeLastOrderAmount, afterLastOrderAmount);
                }
                remainLastOrderAmount = YHMath.sub(remainLastOrderAmount, realAdjustAmount);
            } else if (beforeLastOrderAmount <= remainLastOrderAmount) {
                remainLastOrderAmount = YHMath.sub(remainLastOrderAmount, beforeLastOrderAmount);
            }

            if (remainLastOrderAmount < 0) {
                remainLastOrderAmount = 0;
            }

            if (orderWrapper.getOrderAmoutDetail().getLastOrderAmount() < 0) {
                orderWrapper.getOrderAmoutDetail().setLastOrderAmount(0);
            }
            orderWrapper.getOrder().setAmount(BigDecimal.valueOf(orderWrapper.getOrderAmoutDetail().getLastOrderAmount()));

            logger.info("after adjust order amount,order code is {},real adjust amount is {},remain last order amount {},order amount detail is {}",
                    orderWrapper.getOrder().getOrderCode(), realAdjustAmount, remainLastOrderAmount, orderWrapper.getOrderAmoutDetail());
        }
    }

    private void tryAdjustSubOrderAmount(OrderWrapper orderWrapper, double adjustAmount) {
        //2.调整到订单金额
        OrderAmountDetail orderAmountDetail = orderWrapper.getOrderAmoutDetail();
        double adjustOrderAmount = 0;
        //经过yoho币调整后,还剩多少需要调整
        if (adjustAmount > 0) {
            double orderAmount = orderAmountDetail.getOrderAmount();
            if (orderAmount > adjustAmount) {
                adjustOrderAmount = adjustAmount;
            } else {
                adjustOrderAmount = orderAmount;
            }
            //直接调整到订单金额上
            orderAmountDetail.setOrderAmount(YHMath.sub(orderAmount, adjustOrderAmount));
        }
        orderAmountDetail.setLastOrderAmount(YHMath.sub(orderAmountDetail.getLastOrderAmount(), adjustOrderAmount));
        logger.info("adjust sub order,order code is {},adjustAmount is {},adjustOrderAmount is {},final order amount detail is {}",
                orderWrapper.getOrderCode(), adjustAmount, adjustOrderAmount, orderAmountDetail);

    }

    private OrderWrapper splitOrderForOneSupplier(Orders parentOrder, Integer supplierId,
                                                  List<OrdersGoods> goodsList, InvoiceBo invoiceBo) {
        logger.info("plan to create one sub order for supplier {},parent order code is {}", supplierId, parentOrder.getOrderCode());
        Orders subOrder = createSubOrder(parentOrder);
        subOrder.setIsJitBySupplierId(supplierId);
        subOrder.setIsMultiPackage("N");

        //生成子订单金额
        OrderAmountDetail orderAmountDetail = splitOrderAmount(goodsList);

        subOrder.setDeliverYohoCoin(orderAmountDetail.getGetYohoCoinNum());
        subOrder.setYohoCoinNum(orderAmountDetail.getYohoCoinCutNum());

        subOrder.setAmount(BigDecimal.valueOf(orderAmountDetail.getLastOrderAmount()));
        //电子发票信息，modified by chenchao
        if (invoiceBo != null) {
            subOrder.setInvoiceTypes(invoiceBo.getType());
            subOrder.setInvoice(invoiceBo);
        }


        logger.info("supplier is {},###sub order info is {},###goods list is {},###order amount detail is {}\n",
                supplierId, subOrder, goodsList, orderAmountDetail);
        OrderWrapper order = new OrderWrapper();
        order.setOrder(subOrder);
        order.setOrderCode(subOrder.getOrderCode());
        order.setGoodsList(goodsList);
        order.setOrderAmoutDetail(orderAmountDetail);

        return order;
    }

    private Orders createSubOrder(Orders parentOrder) {
        //生成子订单的订单号
        Long orderCode = getOrderCode(parentOrder.getUid());
        //复制父订单的属性
        Orders subOrder = BeanTool.copyObject(parentOrder, Orders.class);
        if (subOrder == null) {
            logger.warn("split parent order error,parent order is {}", parentOrder);
            throw new ServiceException(ServiceError.ORDER_SPLIT_ORDER_FAIL);
        }
        subOrder.setOrderCode(orderCode);
        subOrder.setUid(parentOrder.getUid());
        subOrder.setParentOrderCode(parentOrder.getOrderCode());
        return subOrder;
    }

    private OrderAmountDetail splitOrderAmount(List<OrdersGoods> goodsList) {
        //商品金额
        double orderAmount = 0;
        //vip减免金额
        double vipCutAmount = 0;
        //促销减免金额
        double promotionCuAmount = 0;
        //优惠券减免金额
        double couponsCutAmount = 0;
        //优惠码减免金额
        double promotioncodeCutAmount = 0;

        //赠送的yoho币
        int getYohoCoinNum = 0;
        //使用的yoho币
        int yohoCoinCutNum = 0;

        double redCutAmount = 0;

        for (OrdersGoods goods : goodsList) {
            double buyNumber = Double.valueOf(goods.getNum());
            orderAmount = YHMath.add(orderAmount, YHMath.mul(goods.getSalesPrice().doubleValue(), buyNumber));

            vipCutAmount = YHMath.add(vipCutAmount, YHMath.mul(goods.getVipCutAmount().doubleValue(), buyNumber));

            promotionCuAmount = YHMath.add(promotionCuAmount, YHMath.mul(goods.getPromotionCutAmount().doubleValue(), buyNumber));

            couponsCutAmount = YHMath.add(couponsCutAmount, YHMath.mul(goods.getCouponsCutAmount().doubleValue(), buyNumber));

            promotioncodeCutAmount = YHMath.add(promotioncodeCutAmount, YHMath.mul(goods.getPromoCodeCutAmount().doubleValue(), buyNumber));

            getYohoCoinNum += goods.getGetYohoCoin() * goods.getNum();
            //使用的yoho币
            yohoCoinCutNum += goods.getYohoCoinCutNum() * goods.getNum();

            redCutAmount = YHMath.add(redCutAmount, YHMath.mul(goods.getRedenvelopeCutAmount().doubleValue(), buyNumber));
        }

        double yohoCoinCutDownAmount = 0.0;
        try {
            yohoCoinCutDownAmount = YHMath.div(yohoCoinCutNum, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
        } catch (IllegalAccessException ex) {
            logger.warn("call YHMath.div error,m is {},d is {}", yohoCoinCutNum, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO, ex);
        }

        double lastOrderAmount = YHMath.sub(orderAmount, vipCutAmount + promotionCuAmount + couponsCutAmount + promotioncodeCutAmount
                + redCutAmount + yohoCoinCutDownAmount);
        OrderAmountDetail amountDetail = new OrderAmountDetail();
        amountDetail.setOrderAmount(orderAmount);
        //设置最终订单金额
        amountDetail.setLastOrderAmount(lastOrderAmount);
        amountDetail.setVipCutAmount(vipCutAmount);
        amountDetail.setPromotionCutAmount(promotionCuAmount);
        amountDetail.setCouponsCutAmount(couponsCutAmount);
        amountDetail.setPromotioncodeCutAmount(promotioncodeCutAmount);

        amountDetail.setGetYohoCoinNum(getYohoCoinNum);

        amountDetail.setYohCoinCutAmount(yohoCoinCutDownAmount);
        amountDetail.setYohoCoinCutNum(yohoCoinCutNum);
        amountDetail.setRedenvelopesCutAmount(redCutAmount);
        return amountDetail;
    }

    private void addOrderAndOrderGoodsToDB(List<OrderWrapper> orderList) {
        //插入数据库中
        List<OrdersGoods> goodsList = new ArrayList<>();
        for (OrderWrapper order : orderList) {
            Orders orders = order.getOrder();
            addOrderToDB(orders);
            setupOrderAndOrderGoodsRelationship(orders, order.getGoodsList());
            //更新子订单支付时间
            updatePayFinishedTime(orders);
            goodsList.addAll(order.getGoodsList());
            //电子发票
            if (orders.getInvoice() != null) {
                orders.getInvoice().setOrderId(orders.getId());
                OrdersMeta ordersMeta = new OrdersMeta();
                ordersMeta.setUid(orders.getUid());
                ordersMeta.setOrderCode(orders.getOrderCode());
                ordersMeta.setOrdersId(orders.getId());
                ordersMeta.setMetaKey(OrdersMateKey.ELECTRONIC_INVOICE);
                ordersMeta.setMetaValue(JSONObject.toJSONString(orders.getInvoice()));
                ordersMetaDAO.insert(ordersMeta);
            }
        }

        addPromotionInfosToDB(orderList);

        addOrderGoodsToDB(goodsList);

    }


    private void addOrderToDB(Orders orders) {
        try {
            ordersMapper.insertShoppingOrder(orders);
        } catch (Exception ex) {
            logger.error("exception happened when add order to database, uid {},  order code {},ordersDo {}",
                    orders.getUid(), orders.getOrderCode(), orders, ex);
            throw ex;
        }

        logger.info("add order info to database success, order code {}, order db id {}, ordersDo {}",
                orders.getOrderCode(), orders.getId(), orders);
    }

    private void addPromotionInfosToDB(List<OrderWrapper> orderWrappers) {
        List<OrderPromotionInfo> list = new ArrayList<>();
        for (OrderWrapper orderWrapper : orderWrappers) {
            OrderPromotionInfo orderPromotionInfo = new OrderPromotionInfo();
            orderPromotionInfo.setOrderCode(orderWrapper.getOrderCode());
            orderPromotionInfo.setUid(orderWrapper.getOrder().getUid());
            orderPromotionInfo.setOrderPromotion(JSON.toJSONString(orderWrapper.getOrderAmoutDetail()));
            list.add(orderPromotionInfo);
        }

        try {
            orderPromotionInfoMapper.insertByBatch(list);
        } catch (Exception ex) {
            logger.error("exception happened when add order promotion info to database,order promotion info\n {}", list, ex);
            throw ex;
        }

        logger.info("add order promotion info to database success,order promotion info {}\n",
                list);

    }

    private void setupOrderAndOrderGoodsRelationship(Orders orders, List<OrdersGoods> goodsList) {
        for (OrdersGoods goods : goodsList) {
            goods.setOrderId(orders.getId());
            goods.setUid(orders.getUid());
            goods.setOrderCode(orders.getOrderCode());
        }
    }

    private void addOrderGoodsToDB(List<OrdersGoods> goodsList) {
        try {
            ordersGoodsMapper.batchInsertOrderGoods(goodsList);
        } catch (Exception ex) {
            logger.error("exception happened when add order goods to database,order goods {}", goodsList, ex);
            throw ex;
        }
        logger.info("add order goods to database success,order goods {}\n",
                goodsList);
    }


    /**
     * 根据uid获取一个订单号,获取5次失败后不再获取
     *
     * @param uid
     * @return
     */
    private Long getOrderCode(Integer uid) {
        checkID(uid, ServiceError.SHOPPING_UID_IS_NULL);
        Long orderCode = null;
        OrderCodeQueue orderCodeQueue = new OrderCodeQueue();
        orderCodeQueue.setUid(uid);
        for (int i = 0; i < 5; i++) {
            orderCodeQueueDAO.insertUid(orderCodeQueue);
            if (orderCodeQueue.getId() < 1) {
                continue;
            }
            orderCode = orderCodeListDAO.selectOrderCodeById(orderCodeQueue.getId());
            if (orderCode > 1) {
                break;
            }
        }
        //获取订单号失败！
        checkArgument(orderCode > 0, ServiceError.SHOPPING_CART_GET_ORDERCODE_ERROR);
        int twoDigitsYear = LocalDate.now().getYear() - 2000;
        return orderCode < 10000000 ? new Long(twoDigitsYear + "0" + orderCode) : new Long(twoDigitsYear + "" + orderCode);
    }


    private int markParentOrderAlreadySplit(Orders parentOrder) {
        int recordNum = ordersMapper.updateOrderAlreadySplit(parentOrder.getOrderCode(), parentOrder.getUid());
        logger.info("mark parent order already split,uid is {},parent order code is {},update db record num is {}", parentOrder.getUid(), parentOrder.getOrderCode(), recordNum);
        return recordNum;
    }

    private void createERPOrderAndSendMQ(List<OrderWrapper> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }

        for (OrderWrapper order : orderList) {
            ERPOrder erpOrder = createErpOrderAndOrderGoodsList(order);
            sendMQ(erpOrder);
        }
    }

    private void sendMQ(ERPOrder erpOrder) {
        //手机号码
        String mobile = erpOrder.getMobile();
        erpOrder.hideMobile(mobile);
        JSONArray array = new JSONArray();
        array.add(erpOrder);
        logger.info("send create order message to mq, uid {}, order code {}, \nmessage is \n{}\n ",
                erpOrder.getUid(), erpOrder.getOrder_code(), array);

        try {
            erpOrder.unHideMobile(mobile);

            Map<String, Object> map = new HashMap<>();
            map.put("order_code", erpOrder.getOrder_code());
            map.put("uid", erpOrder.getUid());
            producerTemplate.send(Constants.ORDER_SUBMIT_TOPIC, array, map);
            logger.info("send create order message to mq success, topic {},uid {}, order code {}",
                    Constants.ORDER_SUBMIT_TOPIC,
                    erpOrder.getUid(), erpOrder.getOrder_code());
        } catch (Exception ex) {
            erpOrder.hideMobile(mobile);
            logger.warn("send create order message to mq fail!,topic {},uid {},message is: \n{}\n",
                    Constants.ORDER_SUBMIT_TOPIC,
                    erpOrder.getUid(), array, ex);
        }
    }

    private ERPOrder createErpOrderAndOrderGoodsList(OrderWrapper orderWrapper) {

        //添加erp订单主要字段
        ERPOrder erpOrder = createERPOrder(orderWrapper);
        //添加促销信息
        erpOrder.setFit_promotions(addShippingCostPromotions(orderWrapper.getOrderCode(), orderWrapper.getOrderAmoutDetail()));
        //添加商品列表
        erpOrder.setGoods_list(createERPOrderGoodsList(orderWrapper));
        return erpOrder;
    }

    private ERPOrder createERPOrder(OrderWrapper orderWrapper) {

        ERPOrder erpOrder = new ERPOrder();

        Orders order = orderWrapper.getOrder();

        OrderAmountDetail orderAmountDetail = orderWrapper.getOrderAmoutDetail();

        erpOrder.setRedenvelopesnum(orderAmountDetail.getRedenvelopesCutAmount());
        erpOrder.setOrder_code(order.getOrderCode());
        erpOrder.setUid(order.getUid());
        erpOrder.setUser_level(order.getUserLevel());
        erpOrder.setOrder_amount(orderAmountDetail.getOrderAmount());
        erpOrder.setLast_order_amount(orderAmountDetail.getLastOrderAmount());
        erpOrder.setAmount(orderAmountDetail.getLastOrderAmount());
        erpOrder.setOrder_type(Integer.valueOf(order.getOrderType()));
        erpOrder.setNeed_invoice(order.getIsInvoice());
        erpOrder.setInvoice_type(Integer.valueOf(order.getInvoicesType()));
        //发票类型 /**纸质 1 ，电子 2    */
        erpOrder.setInvoice_types(order.getInvoiceTypes());
        erpOrder.setInvoice_payable(order.getInvoicesPayable());
        //添加参数:有货币for运费
        erpOrder.setYohocoin_shipping_cost(YHMath.mul(orderAmountDetail.getYohoCoinShippingCost(), Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO));
        //yoho币被稀释成货币，提交给erp需要转换为yoho币,需要减去抵运费，只有抵商品
        erpOrder.setUse_yoho_coin(Double.valueOf(order.getYohoCoinNum()));
        erpOrder.setYoho_coin_num(erpOrder.getUse_yoho_coin());
        Coupon coupon = new Coupon();
        coupon.setCoupon_id(0);
        coupon.setCoupon_code("");
        coupon.setCoupon_amount(0.00);
        coupon.setCoupon_title("");
        erpOrder.setOrders_coupons(coupon);
        erpOrder.setCoupon_id(0);
        erpOrder.setCoupon_code("");
        erpOrder.setCoupon_amount(0.00);
        erpOrder.setCoupon_title("");
        erpOrder.setPayment_type(Integer.valueOf(order.getPaymentType()));
        erpOrder.setShipping_cost(order.getShippingCost().doubleValue());
        erpOrder.setReceipt_time(Integer.valueOf(order.getReceiptTime()));
        erpOrder.setReceipt_time_type(Integer.valueOf(order.getReceiptTime()));

        //erpOrder.setReceipt_time_type(order.getr);//送货时间
        //erpOrder.setReceipt_time_type(order.getReceiptTimeType());
        erpOrder.setOrder_referer("");
        erpOrder.setRemark(order.getRemark());
        erpOrder.setIs_print_price(order.getIsPrintPrice());
        erpOrder.setIs_contact(order.getIsPreContact());
        erpOrder.setIs_need_rapid(order.getIsNeedRapid());
        erpOrder.setAttribute(Integer.valueOf(order.getAttribute()));
        erpOrder.setActivities_id(Integer.valueOf(order.getActivitiesId()));
        erpOrder.setConsignee_name(order.getUserName());
        erpOrder.setPhone(order.getPhone());
        erpOrder.setMobile(order.getMobile());
        erpOrder.setProvince(order.getProvince());
        erpOrder.setCity(order.getCity());
        erpOrder.setDistrict(order.getDistrict());
        erpOrder.setAddress(order.getAddress());
        erpOrder.setZip_code(String.valueOf(order.getZipCode()));
        erpOrder.setEmail(order.getEmail());

        //// TODO: 16/4/26
        erpOrder.setShipping_manner(Integer.valueOf(order.getShippingTypeId()));
        erpOrder.setArea_code(String.valueOf(order.getAreaCode()));
        erpOrder.setIs_jit(order.getIsJit());
        erpOrder.setParent_order_code(order.getParentOrderCode());

        //优惠码
        erpOrder.setPromo_id(0);
        erpOrder.setPromo_code("");
        erpOrder.setPromo_code_amount(0);
        erpOrder.setPromo_code_discount(0);

        erpOrder.getReceiver_info().put("consignee_name", order.getUserName());
        erpOrder.getReceiver_info().put("phone", order.getPhone());
        erpOrder.getReceiver_info().put("mobile", order.getMobile());
        erpOrder.getReceiver_info().put("area_code", order.getAreaCode());
        erpOrder.getReceiver_info().put("address", order.getAddress());
        erpOrder.getReceiver_info().put("zip_code", order.getZipCode());
        erpOrder.getReceiver_info().put("email", order.getEmail());
        erpOrder.getReceiver_info().put("province", order.getProvince());
        erpOrder.getReceiver_info().put("city", order.getCity());
        erpOrder.getReceiver_info().put("district", order.getDistrict());

        return erpOrder;
    }

    /**
     * 添加运费优惠信息
     *
     * @param orderCode
     * @param orderAmountDetail
     * @return
     */
    private List<ERPPromotion> addShippingCostPromotions(long orderCode, OrderAmountDetail orderAmountDetail) {
        List<ERPPromotion> fit_promotions = new ArrayList<>();
        ERPPromotion promotion = new ERPPromotion();
        promotion.setOrder_code(orderCode);
        promotion.setPromotion_id(0);
        double cutDownAmount = orderAmountDetail.getShoppingOrigCost() - orderAmountDetail.getShippingCost();
        promotion.setCutdown_amount(cutDownAmount);
        String template = OrderYmlUtils.getShippingCostShowTemplate();

        String shippinCostFormula = String.format(template, new Object[]{orderAmountDetail.getShoppingOrigCost(), cutDownAmount});
        promotion.setPromotion_title(shippinCostFormula);
        fit_promotions.add(promotion);
        return fit_promotions;
    }


    public List<ERPOrderGoods> createERPOrderGoodsList(OrderWrapper orderWrapper) {
        List<ERPOrderGoods> erpOrderGoodsList = new ArrayList<>();
        List<OrdersGoods> goodsList = orderWrapper.getGoodsList();
        if (CollectionUtils.isNotEmpty(goodsList)) {
            for (OrdersGoods goods : goodsList) {
                ERPOrderGoods erpOrderGoods = new ERPOrderGoods();
                erpOrderGoods.setProduct_skn(goods.getProductSkn());
                erpOrderGoods.setProduct_name(goods.getProductName());
                erpOrderGoods.setColor_id(Integer.valueOf(goods.getColorId()));
                erpOrderGoods.setColor_name(goods.getColorName());
                erpOrderGoods.setProduct_id(goods.getProductId());
                erpOrderGoods.setBrand_id(goods.getBrandId());
                erpOrderGoods.setGoods_id(goods.getGoodsId());
                erpOrderGoods.setErp_sku_id(goods.getErpSkuId());
                erpOrderGoods.setProduct_sku(goods.getErpSkuId());
                erpOrderGoods.setBuy_number(goods.getNum());
                erpOrderGoods.setNum(goods.getNum());
                erpOrderGoods.setSize_id(goods.getSizeId());
                erpOrderGoods.setSize_name(goods.getSizeName());
                erpOrderGoods.setSale_price(Double.valueOf(goods.getSalesPrice()));
                erpOrderGoods.setReal_price(MathUtils.toDouble(goods.getGoodsPrice()));
                erpOrderGoods.setLast_price(goods.getGoodsPrice().doubleValue());
                erpOrderGoods.setGet_yoho_coin(goods.getGetYohoCoin());
                erpOrderGoods.setVip_discount(MathUtils.toDouble(goods.getVipDiscountRate()));
                erpOrderGoods.setReal_vip_price(MathUtils.toDouble(goods.getRealVipPrice()));
                //// TODO: 16/4/26
                erpOrderGoods.setVip_discount_money(MathUtils.toDouble(goods.getVipCutAmount()));
                erpOrderGoods.setGoods_type(Integer.valueOf(goods.getGoodsType()));
                erpOrderGoods.setIs_jit(goods.getIsJit());
                erpOrderGoods.setShop_id(goods.getShopId());
                erpOrderGoods.setSupplier_id(goods.getShopId());

                //每件sku分摊的优惠券
                erpOrderGoods.setCoupons_per(MathUtils.toDouble(goods.getCouponsCutAmount()));
                //每件sku分摊的优惠码
                erpOrderGoods.setPromo_code_per(MathUtils.toDouble(goods.getPromoCodeCutAmount()));
                //每件sku分摊的yoho
                erpOrderGoods.setYoho_coin_per(goods.getYohoCoinCutNum());
                //每件sku分摊的红包
                erpOrderGoods.setRed_envelope_per(MathUtils.toDouble(goods.getRedenvelopeCutAmount()));

                //product_skc为新增的字段,老的订单没有这个值
                erpOrderGoods.setProduct_skc(goods.getProductSkc() == null ? 0 : goods.getProductSkc());

                erpOrderGoodsList.add(erpOrderGoods);
            }
        }
        return erpOrderGoodsList;
    }


    public static void main(String[] args) {

        int parentYohoCoinNum = 994;
        double parentYohoCoinAmount = 0;
        try {
            parentYohoCoinAmount = MathUtils.div(parentYohoCoinNum, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
        } catch (Exception ex) {
            //logger.warn("call YHMath.div error,m is {},d is {}", parentYohoCoinNum, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO, ex);
        }

        List<OrderWrapper> orderList = new ArrayList<>();

        OrderWrapper orderWrapper1 = new OrderWrapper();
        OrderAmountDetail orderAmoutDetail = new OrderAmountDetail();
        orderAmoutDetail.setLastOrderAmount(13);
        orderWrapper1.setOrderAmoutDetail(orderAmoutDetail);

        orderList.add(orderWrapper1);

        orderWrapper1 = new OrderWrapper();
        orderAmoutDetail = new OrderAmountDetail();
        orderAmoutDetail.setLastOrderAmount(1);
        orderWrapper1.setOrderAmoutDetail(orderAmoutDetail);

        orderList.add(orderWrapper1);


        orderWrapper1 = new OrderWrapper();
        orderAmoutDetail = new OrderAmountDetail();
        orderAmoutDetail.setLastOrderAmount(10);
        orderWrapper1.setOrderAmoutDetail(orderAmoutDetail);

        orderList.add(orderWrapper1);


        double totalLastOrderAmount = 0;
        for (OrderWrapper orderWrapper : orderList) {
            totalLastOrderAmount = YHMath.add(totalLastOrderAmount, orderWrapper.getOrderAmoutDetail().getLastOrderAmount());
        }

        System.out.println("parentYohoCoinAmount=" + parentYohoCoinAmount);
        System.out.println("totalLastOrderAmount=" + totalLastOrderAmount);
        double avg = 0;
        if (totalLastOrderAmount > 0) {
            try {
                avg = MathUtils.div(parentYohoCoinAmount, totalLastOrderAmount);
                //avg = BigDecimal.valueOf(parentYohoCoinAmount).divide(BigDecimal.valueOf(totalLastOrderAmount));
            } catch (Exception ex) {
                System.out.println("ex=" + ex);
                //logger.warn("call YHMath.div error,m is {},d is {}", parentYohoCoinNum, Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO, ex);
            }
        }
        System.out.println("avg=" + avg);

        double remainYohoCoinAmount = parentYohoCoinAmount;
        for (int i = orderList.size() - 1; i >= 0; i--) {
            OrderWrapper orderWrapper = orderList.get(i);
            OrderAmountDetail orderAmountDetail = orderWrapper.getOrderAmoutDetail();
            double ratioYohoCoinAmount = YHMath.mul(orderAmountDetail.getLastOrderAmount(), avg);

            System.out.println("ratioYohoCoinAmount=" + ratioYohoCoinAmount);
            double realYohoCoinAmount = ratioYohoCoinAmount;
            if (i == 0) {
                realYohoCoinAmount = Math.max(remainYohoCoinAmount, realYohoCoinAmount);
            }
            remainYohoCoinAmount = YHMath.sub(remainYohoCoinAmount, realYohoCoinAmount);

            System.out.println("remainYohoCoinAmount=" + remainYohoCoinAmount);

            int usedYohoCoinNum = (int) (realYohoCoinAmount * Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
            orderAmountDetail.setYohCoinCutAmount(realYohoCoinAmount);
            orderAmountDetail.setYohoCoinCutNum(usedYohoCoinNum);
            orderAmountDetail.setLastOrderAmount(YHMath.sub(orderAmountDetail.getLastOrderAmount(), realYohoCoinAmount));
            //orderWrapper.getOrder().setAmount(BigDecimal.valueOf(orderAmountDetail.getLastOrderAmount()));
            //orderWrapper.getOrder().setYohoCoinNum(usedYohoCoinNum);

            System.out.println(orderAmountDetail);

            //logger.info("after calc order yoho coin,order code is {},remain yoho coin amount is {},order amount detail is {}", orderWrapper.getOrder().getOrderCode(), remainYohoCoinAmount, orderAmountDetail);
        }

    }
}

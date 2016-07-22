package com.yoho.yhorder.shopping.union.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.common.utils.YHMath;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.product.model.CategoryBo;
import com.yoho.product.request.BatchBaseRequest;
import com.yoho.yhorder.dal.IOrdersMapper;
import com.yoho.yhorder.dal.IReturnedGoodsListDAO;
import com.yoho.yhorder.dal.IUnionUsersDAO;
import com.yoho.yhorder.dal.model.ReturnedGoodsList;
import com.yoho.yhorder.dal.model.UnionUsers;
import com.yoho.yhorder.shopping.model.Order;
import com.yoho.yhorder.shopping.model.OrderGoods;
import com.yoho.yhorder.shopping.union.UnionContext;
import com.yoho.yhorder.shopping.union.UnionService;
import com.yoho.yhorder.shopping.utils.MathUtils;
import com.yoho.yhorder.shopping.utils.MyStringUtils;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by JXWU on 2015/12/5.
 */
@Component
public class DefaultUnionServiceImpl implements UnionService {

    private final Logger logger = LoggerFactory.getLogger("unionPushLog");

    @Autowired
    protected ServiceCaller serviceCaller;

    @Autowired
    protected IOrdersMapper ordersMapper;

    @Autowired
    private IUnionUsersDAO unionUsersMapper;

    @Autowired
    private IReturnedGoodsListDAO returnedGoodsListMapper;

    @Resource(name = "union-redisTemplate")
    private ListOperations<String, String> listOperations;

    @Override
    public void run(String userAgent, UnionContext unionContext) {
        Order order = unionContext.getOrderCreationContext().getOrder();
        logger.info("push channel to union,userAgent is {},uid is {},order code is {}", userAgent, order.getUid(), order.getOrderCode());
        int channel = 2919;// 如果没有默认主包
        try {

            Map<String, String> userAgentMap = MyStringUtils.asStringToMap(userAgent);
            if (userAgentMap.containsKey("Channel")) {
                channel = Integer.parseInt(userAgentMap.get("Channel"));
            }

            UnionUsers unionUsers = unionUsersMapper.selectByPrimaryKey(channel);
            int counter = ordersMapper.selectCountOrderByUid(order.getUid(), order.getOrderCode());
            int isOldUser = counter > 0 ? 1 : 0;

            updateUnion(channel, order, unionUsers);
            // 加入联盟订单库
            orderUnion(channel, isOldUser, userAgent, order);
            // 对接三方联盟
            setUnion(isOldUser, unionUsers, order);

        } catch (Exception ex) {
            logger.warn("push channel to union", ex);
        }
        logger.info("push channel to union end,channel is {}", channel);
    }

    private void updateUnion(int channel, Order order, UnionUsers unionUsers) {
        JSONObject dataJSON = new JSONObject();
        dataJSON.put("order_code", order.getOrderCode());
        dataJSON.put("unionid", channel);
        dataJSON.put("union_name", (unionUsers == null ? channel : unionUsers.getRealName()));

        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("data", dataJSON.toJSONString());

        JSONObject postJSON = new JSONObject();
        postJSON.put("url", "http://portal.admin.yohobuy.com/api/orderunion/updateunion");
        postJSON.put("params", paramsJSON);
        postJSON.put("mode", "post");



        //
        logger.info("updateUnion->post->json {}", postJSON);
        listOperations.leftPush("post_queue", postJSON.toJSONString());

    }

    private void orderUnion(int channel, int isOldUser, String userAgent, Order order) {
        String unionData = order.getUnionData();
        if (org.apache.commons.lang.StringUtils.isEmpty(unionData)) {
            return;
        }
        JSONObject unionJSON = JSONObject.parseObject(unionData);
        if (unionJSON == null || unionJSON.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("client_id", channel);
        builder.append(paramsJSON.get("client_id"));
        paramsJSON.put("order_code", order.getOrderCode());
        builder.append("&").append(paramsJSON.get("order_code"));
        paramsJSON.put("orders_price", order.getLastOrderAmount());
        builder.append("&").append(paramsJSON.get("orders_price"));
        paramsJSON.put("buy_time", order.getCreateTime());
        builder.append("&").append(paramsJSON.get("buy_time"));
        paramsJSON.put("channel_code", (unionJSON.containsKey("channel_code") ? unionJSON.get("channel_code") : ""));
        builder.append("&").append(paramsJSON.get("channel_code"));

        Object append = (unionJSON.containsKey("append") ? unionJSON.get("append") : "");
        if (null == append) {
            append = "";
        }
        paramsJSON.put("append", append);
        builder.append("&").append(paramsJSON.get("append"));
        paramsJSON.put("mbr_name", (unionJSON.containsKey("mbr_name") ? unionJSON.get("mbr_name") : ""));
        builder.append("&").append(paramsJSON.get("mbr_name"));
        paramsJSON.put("u_id", (unionJSON.containsKey("u_id") ? unionJSON.get("u_id") : ""));
        builder.append("&").append(paramsJSON.get("u_id"));

        paramsJSON.put("is_old_user", isOldUser);
        paramsJSON.put("verify_code", MyStringUtils.getMd5(builder.toString()));

        JSONObject getJSON = new JSONObject();

        getJSON.put("url", "http://union.yohobuy.com/order");
        getJSON.put("params", paramsJSON);
        getJSON.put("mode", "get");

        logger.info("orderUnion->post->json {}", getJSON);
        listOperations.leftPush("post_queue", getJSON.toJSONString());
    }

    private void setUnion(int isOldUser, UnionUsers unionUsers, Order order) throws UnsupportedEncodingException {
        String unionData = order.getUnionData();
        if (org.apache.commons.lang.StringUtils.isEmpty(unionData)) {
            logger.info("union data is empty,order code is {}", order.getOrderCode());
            return;
        }
        JSONObject unionJSON = JSONObject.parseObject(unionData);
        if (unionJSON == null || unionJSON.isEmpty()) {
            return;
        }
        int channel = unionJSON.getIntValue("client_id");
        logger.info("setUnion channel is {},unionData is {}", channel, unionData);
        if (channel == 1001 && ShoppingConfig.UNION_MAP.containsKey(channel)) {
            doSetUnionForChannel_1001(isOldUser, unionJSON, order);
        } else if (channel == 2995 && ShoppingConfig.UNION_MAP.containsKey(channel)) {
            doSetUnionForChannel_2995(isOldUser, unionJSON, order);
        } else if ((channel == 3017 || channel == 3019 || channel == 3057) && ShoppingConfig.UNION_MAP.containsKey(channel)) {
            doSetUnionForChannel_MaiDuo(unionJSON, unionUsers, order);
        } else if (channel == 3001 && ShoppingConfig.UNION_MAP.containsKey(channel)) {
            doSetUnionForChannel_3001(unionJSON, order);
        } else if (channel == 1010 || channel == 1009 || channel == 2997 || channel == 3001) {
            doSetUnionForChannel_others(isOldUser, unionJSON, order);
        }
    }

    private void doSetUnionForChannel_1001(int isOldUser, JSONObject unionJSON, Order order) {
        int channel = unionJSON.getIntValue("client_id");
        Object append = unionJSON.containsKey("append") ? unionJSON.getString("append") : "";
        String channelCode = unionJSON.containsKey("channel_code") ? unionJSON.getString("channel_code") : "";
        int create_times = order.getCreateTime();
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("client_id=").append(channel).append("&orders_id=").append(order.getOrderCode())
                .append("&orders_price=").append(order.getLastOrderAmount()).append("&create_time=").append(create_times)
                .append("&channel_code=").append(channelCode).append("&append=").append(append)
                .append("&is_old_user=").append(isOldUser);

        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("client_id", channel);
        paramsJSON.put("orders_id", order.getOrderCode());
        paramsJSON.put("orders_price", order.getLastOrderAmount());
        paramsJSON.put("create_time", create_times);
        paramsJSON.put("channel_code", channelCode);
        paramsJSON.put("append", append);
        paramsJSON.put("is_old_user", isOldUser);
        paramsJSON.put("verify_code", MyStringUtils.getMd5(keyBuilder.toString()));

        JSONObject dataJSON = new JSONObject();
        dataJSON.put("url", ShoppingConfig.UNION_MAP.get(channel));
        dataJSON.put("params", paramsJSON);
        dataJSON.put("mode", "get");

        logger.info("setUnion->post->json {}", dataJSON);
        listOperations.leftPush("post_queue", dataJSON.toJSONString());
    }

    private void doSetUnionForChannel_2995(int isOldUser, JSONObject unionJSON, Order order) {
        int channel = unionJSON.getIntValue("client_id");
        Object append = unionJSON.containsKey("append") ? unionJSON.getString("append") : "";
        String channelCode = unionJSON.containsKey("channel_code") ? unionJSON.getString("channel_code") : "";
        long create_times = order.getCreateTime();
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("client_id=").append(channel).append("&orders_id=").append(order.getOrderCode())
                .append("&orders_price=").append(order.getLastOrderAmount()).append("&create_time=").append(create_times)
                .append("&channel_code=").append(channelCode).append("&append=").append(append)
                .append("dt=m")
                .append("&is_old_user=").append(isOldUser);

        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("client_id", channel);
        paramsJSON.put("orders_id", order.getOrderCode());
        paramsJSON.put("orders_price", order.getLastOrderAmount());
        paramsJSON.put("create_time", create_times);
        paramsJSON.put("channel_code", channelCode);
        paramsJSON.put("append", append);
        paramsJSON.put("is_old_user", isOldUser);
        paramsJSON.put("dt", "m");
        paramsJSON.put("verify_code", MyStringUtils.getMd5(keyBuilder.toString()));

        JSONObject dataJSON = new JSONObject();
        dataJSON.put("url", ShoppingConfig.UNION_MAP.get(channel));
        dataJSON.put("params", paramsJSON);
        dataJSON.put("mode", "get");

        logger.info("setUnion->post->json {}", dataJSON);
        listOperations.leftPush("post_queue", dataJSON.toJSONString());
    }

    private void doSetUnionForChannel_3001(JSONObject unionJSON, Order order) {
        int channel = unionJSON.getIntValue("client_id");
        String u_id = unionJSON.containsKey("u_id") ? unionJSON.getString("u_id") : "";
        String mbr_name = unionJSON.containsKey("mbr_name") ? unionJSON.getString("mbr_name") : "";
        String channel_code = unionJSON.containsKey("channel_code") ? unionJSON.getString("channel_code") : "";
        StringBuilder contentBuilder = new StringBuilder();

        int shopid = 690;
        String context = getOrderXMLData(u_id, mbr_name, channel_code, order);
        contentBuilder.append("content=").append(context).append("&shopid=").append(shopid);

        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("content", context);
        paramsJSON.put("shopid", shopid);
        paramsJSON.put("verify_code", MyStringUtils.getMd5(contentBuilder.toString()));

        JSONObject dataJSON = new JSONObject();
        dataJSON.put("url", ShoppingConfig.UNION_MAP.get(channel));
        dataJSON.put("params", paramsJSON);
        dataJSON.put("mode", "post");

        logger.info("setUnion->post->json {}", dataJSON);
        listOperations.leftPush("post_queue", dataJSON.toJSONString());
    }

    private void doSetUnionForChannel_others(int isOldUser, JSONObject unionJSON, Order order) {
        int channel = unionJSON.getIntValue("client_id");
        Object mbr_name = unionJSON.containsKey("mbr_name") ? unionJSON.get("mbr_name") : "";
        Object append = unionJSON.containsKey("append") ? unionJSON.getString("append") : "";

        long create_times = order.getCreateTime();
        JSONObject paramsJSON = new JSONObject();
        paramsJSON.put("client_id", channel);
        paramsJSON.put("orders_id", order.getOrderCode());
        paramsJSON.put("create_time", create_times);
        paramsJSON.put("channel_code", order.getCancelType());
        paramsJSON.put("append", append);
        paramsJSON.put("mbr_name", mbr_name);
        paramsJSON.put("UID", order.getUid());
        paramsJSON.put("is_old_user", isOldUser);
        paramsJSON.put("url", ShoppingConfig.UNION_MAP.get(channel));

        logger.info("setUnion->post->json {}", paramsJSON);
        listOperations.leftPush("union_orders", paramsJSON.toJSONString());
    }

    private void doSetUnionForChannel_MaiDuo(JSONObject unionJSON, UnionUsers unionUsers, Order order) throws UnsupportedEncodingException {
        int channel = unionJSON.getIntValue("client_id");
        String mbr_name = unionJSON.containsKey("mbr_name") ? unionJSON.getString("mbr_name") : "";
        String maiduoText = getMaiDuoSplitData(channel, mbr_name, unionUsers, order);
        JSONObject dataJSON = new JSONObject();

        JSONObject paramsSON = new JSONObject();
        paramsSON.put("content", maiduoText);
        dataJSON.put("url", ShoppingConfig.UNION_MAP.get(channel));
        dataJSON.put("params", paramsSON);
        dataJSON.put("mode", "post");
        logger.info("setUnion->post->json {}", dataJSON);
        listOperations.leftPush("post_queue", dataJSON.toJSONString());
    }


    private String getMaiDuoSplitData(int client_id, String mbr_name, UnionUsers unionOneUser, Order order) throws UnsupportedEncodingException {
        if (order == null || order.getOrderCode() == null) {
            return "ORDER CODE IS NULL";
        }

        TmpReturnedOrder tmpReturnedOrder = getReturnedGoodsList(unionOneUser.getCommissionRate().doubleValue(), order);
        if (tmpReturnedOrder == null || CollectionUtils.isEmpty(tmpReturnedOrder.tmpReturnedGoodsList)) {
            return "ORDER INFO IS NULL";
        }
        int is_new_custom = ordersMapper.selectUserTypeByOrders(order.getUid(), 4, order.getCreateTime());

        List<String> goodsNameList = new ArrayList<>();
        List<Integer> goodsIdList = new ArrayList<>();
        List<Double> goodsPriceList = new ArrayList<>();
        List<Double> goodsTotalPriceList = new ArrayList<>();
        List<Double> commissionlList = new ArrayList<>();
        List<Integer> goodsNumList = new ArrayList<>();
        List<String> small_sort_idList = new ArrayList<>();
        for (TmpReturnedGoods returnedGoods : tmpReturnedOrder.tmpReturnedGoodsList) {
            goodsNameList.add(returnedGoods.product_name.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&quot;").replaceAll("\"", "&apos;").replaceAll(" ", "%20").replaceAll("|", "%20"));
            goodsIdList.add(returnedGoods.goods_id);
            goodsPriceList.add(returnedGoods.goods_price);
            goodsTotalPriceList.add(returnedGoods.goods_amount);
            commissionlList.add(returnedGoods.commission);
            goodsNumList.add(returnedGoods.buy_number);
            small_sort_idList.add(returnedGoods.small_sort_id);
        }

        String hash = "";
        int channel = 0;
        if (client_id == 3017) {
            hash = "96613bf38393aa3d16451218f22344a8";
            channel = 0;
        } else if (client_id == 3019) {
            hash = "d54be2dbc75753eb863ba6139950656b";
            channel = 1;
        } else if (client_id == 3057) {
            hash = "bbf70bcaf5c52947ad26853f7cc1176d";
            channel = 0;
        } else {
            return "CLIENT ID IS NULL";
        }

        String orderStatusStr = getOrderStatusStr(order.getStatus());
        if ("Y".equals(order.getIsCancel())) {
            orderStatusStr = "-1";
        } else if ("N".equals(order.getPaymentStatus()) && order.getStatus() == 0) {
            orderStatusStr = "0";
        }
        LinkedHashMap<String,Object> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("hash", hash);
        linkedHashMap.put("euid", mbr_name);
        linkedHashMap.put("order_sn", order.getOrderCode());
        linkedHashMap.put("order_time", MyStringUtils.formatSecond(order.getCreateTime()));
        linkedHashMap.put("orders_price", MathUtils.roundPrice(tmpReturnedOrder.total_price));
        linkedHashMap.put("promotion_code", 0);
        linkedHashMap.put("is_new_custom", is_new_custom > 0 ? 0 : 1);
        linkedHashMap.put("channel", channel);
        linkedHashMap.put("status", orderStatusStr);
        //商品id,
        linkedHashMap.put("goods_id", CollectionUtils.isEmpty(goodsIdList) ? "" : org.apache.commons.lang.StringUtils.join(goodsIdList, "|"));
        //商品名称,
        linkedHashMap.put("goods_name", CollectionUtils.isEmpty(goodsNameList) ? "" : org.apache.commons.lang.StringUtils.join(goodsNameList, "|"));

        //商品单价,
        linkedHashMap.put("goods_price", CollectionUtils.isEmpty(goodsPriceList) ? "" : org.apache.commons.lang.StringUtils.join(goodsPriceList, "|"));
        //商品数量,
        linkedHashMap.put("goods_ta", CollectionUtils.isEmpty(goodsNumList) ? "" : org.apache.commons.lang.StringUtils.join(goodsNumList, "|"));

        linkedHashMap.put("goods_cate", CollectionUtils.isEmpty(small_sort_idList) ? "" : org.apache.commons.lang.StringUtils.join(small_sort_idList, "|"));

        linkedHashMap.put("goods_cate_name", 0);
        linkedHashMap.put("totalPrice", CollectionUtils.isEmpty(goodsTotalPriceList) ? "" : org.apache.commons.lang.StringUtils.join(goodsTotalPriceList, "|"));

        //佣金计算,月底计算一次,7%计算,数据库比例已经老的
        linkedHashMap.put("rate", 0);
        linkedHashMap.put("commission","");
        linkedHashMap.put("commission_type", 0);
        linkedHashMap.put("coupon", MathUtils.roundPrice(tmpReturnedOrder.coupon));

        Set<String> keys = linkedHashMap.keySet();
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            builder.append(key).append("=").append(linkedHashMap.get(key)).append("&");
        }

        return URLEncoder.encode(builder.substring(0,builder.length() - 1), "UTF-8");
    }

    private TmpReturnedOrder getReturnedGoodsList(double commission_rate, Order order) {
        Integer status = 30, changePurchaseId = 0;
        List<ReturnedGoodsList> returnedGoodsLists = returnedGoodsListMapper.selectReturnedList(order.getOrderCode(), status, changePurchaseId);
        double returnedPrice = 0;
        int return_key = 0;
        Map<String, Integer> orderGoodsKeyMap = new HashMap<String, Integer>();
        if (CollectionUtils.isNotEmpty(returnedGoodsLists)) {
            for (ReturnedGoodsList returnedGoods : returnedGoodsLists) {
                if (return_key != returnedGoods.getReturnRequestId()) {
                    returnedPrice += returnedGoods.getRealReturnedAmount().doubleValue();
                    return_key = returnedGoods.getReturnRequestId();
                }
                String key = "orderGoods_" + order.getOrderCode() + "_" + returnedGoods.getLastPrice() + "_" + returnedGoods.getProductSku();
                if (orderGoodsKeyMap.containsKey(key)) {
                    Integer num = orderGoodsKeyMap.get(key);
                    orderGoodsKeyMap.put(key, num + 1);
                } else {
                    orderGoodsKeyMap.put(key, 1);
                }
            }
        }

        double goodsTotalAmount = 0;
        TmpReturnedOrder tmpReturnedOrder = new TmpReturnedOrder();
        List<TmpReturnedGoods> tempReturnedGoodsList = new ArrayList<TmpReturnedGoods>();
        for (OrderGoods orderGoods : order.getGoodsList()) {
            String key = "orderGoods_" + order.getOrderCode() + "_" + orderGoods.getGoods_price() + "_" + orderGoods.getErp_sku_id();
            int buy_number = !orderGoodsKeyMap.containsKey(key) ? orderGoods.getBuy_number() : (orderGoods.getBuy_number() - orderGoodsKeyMap.get(key));
            if (buy_number > 0) {
                goodsTotalAmount += buy_number * orderGoods.getGoods_price();
            }
            double goodsAmount = buy_number * orderGoods.getGoods_price();
            double goodsCommission = YHMath.mul(goodsAmount, commission_rate);
            TmpReturnedGoods tempReturnedGoods = new TmpReturnedGoods();
            tempReturnedGoods.product_id = orderGoods.getProduct_id();
            tempReturnedGoods.goods_id = orderGoods.getGoods_id();
            tempReturnedGoods.commission = goodsCommission;
            tempReturnedGoods.goods_price = orderGoods.getGoods_price();
            tempReturnedGoods.buy_number = buy_number;
            tempReturnedGoods.goods_amount = goodsAmount;
            tempReturnedGoods.small_sort_id = orderGoods.getSmallSortId();
            tempReturnedGoods.product_name = orderGoods.getProduct_name();
            tempReturnedGoodsList.add(tempReturnedGoods);

            tmpReturnedOrder.num += buy_number;
        }

        tmpReturnedOrder.tmpReturnedGoodsList = tempReturnedGoodsList;

        double total_amount = order.getAmount() - returnedPrice - order.getShippingCost();
        double tmpCoupon = goodsTotalAmount - total_amount;
        if (tmpCoupon < 0) {
            total_amount = total_amount + tmpCoupon;
        } else {
            tmpReturnedOrder.coupon = tmpCoupon;
        }

        tmpReturnedOrder.total_price = (total_amount < 0 ? 0 : total_amount);
        tmpReturnedOrder.total_commission = tmpReturnedOrder.total_price * commission_rate;
        return tmpReturnedOrder;
    }

    public static class TmpReturnedOrder {
        public int num = 0;
        public double coupon;
        public double total_price;
        public double total_commission;
        public List<TmpReturnedGoods> tmpReturnedGoodsList;
    }

    public static class TmpReturnedGoods {
        public int product_id;
        public int goods_id;
        public double commission;
        public double goods_price;
        public int buy_number;
        public double goods_amount;
        public String small_sort_id;
        public String product_name;
    }


    private String getOrderXMLData(String u_id, String mbr_name, String channel_code, Order order) {
        if (order.getOrderId() == null) {
            return "没有订单";
        }
        List<OrderGoods> goodsList = order.getGoodsList();
        List<JSONObject> goodsJSONList = new ArrayList<JSONObject>();
        Map<Integer, JSONObject> categoryMap = new HashMap<>();
        double orderAmout = order.getAmount().doubleValue() - order.getShippingCost().doubleValue();
        double goodsTotalAmout = 0;
        for (int i = 0; i < goodsList.size(); i++) {
            OrderGoods orderGoods = goodsList.get(i);
            JSONObject goodsJSON = new JSONObject();
            goodsJSON.put("pid", orderGoods.getProduct_id());
            goodsJSON.put("title", "");
            //TODO
            goodsJSON.put("category", "");
            categoryMap.put(new Integer(orderGoods.getMiddleSortId()), goodsJSON);
            goodsJSON.put("category_title", "");
            goodsJSON.put("url", "http://item.yohobuy.com/product/show_" + orderGoods.getProduct_skn() + ".html");
            goodsJSON.put("num", orderGoods.getNum());
            goodsJSON.put("price", orderGoods.getGoods_price());
            goodsJSON.put("refund_num", "");
            goodsJSON.put("comm_type", "A");
            goodsJSON.put("goods_amount", orderGoods.getGoods_amount());
            goodsTotalAmout += orderGoods.getGoods_amount().doubleValue();
            goodsJSONList.add(goodsJSON);
        }

        for (int i = 0; i < goodsJSONList.size(); i++) {
            JSONObject goodsJSON = goodsJSONList.get(i);
            Double goodsAmount = (Double) goodsJSON.get("goods_amount");
            goodsJSON.put("real_pay_fee", MathUtils.roundPrice((orderAmout * (goodsAmount / goodsTotalAmout))));
            goodsJSON.put("commission", MathUtils.roundPrice((orderAmout * (goodsAmount / goodsTotalAmout) * 0.05)));
            goodsJSON.remove("goods_amount");
        }

        //批量查询
        BatchBaseRequest request = new BatchBaseRequest();
        request.setParams(new ArrayList<>(categoryMap.keySet()));
        CategoryBo[] categoryBos = serviceCaller.call(ShoppingConfig.PRODUCT_QUERY_CATEGORYBYIDS_REST_URL, request, CategoryBo[].class);

        if (categoryBos != null) {
            for (CategoryBo bo : categoryBos) {
                JSONObject goodsJSON = categoryMap.get(bo.getCategoryId());
                if (goodsJSON != null) {
                    goodsJSON.put("category", bo.getCategoryId());
                }

            }
        }
        int orderType = ("h5".equals(order.getClientType())) ? 2 : 1;

        StringBuilder xmlBuilder = new StringBuilder("<?xml version='1.0' encoding='utf-8'?><orders  version='4.0'>");
        xmlBuilder.append("<order>")
                .append("<s_id>").append(690).append("</s_id>")
                .append("<order_id_parent>").append(order.getOrderCode()).append("</order_id_parent>")
                .append("<order_id>").append(order.getOrderCode()).append("</order_id>")
                .append("<order_time>").append(MyStringUtils.formatSecond(order.getCreateTime())).append("</order_time>")
                .append("<uid>").append(u_id).append("</uid>")
                .append("<uname>").append(mbr_name).append("</uname>")
                .append("<tc>").append(channel_code).append("</tc>")
                .append("<pay_time>").append("").append("</pay_time>")
                .append("<status>").append(order.getStatus()).append("</status>")
                .append("<locked>").append("").append("</locked>")
                .append("<lastmod>").append("").append("</lastmod>")
                .append("<is_newbuyer>").append("").append("</is_newbuyer>")
                .append("<platform>").append(orderType).append("</is_newbuyer>")
                .append("<code>").append("").append("</code>")
                .append("<remark>").append("").append("</remark>");

        xmlBuilder.append("<products>");
        for (JSONObject goodsJSON : goodsJSONList) {
            xmlBuilder.append("<product>");
            xmlBuilder.append("<pid>").append(goodsJSON.get("pid")).append("</pid>");
            xmlBuilder.append("<title>").append(goodsJSON.get("title")).append("</title>");
            xmlBuilder.append("<category>").append(goodsJSON.get("category")).append("</category>");
            xmlBuilder.append("<category_title>").append(goodsJSON.get("category_title")).append("</category_title>");
            xmlBuilder.append("<url>").append(goodsJSON.get("url")).append("</url>");
            xmlBuilder.append("<num>").append(goodsJSON.get("num")).append("</num>");
            xmlBuilder.append("<price>").append(goodsJSON.get("price")).append("</price>");
            xmlBuilder.append("<real_pay_fee>").append(goodsJSON.get("real_pay_fee")).append("</real_pay_fee>");
            xmlBuilder.append("<refund_num>").append(goodsJSON.get("refund_num")).append("</refund_num>");
            xmlBuilder.append("<commission>").append(goodsJSON.get("commission")).append("</commission>");
            xmlBuilder.append("<comm_type>").append(goodsJSON.get("comm_type")).append("</comm_type>");
            xmlBuilder.append("</product>");
        }
        xmlBuilder.append("</products>");
        xmlBuilder.append("</order>");
        xmlBuilder.append("</orders>");
        return xmlBuilder.toString();
    }

    private String getOrderStatusStr(int status) {
        switch (status) {
            case 0:
                return "待付款";
            case 1:
                return "已付款";
            case 2:
                return "备货中";
            case 3:
                return "配货中";
            case 4:
                return "已发货";
            case 5:
                return "运输中";
            case 6:
                return "已完成";

        }
        return String.valueOf(status);
    }
}
package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.constants.OrdersMateKey;
import com.yoho.service.model.order.constants.YohoodType;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.order.response.OrdersGoods;
import com.yoho.service.model.promotion.request.UserCouponsNums;
import com.yoho.service.model.promotion.request.UserCouponsSendReq;
import com.yoho.service.model.sms.request.SMSTemplateReqBO;
import com.yoho.service.model.sms.response.SMSRspModel;
import com.yoho.service.model.sns.request.CommentRecordAddReq;
import com.yoho.yhorder.common.model.VirtualInfo;
import com.yoho.yhorder.common.utils.BeanTool;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.common.utils.DateUtil;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.model.OrdersMeta;
import com.yoho.yhorder.dal.model.YohoodSeat;
import com.yoho.yhorder.dal.model.YohoodTicketInfo;
import com.yoho.yhorder.dal.model.YohoodTickets;
import com.yoho.yhorder.order.config.YohoodConfig;
import com.yoho.yhorder.order.service.ITicketShoppingService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

/**
 * qianjun 2016/5/31
 */
@Service
public class TicketShoppingServiceImpl implements ITicketShoppingService {

    private final static Logger logger = LoggerFactory.getLogger("ticketLog");

    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Autowired
    private IOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    private YohoodTicketsMapper yohoodTicketsMapper;

    @Autowired
    private IYohoodSeatDAO yohoodSeatDAO;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    private TicketIssueService ticketIssueService;

    /**
     * 发放电子门票
     */
    @Override
    public void issueTicket(int uid, long orderCode) {

        Orders order = checkAndGetOrder(uid, orderCode);
        if (order != null) {
            parseAndIssueTicket(order);
        }
    }

    private void parseAndIssueTicket(Orders order) {
        List<OrdersGoods> goodsList = ordersGoodsMapper.selectOrderGoodsByOrder(order);
        if (CollectionUtils.isEmpty(goodsList)) {
            logger.info("the order goods is empty, order code {}", order.getOrderCode());
            return;
        }

        String ticketType = getTicketType(order);

        ticketIssueService.doIssueTicket(order, goodsList, ticketType);

        //发短信
        sendNoticeSms(order);
        //发券
        sendCoupon(order);
    }

    /**
     * 发短信
     */
    private void sendNoticeSms(Orders order) {
        logger.info("sendNoticeSms param {}.", order);
        Assert.notNull(order, "order must not be null");
        String mobile = order.getMobile();
        // 获取yohood门票类型
        String ticketType = getYohoodTicketType(order);
        // 获取yohood门票
        List<YohoodTickets> yohoodTicketses = getYohoodTicketses(order);
        YohoodTickets yohoodTickets = yohoodTicketses.get(0);
        Date beginDateTime = yohoodTickets.getBeginDate();
        String beginDate = DateUtil.format(beginDateTime, DateUtil.MM_dd);
        String beginTime = DateUtil.format(beginDateTime, DateUtil.HH_mm);
        String endTime = DateUtil.format(yohoodTickets.getEndDate(), DateUtil.HH_mm);
        String week = YohoodConfig.getWeek(beginDateTime);
        int ticketNum = yohoodTicketses.size();
        logger.info("sendNoticeSms ticketType {} and ticketNum {}.", ticketType, ticketNum);
        List<Long> ticketCodes = new ArrayList<>();
        for (YohoodTickets tickets : yohoodTicketses) {
            ticketCodes.add(tickets.getTicketCode());
        }
        String ticketCodeConcat = StringUtils.join(ticketCodes, "，") + "，";
        // 发送短信
        SMSTemplateReqBO smsRequestBO = new SMSTemplateReqBO();
        smsRequestBO.setMobile(mobile);
        if (YohoodType.EXHIBITION_TICKET.equals(ticketType)) {
            smsRequestBO.setTemplate(YohoodType.SMS_TEMPLATE_EXHIBITION_TICKET);
            String code = beginDate + "," + week + "," + ticketNum + "," + ticketCodeConcat + "," + beginTime + "-" + endTime;
            smsRequestBO.setCode(code);
            logger.info("sendNoticeSms param {}.", smsRequestBO);
        }
        if (YohoodType.PACKAGE_TICKET.equals(ticketType)) {
            List<YohoodSeat> yohoodSeats = yohoodSeatDAO.selectByTicketCodes(ticketCodes);
            String entranceTime = YohoodConfig.getEntranceTime(beginDateTime);
            List<String> yohoodSeatList = new ArrayList<>();
            for (YohoodSeat yohoodSeat : yohoodSeats) {
                yohoodSeatList.add(yohoodSeat.getArea() + yohoodSeat.getRowNo() + "排" + yohoodSeat.getColumnNo() + "座");
            }
            String code = beginDate + "," + week + "," + ticketNum + "," + ticketCodeConcat + "," + StringUtils.join(yohoodSeatList, "、")
                    + "," + beginTime + "-" + endTime + "," + entranceTime;
            smsRequestBO.setTemplate(YohoodType.SMS_TEMPLATE_PACKAGE_TICKET);
            smsRequestBO.setCode(code);
            logger.info("sendNoticeSms param {}.", smsRequestBO);
        }
        List<SMSTemplateReqBO> smsTemplateBOList = new ArrayList<>();
        smsTemplateBOList.add(smsRequestBO);
        try {
            serviceCaller.asyncCall("message.smsSend", smsTemplateBOList, SMSRspModel.class);
        } catch (Exception ex) {
            logger.warn("sendNoticeSms error {} .", ex);
        }
    }

    /**
     * 获取yohood门票类型
     */
    private String getYohoodTicketType(Orders order) {
        OrdersMeta virtualInfoMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(order.getId(), "virtual_info");
        if (virtualInfoMeta == null) {
            logger.warn("sendNoticeSms or sendCoupon fail, yohood ticket type is empty.");
            throw new ServiceException(ServiceError.ORDER_TICKET_TYPE_IS_EMPTY);
        }
        String ticketType = "";
        JSONObject json = JSONObject.parseObject(virtualInfoMeta.getMetaValue());
        if (json != null && StringUtils.isNotBlank(json.getString("ticket_type"))) {
            ticketType = json.getString("ticket_type");
        }
        return ticketType;
    }

    /**
     * 获取yohood门票
     */
    private List<YohoodTickets> getYohoodTicketses(Orders order) {
        List<YohoodTickets> yohoodTicketses = yohoodTicketsMapper.selectByOrderCode(order.getOrderCode());
        if (CollectionUtils.isEmpty(yohoodTicketses)) {
            logger.warn("sendNoticeSms or sendCoupon fail, yohood ticket is empty.");
            throw new ServiceException(ServiceError.ORDER_TICKET_IS_EMPTY);
        }
        return yohoodTicketses;
    }

    /**
     * 发券
     */
    private void sendCoupon(Orders order) {
        logger.info("sendCoupon param {}.", order);
        Assert.notNull(order, "order must not be null");
        // 获取yohood门票类型
        String ticketType = getYohoodTicketType(order);
        // 获取yohood门票
        List<YohoodTickets> yohoodTicketses = getYohoodTicketses(order);
        int ticketNum = yohoodTicketses.size();
        logger.info("sendCoupon ticketType {} and ticketNum {}.", ticketType, ticketNum);
        if (YohoodType.EXHIBITION_TICKET.equals(ticketType)) {
            sendCoupon(order.getUid(), order.getOrderCode(), ticketType, ticketNum);
        }
        if (YohoodType.PACKAGE_TICKET.equals(ticketType)) {
            sendCoupon(order.getUid(), order.getOrderCode(), ticketType, ticketNum);
        }
    }

    private void sendCoupon(int uid, long orderCode, String ticketType, int ticketNum) {
        UserCouponsSendReq userCouponsSendReq = new UserCouponsSendReq();
        userCouponsSendReq.setUid(uid);
        userCouponsSendReq.setOrderCode(orderCode);
        List<UserCouponsNums> userCouponsNumses=new ArrayList<>();
        if (YohoodType.EXHIBITION_TICKET.equals(ticketType)) {
            userCouponsNumses.add(getUserCouponsNums(YohoodType.EXHIBITION_TICKET_COUPONID,ticketNum));
        }
        if (YohoodType.PACKAGE_TICKET.equals(ticketType)) {
            userCouponsNumses.add(getUserCouponsNums(YohoodType.PACKAGE_TICKET_COUPONID,ticketNum));
            userCouponsNumses.add(getUserCouponsNums(YohoodType.PACKAGE_TICKET_MORE_COUPONID,ticketNum));
        }
        userCouponsSendReq.setCouponIdNums(userCouponsNumses);
        logger.info("sendCoupon param {}.", userCouponsSendReq);
        try {
            serviceCaller.asyncCall("promotion.sendUserManyCoupons", userCouponsSendReq, String.class);
        } catch (Exception ex) {
            logger.warn("sendCoupon error {} .", ex);
        }
    }

    /**
     * 获取券id对应的数量
     *
     */
    private UserCouponsNums getUserCouponsNums(int couponId,int ticketNum) {
        UserCouponsNums userCouponsNums = new UserCouponsNums();
        userCouponsNums.setCouponId(couponId);
        userCouponsNums.setNums(ticketNum);
        return userCouponsNums;
    }

    /**
     * 获取票类型,
     * 1:展览票
     * 2:套票
     *
     * @param order
     * @return
     */
    private String getTicketType(Orders order) {
        OrdersMeta ordersMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(order.getId(), OrdersMateKey.VIRTUAL_INFO);
        if (ordersMeta == null) {
            //默认展览票
            return Constants.SHOW_TICKET;
        }

        VirtualInfo virtualInfo = BeanTool.string2Value(ordersMeta.getMetaValue(), VirtualInfo.class);

        if (virtualInfo == null) {
            //默认展览票
            return Constants.SHOW_TICKET;
        }
        return Constants.SEASON_TICKET.equals(virtualInfo.getTicketType()) ? Constants.SEASON_TICKET : Constants.SHOW_TICKET;
    }


    /**
     * 校验:
     * 1.参数
     * 2.订单必须已经支付
     * 3.订单必须为虚拟订单
     * 4.订单没有分配过票
     *
     * @param uid
     * @param orderCode
     * @return
     */
    private Orders checkAndGetOrder(int uid, long orderCode) {

        if (isInvalidParam(uid, orderCode)) {
            logger.info("invalid param,uid is {},order code is {}", uid, orderCode);
            return null;
        }

        Orders order = ordersMapper.selectByCodeAndUid(String.valueOf(orderCode), String.valueOf(uid));
        if (order == null) {
            logger.warn("not find order by order code {},uid {}", orderCode, uid);
            return null;
        }

        //2.判断是否已经支付
        if (isNotPaidOrder(order)) {
            logger.warn("order is not paid,order code is {}", order.getOrderCode());
            return null;
        }

        if (isNotTicketOrder(order)) {
            logger.warn("order is not ticket,order code is {}", order.getOrderCode());
            return null;
        }

        if (isAssignTicket(order.getOrderCode())) {
            logger.warn("issue ticket fail,orderCode:{},you has buy the ticket .", orderCode);
            throw new ServiceException(ServiceError.SHOPPING_TICKET_HAS_BUY);
        }

        return order;
    }

    private boolean isInvalidParam(int uid, long orderCode) {
        return !isValidParam(uid, orderCode);
    }

    private boolean isValidParam(int uid, long orderCode) {
        return uid > 0 && orderCode > 0;
    }

    private boolean isNotPaidOrder(Orders order) {
        return !"Y".equals(order.getPaymentStatus());
    }

    /**
     * 非虚拟订单
     *
     * @param order
     * @return
     */
    private boolean isNotTicketOrder(Orders order) {
        return Constants.ATTRIBUTE_VIRTUAL != order.getAttribute();
    }

    /**
     * 订单是否已经分配过票
     *
     * @param orderCode
     * @return
     */
    private boolean isAssignTicket(long orderCode) {
        int count = yohoodTicketsMapper.selectCountByOrderCode(orderCode);
        return count > 0;
    }


    @Transactional
    @Service
    public static class TicketIssueService {

        @Autowired
        private YohoodTicketInfoMapper yohoodTicketInfoMapper;

        @Autowired
        private YohoodTicketsMapper yohoodTicketsMapper;

        @Autowired
        private IYohoodSeatDAO yohoodSeatDAO;

        @Database(DataSource = "yohood")
        public void doIssueTicket(Orders order, List<OrdersGoods> goodsList, String ticketType) {

            Assert.notNull(order, "order must not be null");
            Assert.notEmpty(goodsList, "goods list must not be empty");

            for (OrdersGoods ordersGoods : goodsList) {
                //票的生效时间,如 9月16日 13:30 - 16:00
                Pair<Date, Date> beginAndEndDatePair = parseBeginAndEndDate(ordersGoods.getColorName());

                if (beginAndEndDatePair == null || beginAndEndDatePair.getKey() == null || beginAndEndDatePair.getValue() == null) {
                    logger.warn("ticket product skc color name format is error, color name is {},order goods is {}", ordersGoods.getColorName(), ordersGoods);
                    throw new ServiceException(ServiceError.ORDER_TICKET_COLORNAME_FORMAT_IS_RIGHT);
                }

                loopIssueTicket(order.getUid(), order.getOrderCode(), beginAndEndDatePair.getKey(), beginAndEndDatePair.getValue(), ordersGoods.getSizeName(), ordersGoods.getNum(), ticketType);
            }
        }


        /**
         * @param dateStr 如 9月16日 13:30 - 16:00
         * @return 解析程9.16 13:30  9.16 16:00
         */
        private static Pair<Date, Date> parseBeginAndEndDate(String dateStr) {
            if (StringUtils.isEmpty(dateStr)) {
                return null;
            }

            int index = dateStr.indexOf("-");
            if (index < 1) {
                return null;
            }

            Date beginDate = null;
            Date endDate = null;

            try {
                String beginDateStr = dateStr.substring(0, index).trim();
                String endDateStr = dateStr.substring(index + 1).trim();

                beginDate = DateUtil.parse(beginDateStr, DATE_FORMAT);

                endDateStr = DateUtil.format(beginDate, DateUtil.yyyy_MM_dd) + " " + endDateStr;
                endDate = DateUtil.parseDateFormat(endDateStr, DATE_FORMAT);

                return Pair.of(beginDate, endDate);

            } catch (Exception ex) {
                logger.warn("parse {} to begin date and end date error", dateStr, ex);
            }

            return null;
        }

        private void loopIssueTicket(int uid, long orderCode, Date beginDate, Date endDate, String area, int ticketNum, String ticketType) {
            for (int i = 0; i < ticketNum; i++) {

                //1.分配一个票
                YohoodTickets yohoodTickets = bindOrderAndTicket(uid, orderCode, beginDate, endDate);

                //2.套票分配一个座位
                bindTicketAndSeatIfNessary(ticketType, area, yohoodTickets);
            }
        }

        /**
         * 绑定订单和电子票的关系
         *
         * @param orderCode
         * @param uid
         * @param beginDate
         * @return
         */
        private YohoodTickets bindOrderAndTicket(int uid, Long orderCode, Date beginDate, Date endDate) throws ServiceException {

            logger.info("plan to bind one ticket for order,uid is {},order code is {},begin date is {},end date is {}",
                    uid, orderCode, beginDate, endDate);
            /**
             * 获取一个可用的票
             */
            YohoodTicketInfo validateTicketInfo = getOneValidateTicket();

            if (validateTicketInfo == null) {
                logger.info("not find validate ticket");
                throw new ServiceException(ServiceError.ORDER_TICKET_SOLD_OUT);
            }

            logger.info("get one validate ticket,ticket info is {}", validateTicketInfo);

            boolean isNotBind = isNotBindOrder(validateTicketInfo);
            if (!isNotBind) {
                logger.info("ticket was bind,ticket info is {}", validateTicketInfo);
                throw new ServiceException(ServiceError.ORDER_TICKET_SOLD_OUT);
            }

            YohoodTickets yohoodTickets = createYohoodTickets(uid, orderCode, beginDate, endDate, validateTicketInfo);

            boolean success = doBindOrderAndTicket(yohoodTickets);

            if (!success) {
                logger.info("bind order and ticket error,order code is {},ticket info is {}", orderCode, validateTicketInfo);
                throw new ServiceException(ServiceError.ORDER_TICKET_SOLD_OUT);
            }

            logger.info("bind one ticket for order success");

            return yohoodTickets;
        }

        private void bindTicketAndSeatIfNessary(String ticketType, String area, YohoodTickets yohoodTickets) throws ServiceException {
            logger.info("plan to bind ticket and seat,ticket type is {},area is {},ticket code {}", ticketType, area, yohoodTickets.getTicketCode());
            if (!Constants.SEASON_TICKET.equals(ticketType)) {
                logger.info("ticket is not season");
                return;
            }
            boolean success = bindTicketAndSeat(area, yohoodTickets);
            if (!success) {
                logger.warn("bind ticket and seat error,ticket type is {},area is {},ticket info is {}", ticketType, area, yohoodTickets);
                throw new ServiceException(ServiceError.ORDER_TICKET_SEAT_SOLD_OUT);
            }
            logger.info("bind ticket and seat success");
        }

        private boolean bindTicketAndSeat(String area, YohoodTickets yohoodTickets) {
            YohoodSeat seat = new YohoodSeat();
            seat.setTicketCode(yohoodTickets.getTicketCode());
            seat.setUid(yohoodTickets.getUid());
            seat.setBeginDate(yohoodTickets.getBeginDate());
            seat.setEndDate(yohoodTickets.getEndDate());
            seat.setArea(area);
            int n = yohoodSeatDAO.updateSeatToUse(seat);
            return n == 1;
        }


        /**
         * 获取一个可用的票,返回null表示票无效,要么没有票了,要么票在查询的过程中别别人占用了
         *
         * @return
         */
        public YohoodTicketInfo getOneValidateTicket() {
            String employ_code = UUID.randomUUID().toString();
            YohoodTicketInfo ticketInfo = null;
            for (int tryTimes = 0; tryTimes < 5; tryTimes++) {
                ticketInfo = yohoodTicketInfoMapper.selectByStatusAndEmployCode(1, "0");
                if (ticketInfo == null) {
                    return null;
                }
                ticketInfo.setEmployCode(employ_code);
                ticketInfo.setStatus((short) 2);
                int n = yohoodTicketInfoMapper.updateByPrimaryKeySelective(ticketInfo);
                if (n < 1) {
                    continue;
                }

                return ticketInfo;
            }

            return null;
        }

        /**
         * 是否已经绑定
         *
         * @param ticketInfo
         * @return
         */
        private boolean isNotBindOrder(YohoodTicketInfo ticketInfo) {
            YohoodTickets yohoodTickets = yohoodTicketsMapper.selectByStatusAndTicketCode((short) 1, ticketInfo.getTicketCode());
            if (yohoodTickets == null) {
                return true;
            }
            return false;
        }

        private YohoodTickets createYohoodTickets(int uid, long orderCode, Date beginDate, Date endDate, YohoodTicketInfo ticketInfo) {
            YohoodTickets yohoodTickets = new YohoodTickets();
            yohoodTickets.setTicketCode(ticketInfo.getTicketCode());
            yohoodTickets.setOrderCode(orderCode);
            yohoodTickets.setUid(uid);
            yohoodTickets.setStatus((short) 1);
            yohoodTickets.setBeginDate(beginDate);
            yohoodTickets.setEndDate(endDate);
            yohoodTickets.setIsSign("N");
            yohoodTickets.setSignTime(0);
            yohoodTickets.setCreateTime((int) (System.currentTimeMillis() / 1000));

            return yohoodTickets;
        }

        private boolean doBindOrderAndTicket(YohoodTickets yohoodTickets) {

            int n = yohoodTicketsMapper.insertSelective(yohoodTickets);
            return n == 1;
        }

    }

    /**
     * 插入待评价记录
     */
    @Override
    public void addOrderGoodsToComment(Orders order) {
        List<Integer> productIds = new ArrayList<>();
        List<OrdersGoods> goods = ordersGoodsMapper.selectOrderGoodsByOrder(order);
        for (OrdersGoods orderGoods : goods) {
            productIds.add(orderGoods.getProductId());
        }
        CommentRecordAddReq request = new CommentRecordAddReq();
        request.setUid(order.getUid());
        request.setOrderCode(order.getOrderCode());
        request.setProductIds(productIds);
        logger.debug("call service {} to add comment for order code {},it include productIds are {}", "sns.addCommentRecordList", order.getOrderCode(), productIds);
        try {
            serviceCaller.asyncCall("sns.addCommentRecordList", request, Integer.class);
        } catch (Exception ex) {
            logger.warn("addOrderGoodsToComment error {} .", ex);
        }
    }
}
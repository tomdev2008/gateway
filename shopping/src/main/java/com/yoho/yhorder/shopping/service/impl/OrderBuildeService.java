package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.ShoppingDeliveryAddress;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.service.model.request.UserAddressReqBO;
import com.yoho.service.model.response.AreaRspBo;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.common.model.VirtualInfo;
import com.yoho.yhorder.dal.model.SysConfig;
import com.yoho.yhorder.shopping.cache.SysConfigCacheService;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.model.Order;
import com.yoho.yhorder.shopping.model.OrderReceiver;
import com.yoho.yhorder.shopping.service.ExternalDegradeService;
import com.yoho.yhorder.shopping.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by wujiexiang on 16/5/24.
 */
@Component
public class OrderBuildeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ServiceCaller serviceCaller;

    @Autowired
    private SysConfigCacheService sysConfigCacheService;

    @Autowired
    private ExternalDegradeService externalDegradeService;

    public Order build(ShoppingSubmitRequest request, ChargeContext chargeContext) {
        OrderBuilder orderBuilder = new OrderBuilder()
                .addBasicInfo(request)
                .addDeliveryWay(request)
                .addGoodsList(chargeContext)
                .addShoppingItems(chargeContext)
                .addChargeResult(chargeContext)
                .addPromotionInfoList(chargeContext);

        ChargeParam chargeParam = chargeContext.getChargeParam();

        if (Constants.TICKET_CHARGE_TYPE.equals(chargeParam.getChargeTypeName())) {
            buildVirtaulInfo(request, chargeContext, orderBuilder);
        } else {
            //非虚拟商品必须有地址
            addUserAddressRspBOAndReceiver(request, orderBuilder);
        }

        Order order = orderBuilder.build();
        return order;
    }


    private void addUserAddressRspBOAndReceiver(ShoppingSubmitRequest request, OrderBuilder orderBuilder) {
        int uid = request.getUid();
        String saleChannel = request.getSale_channel();
        Integer addressId = request.getAddress_id();
        OrderReceiver receiver = null;
        UserAddressRspBO userAddressRspBO = null;
        if (OrderReceiverParser.isYohoodSite(saleChannel, addressId)) {
            receiver = new OrderReceiver();
            receiver.setAddress("上海市长宁区兴义路99号,YOHOOD现场");
            receiver.setProvince("上海");
            receiver.setCity("上海");
            receiver.setDistrict("长宁区");
        } else {
            userAddressRspBO = queryUserAddressIfNull(uid, addressId,request.getDelivery_address());
            //判断收货地址
            if (userAddressRspBO == null || MyStringUtils.isEmpty(userAddressRspBO.getAreaCode())) {
                throw new ServiceException(ServiceError.SHOPPING_ORDER_ADDRESS_IS_NULL);
            }
            receiver = OrderReceiverParser.parseUserAddressRspBO(userAddressRspBO);
        }

        receiver.setShippingManner(request.getDelivery_way());

        orderBuilder.addUserAddressRspBO(userAddressRspBO);
        orderBuilder.addReceiver(receiver);
    }


    /**
     * 查询用户地址
     * @param uid
     * @param addressId
     * @param deliveryAddress
     * @return
     * @throws ServiceException
     */
    private UserAddressRspBO queryUserAddressIfNull(int uid, Integer addressId, ShoppingDeliveryAddress deliveryAddress) throws ServiceException {

        /**
         * 获取用户地址
         * 先根据addressId获取,若获取服务异常并且开启降级服务,则使用deliveryAddress地址
         */

        MyAssert.isTrue((addressId == null && deliveryAddress == null), ServiceError.SHOPPING_ORDER_ADDRESS_IS_NULL);
        MyAssert.isTrue((addressId < 1 && deliveryAddress == null), ServiceError.SHOPPING_ORDER_ADDRESS_IS_NULL);
        if (OrderReceiverParser.isValidAddressId(addressId)) {
            return externalDegradeService.getUserAddressByUidAndAddressId(uid, addressId, transToUserAddressRspBO(deliveryAddress));
        } else {
            logger.warn("get user address from ShoppingDeliveryAddress param,uid is {},deliveryAddress is {}", uid, deliveryAddress);
            return transToUserAddressRspBO(deliveryAddress);
        }
    }

    private UserAddressRspBO transToUserAddressRspBO(ShoppingDeliveryAddress deliveryAddress) {
        if (deliveryAddress == null) {
            return null;
        }
        UserAddressRspBO defaultValue = new UserAddressRspBO();
        defaultValue.setAddress(deliveryAddress.getAddress());
        defaultValue.setAddresseeName(deliveryAddress.getConsignee());
        defaultValue.setAreaCode(deliveryAddress.getArea_code());
        defaultValue.setEmail(deliveryAddress.getEmail());
        defaultValue.setId(deliveryAddress.getAddress_id());
        defaultValue.setMobile(deliveryAddress.getMobile());
        defaultValue.setPhone(deliveryAddress.getPhone());
        defaultValue.setZipCode(deliveryAddress.getZip_code());
        AreaRspBo areaRspBo = new AreaRspBo();
        defaultValue.setArea(areaRspBo);

        return defaultValue;
    }


    private UserAddressRspBO getUserAddressByUidAndAddressId(Integer uid, Integer addressId) {
        logger.debug("call service {} for uid {}", ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL, uid);
        UserAddressReqBO userAddressReqBO = new UserAddressReqBO();
        userAddressReqBO.setUid(uid);
        userAddressReqBO.setId(addressId);
        UserAddressRspBO userAddressBO = serviceCaller.call(ShoppingConfig.USERS_QUERY_ADDRESS_REST_URL, userAddressReqBO, UserAddressRspBO.class);
        if (userAddressBO != null) {
            return userAddressBO;
        }
        return null;
    }

    private void buildVirtaulInfo(ShoppingSubmitRequest request, ChargeContext chargeContext, OrderBuilder orderBuilder) {
        //虚拟商品下单
        bindMobile(request.getMobile(), orderBuilder);

        bindVirtualInfo(chargeContext, orderBuilder);

    }

    /**
     * 只需要手机号码
     * @param mobile
     * @param orderBuilder
     */
    private void bindMobile(String mobile, OrderBuilder orderBuilder) {
        OrderReceiver receiver = new OrderReceiver();
        receiver.setMobile(mobile);
        orderBuilder.addReceiver(receiver);
    }

    private void bindVirtualInfo(ChargeContext chargeContext, OrderBuilder orderBuilder) {
        VirtualInfo virtualInfo = new VirtualInfo();
        virtualInfo.setVirtutalType(Constants.VIRTUAL_TYPE_TICKET);
        virtualInfo.setTicketType(checkAndGetTicketType(chargeContext));

        orderBuilder.addVirtualInfo(virtualInfo);
    }

    private String checkAndGetTicketType(ChargeContext chargeContext) {
        List<ChargeGoods> mainGoods = chargeContext.getMainGoods();
        String ticketType = null;
        if (CollectionUtils.isNotEmpty(mainGoods)) {
            for (ChargeGoods chargeGoods : mainGoods) {
                int skn = Integer.parseInt(chargeGoods.getShoppingGoods().getProduct_skn());
                ticketType = findTicketType(skn);
                if (StringUtils.isEmpty(ticketType)) {
                    logger.warn("not find ticket type,skn is {}", skn);
                } else {
                    break;
                }
            }
        }

        if (ticketType == null) {
            logger.warn("ticket type is null");
            throw new ServiceException(ServiceError.SHOPPING_TICKET_SYSCONFIG_IS＿NULL);
        }

        return ticketType;

    }

    private String findTicketType(int skn) {
        SysConfig sysConfig = sysConfigCacheService.getTicketConfigOfThisYear();
        if (sysConfig == null) {
            return null;
        }
        try {
            JSONObject configValueJson = JSONObject.parseObject(sysConfig.getConfigValue());
            if (configValueJson != null) {
                JSONArray array = configValueJson.getJSONArray("show");
                if (array != null && array.contains(skn)) {
                    //展览票
                    return Constants.SHOW_TICKET;
                }

                array = configValueJson.getJSONArray("season");
                if (array != null && array.contains(skn)) {
                    //套票
                    return Constants.SEASON_TICKET;
                }
            }
        } catch (Exception ex) {
            logger.warn("get ticket type error", ex);
        }
        return null;
    }
}
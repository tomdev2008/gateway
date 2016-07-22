package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.model.CustomerServiceAddressBO;
import com.yoho.service.model.order.request.DeliveryAddressRequest;
import com.yoho.service.model.order.response.Orders;
import com.yoho.service.model.request.AreaReqBO;
import com.yoho.service.model.request.UserAddressReqBO;
import com.yoho.service.model.response.AreaRspBo;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.common.convert.BeanConvert;
import com.yoho.yhorder.common.utils.YHStringUtils;
import com.yoho.yhorder.dal.*;
import com.yoho.yhorder.dal.domain.CustomerServiceAddress;
import com.yoho.yhorder.dal.domain.DeliveryAddress;
import com.yoho.yhorder.dal.domain.OrdersProcessStatus;
import com.yoho.yhorder.order.config.ServerURL;
import com.yoho.yhorder.order.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * qianjun 2016/6/14
 */
@Service
public class DeliveryAddressServiceImpl implements IDeliveryAddressService {

    @Autowired
    private IOrdersMapper ordersMapper;

    @Autowired
    private IOrderDefaultPreferencesDAO orderDefaultPreferencesDAO;

    @Autowired
    private ServiceCaller serviceCaller;

    @Autowired
    @Qualifier("mqErpService")
    private IErpService mqErpService;


    @Autowired
    private IOrdersDeliveryAddressRepository ordersDeliveryAddressService;

    @Autowired
    private IOrdersProcessStatusService ordersStatusService;


    private Logger logger = LoggerFactory.getLogger(DeliveryAddressServiceImpl.class);

    /**
     * 客服修改收货人地址，批量更新订单表中订单
     */
    @Override
    public void updateBatchDeliveryAddress(JSONArray jsonArray) {
        int size = jsonArray.size();
        List<CustomerServiceAddressBO> customerServiceAddressBOs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            CustomerServiceAddressBO customerServiceAddressBO = new CustomerServiceAddressBO();
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            customerServiceAddressBO.setOrderCode(jsonObject.getLongValue("order_code"));
            customerServiceAddressBO.setUserName(jsonObject.getString("user_name"));
            customerServiceAddressBO.setMobile(jsonObject.getString("mobile"));
            customerServiceAddressBO.setPhone(jsonObject.getString("phone"));
            customerServiceAddressBO.setAreaCode(jsonObject.getString("area_code"));
            customerServiceAddressBO.setProvince(jsonObject.getString("province"));
            customerServiceAddressBO.setCity(jsonObject.getString("city"));
            customerServiceAddressBO.setDistrict(jsonObject.getString("district"));
            customerServiceAddressBO.setAddress(jsonObject.getString("address"));
            customerServiceAddressBO.setZipCode(jsonObject.getString("zip_code"));
            customerServiceAddressBO.setIsPreContact(jsonObject.getString("is_pre_contact"));
            customerServiceAddressBO.setIsInvoice(jsonObject.getString("is_invoice"));
            customerServiceAddressBO.setInvoicesType(jsonObject.getString("invoices_type"));
            customerServiceAddressBO.setInvoicesPayable(jsonObject.getString("invoices_payable"));
            customerServiceAddressBO.setPaymentType(jsonObject.getIntValue("payment_type"));
            customerServiceAddressBOs.add(customerServiceAddressBO);
        }
        if (CollectionUtils.isNotEmpty(customerServiceAddressBOs)) {
            List<CustomerServiceAddress> customerServiceAddresses = getCustomerServiceAddresses(customerServiceAddressBOs);
            ordersMapper.updateBatchByOrderCodeSelective(customerServiceAddresses);
        }
    }

    private List<CustomerServiceAddress> getCustomerServiceAddresses(List<CustomerServiceAddressBO> customerServiceAddressBOs) {
        List<CustomerServiceAddress> customerServiceAddresses = new ArrayList<>();
        for (CustomerServiceAddressBO customerServiceAddressBO : customerServiceAddressBOs) {
            CustomerServiceAddress customerServiceAddress = new CustomerServiceAddress();
            customerServiceAddress.setOrderCode(customerServiceAddressBO.getOrderCode());
            customerServiceAddress.setUserName(customerServiceAddressBO.getUserName());
            customerServiceAddress.setMobile(customerServiceAddressBO.getMobile());
            customerServiceAddress.setPhone(customerServiceAddressBO.getPhone());
            customerServiceAddress.setAreaCode(Integer.parseInt(customerServiceAddressBO.getAreaCode()));
            customerServiceAddress.setProvince(customerServiceAddressBO.getProvince());
            customerServiceAddress.setCity(customerServiceAddressBO.getCity());
            customerServiceAddress.setDistrict(customerServiceAddressBO.getDistrict());
            customerServiceAddress.setAddress(customerServiceAddressBO.getAddress());
            customerServiceAddress.setZipCode(Integer.parseInt(customerServiceAddressBO.getZipCode()));
            customerServiceAddress.setIsPreContact(customerServiceAddressBO.getIsPreContact());
            customerServiceAddress.setIsInvoice(customerServiceAddressBO.getIsInvoice());
            customerServiceAddress.setInvoicesType(customerServiceAddressBO.getInvoicesType());
            customerServiceAddress.setInvoicesPayable(customerServiceAddressBO.getInvoicesPayable());
            customerServiceAddress.setPaymentType(new Integer(customerServiceAddressBO.getPaymentType()).byteValue());
            customerServiceAddresses.add(customerServiceAddress);
        }
        return customerServiceAddresses;
    }


    /**
     * 修改订单收货地址
     */
    @Override
    public void updateDeliveryAddress(DeliveryAddressRequest deliveryAddressRequest) {
        logger.info("updateDeliveryAddress by orderCode {} and deliveryAddress is {}.", deliveryAddressRequest.getOrderCode(), deliveryAddressRequest);
        // 验证订单号
        if (StringUtils.isBlank(deliveryAddressRequest.getOrderCode()) || Long.valueOf(deliveryAddressRequest.getOrderCode()) < 1) {
            logger.warn("updateDeliveryAddress fail, request orderCode {} is empty.", deliveryAddressRequest.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_ORDER_CODE_IS_EMPTY);
        }
        // 获取订单
        Orders orders = ordersMapper.selectByOrderCode(deliveryAddressRequest.getOrderCode());
        // 验证订单参数
        validateOrderParameter(orders);
        List<Orders> otherSubOrderses = getOtherSubOrderses(orders);
        List<OrdersProcessStatus> subOrdersProcessStatuses = ordersStatusService.select(otherSubOrderses);
        for (OrdersProcessStatus subOrdersProcessStatus : subOrdersProcessStatuses) {
            // 验证订单是否处于已发货状态
            if (subOrdersProcessStatus.getValue() >= 3) {
                logger.warn("updateDeliveryAddress fail, the order {} has been delivered, JIT split order or advance goods", orders.getOrderCode());
                throw new ServiceException(ServiceError.ORDER_HAS_BEEN_DELIVERED);
            }
        }
        DeliveryAddress deliveryAddress = getDeliveryAddress(deliveryAddressRequest, orders);
        // JIT拆单和预售商品拆单
        if (!otherSubOrderses.isEmpty()) {
            logger.info("updateDeliveryAddress by orderCode {} update delivery address by parentOrderCode {}.", orders.getOrderCode(), orders.getParentOrderCode());
            deliveryAddress.setParentOrderCode(orders.getParentOrderCode());
            ordersMapper.updateByParentOrderCodeSelective(deliveryAddress);
            ordersDeliveryAddressService.update(otherSubOrderses, deliveryAddress);
            logger.info("updateDeliveryAddress by orderCode {} update delivery address by parentOrderCode {} to db success.", orders.getOrderCode(), orders.getParentOrderCode());
            JSONArray messages = new JSONArray();
            for (Orders otherSubOrders : otherSubOrderses) {
                deliveryAddress.setOrderCode(otherSubOrders.getOrderCode());
                deliveryAddress.setParentOrderCode(otherSubOrders.getParentOrderCode());
                messages.add(createDeliveryAddressMessage(deliveryAddress));
            }
            logger.info("updateDeliveryAddress by orderCode {} update delivery address by parentOrderCode {} to erp success.", orders.getOrderCode(), orders.getParentOrderCode());
            // 修改父订单收货地址
            Orders parentOrder = ordersMapper.selectByOrderCode(orders.getParentOrderCode().toString());
            if (parentOrder != null) {
                ordersDeliveryAddressService.update(parentOrder, deliveryAddress);
                deliveryAddress.setOrderCode(parentOrder.getOrderCode());
                deliveryAddress.setParentOrderCode(null);
                messages.add(createDeliveryAddressMessage(deliveryAddress));
            }
            mqErpService.updateDeliveryAddress(messages);
        } else {
            JSONArray messages = new JSONArray();
            logger.info("updateDeliveryAddress by orderCode {} update delivery address.", orders.getOrderCode());
            ordersMapper.updateByOrderCodeSelective(deliveryAddress);
            ordersDeliveryAddressService.update(orders, deliveryAddress);
            logger.info("updateDeliveryAddress by orderCode {} update delivery address to db success.", orders.getOrderCode());
            // 发送修改收货地址给MQ
            messages.add(createDeliveryAddressMessage(deliveryAddress));
            logger.info("updateDeliveryAddress by orderCode {} update delivery address to erp success.", orders.getOrderCode());
            // 修改子订单收货地址
            updateSubOrdersesDeliveryAddress(orders, deliveryAddress, messages);
            mqErpService.updateDeliveryAddress(messages);
        }
        logger.info("updateDeliveryAddress by orderCode {} and addressId is {} success.", deliveryAddressRequest.getOrderCode(), deliveryAddressRequest.getAddressId());
    }

    private DeliveryAddress getDeliveryAddress(DeliveryAddressRequest deliveryAddressRequest, Orders orders) {
        DeliveryAddress deliveryAddress = new DeliveryAddress();
        if (StringUtils.isNotEmpty(deliveryAddressRequest.getAreaCode())) {
            int areaCode = Integer.parseInt(deliveryAddressRequest.getAreaCode());
            AreaRspBo district = queryAreaRspBo(areaCode);
            logger.info("updateDeliveryAddress by district  {}.", district);
            AreaRspBo city = district.getParent();
            AreaRspBo province = city.getParent();
            // 校验修改地址时是否支持顺丰快递
            if (orders.getShippingTypeId() == 2 && "N".equals(district.getIsSupport())) {
                logger.warn("updateDeliveryAddress fail, delivery address does not support SF express transport , areaCode {}", deliveryAddressRequest.getAreaCode());
                throw new ServiceException(ServiceError.ORDER_DELIVERY_ADDRESSID_NOT_SUPPORT_SF_EXPRESS_TRANSPORT);
            }
            deliveryAddress.setOrderCode(orders.getOrderCode());
            deliveryAddress.setUserName(deliveryAddressRequest.getUserName());
            deliveryAddress.setAreaCode(areaCode);
            deliveryAddress.setProvince(province.getCaption());
            deliveryAddress.setCity(city.getCaption());
            deliveryAddress.setDistrict(district.getCaption());
            deliveryAddress.setAddress(deliveryAddressRequest.getAddress());
            deliveryAddress.setPhone(deliveryAddressRequest.getPhone());
            deliveryAddress.setMobile(deliveryAddressRequest.getMobile());
            deliveryAddress.setDeliveryAddressUpdateTimes(1);
        } else {
            // 根据addressId获取用户地址详细信息
            int addressId = Integer.parseInt(deliveryAddressRequest.getAddressId());
            UserAddressRspBO userAddressRspBO = queryAddressInfo(addressId);
            logger.info("updateDeliveryAddress by userAddressRspBO {}.", userAddressRspBO);
            AreaRspBo district = userAddressRspBO.getArea();
            AreaRspBo city = district.getParent();
            AreaRspBo province = city.getParent();
            // 校验修改地址时是否支持顺丰快递
            if (orders.getShippingTypeId() == 2 && "N".equals(district.getIsSupport())) {
                logger.warn("updateDeliveryAddress fail, delivery address does not support SF distribution area , areaCode {}", userAddressRspBO.getAreaCode());
                throw new ServiceException(ServiceError.ORDER_DELIVERY_ADDRESSID_NOT_SUPPORT_SF_DISTRIBUTION_AREA);
            }
            deliveryAddress.setOrderCode(orders.getOrderCode());
            deliveryAddress.setAddressId(Integer.valueOf(deliveryAddressRequest.getAddressId()));
            deliveryAddress.setUserName(userAddressRspBO.getAddresseeName());
            deliveryAddress.setPhone(userAddressRspBO.getPhone());
            deliveryAddress.setMobile(userAddressRspBO.getMobile());
            deliveryAddress.setAreaCode(Integer.valueOf(userAddressRspBO.getAreaCode()));
            deliveryAddress.setProvince(province.getCaption());
            deliveryAddress.setCity(city.getCaption());
            deliveryAddress.setDistrict(district.getCaption());
            deliveryAddress.setAddress(userAddressRspBO.getAddress());
            try {
                deliveryAddress.setZipCode(StringUtils.isBlank(userAddressRspBO.getZipCode()) || "null".equals(userAddressRspBO.getZipCode()) ? null : Integer.parseInt(userAddressRspBO.getZipCode()));
            } catch (Exception e) {
                logger.warn("updateDeliveryAddress ZipCode fail,ZipCode exist not numerical character uid is {}.", orders.getUid());
                deliveryAddress.setZipCode(0);
            }
            deliveryAddress.setEmail(userAddressRspBO.getEmail());
            deliveryAddress.setDeliveryAddressUpdateTimes(1);
            // 保存或更新默认地址
            saveOrdersDefaultPreferences(userAddressRspBO, orders, deliveryAddress);
        }
        deliveryAddress.setUserName(YHStringUtils.truncateAndFilterEmojiCharacter(deliveryAddress.getUserName(), Orders.USER_NAME_MAX_LENGTH));
        return deliveryAddress;
    }

    private AreaRspBo queryAreaRspBo(int areaCode) {
        AreaReqBO areaReqBO = new AreaReqBO();
        areaReqBO.setCode(areaCode);
        AreaRspBo district = serviceCaller.call("users.getAreaByCode", areaReqBO, AreaRspBo.class);
        if (district == null) {
            logger.warn("updateDeliveryAddress fail, can not find AreaRspBo district by areaCode {}", areaCode);
            throw new ServiceException(ServiceError.ORDER_THE_REGION_NOT_EXIST);
        }
        AreaRspBo city = district.getParent();
        if (city == null) {
            logger.warn("updateDeliveryAddress fail, can not find AreaRspBo city by areaCode {}", areaCode);
            throw new ServiceException(ServiceError.ORDER_THE_REGION_NOT_EXIST);
        }
        AreaRspBo province = city.getParent();
        if (province == null) {
            logger.warn("updateDeliveryAddress fail, can not find AreaRspBo province by areaCode {}", areaCode);
            throw new ServiceException(ServiceError.ORDER_THE_REGION_NOT_EXIST);
        }
        return district;
    }

    private void updateSubOrdersesDeliveryAddress(Orders orders, DeliveryAddress deliveryAddress, JSONArray messages) {
        logger.info("updateDeliveryAddress by orderCode {} update sub orders delivery address.", orders.getOrderCode());
        List<Orders> subOrderses = ordersMapper.selectByParentOrderCode(orders.getOrderCode().toString());
        if (subOrderses.isEmpty()) {
            logger.info("updateDeliveryAddress by orderCode {} update sub orders delivery address success.", orders.getOrderCode());
            return;
        }
        deliveryAddress.setParentOrderCode(orders.getOrderCode());
        ordersMapper.updateByParentOrderCodeSelective(deliveryAddress);
        ordersDeliveryAddressService.update(subOrderses, deliveryAddress);
        logger.info("updateDeliveryAddress by orderCode {} update sub orders delivery address to db success.", orders.getOrderCode());
        // 修改收货地址给ERP
        for (Orders subOrders : subOrderses) {
            deliveryAddress.setOrderCode(subOrders.getOrderCode());
            deliveryAddress.setParentOrderCode(subOrders.getParentOrderCode());
            messages.add(createDeliveryAddressMessage(deliveryAddress));
        }
        logger.info("updateDeliveryAddress by orderCode {} update sub orders delivery address to erp success.", orders.getOrderCode());
    }

    private List<Orders> getOtherSubOrderses(Orders orders) {
        if (orders.getParentOrderCode() != null && orders.getParentOrderCode() > 0) {
            return ordersMapper.selectByParentOrderCode(orders.getParentOrderCode().toString());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 验证订单参数
     */
    private void validateOrderParameter(Orders orders) {
        if (orders == null) {
            logger.warn("updateDeliveryAddress fail , the order does not exist");
            throw new ServiceException(ServiceError.ORDER_NOT_EXIST);
        }
        // 验证订单未支付但是已经取消
        if ("Y".equals(orders.getIsCancel())) {
            logger.warn("updateDeliveryAddress fail, the order {} has been cancelled", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_THE_ORDER_HAS_BEEN_CANCELLED);
        }
        // 验证订单是否被删除
        if (orders.getOrdersStatus() != 1) {
            logger.warn("updateDeliveryAddress fail, the order {} has been removed", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_HAS_BEEN_REMOVED);
        }
        // 验证订单是否处于已发货状态
        OrdersProcessStatus ordersProcessStatus = ordersStatusService.select(orders);
        if (ordersProcessStatus.getValue() >= 3) {
            logger.warn("updateDeliveryAddress fail, the order {} has been delivered", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_HAS_BEEN_DELIVERED);
        }
        DeliveryAddress ordersDeliveryAddress = ordersDeliveryAddressService.select(orders);
        // 验证收货地址是否已经修改一次
        if (ordersDeliveryAddress.getDeliveryAddressUpdateTimes() != null && ordersDeliveryAddress.getDeliveryAddressUpdateTimes() == 1) {
            logger.warn("updateDeliveryAddress fail, the order {} delivery address can modify only 1 times", orders.getOrderCode());
            throw new ServiceException(ServiceError.ORDER_MODIFY_ONE_TIMES_DELIVERY_ADDRESS);
        }
    }

    /**
     * 根据addressId获取用户地址详细信息
     */
    private UserAddressRspBO queryAddressInfo(int addressId) {
        logger.info("call users.getAddress service by addressId {}", addressId);
        UserAddressReqBO userAddressReqBO = new UserAddressReqBO();
        userAddressReqBO.setId(addressId);
        UserAddressRspBO userAddressRspBO = serviceCaller.call(ServerURL.USERS_QUERY_ADDRESS_REST_URL, userAddressReqBO, UserAddressRspBO.class);
        AreaRspBo district = userAddressRspBO.getArea();
        if (district == null) {
            logger.warn("updateDeliveryAddress fail, can not find AreaRspBo district by areaCode {}", userAddressRspBO.getAreaCode());
            throw new ServiceException(ServiceError.ORDER_THE_REGION_NOT_EXIST);
        }
        AreaRspBo city = district.getParent();
        if (city == null) {
            logger.warn("updateDeliveryAddress fail, can not find AreaRspBo city by areaCode {}", userAddressRspBO.getAreaCode());
            throw new ServiceException(ServiceError.ORDER_THE_REGION_NOT_EXIST);
        }
        AreaRspBo province = city.getParent();
        if (province == null) {
            logger.warn("updateDeliveryAddress fail, can not find AreaRspBo province by areaCode {}", userAddressRspBO.getAreaCode());
            throw new ServiceException(ServiceError.ORDER_THE_REGION_NOT_EXIST);
        }
        return userAddressRspBO;
    }

    /**
     * 保存或更新默认地址
     */
    private void saveOrdersDefaultPreferences(UserAddressRspBO userAddressRspBO, Orders orders, DeliveryAddress deliveryAddress) {
        logger.info("updateDeliveryAddress by orderCode {} save DefaultPreferences.", orders.getOrderCode());
        JSONObject preferencesJSON = new JSONObject();
        if (userAddressRspBO != null) {
            JSONObject deliveryAddressJSON = new JSONObject();
            deliveryAddressJSON.put("id", userAddressRspBO.getId());
            deliveryAddressJSON.put("uid", userAddressRspBO.getUid());
            deliveryAddressJSON.put("addressee_name", deliveryAddress.getUserName());
            deliveryAddressJSON.put("address", deliveryAddress.getAddress());
            deliveryAddressJSON.put("area_code", deliveryAddress.getAreaCode());
            deliveryAddressJSON.put("zip_code", deliveryAddress.getZipCode());
            deliveryAddressJSON.put("mobile", deliveryAddress.getMobile());
            deliveryAddressJSON.put("phone", deliveryAddress.getPhone());
            deliveryAddressJSON.put("is_default", userAddressRspBO.getIsDefault());
            deliveryAddressJSON.put("email", deliveryAddress.getEmail());
            JSONObject areaNames = new JSONObject();
            areaNames.put("province", deliveryAddress.getProvince());
            areaNames.put("city", deliveryAddress.getCity());
            areaNames.put("county", deliveryAddress.getDistrict());
            areaNames.put("is_support", userAddressRspBO.getArea().getIs_support());
            areaNames.put("is_delivery", userAddressRspBO.getArea().getIs_delivery());
            deliveryAddressJSON.put("areaNames", areaNames);
            preferencesJSON.put("delivery_address", deliveryAddressJSON);
        } else {
            preferencesJSON.put("delivery_address", new JSONArray());
        }
        preferencesJSON.put("payment_type", orders.getPaymentType());
        preferencesJSON.put("shipping_manner", orders.getShippingTypeId());
        preferencesJSON.put("receiving_time", orders.getReceivingTime());
        preferencesJSON.put("uid", orders.getUid());
        orderDefaultPreferencesDAO.insertDefaultPreferences(orders.getUid(), preferencesJSON.toJSONString());
        logger.info("updateDeliveryAddress by orderCode {} save DefaultPreferences success.", orders.getOrderCode());
    }

    /**
     * 发送修改收货地址给MQ
     */
    private JSONObject createDeliveryAddressMessage(DeliveryAddress deliveryAddress) {
        JSONObject message = new JSONObject();
        message.put("order_code", deliveryAddress.getOrderCode());
        message.put("parent_order_code", deliveryAddress.getParentOrderCode());
        message.put("user_name", deliveryAddress.getUserName());
        message.put("phone", deliveryAddress.getPhone());
        message.put("mobile", deliveryAddress.getMobile());
        message.put("area_code", deliveryAddress.getAreaCode());
        message.put("province", deliveryAddress.getProvince());
        message.put("city", deliveryAddress.getCity());
        message.put("district", deliveryAddress.getDistrict());
        message.put("address", deliveryAddress.getAddress());
        message.put("zip_code", deliveryAddress.getZipCode());
        message.put("email", deliveryAddress.getEmail());
        message.put("address_id", deliveryAddress.getAddressId());
        return message;
    }


}

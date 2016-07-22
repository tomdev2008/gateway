package com.yoho.yhorder.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.core.rest.client.hystrix.AsyncFuture;
import com.yoho.core.rest.exception.ServiceNotAvaibleException;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.order.config.ServerURL;
import com.yoho.yhorder.order.model.ERPOrder;
import com.yoho.yhorder.order.service.IErpService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Created by yoho on 2015/11/27.
 */
@Service("erpService")
public class ErpServiceImpl implements IErpService {
    private static Logger logger = LoggerFactory.getLogger(ErpServiceImpl.class);

    @Value("${erp.apiBaseUrl:http://portal.admin.yohobuy.com}")
    private String apiBaseUrl;

    @Autowired
    private ServiceCaller serviceCaller;

    /**
     * 在ERP系统上创建换货订单
     *
     * @param changeOrder
     * @return
     */
    @Override
    public JSONObject createChangeOrder(JSONObject changeOrder) {
        try {
            LinkedMultiValueMap<String, Object> request = new LinkedMultiValueMap<String, Object>();
            request.add("data", changeOrder.toJSONString());
            logger.info(" req is {}", changeOrder.toJSONString());
            String responseText = serviceCaller.post("order.saveChangeOrderToErp", apiBaseUrl + ServerURL.ERP_SAVE_EXCHANGE_ORDER, request, String.class, null).get();
            logger.info(" createChangeOrder erp return  is {}", responseText);
            if (StringUtils.isNotEmpty(responseText)) {
                JSONObject response = JSONObject.parseObject(responseText);
                if (response.getIntValue("code") == 200) {
                    return (JSONObject) response.get("data");
                } else {
                    logger.warn(" createChangeOrder erp return result is {}", response);
                    ServiceException serviceException = new ServiceException(ServiceError.ORDER_SERVICE_ERROR);
                    serviceException.setParams(response.getString("message"));
                    throw serviceException;
                }
            } else {
                return new JSONObject();
            }

        } catch (RestClientException | JSONException | ServiceNotAvaibleException e) {
            logger.error("Erp SAVE change order fail is {}", e);
            throw new ServiceException(ServiceError.ORDER_CHANGE_APPLY_ERP_ERROR);
        }
    }

    @Override
    public void cancelOrder(JSONObject request) {
        //
    }

    @Override
    public void confirmOrder(JSONObject request, Map<String, Object> map) {
        //
    }

    /**
     * 在erp系统提交退货申请
     *
     * @param request
     */
    public JSONObject refundGoods(JSONObject request) {
        LinkedMultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("data", request.toJSONString());
        try {
            String responseText = serviceCaller.post("order.refundGoodsToErp", apiBaseUrl + "/api/returned/create", requestBody, String.class, null).get();
            JSONObject response;
            if (StringUtils.isNotEmpty(responseText)) {
                response = JSONObject.parseObject(responseText);
                if (response.getIntValue("code") != 200) {
                    String message = response.getString("message");
                    ServiceException serviceException = new ServiceException(ServiceError.ORDER_SUBMIT_CALL_ERP);
                    serviceException.setParams(message);
                    throw serviceException;
                }
            } else {
                throw new ServiceException(ServiceError.ORDER_REFUND_SUBMIT_FAIL);
            }
            return response;
        } catch (RestClientException e) {
            logger.error("Erp refund submit fail:", e);
            throw new ServiceException(ServiceError.ORDER_REFUND_SUBMIT_FAIL);
        } catch (JSONException e) {
            logger.error("Erp refund submit fail:", e);
            throw new ServiceException(ServiceError.ORDER_REFUND_SUBMIT_FAIL);
        }
    }

    /**
     * 在erp系统提交退货物流数据
     *
     * @param request
     */
    @Override
    public void setRefundExpressData(JSONObject request) {
        LinkedMultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("data", request.toJSONString());
        try {
            String responseText = serviceCaller.post("order.setRefundExpressDataToErp", apiBaseUrl + "/api/returned/express", requestBody, String.class, null).get();
            if (StringUtils.isNotEmpty(responseText)) {
                JSONObject response = JSONObject.parseObject(responseText);
                if (response.getIntValue("code") != 200) {
                    String message = response.getString("message");
                    ServiceException serviceException = new ServiceException(ServiceError.ORDER_SET_EXPRESS_CALL_ERP);
                    serviceException.setParams(message);
                    throw serviceException;
                }
            } else {
                throw new ServiceException(ServiceError.ORDER_REFUND_EXPRESS_DATA_FAIL);
            }
        } catch (RestClientException e) {
            logger.error("Erp set refund express fail:", e);
            throw new ServiceException(ServiceError.ORDER_REFUND_EXPRESS_DATA_FAIL);
        } catch (JSONException e) {
            logger.error("Erp set refund express fail:", e);
            throw new ServiceException(ServiceError.ORDER_REFUND_EXPRESS_DATA_FAIL);
        }
    }


    /**
     * 在erp系统提交提货物流数据
     *
     * @param express
     */
    @Override
    public void updateChangeOrderExpressInfo(Map<String, Object> express) {
        try {
            LinkedMultiValueMap<String, Object> request = new LinkedMultiValueMap<String, Object>();
            request.add("data", JSONObject.toJSONString(express));
            String responseText = serviceCaller.post("order.setChangeExpressDataToErp", apiBaseUrl + ServerURL.ERP_SAVE_EXCHANGE_EXPRESS, request, String.class, null).get();
            logger.info("erp return {}", responseText);
            if (StringUtils.isNotEmpty(responseText)) {
                JSONObject response = JSONObject.parseObject(responseText);
                if (response.getIntValue("code") != 200) {
                    logger.warn("erp return {},req is {}", responseText, JSON.toJSON(request));
                    throw new ServiceException(ServiceError.ORDER_SAVE_EXPRESS_FAIL);
                }
            } else {
                logger.warn("erp return  null !!!!");
                throw new ServiceException(ServiceError.ORDER_SAVE_EXPRESS_FAIL);
            }
        } catch (RestClientException e) {
            logger.error("Erp submit order status fail:", e);
            throw new ServiceException(ServiceError.ORDER_SAVE_EXPRESS_FAIL);
        } catch (JSONException e) {
            logger.error("Erp submit order status fail:", e);
            throw new ServiceException(ServiceError.ORDER_SAVE_EXPRESS_FAIL);
        }
    }

    /**
     * 修改订单收货地址
     *
     * @param request
     */
    @Override
    public void updateDeliveryAddress(JSONArray request) {

    }
}

package com.yoho.yhorder.order.restapi;

import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.model.ExpressCompanyBO;
import com.yoho.service.model.order.model.WaybillInfoBO;
import com.yoho.service.model.order.request.ExpressCompanyRequest;
import com.yoho.service.model.order.request.NewExpressRequest;
import com.yoho.service.model.order.request.WaybillInfoRequest;
import com.yoho.yhorder.order.service.IExpressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * qianjun
 * 2015/12/21.
 */
@Controller
@RequestMapping("/expressInfo")
@ServiceDesc(serviceName = "order")
public class ExpressController {

    private static final Logger logger = LoggerFactory.getLogger(ExpressController.class);

    @Autowired
    IExpressService expressService;

    /**
     * 获取物流公司列表
     */
    @RequestMapping("/getExpressCompany")
    @ResponseBody
    public List<ExpressCompanyBO> getExpressCompany(@RequestBody ExpressCompanyRequest expressCompanyRequest) {
        logger.info("get express company by status[{}]", expressCompanyRequest.getStatus());
        List<ExpressCompanyBO> expressCompanyBOList = expressService.getExpressCompanyList(expressCompanyRequest.getStatus());
        return expressCompanyBOList;
    }

    /**
     * 新退换货物流信息
     */
    @RequestMapping("/getNewExpress")
    @ResponseBody
    public Map<String, Object> getNewExpress(@RequestBody NewExpressRequest newExpressRequest) {
        logger.info("get new express by id[{}], uid[{}] and type[{}]", newExpressRequest.getId(), newExpressRequest.getUid(), newExpressRequest.getType());
        Map<String, Object> expressData = expressService.getNewExpress(newExpressRequest.getId(), newExpressRequest.getUid(), newExpressRequest.getType());
        if (expressData == null) {
            logger.warn("no Logistics Information");
            throw new ServiceException(ServiceError.ORDER_NO_LOGISTICS_COMPANY);
        }
        return expressData;
    }

    /**
     * 退货物流信息
     */
    @RequestMapping("/getRefundExpress")
    @ResponseBody
    public List<WaybillInfoBO> getRefundExpress(@RequestBody WaybillInfoRequest waybillInfoRequest) {
        logger.info("get refund express by logistics type[{}] and  waybillCode[{}]", waybillInfoRequest.getLogisticsType(), waybillInfoRequest.getWaybillCode());
        List<WaybillInfoBO> waybillInfoBOList = expressService.getExpressByTypeAndCode(waybillInfoRequest.getLogisticsType(), waybillInfoRequest.getWaybillCode());
        return waybillInfoBOList;
    }


}

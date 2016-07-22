package com.yoho.yhorder.order.service.impl;

import com.yoho.core.common.helpers.ImagesHelper;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.model.ExpressCompanyBO;
import com.yoho.service.model.order.model.WaybillInfoBO;
import com.yoho.yhorder.common.convert.BeanConvert;
import com.yoho.yhorder.common.utils.CalendarUtils;
import com.yoho.yhorder.dal.IChangeGoodsDao;
import com.yoho.yhorder.dal.IExpressCompanyDao;
import com.yoho.yhorder.dal.IRefundGoodsDao;
import com.yoho.yhorder.dal.IWaybillInfoDao;
import com.yoho.yhorder.dal.domain.ChangeGoodsMainInfo;
import com.yoho.yhorder.dal.model.ExpressCompany;
import com.yoho.yhorder.dal.model.RefundGoods;
import com.yoho.yhorder.dal.model.WaybillInfo;
import com.yoho.yhorder.order.service.IExpressService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * qianjun
 * 2015/11/27
 */
@Service("expressService")
public class ExpressServiceImpl implements IExpressService {
    private static final Logger logger = LoggerFactory.getLogger(ExpressServiceImpl.class);

    @Autowired
    IExpressCompanyDao expressCompanyDao;

    @Autowired
    private IRefundGoodsDao refundGoodsDao;

    @Autowired
    IWaybillInfoDao waybillInfoDao;

    @Autowired
    IChangeGoodsDao changeGoodsDao;

    @Autowired
    BeanConvert beanConvert;

    /**
     * 获取物流公司列表
     *
     * @param status
     */
    @Override
    public List<ExpressCompanyBO> getExpressCompanyList(Byte status) {
        logger.info("getExpressCompanyList by status [{}].", status);
        List<ExpressCompany> expressCompanyList = expressCompanyDao.selectAll(status);
        List<ExpressCompanyBO> expressCompanyBOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(expressCompanyList)) {
            beanConvert.convertFromBatch(expressCompanyList, expressCompanyBOList, ExpressCompanyBO.class);
        }
        return expressCompanyBOList;
    }

    /**
     * 新退换货物流信息
     */
    public Map<String, Object> getNewExpress(Integer id, Integer uid, String type) {
        logger.info("getNewExpress by id [{}] , uid [{}] and type [{}].", id, uid, type);
        // 验证请求参数
        if (id == null || id < 1 || StringUtils.isEmpty(type) || uid == null) {
            logger.warn("getNewExpress fail, parameter passing incorrect.");
            throw new ServiceException(ServiceError.ORDER_PARAMETER_PASSING_INCORRECT);
        }
        RefundGoods refundGoodsBO = null;
        ChangeGoodsMainInfo changeGoodsMainInfo = null;
        Integer expressId = null;
        String expressNumber = null;
        if ("refund".equalsIgnoreCase(type)) {
            refundGoodsBO = refundGoodsDao.selectByIdAndUid(id, uid);
            if (refundGoodsBO == null) {
                logger.warn("getNewExpress fail, no exchange or refund information");
                throw new ServiceException(ServiceError.ORDER_NO_EXCHANGE_REFUND_INFORMATION);
            }
            expressId = refundGoodsBO.getExpressId();
            expressNumber = refundGoodsBO.getExpressNumber();
        } else if ("change".equalsIgnoreCase(type)) {
            changeGoodsMainInfo = changeGoodsDao.selectById(id);
            if (changeGoodsMainInfo == null) {
                logger.warn("getNewExpress fail, no exchange or refund information");
                throw new ServiceException(ServiceError.ORDER_NO_EXCHANGE_REFUND_INFORMATION);
            }
            expressId = changeGoodsMainInfo.getExpressId();
            expressNumber = changeGoodsMainInfo.getExpressNumber();
        }
        //获取物流公司列表
        List<ExpressCompanyBO> expressCompanyList = getExpressCompanyList((byte) 1);
        ExpressCompanyBO expressCompany = getExpressCompanyById(expressId, expressCompanyList);
        if (expressCompany == null) {
            Map<String, Object> expressData = new LinkedHashMap<>();
            expressData.put("url", "");
            expressData.put("logo", "");
            expressData.put("caption", "");
            expressData.put("is_support", "2");
            expressData.put("express_number", expressNumber);
            return expressData;
        }
        List<Map<String, Object>> expressInfo = getRefundExpress(expressId, expressNumber);
        Map<String, Object> expressData = new LinkedHashMap<>();
        expressData.put("url", expressCompany.getCompanyUrl() == null ? "" : expressCompany.getCompanyUrl());
        expressData.put("logo", expressCompany.getCompanyLogo() == null ? "" : ImagesHelper.url(expressCompany.getCompanyLogo(), "taobaocms", 1));
        expressData.put("caption", refundGoodsBO != null ? refundGoodsBO.getExpressCompany() : changeGoodsMainInfo.getExpressCompany());
        expressData.put("is_support", CollectionUtils.isEmpty(expressInfo) ? "3" : "1");
        expressData.put("express_number", expressNumber);
        expressData.put("express_detail", expressInfo);
        return expressData;
    }

    private ExpressCompanyBO getExpressCompanyById(Integer expressId, List<ExpressCompanyBO> expressCompanyList) {
        for (ExpressCompanyBO expressCompany : expressCompanyList) {
            if (expressCompany.getId().equals(expressId)) {
                return expressCompany;
            }
        }
        return null;
    }

    private List<Map<String, Object>> getRefundExpress(Integer expressId, String expressNumber) {
        List<WaybillInfoBO> waybillInfoBOList = getExpressByTypeAndCode(expressId.byteValue(), expressNumber);
        List<Map<String, Object>> expressList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(waybillInfoBOList)) {
            for (WaybillInfoBO waybillInfoBO : waybillInfoBOList) {
                Map<String, Object> expressMap = new LinkedHashMap<>();
                expressMap.put("acceptTime", CalendarUtils.parsefomatSeconds(waybillInfoBO.getCreateTime(), CalendarUtils.LONG_FORMAT_LINE));
                expressMap.put("accept_address", waybillInfoBO.getAddressInfo());
                expressMap.put("express_id", waybillInfoBO.getLogisticsType());
                expressMap.put("express_number", waybillInfoBO.getWaybillCode());
                expressMap.put("order_code", waybillInfoBO.getOrderCode());
                expressList.add(expressMap);
            }
        }
        return expressList;
    }

    /**
     * 通过物流类型和运货单号获取退货物流信息
     */
    @Override
    public List<WaybillInfoBO> getExpressByTypeAndCode(Byte logisticsType, String waybillCode) {
        logger.info("getExpressByTypeAndCode by logistics Type [{}] and waybillCode [{}].", logisticsType, waybillCode);
        if (StringUtils.isEmpty(waybillCode) || logisticsType < 1) {
            return new ArrayList<>();
        }
        List<WaybillInfo> waybillInfoList = waybillInfoDao.selectByLogisticsTypeAndWaybillCode(logisticsType, waybillCode);
        List<WaybillInfoBO> waybillInfoBOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(waybillInfoList)) {
            beanConvert.convertFromBatch(waybillInfoList, waybillInfoBOList, WaybillInfoBO.class);
        }
        return waybillInfoBOList;
    }

}

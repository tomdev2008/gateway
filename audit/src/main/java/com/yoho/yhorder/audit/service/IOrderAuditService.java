package com.yoho.yhorder.audit.service;

import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.model.audit.AuditGoods;
import com.yoho.service.model.order.request.AuditRequest;
import com.yoho.service.model.order.response.shopping.ShoppingGoods;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by wujiexiang on 16/3/23.
 * 购物车稽核服务
 */
public interface IOrderAuditService {
    /**
     * 稽核用户和商品,是否支持货到付款
     *
     */
    Pair<String, String> auditCodPay(AuditRequest auditRequest) throws ServiceException;
    /**
     * 删除用户黑名单
     * @param uid
     * @return
     */
    int removeBackList(int uid);
}
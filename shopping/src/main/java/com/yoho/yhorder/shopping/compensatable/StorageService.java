package com.yoho.yhorder.shopping.compensatable;

import com.yoho.core.transaction.annoation.TxCompensatable;
import com.yoho.core.transaction.annoation.TxCompensateArgs;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.product.request.UpdateStorageRequest;
import com.yoho.yhorder.shopping.model.OrderGoods;
import com.yoho.yhorder.shopping.service.ExternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wujiexiang on 16/5/17.
 * 库存
 */
@Component
public class StorageService {

    private final Logger orderSubmitLog = LoggerFactory.getLogger("orderSubmitLog");

    private final Logger orderCompensateLog = LoggerFactory.getLogger("orderCompensateLog");

    @Autowired
    private ExternalService externalService;

    /**
     * 减库存
     *
     * @param uid
     * @param orderCode
     * @param orderGoodsList
     */
    @TxCompensatable(value = StorageService.class)
    public void decreaseStorage(@TxCompensateArgs("uid") int uid, @TxCompensateArgs("orderCode") long orderCode, @TxCompensateArgs("orderGoodsList") List<OrderGoods> orderGoodsList) {
        Assert.notEmpty(orderGoodsList, "orderGoodsList must not be empty");
        //需要先合并sku，一个sku可能会出现两次
        Map<Integer, UpdateStorageRequest> skuMap = new HashMap<>();
        UpdateStorageRequest updateStorageRequest = null;
        for (OrderGoods orderGoods : orderGoodsList) {
            Integer skuId = orderGoods.getProduct_sku();
            if (!skuMap.containsKey(skuId)) {
                updateStorageRequest = new UpdateStorageRequest();
                updateStorageRequest.setSkuId(skuId);
                updateStorageRequest.setStorageNum(0);
                skuMap.put(skuId, updateStorageRequest);
            }
            updateStorageRequest = skuMap.get(skuId);
            updateStorageRequest.setStorageNum(updateStorageRequest.getStorageNum() + orderGoods.getBuy_number());
        }
        List<UpdateStorageRequest> updateStorageRequestList = new ArrayList<>(skuMap.values());
        orderSubmitLog.info("order {} need to decrease storage,storage request list is {}", orderCode, updateStorageRequestList);
        try {
            externalService.batchDecreaseStorageBySkuId(updateStorageRequestList);
            orderSubmitLog.info("decrease storage success,order code {}", orderCode);
        } catch (Exception ex) {
            //erp 会每天同步库存
            orderSubmitLog.warn("decrease storage fail!,order code is {},storage request list is {}", orderCode, updateStorageRequestList, ex);
            throw new ServiceException(ServiceError.SHOPPING_BUSCHECK_STORAGE_NUM_NOT_SUPPORT);
        }
    }

    /**
     * 暂时前台不主动还,由erp来还
     * @param message
     */
    public void compensate(String message) {
        orderCompensateLog.info("StorageService compensate,message is {}", message);
        orderCompensateLog.info("StorageService nothing to do");
    }
}

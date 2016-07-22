package com.yoho.yhorder.order.service.impl;

import com.yoho.core.rest.client.ServiceCaller;
import com.yoho.product.model.ProductBo;
import com.yoho.product.request.BatchBaseRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ThirdModelServiceImpl
 * 调用其他模块服务类
 *
 * @author zhangyonghui
 * @date 2015/11/4
 */
@Service
public class ThirdModelServiceImpl {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ServiceCaller serviceCaller;


    /**
     * 查询productList详细信息
     * 调用商品的productList 服务
     *
     * @param productIdList
     * @return
     */
    public List<ProductBo> getProductList(List<Integer> productIdList) {
        if (productIdList.isEmpty()) {
            return Collections.emptyList();
        }
        logger.info("getProductList call product service");
        BatchBaseRequest request = new BatchBaseRequest<>();
        request.setParams(productIdList);
        ProductBo[] productBoList = serviceCaller.call("product.showProductDetailByProductIds", request, ProductBo[].class);
        return Arrays.asList(productBoList);
    }

}

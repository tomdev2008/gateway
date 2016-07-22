package com.yoho.yhorder.shopping.service.impl;

import com.yoho.product.request.BatchBaseRequest;
import com.yoho.service.model.order.request.ShoppingCartGetProductRequest;
import com.yoho.service.model.order.response.ShoppingCartGetProductResponse;
import com.yoho.yhorder.shopping.utils.MyStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Random;

/**
 * Created by yoho on 2016/2/17.
 */
@Service
public class ShoppingCartGetProductService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ShoppingCartGetProductResponse getProduct(ShoppingCartGetProductRequest request) {
        int page = request.getPage() == null ? 1 : request.getPage();
        String price = MyStringUtils.isEmpty(request.getPrice()) ? "0,100" : request.getPrice();
        logger.info("shopping cart get product by page {}, price {}.", page, price);
        page = randomInt(page, page + 10);
        int size = 6;
        //TODO 查询某段价格范围内的商品IDS
        //$result = QModels_Search_Source_Client::getSourceIds($getUserParams, $page, $size, $parans);
        //$total = $result['total'];
        int total = 100;
        if (total < (page * size)) {
            int maxRand = (int) Math.ceil(total / 6);
            page = randomInt(1, maxRand > 0 ? maxRand : 1);
            //$result = QModels_Search_Source_Client::getSourceIds($getUserParams, $page, $size, $parans);
        }
        BatchBaseRequest<Integer> ids = new BatchBaseRequest();
        ids.setParams(Collections.emptyList());
        //TODO 查询根据商品IDS批量查询商品
        // $productArr[] = QINProduct_Models_Product_Client::getFullProductById($product_id);
        ShoppingCartGetProductResponse response = new ShoppingCartGetProductResponse();
        response.setPage(page);
        response.setSize(size);
        response.setTotal(total);
        response.setProduct(Collections.emptyList());
        return response;
    }

    public static int randomInt(int min, int max) {
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;
    }
}

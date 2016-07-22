package com.yoho.yhorder.shopping.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.request.ShoppingComputeRequest;
import com.yoho.service.model.order.request.ShoppingReAddRequest;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.service.model.order.response.shopping.*;
import com.yoho.service.model.promotion.LimitCodeUserBo;
import com.yoho.service.model.response.UserAddressRspBO;
import com.yoho.yhorder.common.bean.ShoppingItem;
import com.yoho.yhorder.dal.model.ShoppingCart;
import com.yoho.yhorder.dal.model.ShoppingCartItems;
import com.yoho.yhorder.shopping.service.ExternalService;
import com.yoho.yhorder.shopping.service.IShoppingCartService;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MyAssert;
import com.yoho.yhorder.shopping.utils.ReflectTool;
import com.yoho.yhorder.shopping.utils.ShoppingConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JXWU on 2015/12/12.
 */
@Service
public class ShoppingCartServiceImpl implements IShoppingCartService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ShoppingCartAddService addService;

    @Autowired
    private ShoppingCartQueryService queryService;

    @Autowired
    private ShoppingCartSimpleActionService simpleActionService;

    @Autowired
    private ShoppingCartPaymentService paymentService;

    @Autowired
    private ShoppingCartComputeService computeService;

    @Autowired
    private ShoppingCartUseCouponService useCouponService;

    @Autowired
    private ShoppingPromotionCodeService promotionCodeService;

    @Autowired
    private ShoppingCartSubmitService submitService;

    @Override
    public ShoppingAddResponse add(ShoppingCartRequest request) {
        //参数校验
        //result(400, '加入商品不能为空');
        MyAssert.isTrue(request.getProduct_sku() < 1, ServiceError.SHOPPING_PRODUCTSKU_IS_NULL);
        return addService.add(request);
    }

    @Override
    public ShoppingQueryResponse query(ShoppingCartRequest request) {
        return queryService.query(request);
    }

    @Override
    public ShoppingAddResponse increase(ShoppingCartRequest request) {

        //参数校验
        MyAssert.isTrue(request.getProduct_sku() < 1, ServiceError.SHOPPING_PRODUCTSKU_IS_NULL);

        //设置默认值
        if (request.getIncrease_number() < 1) {
            request.setIncrease_number(1);
        }
        return simpleActionService.increase(request);
    }

    @Override
    public ShoppingAddResponse decrease(ShoppingCartRequest request) {
        //参数校验
        MyAssert.isTrue(request.getProduct_sku() < 1, ServiceError.SHOPPING_PRODUCTSKU_IS_NULL);
        //设置默认值
        if (request.getDecrease_number() < 1) {
            request.setDecrease_number(1);
        }
        return simpleActionService.decrease(request);
    }

    @Override
    public ShoppingAddResponse remove(ShoppingCartRequest request) {
        return simpleActionService.remove(request);
    }

    @Override
    public ShoppingAddResponse swap(ShoppingCartRequest request) {
        MyAssert.isTrue(StringUtils.isEmpty(request.getSwap_data()), ServiceError.SHOPPING_SWAP_DATA_IS_NULL);
        return simpleActionService.swap(request);
    }

    @Override
    public ShoppingCountResponse count(ShoppingCartRequest request) {
        return simpleActionService.count(request);
    }

    @Override
    public void selected(ShoppingCartRequest request) {
        MyAssert.isTrue(StringUtils.isEmpty(request.getProduct_sku_list()), ServiceError.SHOPPING_PRODUCT_SKU_LIST_IS_NULL);
        simpleActionService.selected(request);
    }

    @Override
    public ShoppingAddResponse addfavorite(ShoppingCartRequest request) {
        MyAssert.isTrue(request.getUid() < 1, ServiceError.SHOPPING_UID_IS_NULL);
        MyAssert.isTrue(StringUtils.isEmpty(request.getProduct_sku_list()), ServiceError.SHOPPING_PRODUCT_SKU_LIST_IS_NULL);
        return simpleActionService.addfavorite(request);
    }

    @Override
    public ShoppingPaymentResponse payment(ShoppingCartRequest request) {
        // return self::result(400, 'UID不能为空.');
        MyAssert.isTrue(request.getUid() < 1, ServiceError.SHOPPING_UID_IS_NULL);
        MyAssert.isFalse(ArrayUtils.contains(Constants.CART_TYPE_ARRAY_SUPPORT,request.getCart_type()),ServiceError.SHOPPING_CART_TYPE_NOT_SUPPORT);
        return paymentService.payment(request);
    }

    @Override
    public ShoppingComputeResponse compute(ShoppingComputeRequest request) {
        MyAssert.isTrue(request.getUid() < 1, ServiceError.SHOPPING_UID_IS_NULL);
        MyAssert.isFalse(ArrayUtils.contains(Constants.CART_TYPE_ARRAY_SUPPORT,request.getCart_type()),ServiceError.SHOPPING_CART_TYPE_NOT_SUPPORT);
        return computeService.compute(request);
    }

    @Override
    public ShoppingSubmitResponse submit(ShoppingSubmitRequest request) {
        int uid = request.getUid();
        MyAssert.isTrue(uid < 1, ServiceError.SHOPPING_UID_IS_NULL);

        MyAssert.isFalse(ArrayUtils.contains(Constants.CART_TYPE_ARRAY_SUPPORT,request.getCart_type()),ServiceError.SHOPPING_CART_TYPE_NOT_SUPPORT);

        //return self::result(400, '配货地址不能为空.');

        //self::result(400, '请选择支付类型.');
        MyAssert.isTrue(request.getPayment_type() == null, ServiceError.SHOPPING_ORDER_PAYMENTTYPE_IS_NULL);

        //return self::result(400, '请选择支付方式.');
        MyAssert.isTrue(request.getPayment_type() == 1 && (request.getPayment_id() == null || request.getPayment_id() < 1), ServiceError.SHOPPING_ORDER_PAYBY_IS_NULL);

        //2.设置默认值
        logger.debug("inject default value for request");
        ReflectTool.injectDefaultValue(request, ShoppingConfig.SHOPPING_SUBMIT_DEFAULT_REQUEST_DATA_MAP);

        //3.
        return submitService.submit(request);
    }


    @Override
    public ShoppingUseCouponResponse useCoupon(ShoppingComputeRequest request) {
        //
        MyAssert.isTrue(request.getUid() < 1, ServiceError.SHOPPING_UID_IS_NULL);

        //return self::result(400, '没有选中优惠券.');
        MyAssert.isTrue(StringUtils.isEmpty(request.getCoupon_code()), ServiceError.SHOPPING_COUPONCODE_IS_NULL);
        return useCouponService.useCoupon(request);
    }


    @Override
    public ShoppingPromotionCodeResponse usePromotionCode(ShoppingComputeRequest request) {
        MyAssert.isTrue(request.getUid() < 1, ServiceError.SHOPPING_UID_IS_NULL);
        MyAssert.isTrue(StringUtils.isEmpty(request.getPromotion_code()), ServiceError.SHOPPING_PRMOTIONCODE_NOT_EXISTS);
        return promotionCodeService.usePromotionCode(request);
    }

    @Override
    public ShoppingAddResponse readd(ShoppingReAddRequest request) {
        MyAssert.isTrue(request.getUid() < 1, ServiceError.SHOPPING_UID_IS_NULL);
        MyAssert.isTrue(StringUtils.isEmpty(request.getOrder_code()), ServiceError.SHOPPING_REQUESTPARAMS_IS_NULL);
        return addService.readd(request);
    }


    /**
     * product_sku_list [{"goods_type":"ordinary","buy_number":1,"selected":"Y","product_sku":"1006277","prmotion_id":11111}]
     * @param request
     * @return
     */
    @Override
    public ShoppingQueryResponse selectedAndCart(ShoppingCartRequest request) {
        logger.info("enter selectedAndCart in service,request is {}", request);
        simpleActionService.selected(request);
        return queryService.query(request);
    }

    /**
     * product_sku_list [{"number":1,"product_sku":"1006277","prmotion_id":11111}]
     * @param request
     * @return
     */
    @Override
    public ShoppingQueryResponse removeAndCart(ShoppingCartRequest request) {
        logger.info("enter removeAndCart in service,request is {}", request);
        List<ShoppingCartItems> removeItems = stringToShoppingCartItemList(request.getProduct_sku_list());
        logger.info("after parse,remove items list are {}", removeItems);

        simpleActionService.removeItemsByShoppingCartId(request,removeItems);

        return queryService.query(request);
    }


    @Override
    public ShoppingQueryResponse addfavoriteAndCart(ShoppingCartRequest request) {

        logger.info("enter addfavoriteAndCart in service,request is {}", request);

        MyAssert.isTrue(request.getUid() < 1, ServiceError.SHOPPING_UID_IS_NULL);

        List<ShoppingCartItems> removeItems = stringToShoppingCartItemList(request.getProduct_sku_list());

        logger.info("after parse,remove items list are {}", removeItems);

        simpleActionService.addItemsToFavorite(request.getUid(), removeItems);

        simpleActionService.removeItemsByShoppingCartId(request,removeItems);

        return queryService.query(request);
    }


    /**
     * str 类似[{"product_sku":"1006277","prmotion_id":11111}]
     *
     * @param str
     * @return
     */
    private List<ShoppingCartItems> stringToShoppingCartItemList(String str) throws ServiceException{
        List<ShoppingCartItems> items = new ArrayList<>();
        try {
            JSONArray array = JSON.parseArray(str);
            List<JSONObject> errorJsonList = new ArrayList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject ele = array.getJSONObject(i);
                    ShoppingCartItems item = new ShoppingCartItems();
                    Integer product_sku_value = ele.getInteger("product_sku");
                    if (product_sku_value == null || product_sku_value == 0) {
                        errorJsonList.add(ele);
                        continue;
                    }
                    item.setSkuId(ele.getInteger("product_sku"));
                    item.setPromotionId(ele.getInteger("promotion_id"));
                    item.setNum(ele.getInteger("buy_number"));
                    items.add(item);
                }
            }

            if (CollectionUtils.isNotEmpty(errorJsonList)) {
                logger.warn("error params are {}",errorJsonList);
            }

        } catch (Exception ex) {
            logger.warn("parse {} to ShoppingCartItems error", str, ex);
            throw new ServiceException(ServiceError.SHOPPING_PACKAGE_FORMAT_IS_NULL);
        }
        return items;
    }
}

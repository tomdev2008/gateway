package com.yoho.yhorder.shopping.restapi;

/**
 * Created by JXWU on 2015/11/18.
 */

import com.yoho.core.dal.datasource.annotation.Database;
import com.yoho.core.rest.annotation.ServiceDesc;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.request.*;
import com.yoho.service.model.order.response.shopping.*;
import com.yoho.yhorder.shopping.cache.StatCacheService;
import com.yoho.yhorder.shopping.service.IShoppingCartMergeService;
import com.yoho.yhorder.shopping.service.IShoppingCartService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@RequestMapping(value = "/shopping")
public class ShoppingCartController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Logger orderSubmitLogger = LoggerFactory.getLogger("orderSubmitLog");

    private final Logger cartQuerylogger = LoggerFactory.getLogger("cartQueryLog");

    private final Logger addPaymentComputeLogger = LoggerFactory.getLogger("addPaymentComputeLog");

    @Autowired
    private IShoppingCartService shoppingCartService;

    @Resource
    IShoppingCartMergeService shoppingCartMergeService;

    @Resource
    private StatCacheService statCacheService;

    @RequestMapping("/add")
    @ResponseBody
    @ServiceDesc(serviceName = "addShopping")
    @Database(ForceMaster = true)
    public ShoppingAddResponse add(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart add in controller, request is: {}", request);
        try {
            return shoppingCartService.add(request);
        } catch (Exception ex) {
            addPaymentComputeLogger.error("process shopping cart add failed, request is: {}", request, ex);
            throw ex;
        }
    }

    @RequestMapping("/cart")
    @ResponseBody
    @ServiceDesc(serviceName = "cartShopping")
    //@Database(ForceMaster = true)
    public ShoppingQueryResponse query(@RequestBody ShoppingCartRequest request) {

        cartQuerylogger.info("\n\n\n\n\n*********************************************************************");
        cartQuerylogger.info("receive shopping cart query in controller, request {} ", request);

        try {
            return shoppingCartService.query(request);
        } catch (Exception ex) {
            cartQuerylogger.error("process shopping cart query failed, request is: {}", request, ex);
            throw  ex;
        }
    }


    @RequestMapping("/increase")
    @ResponseBody
    @ServiceDesc(serviceName = "increaseShopping")
    @Database(ForceMaster = true)
    public ShoppingAddResponse increase(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart increase in controller, request is: {}", request);
        try {
            return shoppingCartService.increase(request);
        }catch (Exception ex){
            addPaymentComputeLogger.error("process shopping cart increase failed, request is: {}", request, ex);
            throw ex;
        }
    }


    @RequestMapping("/decrease")
    @ResponseBody
    @ServiceDesc(serviceName = "decreaseShopping")
    @Database(ForceMaster = true)
    public ShoppingAddResponse decrease(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart decrease in controller, request is: {}", request);
        try {
            return shoppingCartService.decrease(request);
        }catch (Exception ex){
            addPaymentComputeLogger.error("process shopping cart decrease failed, request is: {}", request, ex);
            throw ex;
        }
    }

    @RequestMapping("/addfavorite")
    @ResponseBody
    @ServiceDesc(serviceName = "addfavorite")
    @Database(ForceMaster = true)
    public ShoppingAddResponse addfavorite(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart addfavorite in controller, request is: {}", request);
        try {
            return shoppingCartService.addfavorite(request);
        }catch (Exception ex){
            addPaymentComputeLogger.error("process shopping cart addfavorite failed, request is: {}", request, ex);
            throw ex;
        }
    }

    @RequestMapping("/remove")
    @ResponseBody
    @ServiceDesc(serviceName = "removeShopping")
    @Database(ForceMaster = true)
    public ShoppingAddResponse remove(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart remove in controller, request is: {}", request);
        try {
            return shoppingCartService.remove(request);
        }catch (Exception ex){
            addPaymentComputeLogger.error("process shopping cart remove failed, request is: {}", request, ex);
            throw ex;
        }
    }

    @RequestMapping("/swap")
    @ResponseBody
    @ServiceDesc(serviceName = "swapShopping")
    @Database(ForceMaster = true)
    public ShoppingAddResponse swap(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart swap in controller, request is: {}", request);
        try {
            return shoppingCartService.swap(request);
        }catch (Exception ex){
            addPaymentComputeLogger.error("process shopping cart swap failed, request is: {}", request, ex);
            throw ex;
        }
    }

    @RequestMapping("/payment")
    @ResponseBody
    @ServiceDesc(serviceName = "paymentShopping")
    @Database(ForceMaster = true)
    public ShoppingPaymentResponse payment(@RequestBody ShoppingCartRequest request) {

        addPaymentComputeLogger.info("\n\nreceive shopping cart payment in controller, request is: {}", request);
        try {
            return shoppingCartService.payment(request);
        } catch (Exception e) {
            addPaymentComputeLogger.error("process shopping cart payment failed, request is: {}", request, e);
            throw e;
        }

    }


    @RequestMapping("/count")
    @ResponseBody
    @ServiceDesc(serviceName = "countShopping")
    @Database(ForceMaster = true)
    public ShoppingCountResponse count(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart count in controller, request is: {}", request);
        try {
            return shoppingCartService.count(request);
        } catch (Exception e) {
            addPaymentComputeLogger.error("process shopping cart count failed, request is: {}", request, e);
            throw e;
        }
    }

    @RequestMapping("/selected")
    @ResponseBody
    @ServiceDesc(serviceName = "selectedShopping")
    @Database(ForceMaster = true)
    public void selected(@RequestBody ShoppingCartRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart selected in controller, request is: {}", request);
        try {
             shoppingCartService.selected(request);
        } catch (Exception e) {
            addPaymentComputeLogger.error("process shopping cart selected failed, request is: {}", request, e);
            throw e;
        }
    }

    @RequestMapping("/compute")
    @ResponseBody
    @ServiceDesc(serviceName = "computeShopping")
    @Database(ForceMaster = true)
    public ShoppingComputeResponse compute(@RequestBody ShoppingComputeRequest request) {

        addPaymentComputeLogger.info("\n\nreceive shopping cart compute in controller, request is: {}", request);

        try {
            ShoppingComputeResponse response = shoppingCartService.compute(request);
            return response;

        } catch (Exception ex) {
            addPaymentComputeLogger.error("process shopping cart compute failed, request is: {}", request, ex);
            throw ex;
        }

    }

    @RequestMapping("/useCoupon")
    @ResponseBody
    @ServiceDesc(serviceName = "useCouponShopping")
    @Database(ForceMaster = true)
    public ShoppingUseCouponResponse useCoupon(@RequestBody ShoppingComputeRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart useCoupon in controller, request is: {}", request);
        try {
            ShoppingUseCouponResponse response = shoppingCartService.useCoupon(request);
            addPaymentComputeLogger.info("shopping cart useCoupon in controller,response is\n {}",response);
            return response;
        } catch (Exception e) {
            addPaymentComputeLogger.error("process shopping cart useCoupon failed, request is: {}", request, e);
            throw e;
        }
    }

    @RequestMapping("/submit")
    @ResponseBody
    @ServiceDesc(serviceName = "submitShopping")
    @Database(ForceMaster = true)
    public ShoppingSubmitResponse submit(@RequestBody ShoppingSubmitRequest request) {

        orderSubmitLogger.info("\n\n\n\n\n*********************************************************************");
        orderSubmitLogger.info("receive shopping cart submit in controller, request {} ", request);

        try {
            return shoppingCartService.submit(request);
        } catch (Exception ex) {
            /**
             * 捕获所有异常，打印日志后，抛出
             */
            orderSubmitLogger.error("process shopping cart submit failed, request {}", request, ex);
            throw ex;
        }

    }

    /**
     * 合并购物车
     *
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/mergeShoppingCart")
    @ResponseBody
    @ServiceDesc(serviceName = "mergeShoppingCart")
    @Database(ForceMaster = true)
    public ShoppingCartMergeResponseBO mergeCart(@RequestBody ShoppingCartMergeRequestBO request) throws ServiceException {
        logger.info("mergeCart with param {}", request);
        if (request.getUid() == 0 || StringUtils.isEmpty(request.getShopping_key())) {
            logger.debug("mergeShoppingCart error with param {}", request);
            return null;
        }
        try {
            return shoppingCartMergeService.mergeCart(request);
        }catch (Exception ex){
            addPaymentComputeLogger.error("process shopping cart mergeCart failed, request is: {}", request, ex);
            throw ex;
        }
    }


    /**
     * 合并购物车By local shopping cart
     *
     * @param request
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/mergeShoppingCartByLocal")
    @ResponseBody
    @ServiceDesc(serviceName = "mergeShoppingCartByLocal")
    @Database(ForceMaster = true)
    public ShoppingCartMergeResponseBO mergeCartByLocal(@RequestBody ShoppingCartLocalMergeRequestBO request) throws ServiceException {
        logger.info("mergeShoppingCartByLocal with param {}", request);
        if (request.getUid() == 0 || StringUtils.isEmpty(request.getProduct_sku_list())) {
            logger.debug("mergeShoppingCart error with param {}", request);
            return null;
        }
        try {
            return shoppingCartMergeService.mergeCartByLocal(request);
        }catch (Exception ex){
            addPaymentComputeLogger.error("process shopping cart mergeCartByLocal failed, request is: {}", request, ex);
            throw ex;
        }
    }


    @RequestMapping("/erpSubmitStat")
    @ResponseBody
    @ServiceDesc(serviceName = "erpSubmitStat")
    public Map erpSubmitStat() {
        return statCacheService.getErpSubmitCounterMapSinceStartup().asMap();
    }


    @RequestMapping("/usePromotionCode")
    @ResponseBody
    @ServiceDesc(serviceName = "usePromotionCode")
    @Database(ForceMaster = true)
    public ShoppingPromotionCodeResponse usePromotionCode(@RequestBody ShoppingComputeRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart usePromotionCode in controller, request is: {}", request);
        try {
            return shoppingCartService.usePromotionCode(request);
        } catch (Exception e) {
            addPaymentComputeLogger.error("process shopping cart usePromotionCode failed, request is: {}", request, e);
            throw e;
        }
    }


    @RequestMapping("/readd")
    @ResponseBody
    @ServiceDesc(serviceName = "readd")
    @Database(ForceMaster = true)
    public ShoppingAddResponse readd(@RequestBody ShoppingReAddRequest request) {
        addPaymentComputeLogger.info("\n\nreceive shopping cart readd in controller, request is: {}", request);
        try {
            return shoppingCartService.readd(request);
        } catch (Exception e) {
            addPaymentComputeLogger.error("process shopping cart readd failed, request is: {}", request, e);
            throw e;
        }
    }

    @RequestMapping("/selectedAndCart")
    @ResponseBody
    @ServiceDesc(serviceName = "selectedAndCart")
    @Database(ForceMaster = true)
    public ShoppingQueryResponse selectedAndCart(@RequestBody ShoppingCartRequest request) {
        cartQuerylogger.info("\n\nreceive shopping cart selectedAndCart in controller, request is: {}", request);
        try {
            return shoppingCartService.selectedAndCart(request);
        } catch (Exception e) {
            cartQuerylogger.error("process shopping cart selectedAndCart failed, request is: {}", request, e);
            throw e;
        }
    }

    @RequestMapping("/removeAndCart")
    @ResponseBody
    @ServiceDesc(serviceName = "removeAndCart")
    @Database(ForceMaster = true)
    public ShoppingQueryResponse removeAndCart(@RequestBody ShoppingCartRequest request) {
        cartQuerylogger.info("\n\nreceive shopping cart removeAndCart in controller, request is: {}", request);
        try {
            return shoppingCartService.removeAndCart(request);
        } catch (Exception e) {
            cartQuerylogger.error("process shopping cart removeAndCart failed, request is: {}", request, e);
            throw e;
        }
    }


    @RequestMapping("/addfavoriteAndCart")
    @ResponseBody
    @ServiceDesc(serviceName = "addfavoriteAndCart")
    @Database(ForceMaster = true)
    public ShoppingQueryResponse addfavoriteAndCart(@RequestBody ShoppingCartRequest request) {
        cartQuerylogger.info("\n\nreceive shopping cart addfavoriteAndCart in controller, request is: {}", request);
        try {
            return shoppingCartService.addfavoriteAndCart(request);
        } catch (Exception e) {
            cartQuerylogger.error("process shopping cart addfavoriteAndCart failed, request is: {}", request, e);
            throw e;
        }
    }


}
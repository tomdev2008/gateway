package com.yoho.yhorder.shopping.charge.caculator;

import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.model.PackageBO;
import com.yoho.service.model.order.model.SimpleGoodsBO;
import com.yoho.service.model.order.response.shopping.ShoppingGoods;
import com.yoho.yhorder.common.utils.Constants;
import com.yoho.yhorder.common.utils.MathUtils;
import com.yoho.yhorder.common.utils.OrderPackageUtils;
import com.yoho.yhorder.shopping.charge.ChargeContext;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by wujiexiang on 16/4/21.
 * 包裹计算,包括运费
 */
@Component
public class PackageSplitCharge {
    private final Logger logger = LoggerFactory.getLogger("calculateLog");

    public void charge(ChargeContext chargeContext) {

        //#不计算运费
        if (!chargeContext.getChargeParam().isNeedCalcShippingCost()) {
            return;
        }

        /**
         * 先将经销和代销分开,然后逐个拆分
         */
        List<PackageBO> packageList = splitClassficPackage(chargeContext);

        //设置拆分结果
        setupSplitedPackageList(chargeContext.getChargeTotal(), packageList);
    }


    private List<PackageBO> splitClassficPackage(ChargeContext chargeContext) {
        List<PackageBO> packageList = new ArrayList<>();

        List<ShoppingGoods> shoppingGoodsList = chargeContext.mergeChargedGoodsListByCartType();
        if (CollectionUtils.isEmpty(shoppingGoodsList)) {
            return packageList;
        }

        //将经销和代销分开
        TreeMap<Integer, List<ShoppingGoods>> agentGoodsMap = classfic(shoppingGoodsList);

        packageList = splitPackageAndShippingCost(agentGoodsMap, chargeContext.getChargeTotal().getLastShippingCost());

        return packageList;

    }

    /**
     * 将经销和代销商品分开
     *
     * @param shoppingGoodsList
     * @return
     */
    private TreeMap<Integer, List<ShoppingGoods>> classfic(List<ShoppingGoods> shoppingGoodsList) {
        List<ShoppingGoods> tmpList = null;

        //key=0表示经销,其他代销
        TreeMap<Integer, List<ShoppingGoods>> agentGoodsMap = new TreeMap<>();

        for (ShoppingGoods goods : shoppingGoodsList) {
            Integer supplierId = goods.getSupplier_id();
            if (!"Y".equals(goods.getSelected())) {
                continue;
            }
            if (Constants.IS_JIT_PRODUCT_STR.equals(goods.getIs_jit())) {
                if (supplierId == null || supplierId <= 0) {
                    logger.warn("goods is jit,but supplier_id is error,goods is {}", goods);
                    throw new ServiceException(ServiceError.SHOPPING_JIT_SUPPLIER_ERROR);
                }
            } else if (supplierId == null || supplierId != 0) {
                logger.warn("goods is not jit,but supplier_id is error,skn:{},sku:{},is_jit:{},supplier_id:{}",
                        goods.getProduct_skn(),goods.getProduct_sku(),goods.getIs_jit(),goods.getSupplier_id());
                supplierId = 0;

            }
            if (!agentGoodsMap.containsKey(supplierId)) {
                tmpList = new ArrayList<>();
                agentGoodsMap.put(supplierId, tmpList);
            }
            tmpList = agentGoodsMap.get(supplierId);

            tmpList.add(goods);

        }

        return agentGoodsMap;
    }

    /**
     * 拆分包裹及其运费
     *
     * @param agentGoodsMap
     * @param shippingCost
     * @return
     */
    private List<PackageBO> splitPackageAndShippingCost(TreeMap<Integer, List<ShoppingGoods>> agentGoodsMap, double shippingCost) {
        int packageNum = agentGoodsMap.size();
        if (!OrderPackageUtils.canSplitSubOrder(packageNum)) {
            return new ArrayList<>();
        }

        List<PackageBO> packageList = splitPackage(agentGoodsMap);

        splitPackageShippingCost(packageList, shippingCost);

        return packageList;
    }

    private List<PackageBO> splitPackage(TreeMap<Integer, List<ShoppingGoods>> agentGoodsMap) {
        List<PackageBO> packageList = new ArrayList<>();
        //需要拆分
        Set<Integer> supplierIds = agentGoodsMap.keySet();
        for (Integer supplierId : supplierIds) {
            PackageBO packageBO = new PackageBO();
            packageBO.setSupplierId(supplierId.toString());
            packageBO.setGoodsList(getPackagedGoodsList(agentGoodsMap.get(supplierId)));

            logger.info("package split pre charge,supplier id is {},PackageBO is {}", supplierId, packageBO);
            packageList.add(packageBO);
        }
        return packageList;
    }

    private void splitPackageShippingCost(List<PackageBO> packageList, double shippingCost) {
        if (CollectionUtils.isEmpty(packageList)) {
            return;
        }
        int packageNum = packageList.size();
        double[] packageShippingCostArray = OrderPackageUtils.caclPackageShippingCost(shippingCost, packageNum);
        for (int i = 0; i < packageNum; i++) {
            PackageBO packageBO = packageList.get(i);
            double shippingCutCost = shippingCost - packageShippingCostArray[i];
            packageBO.setShoppingCost(MathUtils.formatCurrency(packageShippingCostArray[i]));
            packageBO.setShoppingOrigCost(MathUtils.formatCurrency(shippingCost));
            packageBO.setShoppingCutCost(MathUtils.formatCurrency(shippingCutCost));
        }
    }

    private List<SimpleGoodsBO> getPackagedGoodsList(List<ShoppingGoods> list) {
        List<SimpleGoodsBO> simpleList = new ArrayList<>(list.size());
        for (ShoppingGoods shoppingGoods : list) {
            SimpleGoodsBO bo = new SimpleGoodsBO();
            bo.setBuyNumber(shoppingGoods.getBuy_number());
            bo.setGoodsImages(shoppingGoods.getGoods_images());
            bo.setGoodsType(shoppingGoods.getGoods_type());
            bo.setProductId(shoppingGoods.getProduct_id());
            bo.setProductName(shoppingGoods.getProduct_name());
            bo.setProductSkc(shoppingGoods.getProduct_skc());
            bo.setProductSkn(shoppingGoods.getProduct_skn());
            bo.setProductSku(shoppingGoods.getProduct_sku());

            simpleList.add(bo);
        }

        return simpleList;
    }


    /**
     * 添加包裹拆分的结果
     *
     * @param chargeTotal
     * @param packageList
     */
    private void setupSplitedPackageList(ChargeTotal chargeTotal, List<PackageBO> packageList) {
        chargeTotal.setPackageList(packageList);
    }
}

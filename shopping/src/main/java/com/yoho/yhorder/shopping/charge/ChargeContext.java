package com.yoho.yhorder.shopping.charge;

import com.yoho.core.common.utils.YHMath;
import com.yoho.service.model.order.model.PromotionBO;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.service.model.order.response.shopping.*;
import com.yoho.yhorder.shopping.charge.model.ChargeGoods;
import com.yoho.yhorder.shopping.charge.model.ChargeParam;
import com.yoho.yhorder.shopping.charge.model.ChargeTotal;
import com.yoho.yhorder.shopping.charge.model.DiscountType;
import com.yoho.yhorder.shopping.model.UserInfo;
import com.yoho.yhorder.shopping.utils.Constants;
import com.yoho.yhorder.shopping.utils.MathUtils;
import com.yoho.yhorder.shopping.utils.MyStringUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import static com.yoho.yhorder.shopping.charge.promotion.impl.BigDecimalHelper.toDouble;

/**
 * Created by JXWU on 2015/11/19.
 */
@Data
public class ChargeContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Logger calculateLog = LoggerFactory.getLogger("calculateLog");

    /**
     * setup
     *
     * @param gift
     */
    public void setupGift(PromotionGift gift) {
        this.giftList.add(gift);
    }

    /**
     * setup price gift
     *
     * @param gift
     */
    public void setupPriceGift(PromotionGift gift) {
        this.priceGiftList.add(gift);
    }

    /**
     * 添加一个新的优惠信息。这个优惠信息会发送给客户端
     *
     * @param promotionInfo
     * @param cutdownAmount
     */
    public void setupPromotionInfo(PromotionInfo promotionInfo, BigDecimal cutdownAmount) {

        this.setupPromotionInfo(promotionInfo,toDouble(cutdownAmount));
    }

    public void setupPromotionInfo(PromotionInfo promotionInfo, Double cutdownAmount) {
        PromotionBO promotionBO = new PromotionBO(Integer.parseInt(promotionInfo.getId()), promotionInfo.getTitle(), cutdownAmount, promotionInfo.getPromotionType());

        boolean existed = this.setupPromotionInfo(promotionBO);

        if (!existed) {
            this.getChargeTotal().getUsePromotionInfoList().add(promotionInfo);
        }
    }

    /**
     * 添加一个新的优惠信息。这个优惠信息会发送给客户端
     *
     * @param promotionBO
     */
    public boolean setupPromotionInfo(PromotionBO promotionBO) {

        boolean existed = false;
        for (PromotionBO promotionBo : this.getChargeTotal().getPromotionInfoList()) {
            if (promotionBo.getPromotion_id() == promotionBO.getPromotion_id()) {
                existed = true;
                break;
            }
        }

        if (!existed) {
            this.getChargeTotal().addPromotionInfo(promotionBO);
            logger.info("add promotion info : {} to charge total", promotionBO);
        }

        return existed;
    }

    /**
     * 添加优惠了多少钱。在原来总的优惠金额上添加
     *
     * @param cutdown 新优惠的总额
     */
    public void addCutdownAmout(BigDecimal cutdown) {
        double newDiscount = YHMath.add(this.getChargeTotal().getDiscountAmount() ,toDouble(cutdown));
        this.getChargeTotal().setDiscountAmount(newDiscount);
    }


    /**
     * 设置某个promotion ID的 额外属性。
     *
     * @param promotionId 促销ID
     * @param toAddIds    额外的属性
     */
    public void setupPromotionIDS(String promotionId, int... toAddIds) {

        List<Integer> sknIds = new LinkedList<>();
        if (toAddIds != null) {
            for (int toadd : toAddIds) {
                sknIds.add(toadd);
            }
        }
        this.setupPromotionIDS(promotionId, sknIds);
    }

    /**
     * 设置某个promotion ID的 额外属性。
     *
     * @param promotionId 促销ID
     * @param sknIds      额外的属性
     */
    public void setupPromotionIDS(String promotionId, List<Integer> sknIds) {

        List<Integer> ids = promotionIDS.get(promotionId);
        if (ids == null) {
            ids = new LinkedList<>();
            promotionIDS.put(promotionId, ids);
        }

        if (sknIds != null) {
            ids.addAll(sknIds);
        }
    }

    //用户请求数据，算费时使用
    private ChargeParam chargeParam;

    //算费时查询设置
    private UserInfo userInfo = null;

    //算费结果对象
    private ChargeTotal chargeTotal;

    //原始所有的sku，还未分类
    private List<ChargeGoods> chargeGoodsList;

    //购物车是普通产品，mainGoods为普通商品列表，购物车类型是预约，mianGoods为预约商品列表。
    private List<ChargeGoods> mainGoods = new ArrayList<>();

    //gift商品列表
    private List<ChargeGoods> mainGoodsGift = new ArrayList<>();

    //加购价商品列表
    private List<ChargeGoods> mainGoodsPriceGift = new ArrayList<>();

    //outlet商品
    private List<ChargeGoods> outletGoods = new ArrayList<>();

    //预售售罄商品
    List<ChargeGoods> preSoldOutGoods = new ArrayList<>();

    //普通售罄商品
    List<ChargeGoods> soldOutGoods = new ArrayList<>();

    List<ChargeGoods> preOffShelvesGoods = new ArrayList<>();

    List<ChargeGoods> offShelvesGoods = new ArrayList<>();

    //可参加的加价购活动
    private List<PromotionGift> priceGiftList = new ArrayList<>();
    //可参加的赠品活动
    private List<PromotionGift> giftList = new ArrayList<>();


    /**
     * Promotion Id -- > SKN IDS
     */
    private Map<String /* promotion Id*/, List<Integer> /* skn ids*/> promotionIDS = new HashMap<>();


    private void clear() {
        this.mainGoods.clear();
        this.mainGoodsGift.clear();
        this.mainGoodsPriceGift.clear();
        this.outletGoods.clear();
        this.preSoldOutGoods.clear();
        this.soldOutGoods.clear();
        this.preOffShelvesGoods.clear();
        this.offShelvesGoods.clear();
    }

    public void classfic() {
        //清除之前的分类，重新分类
        this.clear();
        this.classfic(this.chargeGoodsList);
    }

    /**
     * 将商品进行分类成普通商品、gift商品、pride_gift等
     */

    public void classfic(List<ChargeGoods> chargeGoodsList) {

        if (CollectionUtils.isEmpty(chargeGoodsList)) {
            return;
        }

        List<ChargeGoods> mainGoods = new ArrayList<>();
        List<ChargeGoods> mainGoodsGift = new ArrayList<>();
        List<ChargeGoods> mainGoodsPriceGift = new ArrayList<>();
        List<ChargeGoods> advanceGoods = new ArrayList<>();
        List<ChargeGoods> outletGoods = new ArrayList<>();

        List<ChargeGoods> preSoldOutGoods = new ArrayList<>();
        List<ChargeGoods> soldOutGoods = new ArrayList<>();
        List<ChargeGoods> preOffShelvesGoods = new ArrayList<>();
        List<ChargeGoods> offShelvesGoods = new ArrayList<>();

        ChargeParam chargeParam = this.chargeParam;
        for (ChargeGoods chargeGoods : chargeGoodsList) {

            //商品是否下架
            if (chargeGoods.getShoppingGoods().getOff_shelves() == 1) {
                logger.info("sku {} was off shelves", chargeGoods.getShoppingGoods().getProduct_sku());
                if (chargeGoods.isShoppingGoodsAdvanced()) {
                    preOffShelvesGoods.add(chargeGoods);
                } else {
                    offShelvesGoods.add(chargeGoods);
                }
                continue;
            }

            //库存数量
            int storageNum = Integer.parseInt(chargeGoods.getShoppingGoods().getStorage_number());

            if (storageNum < 1) {
                logger.info("sku {} was sold out,storage num is {}", chargeGoods.getShoppingGoods().getProduct_sku(), storageNum);

                if (chargeGoods.isShoppingGoodsAdvanced()) {
                    preSoldOutGoods.add(chargeGoods);
                } else {
                    soldOutGoods.add(chargeGoods);
                }

                continue;
            }

            /**
             *  promotion id > 0,  说明是促销商品
             */
            if (Integer.parseInt(chargeGoods.getShoppingGoods().getPromotion_id()) > 0) {
                if ("Gift".equals(chargeGoods.getPromotionType())) {
                    chargeGoods.getShoppingGoods().setGoods_type(Constants.ORDER_GOODS_TYPE_GIFT_STR);
                    mainGoodsGift.add(chargeGoods);
                } else {
                    chargeGoods.getShoppingGoods().setGoods_type(Constants.ORDER_GOODS_TYPE_PRICE_GIFT_STR);
                    mainGoodsPriceGift.add(chargeGoods);
                }
            } else if (chargeGoods.isShoppingGoodsAdvanced()) {
                chargeGoods.getShoppingGoods().setGoods_type(Constants.ORDER_GOODS_TYPE_ADVANCE_STR);
                if (Constants.PRESALE_CART_TYPE.equals(chargeParam.getCartType())) {
                    chargeParam.setAttribute(Constants.ATTRIBUTE_PRESALE);//预售订单
                }
                advanceGoods.add(chargeGoods);
            } else if (chargeGoods.isShoppingGoodsOutlets()) {
                chargeGoods.getShoppingGoods().setGoods_type(Constants.ORDER_GOODS_TYPE_OUTLET_STR);
                outletGoods.add(chargeGoods);
            } else if (Constants.ATTRIBUTE_VIRTUAL_STR.equals(chargeGoods.getShoppingGoods().getAttribute())) {
                chargeGoods.getShoppingGoods().setGoods_type(Constants.ORDER_GOODS_TYPE_TICKET_STR);
                mainGoods.add(chargeGoods);
            } else {
                /**
                 * 原php代码如下
                 *if ($val['attribute'] == 3) {
                 $val['goods_type'] = YOHOCart_Config::ORDER_GOODS_TYPE_ORDINARY_STR;
                 } else {
                 $val['goods_type'] = YOHOCart_Config::ORDER_GOODS_TYPE_ORDINARY_STR;
                 }
                 */
                chargeGoods.getShoppingGoods().setGoods_type(Constants.ORDER_GOODS_TYPE_ORDINARY_STR);
                mainGoods.add(chargeGoods);
            }
            logger.info("sku {} ,goods_type {},buy number {},real price {},selected {},promotion id {},promotion flag {}",chargeGoods.getShoppingGoods().getProduct_sku(),
                    chargeGoods.getShoppingGoods().getGoods_type(),
                    chargeGoods.getBuyNumber(),
                    chargeGoods.getRealPriceBigDecimal(),
                    chargeGoods.isSelected(),
                    chargeGoods.getShoppingGoods().getPromotion_id(),
                    chargeGoods.getShoppingGoods().getPromotion_flag());
        }

        //预售购物车
        if (chargeParam.isPreSaleCart()) {
            mainGoods = advanceGoods;
        }

        this.mainGoods.addAll(mainGoods);
        this.mainGoodsGift.addAll(mainGoodsGift);
        this.mainGoodsPriceGift.addAll(mainGoodsPriceGift);
        this.outletGoods.addAll(outletGoods);
        this.preSoldOutGoods.addAll(preSoldOutGoods);
        this.soldOutGoods.addAll(soldOutGoods);
        this.preOffShelvesGoods.addAll(preOffShelvesGoods);
        this.offShelvesGoods.addAll(offShelvesGoods);

        //设置jit参数
        setupJitAttributeForChargeParam();

        logger.debug("classfic success. {}", this);
    }

    /**
     * 返回结果算费
     */
    public ShoppingChargeResult getChargeResult() {
        ChargeTotal chargeTotal = this.getChargeTotal();
        List<PromotionFormula> promotionFormulaList = new ArrayList<>();
        promotionFormulaList.add(new PromotionFormula("商品金额", MathUtils.formatCurrencyStr(chargeTotal.getOrderAmount())));


        List<PromotionFormula> chargePromotionFormulaList = chargeTotal.getPromotionFormulaList();
        if (chargeTotal.getDiscountAmount() > 0) {
            double discountAmount = MathUtils.round(chargeTotal.getDiscountAmount());
            chargePromotionFormulaList.add(new PromotionFormula("-", "活动金额", MathUtils.formatCurrencyStr(discountAmount)));
        }

        if (chargeTotal.getVipCutdownAmount() > 0) {
            chargePromotionFormulaList.add(new PromotionFormula("-", "VIP优惠", MathUtils.formatCurrencyStr(chargeTotal.getVipCutdownAmount())));
        }

        //合并
        promotionFormulaList.addAll(chargePromotionFormulaList);

        //
        String promotionFormulaStr = "总计" + MathUtils.formatCurrencyStr(chargeTotal.getLastOrderAmount()) + "=" + MyStringUtils.toString(promotionFormulaList);

        ShoppingChargeTotal shopping_cart_data = new ShoppingChargeTotal();

        shopping_cart_data.setOrder_amount(chargeTotal.getOrderAmount());
        shopping_cart_data.setLast_order_amount(chargeTotal.getLastOrderAmount());
        shopping_cart_data.setDiscount_amount(chargeTotal.getDiscountAmount());
        shopping_cart_data.setGoods_count(getAllGoodsCount());
        shopping_cart_data.setSelected_goods_count(chargeTotal.getSelectedGoodsCount());
        shopping_cart_data.setGain_yoho_coin(chargeTotal.getGainYohoCoin());
        shopping_cart_data.setPromotion_formula(promotionFormulaStr);
        shopping_cart_data.setStr_order_amount(MathUtils.formatCurrencyStr(chargeTotal.getOrderAmount()));
        shopping_cart_data.setStr_discount_amount(MathUtils.formatCurrencyStr(chargeTotal.getDiscountAmount()));
        shopping_cart_data.setShipping_cost(chargeTotal.getShippingCost());
        shopping_cart_data.setFast_shopping_cost(chargeTotal.getFastShoppingCost() + chargeTotal.getShippingCost());
        shopping_cart_data.setPromotion_formula_list(promotionFormulaList);


        ShoppingChargeResult chargeResult = new ShoppingChargeResult();
        chargeResult.setShopping_cart_data(shopping_cart_data);

        chargeResult.setMust_online_payment(chargeTotal.isMustOnlinePayment());
        chargeResult.setGoods_list(this.mergeChargedGoodsListByCartType());
        chargeResult.setPromotion_info(chargeTotal.getPromotionInfoList());

        //售罄商品
        chargeResult.setSold_out_goods_list(this.getSoldOutGoodsByCartType());

        //下架商品
        chargeResult.setOff_shelves_goods_list(this.getOffShelvesGoodsByCartType());

        //促销赠送商品
        chargeResult.setGift_list(this.giftList);
        //促销赠送加购价商品
        chargeResult.setPrice_gift(this.priceGiftList);

        return chargeResult;
    }

    private List<ShoppingGoods> getSoldOutGoodsByCartType() {
        List<ShoppingGoods> shoppingGoodsList = new ArrayList<ShoppingGoods>();
        if (Constants.ORDINARY_CART_TYPE.equals(this.getChargeParam().getCartType())) {
            if (CollectionUtils.isNotEmpty(this.soldOutGoods)) {
                for (ChargeGoods goods : this.soldOutGoods) {
                    shoppingGoodsList.add(goods.getShoppingGoods());
                }
            }
        } else {
            if (CollectionUtils.isNotEmpty(this.preSoldOutGoods)) {
                for (ChargeGoods goods : this.preSoldOutGoods) {
                    shoppingGoodsList.add(goods.getShoppingGoods());
                }
            }
        }
        return shoppingGoodsList;
    }

    private List<ShoppingGoods> getOffShelvesGoodsByCartType() {
        List<ShoppingGoods> shoppingGoodsList = new ArrayList<>();
        List<ChargeGoods> offShelvesGoods = null;
        if (Constants.ORDINARY_CART_TYPE.equals(this.getChargeParam().getCartType())) {
            offShelvesGoods = this.offShelvesGoods;
        } else {
            offShelvesGoods = this.preOffShelvesGoods;
        }
        if (CollectionUtils.isNotEmpty(offShelvesGoods)) {
            for (ChargeGoods goods : offShelvesGoods) {
                shoppingGoodsList.add(goods.getShoppingGoods());
            }
        }
        return shoppingGoodsList;
    }


    public List<ChargeGoods> getSelectedChargeGoodsByCartType() {
        List<ChargeGoods> chargeGoodsList = new ArrayList<>();
        chargeGoodsList.addAll(this.mainGoods);
        if (Constants.ORDINARY_CART_TYPE.equals(this.getChargeParam().getCartType())) {

            if (CollectionUtils.isNotEmpty(this.mainGoodsPriceGift)) {
                chargeGoodsList.addAll(this.mainGoodsPriceGift);
            }

            if (CollectionUtils.isNotEmpty(this.outletGoods)) {
                chargeGoodsList.addAll(this.outletGoods);
            }
        }

        List<ChargeGoods> selectedChargeGoodsList = new ArrayList<>();

        for (ChargeGoods chargeGoods : chargeGoodsList) {
            if (chargeGoods.isSelected()) {
                selectedChargeGoodsList.add(chargeGoods);
            }
        }
        return selectedChargeGoodsList;

    }


    /**
     * 合并算费后的商品列表
     */
    public List<ShoppingGoods> mergeChargedGoodsListByCartType() {
        List<ShoppingGoods> shoppingGoodsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(this.mainGoods)) {
            for (ChargeGoods goods : this.mainGoods) {
                shoppingGoodsList.add(goods.getShoppingGoods());
            }
        }
        if (Constants.ORDINARY_CART_TYPE.equals(this.getChargeParam().getCartType())) {
            if (CollectionUtils.isNotEmpty(this.mainGoodsGift)) {
                for (ChargeGoods goods : this.mainGoodsGift) {
                    shoppingGoodsList.add(goods.getShoppingGoods());
                }
            }

            if (CollectionUtils.isNotEmpty(this.mainGoodsPriceGift)) {
                for (ChargeGoods goods : this.mainGoodsPriceGift) {
                    shoppingGoodsList.add(goods.getShoppingGoods());
                }
            }

            if (CollectionUtils.isNotEmpty(this.outletGoods)) {
                for (ChargeGoods goods : this.outletGoods) {
                    shoppingGoodsList.add(goods.getShoppingGoods());
                }
            }
        }
        return shoppingGoodsList;
    }

    public void caculatePerSkuDiscount(double cutdownAmount, DiscountType type) {

        List<ChargeGoods> selectedMainGoodsList = findSelectedGoods(this.getMainGoods());

        BigDecimal totalAmount = totalAmount(selectedMainGoodsList);

        if (totalAmount.doubleValue() > 0) {
            this.caculatePerSkuDiscount(selectedMainGoodsList, totalAmount, cutdownAmount, type);
        }
    }

    public void caculatePerSkuDiscount(List<ChargeGoods> mainGoodsList, double cutdownAmount, DiscountType type) {
        BigDecimal totalAmount = totalAmount(mainGoodsList);
        if (totalAmount.doubleValue() > 0) {
            this.caculatePerSkuDiscount(mainGoodsList, totalAmount, cutdownAmount, type);
        }
    }

    /**
     *
     * @param mainGoodsList
     * @param totalAmount
     * @param cutdownAmount  yoho币是数量，其他为金额
     * @param type
     */
    public void caculatePerSkuDiscount(List<ChargeGoods> mainGoodsList, BigDecimal totalAmount, double cutdownAmount, DiscountType type) {
        BigDecimal discountAmountTotal = BigDecimal.valueOf(cutdownAmount);
        double realCutdownAmount=0.00;
        for (ChargeGoods chargeGoods : mainGoodsList) {
            double discountAmountPerSku = MathUtils.downToDouble(discountAmountTotal.multiply(chargeGoods.getRealPriceAfterDiscount()).abs().divide(totalAmount, 4, BigDecimal.ROUND_HALF_EVEN));
            chargeGoods.getDiscountPerSku().setDiscountAmount(discountAmountPerSku, type);

            //mark total cutdownAmount += > add?
            realCutdownAmount= YHMath.add(realCutdownAmount,YHMath.mul(chargeGoods.getDiscountPerSku().getCurrentDiscountAmount(type),chargeGoods.getBuyNumber()));
            calculateLog.info("[{}] caculate every sku discount,skn {},sku {},buyNumber {},real price {},totalAmount {},realCutdownAmount {},discountAmountTotal {},real price after discount {},DiscountType {},discount per sku {} ",
                    this.getChargeParam().getUid(),
                    chargeGoods.getShoppingGoods().getProduct_skn(),
                    chargeGoods.getShoppingGoods().getProduct_sku(),
                    chargeGoods.getShoppingGoods().getBuy_number(),
                    chargeGoods.getShoppingGoods().getReal_price(),
                    totalAmount,
                    realCutdownAmount,
                    discountAmountTotal,
                    chargeGoods.getRealPriceAfterDiscount(),
                    type,
                    chargeGoods.getDiscountPerSku());
        }

        double lostCutdownAmount;
        lostCutdownAmount = YHMath.sub(cutdownAmount, realCutdownAmount);
        if (type == DiscountType.YOHOCOIIN) {//yohoCoin需要转成钱
            try {
                lostCutdownAmount = YHMath.div(lostCutdownAmount, com.yoho.yhorder.common.utils.Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO);
            } catch (IllegalAccessException ex) {
                calculateLog.warn("call YHMath.div error,m is {},d is {}", lostCutdownAmount, com.yoho.yhorder.common.utils.Constants.YOHOCOIN_CURRENCY_DILUTION_RATIO, ex);
            }
        }
        //有丢失的精度 进行分摊
        if (lostCutdownAmount > 0) {
            calculateLog.info("need to split lost cutdown amount {} , type {}", lostCutdownAmount, type);
            caculatePerSkuDiscountForLost(mainGoodsList, lostCutdownAmount, type);
        }
    }


    /**
     * 分摊因为精度丢失的优惠金额
     * @param mainGoodsList
     * @param lostCutdownAmount
     * @param type
     */
    public void caculatePerSkuDiscountForLost(List<ChargeGoods> mainGoodsList, double lostCutdownAmount, DiscountType type) {

        Collections.sort(mainGoodsList, new Comparator<ChargeGoods>() {   //排序，先按照购买数量(从小到大) 然后根据价钱(由高到低)
            @Override
            public int compare(ChargeGoods o1, ChargeGoods o2) {
                int rel=0;
                if(o1.getBuyNumber()!=o2.getBuyNumber()){
                    rel=(o2.getBuyNumber()-o1.getBuyNumber())>0 ? -1 : 1;
                }else {
                    rel = o2.getRealPriceAfterDiscount().compareTo(o1.getRealPriceAfterDiscount());
                }
                return rel;
            }
        });

        List<ChargeGoods> splitGoodsList = new ArrayList<ChargeGoods>(); //记录拆分商品

        Iterator<ChargeGoods> it = mainGoodsList.iterator();

        while (it.hasNext() && lostCutdownAmount > 0) {
            ChargeGoods chargeGoods = it.next();
            int buyNum = chargeGoods.getBuyNumber();
            calculateLog.info("split lost cutdown amount iter : num: {}, realPrice: {}, discount: {}, left lostCutdownAmount {}",
                    chargeGoods.getBuyNumber(), chargeGoods.getRealPriceBigDecimal(), chargeGoods.getDiscountPerSku(), lostCutdownAmount);
            for (int i = 0; i < buyNum; i++) {//同一件SKU商品，最后一件使用原来的，其余的复制一份单独拆分出来
                ChargeGoods splitChargeGoods;
                if (i < buyNum - 1) {
                    splitChargeGoods = ChargeGoods.clone(chargeGoods);
                    splitChargeGoods.getShoppingGoods().setBuy_number("1");
                    chargeGoods.getShoppingGoods().setBuy_number("" + (buyNum - 1));
                    splitGoodsList.add(splitChargeGoods);           //拆分出来的商品添加到拆分列表，最后加到context的列表
                } else {
                    splitChargeGoods = chargeGoods;
                    chargeGoods.getShoppingGoods().setBuy_number("1");
                }

                double addLostCutdownAmount;
                if (splitChargeGoods.getRealPriceAfterDiscount().doubleValue() >= lostCutdownAmount) {
                    addLostCutdownAmount = lostCutdownAmount;
                    lostCutdownAmount = 0;
                } else {
                    addLostCutdownAmount = splitChargeGoods.getRealPriceAfterDiscount().doubleValue();
                    lostCutdownAmount = YHMath.sub(lostCutdownAmount, addLostCutdownAmount);
                }

                if (type == DiscountType.YOHOCOIIN) {
                    splitChargeGoods.getDiscountPerSku().setYohoCoinDiscountAmountByAmount(addLostCutdownAmount);
                } else {
                    splitChargeGoods.getDiscountPerSku().setDiscountAmount(addLostCutdownAmount, type);
                }
                if (lostCutdownAmount <= 0)
                    break;
            }
        }

        if (lostCutdownAmount > 0) {
            calculateLog.warn("split lost cutdown amount last leave {}", lostCutdownAmount);
        }

        mainGoodsList.addAll(splitGoodsList);

        for (ChargeGoods newChargeGoods : splitGoodsList) {
            String goodsType = newChargeGoods.getShoppingGoods().getGoods_type();
            calculateLog.info("split lost cutdown amount last : num: {}, realPrice: {}, discount: {}, goodsType:{} ",
                    newChargeGoods.getBuyNumber(), newChargeGoods.getRealPriceBigDecimal(), newChargeGoods.getDiscountPerSku(), goodsType);
            if (goodsType.equals(Constants.ORDER_GOODS_TYPE_PRICE_GIFT_STR)) {
                this.mainGoodsPriceGift.add(newChargeGoods);
            } else if (goodsType.equals(Constants.ORDER_GOODS_TYPE_OUTLET_STR)) {
                this.outletGoods.add(newChargeGoods);
            } else if (goodsType.equals(Constants.ORDER_GOODS_TYPE_ORDINARY_STR)) {
                this.mainGoods.add(newChargeGoods);
            } else {
                calculateLog.warn("split lost cutdown amount unkown goods type : {}", newChargeGoods);
            }
        }
    }

    private List<ChargeGoods> findSelectedGoods(List<ChargeGoods> mainGoodsList) {
        List<ChargeGoods> selectedMainGoodsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mainGoodsList)) {
            for (ChargeGoods chargeGoods : mainGoodsList) {
                if (chargeGoods.isSelected()) {
                    selectedMainGoodsList.add(chargeGoods);
                }
            }
        }
        return selectedMainGoodsList;
    }

    private BigDecimal totalAmount(List<ChargeGoods> mainGoodsList) {
        BigDecimal totalAmount = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(mainGoodsList)) {
            for (ChargeGoods chargeGoods : mainGoodsList) {
                if (chargeGoods.isSelected()) {
                    totalAmount = totalAmount.add(chargeGoods.getRealPriceTotalBigDecimalAfterDiscount());
                }
            }
        }
        return totalAmount;
    }

    /**
     * 未售罄商品+售罄的商品
     * @return
     */
    private int getAllGoodsCount() {
        //
        List<ChargeGoods> soldOutGoodsList = null;
        List<ChargeGoods> offShelvesGoods = null;
        if (Constants.ORDINARY_CART_TYPE.equals(this.getChargeParam().getCartType())) {
            soldOutGoodsList = this.soldOutGoods;
            offShelvesGoods = this.offShelvesGoods;
        } else {
            soldOutGoodsList = this.preSoldOutGoods;
            offShelvesGoods = this.preOffShelvesGoods;
        }
        int nonSoldoutGoodsCount = chargeTotal.getGoodsCount();
        int soldoutGoodsCount = 0;
        if (CollectionUtils.isNotEmpty(soldOutGoodsList)) {
            for (ChargeGoods goods : soldOutGoodsList) {
                soldoutGoodsCount += goods.getBuyNumber();
            }
        }

        int offShelvesGoodsCount = 0;
        if (CollectionUtils.isNotEmpty(offShelvesGoods)) {
            for (ChargeGoods goods : offShelvesGoods) {
                offShelvesGoodsCount += goods.getBuyNumber();
            }
        }
        return nonSoldoutGoodsCount + soldoutGoodsCount + offShelvesGoodsCount;
    }

    private void setupJitAttributeForChargeParam()
    {
        List<ShoppingGoods>  shoppingGoodsList =  mergeChargedGoodsListByCartType();
        for(ShoppingGoods shoppingGoods :shoppingGoodsList)
        {
            if ("Y".equals(shoppingGoods.getIs_jit())) {
                chargeParam.setIsJit(true);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this);
    }

}

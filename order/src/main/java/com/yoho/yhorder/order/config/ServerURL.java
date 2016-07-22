package com.yoho.yhorder.order.config;

/**
 * url地址key配置
 * @author lijian
 *
 */
public interface ServerURL {

    //批量查询产品信息
    public   static  final String PRODUCT_BATCH_QUERY_PRODUCT_BASIC_INFO="product.batchQueryProductBasicInfo";

    //批量查询产品信息
    public   static  final String PRODUCT_BATCH_QUERY_CHANGE_PRODUCT_SKC_INFO="product.queryChangeProductByIds";

    //批量查询商品信息
    public   static  final String PRODUCT_BATCH_QUERY_GOODS_INFO="product.batchQueryGoodsById";

    //批量查询产品库存型号
    public   static  final String PRODUCT_BATCH_QUERY_CHANGE_PRODUCT_SKU="product.queryBatchChangeGoods";

    //根据区域code查询区域信息
    public  static  final String USERS_GET_AREA_INFO_BY_CODE="users.getAreaByCode";

    public  static  final String USERS_GET_VIP_INFO="users.getVipDetailInfo";

    //根据addressId获取用户地址详细信息
    public final static String USERS_QUERY_ADDRESS_REST_URL = "users.getAddress";

    //erp系统地址后缀 生成换货订单
    public  static  final String ERP_SAVE_EXCHANGE_ORDER="/api/exchange/create";
    //erp系统地址后缀 生成换货物流信息
    public  static  final String ERP_SAVE_EXCHANGE_EXPRESS="/api/exchange/express";

}

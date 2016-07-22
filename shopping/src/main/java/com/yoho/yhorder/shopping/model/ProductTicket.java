package com.yoho.yhorder.shopping.model;

import lombok.Data;

/**
 * 
 * @author wangshijie 2016/05/16
 *
 */
@Data
public class ProductTicket {
    /**
     * 购物车KEY
     * @var string
     */
	private String shopping_key = "";

    /**
     * 购物车ID
     * @var int
     */
	private int shopping_id = 0;

    /**
     * 商品SKU
     * @var int
     */
	private int product_sku = 0;

    /**
     * 购买数量
     * @var int
     */
	private int buy_number = 1;

    /**
     * UID
     * @var
     */
	private int uid;

    /**
     * 商品类型
     * @var int
     */
	private int product_type = 1;

    /**
     * 商品名称
     * @var string
     */
	private String product_name = "";

    /**
     * 库存数量
     * @var int
     */
	private int storage_number = 0;
	private int product_skn = 0;
	private double useYohoCoin = 0.0;

}

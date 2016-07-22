package com.yoho.yhorder.order.config;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * 订单状态配置
 * @author lijian
 *
 */
public final class OrderConfig {

    /**
     * 换货状态
     * @var unknown
     */

	private static Map<Integer,String> changeStatu=Maps.newConcurrentMap();

    /**
     * 送货时间
     * @var unknown
     */

    private static Map<Integer,String> receiptTime=Maps.newConcurrentMap();

    public static Map<Integer, String> getReceiptTime() {
        return receiptTime;
    }



    /*
    * 换货方式
    * */
    public static  final Integer SEND_TYPE_ID=10;

    public static  final String DOOR_TYPE="上门换货";

    public static  final String SEND_TYPE="寄回换货";

    public static  final String CHANGE_REMARK="用户换货";

    public static  final String SAVE_CHANGE_REMARK="用户申请换货";

    static
	{
        initChangeStatu();
        initReceiptTime();
 	}

    public static Map<Integer, String> getChangeStatu() {
        return Collections.unmodifiableMap(changeStatu);
    }

    public synchronized static void initChangeStatu()
	{
        changeStatu.put( 0, "提交申请");
        changeStatu.put( 10, "审核通过");
        changeStatu.put( 20, "商品寄回");
        changeStatu.put( 30, "商品入库");
        changeStatu.put( 40, "换货发出");
        changeStatu.put( 50, "换货完成");
        changeStatu.put( 91, "已取消");
	}


    public synchronized static void initReceiptTime()
    {
        receiptTime.put(1, "只工作日送货(双休日、节假日不用送");
        receiptTime.put(2, "工作日、双休日和节假日均送货");
        receiptTime.put(3, "只双休日、节假日送货(工作时间不送货)");
    }


}

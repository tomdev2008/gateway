package com.yoho.error.event;

/**
 *  支付事件: 支持支付宝和银联支付
 *
 * Created by chunhua.zhang@yoho.cn on 2016/2/19.
 */
public class PaymentEvent extends  CommonEvent {

    /**
	 * @Fields serialVersionUID 
	 */
	private static final long serialVersionUID = 1519723141207084976L;
	private final String payType;
    private final String out_trade_no;
    private final String trade_no;
    private final String total_fee;
    private final String trade_status;
    private String status;
    /**
     * Create a new ApplicationEvent.
     * @param payType 支付类型： alipay or unionpay
     * @param out_trade_no  订单号
     * @param trade_no 平台相关的交易号
     * @param total_fee 总金额
     * @param trade_status  处理状态
     *
     */
    public PaymentEvent(String payType, String out_trade_no, String trade_no, String total_fee, String trade_status) {
        super("payment");
        this.out_trade_no = out_trade_no;
        this.trade_no = trade_no;
        this.total_fee = total_fee;
        this.payType = payType;
        this.trade_status = trade_status;
        this.status = "INIT";
    }


    public void setStatus(String status) {
        this.status = status;
    }
    public String getPayType() {
        return payType;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public String getTrade_no() {
        return trade_no;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public String getTrade_status() {
        return trade_status;
    }

    public String getStatus() {
        return status;
    }


}

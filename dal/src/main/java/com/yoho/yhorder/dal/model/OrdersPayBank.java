package com.yoho.yhorder.dal.model;

public class OrdersPayBank {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column orders_pay_bank.id
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column orders_pay_bank.order_code
     *
     * @mbggenerated
     */
    private Long orderCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column orders_pay_bank.payment
     *
     * @mbggenerated
     */
    private Byte payment;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column orders_pay_bank.bank_code
     *
     * @mbggenerated
     */
    private String bankCode;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column orders_pay_bank.id
     *
     * @return the value of orders_pay_bank.id
     *
     * @mbggenerated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column orders_pay_bank.id
     *
     * @param id the value for orders_pay_bank.id
     *
     * @mbggenerated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column orders_pay_bank.order_code
     *
     * @return the value of orders_pay_bank.order_code
     *
     * @mbggenerated
     */
    public Long getOrderCode() {
        return orderCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column orders_pay_bank.order_code
     *
     * @param orderCode the value for orders_pay_bank.order_code
     *
     * @mbggenerated
     */
    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column orders_pay_bank.payment
     *
     * @return the value of orders_pay_bank.payment
     *
     * @mbggenerated
     */
    public Byte getPayment() {
        return payment;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column orders_pay_bank.payment
     *
     * @param payment the value for orders_pay_bank.payment
     *
     * @mbggenerated
     */
    public void setPayment(Byte payment) {
        this.payment = payment;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column orders_pay_bank.bank_code
     *
     * @return the value of orders_pay_bank.bank_code
     *
     * @mbggenerated
     */
    public String getBankCode() {
        return bankCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column orders_pay_bank.bank_code
     *
     * @param bankCode the value for orders_pay_bank.bank_code
     *
     * @mbggenerated
     */
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode == null ? null : bankCode.trim();
    }
}
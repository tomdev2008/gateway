package com.yoho.yhorder.invoice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yoho.error.ServiceError;
import com.yoho.error.exception.ServiceException;
import com.yoho.service.model.order.constants.ElecInvoiceType;
import com.yoho.service.model.order.constants.OrderStatus;
import com.yoho.service.model.order.constants.OrdersMateKey;
import com.yoho.service.model.order.model.invoice.GoodsItemBo;
import com.yoho.service.model.order.model.invoice.InvoiceAmount;
import com.yoho.service.model.order.model.invoice.InvoiceBo;
import com.yoho.service.model.order.model.invoice.OrderInvoiceBo;
import com.yoho.yhorder.dal.IOrdersMetaDAO;
import com.yoho.yhorder.dal.InvoiceLogsMapper;
import com.yoho.yhorder.dal.model.InvoiceLogs;
import com.yoho.yhorder.dal.model.OrdersMeta;
import com.yoho.yhorder.invoice.helper.DigitHelper;
import com.yoho.yhorder.invoice.helper.InvoiceCalculator;
import com.yoho.yhorder.invoice.helper.InvoiceSequenceGenerator;
import com.yoho.yhorder.invoice.helper.MapUtil;
import com.yoho.yhorder.invoice.model.InvoiceProxy;
import com.yoho.yhorder.invoice.service.InvoiceService;
import com.yoho.yhorder.invoice.service.merger.InvoiceMerger;
import com.yoho.yhorder.invoice.webservice.SoapClient;
import com.yoho.yhorder.invoice.webservice.constant.InterfaceCode;
import com.yoho.yhorder.invoice.webservice.constant.InvoiceSoapErrorCode;
import com.yoho.yhorder.invoice.webservice.manager.XStreamManager;
import com.yoho.yhorder.invoice.webservice.xmlbean.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchao on 2016/6/14.
 */
@Service
public class InvoiceServiceImpl implements InvoiceService{
    private static final Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);
    /**
     * 流水号分段头部标识
     */
    private static final String series_blueinvoice = "b";
    private static final String series_redinvoice = "r";
    /**
     * 0-蓝字发票；1-红字发票
     */
    private String issue_invoice_type_blue = "0";
    private String issue_invoice_type_red = "1";

    /**
     * 销售方纳税人识别号
     */
    @Value("${sales.tax.payers:9132010058047114XR}")
    private String sales_tax_payers;

    /**
     * 销售方名称
     */
    @Value("${seller.name:有货（江苏）商贸服务有限公司}")
    private String seller_name;

    /**
     * 销售方地址、电话
     */
    @Value("${seller.contact.info:南京市建邺区嘉陵江东街18号05幢17、18层 025-87781000}")
    private String seller_contact_info;

    /**
     * 开票人
     */
    @Value("${issuer.name:有货}")
    private String issuer_name;

    /**
     * 税率
     */
    @Value("${tax.rate:0.17}")
    private double tax_rate;

    @Autowired
    private IOrdersMetaDAO ordersMetaDAO;

    @Autowired
    private SoapClient cxfClient;

    /**
     * 日志表
     */
    @Autowired
    private InvoiceLogsMapper invoiceLogsMapper;


    @Override
    public int add(InvoiceBo invoiceBo) {
        if (invoiceBo.getOrderId() <= 0) {
            logger.error("in InvoiceServiceImpl.add param invoiceBo {}", invoiceBo);
            return 0;
        }
        OrdersMeta ordersMeta = new OrdersMeta();
        ordersMeta.setUid(invoiceBo.getUid());
        ordersMeta.setOrderCode(invoiceBo.getOrderCode());
        ordersMeta.setOrdersId(invoiceBo.getOrderId());
        ordersMeta.setMetaKey(OrdersMateKey.ELECTRONIC_INVOICE);
        ordersMeta.setMetaValue(JSONObject.toJSONString(invoiceBo));
        return ordersMetaDAO.insert(ordersMeta);
    }

    @Override
    public InvoiceBo queryByOrderId(int orderId) {
        InvoiceBo invoiceBo = null;
        if (orderId <= 0) {
            logger.error("in InvoiceServiceImpl.queryByOrderId param orderId {}", orderId);
            return invoiceBo;
        }
        OrdersMeta ordersMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orderId, OrdersMateKey.ELECTRONIC_INVOICE);
        if (ordersMeta != null && StringUtils.isNotBlank(ordersMeta.getMetaValue())) {
            invoiceBo = JSONObject.parseObject(ordersMeta.getMetaValue(), InvoiceBo.class);
        }
        return invoiceBo;
    }

    @Override
    public InvoiceBo queryByOrderIdNUserid(int orderId, int uid) {
        InvoiceBo invoiceBo = null;
        if (orderId<=0){
            logger.error("in InvoiceServiceImpl.queryByOrderId param orderId {}",orderId);
            return invoiceBo;
        }
        OrdersMeta ordersMeta = ordersMetaDAO.selectByOrdersIdAndMetaKey(orderId, OrdersMateKey.ELECTRONIC_INVOICE);
        if (ordersMeta != null && StringUtils.isNotBlank(ordersMeta.getMetaValue())){
            invoiceBo = JSONObject.parseObject(ordersMeta.getMetaValue(), InvoiceBo.class);
        }
        return invoiceBo;
    }

    @Override
    public List<InvoiceBo> queryByOrderIds(List<Integer> orderIds) {

        return null;
    }

    @Override
    public int update(InvoiceBo invoiceBo) {
        List<Integer> ordersIds = Lists.newArrayList();
        ordersIds.add(invoiceBo.getOrderId());
        return ordersMetaDAO.updateMetaValueByOrdersIdsAndMetaKey(ordersIds, OrdersMateKey.ELECTRONIC_INVOICE, JSONObject.toJSONString(invoiceBo));
    }

    @Override
    public InvoiceBo issueInvoice(OrderInvoiceBo orderInvoice) {
        logger.info("in issueInvoice, in param orderInvoice {}", orderInvoice);
        if (orderInvoice == null) {
            String errorInfo = "in issueInvoice in param orderInvoice is null";
            logger.error(errorInfo);
            throw new IllegalArgumentException(errorInfo);
        }
        //订单id
        if (orderInvoice.getOrderId() <= 0) {
            logger.error("InvoiceServiceImpl.issueInvoice param orderInvoice {}", orderInvoice);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        //货品明细,正常单全部货品，退货是用户退掉的货品
        if (CollectionUtils.isEmpty(orderInvoice.getGoodsItemList())) {
            logger.error("InvoiceServiceImpl.issueInvoice param orderInvoice.getGoodsItemList {}", orderInvoice.getGoodsItemList());
            throw new ServiceException(ServiceError.ORDER_INVOICE_GOODSITEMS_EMPTY);
        }
        //发票基本数据（来自用户或开票后的补全）
        final InvoiceBo invoice = queryByOrderId(orderInvoice.getOrderId());
        if (invoice == null) {
            logger.error("InvoiceServiceImpl.issueInvoice queryByOrderId {}", invoice);
            throw new ServiceException(ServiceError.ORDER_INVOICE_NOT_ELETRONIC);
        }
        //正常下单 退货 标示， very important
        if (orderInvoice.getOrderStatus() == null) {
            logger.error("InvoiceServiceImpl.issueInvoice getOrderStatus.getOrderStatus {}", orderInvoice.getOrderStatus());
            throw new ServiceException(ServiceError.ORDER_INVOICE_ORDERSTATUS_NULL);
        }

        //组装xml字符串，准备发起soap
        InvoiceProxy invoiceProxy = buildBlueInvoiceReq(invoice, orderInvoice);
        CommonFPKJReq fpkjReq = invoiceProxy.getFpkjReq();

        // 生成发票
        FpkjResp fpkjResp = buildFpkjResp(fpkjReq);
        logger.info("issueInvoice buildFpkjResp fpkjResp {}", fpkjResp);
        //当完成发票开具后回填数据库
        if (fpkjResp != null) {
            if (fpkjResp.getReturnStateInfo() != null) {
                invoice.setReturnCode(fpkjResp.getReturnStateInfo().getReturnCode());
                invoice.setReturnMessage(fpkjResp.getReturnStateInfo().getReturnMessage());
            }
            if (fpkjResp.getIssueSuccess()) {
                logger.debug("do persistent invoice");
                //发票信息回填DB
                orderInvoice.setAmount(fpkjReq.getTotalValoremTax());
                orderInvoice.setGoodsItemList(invoiceProxy.getGoodsItemList());
                invoice.setOrderInvoice(orderInvoice);
                //
                invoice.setInvoiceAmount(invoiceProxy.getInvoiceAmount());
                InvoiceMerger.mergeFpkjResp(invoice, fpkjResp);
                //蓝票流水号
                invoice.setLastBlueSequence(fpkjReq.getFpReqSeriseNum());
                //拆单时订单ID发生错误，这里可以补救一次
                invoice.setOrderId(orderInvoice.getOrderId());
                invoice.setShowInvoice(true);
                int row = update(invoice);
                logger.debug("in issueInvoice do update,row {} , order id {}", row, invoice.getOrderId());
            }
            //记录日志
            InvoiceLogs invoiceLogs = new InvoiceLogs();
            invoiceLogs.setOrderId(orderInvoice.getOrderId());
            invoiceLogs.setReqParam(JSONObject.toJSONString(orderInvoice));
            invoiceLogs.setInvoiceInfo(JSONObject.toJSONString(invoice));
            invoiceLogs.setSeriesNum(fpkjReq.getFpReqSeriseNum());
            invoiceLogs.setType(ElecInvoiceType.blue.getIntVal());
            invoiceLogs.setCreateTime(getCurrentTimeSeconds());
            if (InvoiceSoapErrorCode.SUCCESS_KP.equalsIgnoreCase(invoice.getReturnCode())){
                invoiceLogs.setIssueStatus(InvoiceSoapErrorCode.ISSUE_SUCCESS);
            }else{
                invoiceLogs.setIssueStatus(InvoiceSoapErrorCode.ISSUR_FAIL);
            }
            invoiceLogsMapper.insert(invoiceLogs);
        }

        return invoice;
    }

    @Override
    public InvoiceBo redInvoice(OrderInvoiceBo orderInvoice) {
        /*
         * 第一步，校验
         */
        if (orderInvoice == null) {
            String errorInfo = "in redInvoice in param orderInvoice is null";
            logger.error(errorInfo);
            throw new IllegalArgumentException(errorInfo);
        }
        //订单id
        if (orderInvoice.getOrderId() <= 0) {
            logger.error("InvoiceServiceImpl.redInvoice param orderInvoice {}", orderInvoice);
            throw new ServiceException(ServiceError.ORDER_NULL);
        }
        //发票基本数据（来自用户或开票后的补全）
        InvoiceBo invoice = queryByOrderId(orderInvoice.getOrderId());
        logger.info("in redInvoice，fetch from db by orderId {}, invoice {}", orderInvoice.getOrderId(), invoice);
        if (invoice == null) {
            logger.error("InvoiceServiceImpl.redInvoice invoice is null");
            throw new ServiceException(ServiceError.ORDER_INVOICE_NOT_ELETRONIC);
        }
        /*
        第二步，组装xml字符串，准备发起soap
         */
        //2.1 组装xml字符串
        CommonFPKJReq fpkjReq = buildRedInvoiceReq(invoice);
        logger.info("refund invoice fpkjReq {}", fpkjReq);
        //2.2 发起soap请求，生成发票
        FpkjResp fpkjResp = buildFpkjResp(fpkjReq);
        /*
        第三步，记录发票修改信息
         */
        //记录当前冲红的流水号
        if (fpkjResp != null) {
            if (fpkjResp.getReturnStateInfo() != null) {
                invoice.setReturnCode(fpkjResp.getReturnStateInfo().getReturnCode());
                invoice.setReturnMessage(fpkjResp.getReturnStateInfo().getReturnMessage());
            }
            if (fpkjResp.getIssueSuccess()) {
                //发票信息回填DB
                InvoiceMerger.mergeFpkjResp(invoice, fpkjResp);
                invoice.setLastRedSequence(fpkjReq.getFpReqSeriseNum());
                invoice.setShowInvoice(false);
                update(invoice);
            }
            //记录日志
            InvoiceLogs invoiceLogs = new InvoiceLogs();
            invoiceLogs.setOrderId(orderInvoice.getOrderId());
            invoiceLogs.setReqParam(JSONObject.toJSONString(orderInvoice));
            invoiceLogs.setInvoiceInfo(JSONObject.toJSONString(invoice));
            invoiceLogs.setSeriesNum(fpkjReq.getFpReqSeriseNum());
            invoiceLogs.setType(ElecInvoiceType.red.getIntVal());
            invoiceLogs.setCreateTime(getCurrentTimeSeconds());
            if (InvoiceSoapErrorCode.SUCCESS_KP.equalsIgnoreCase(invoice.getReturnCode())){
                invoiceLogs.setIssueStatus(InvoiceSoapErrorCode.ISSUE_SUCCESS);
            }else{
                invoiceLogs.setIssueStatus(InvoiceSoapErrorCode.ISSUR_FAIL);
            }
            invoiceLogsMapper.insert(invoiceLogs);
        }
        return invoice;
    }

    /**
     * 组装蓝票请求
     *
     * @param invoice
     * @return
     */
    private InvoiceProxy buildBlueInvoiceReq(InvoiceBo invoice, OrderInvoiceBo orderInvoice) {
        InvoiceProxy invoiceProxy = new InvoiceProxy();
        CommonFPKJReq fpkjReq = new CommonFPKJReq();

        //实际总价，用户的实际付款
        double actualAmount = 0D;
        List<GoodsItemBo> goodsItemList = Lists.newArrayList();
        String sequence = "";
        //正常下单
        if(orderInvoice.getOrderStatus() == OrderStatus.normal){
            //实际存在运费，且支付金额大于
            if (orderInvoice.getShippingCost() != null && orderInvoice.getAmount() > orderInvoice.getShippingCost()){
                actualAmount = InvoiceCalculator.calculateSubtract(orderInvoice.getAmount(),orderInvoice.getShippingCost()).doubleValue();
                actualAmount = DigitHelper.formatDouble(actualAmount, 2);
            }
            if (orderInvoice.getShippingCost() == null || orderInvoice.getShippingCost()> orderInvoice.getAmount()){
                actualAmount = orderInvoice.getAmount();
            }
            goodsItemList = orderInvoice.getGoodsItemList();
            sequence = new StringBuilder(series_blueinvoice)
                    .append(InvoiceSequenceGenerator.seperator)
                    .append(InvoiceSequenceGenerator.newSequence(orderInvoice.getOrderId())).toString();
        }
        //退单
        if (orderInvoice.getOrderStatus() == OrderStatus.refund) {
            //之前的金额,冲红回写数据库中是负数，做一个保护
            double preOrderAmount = invoice.getOrderInvoice().getAmount() < 0 ? -invoice.getOrderInvoice().getAmount()
                    : invoice.getOrderInvoice().getAmount();
            //这里退款的金额不会大于实际支付的金额，有货币啥的照数返还
            actualAmount = InvoiceCalculator.calculateSubtract(preOrderAmount, orderInvoice.getRefoundAmount()).doubleValue() ;
            actualAmount = DigitHelper.formatDouble(actualAmount,2);
            //将退货商品移除，计算退货的金额
            goodsItemList = invoice.getOrderInvoice().getGoodsItemList();
            removeRefoundGoods(goodsItemList, orderInvoice.getGoodsItemList());
            //流水号
            sequence = InvoiceSequenceGenerator.nextSequence(invoice.getLastBlueSequence(), 1);
        }
        if (actualAmount == 0D){
            logger.error("actualAmount is 0, can't issue invoice");
            throw new ServiceException(ServiceError.ORDER_INVOICE_ACTUAL_AMOUNT_IS_ZERO);
        }
        //发票请求流水号	20	是 ,直接使用订单号
        fpkjReq.setFpReqSeriseNum(sequence);
        //开票类型	1	是 0-蓝字发票；1-红字发票
        fpkjReq.setKpType(issue_invoice_type_blue);

        buildInvoiceReqBaseInfo(fpkjReq, invoice);

        CommonFPKJProject fpkjProject = new CommonFPKJProject();
        logger.debug("before calculateBlueInvoiceAmount, goodsItemList {}", goodsItemList);
        InvoiceAmount invoiceAmount ;
        invoiceAmount = InvoiceCalculator.calculateBlueInvoiceAmount(goodsItemList, actualAmount, tax_rate);
        logger.debug("after calculateBlueInvoiceAmount,  goodsItemList {}", goodsItemList);
        //价税合计		是
        fpkjReq.setTotalValoremTax(actualAmount);
        //合计金额		是
        fpkjReq.setCombinedAmount(invoiceAmount.getTotalProjectAmount());
        //合计税额		是
        fpkjReq.setCombinedTax(invoiceAmount.getTotalTaxAmount());
        List<ProjectDetail> projectDetails = buildProjectDetails(goodsItemList, invoiceAmount.getHasDiscount());
        fpkjProject.setSize(projectDetails.size());
        fpkjProject.setProjectDetails(projectDetails);
        //
        fpkjReq.setProject(fpkjProject);
        //at last, load all object ,return
        invoiceProxy.setFpkjReq(fpkjReq);
        invoiceProxy.setGoodsItemList(goodsItemList);
        invoiceProxy.setInvoiceAmount(invoiceAmount);
        return invoiceProxy;
    }

    /**
     * 发票开具响应消息
     *
     * @param fpkjReq
     * @return
     */
    private FpkjResp buildFpkjResp(CommonFPKJReq fpkjReq) {
        FpkjResp fpkjResp = null;
        ReturnStateInfo returnStateInfo;
        try {
            XStreamManager.getXstream().alias(ProjectDetail.class.getSimpleName().toLowerCase(), ProjectDetail.class);
            XStreamManager.getXstream().aliasSystemAttribute(null, "class"); // 去掉 class 属性
            String soapParam = XStreamManager.getXstream().toXML(fpkjReq);
            logger.debug("soapParam in issueInvoice {}", soapParam);
            String respStr = cxfClient.httpsRequest(InterfaceCode.ISSUE, soapParam);
            logger.debug("get invoice resp from http {}", respStr);
            XStreamManager.getXstream().alias("interface", CommonResp.RespInterface.class);
            CommonResp.RespInterface resp = (CommonResp.RespInterface) XStreamManager.getXstream().fromXML(respStr);
            logger.debug("xml 2 javabean resp is {}", resp);
            returnStateInfo = resp.getReturnStateInfo();
            if (returnStateInfo != null) {
                if (InvoiceSoapErrorCode.SUCCESS_KP.equalsIgnoreCase(returnStateInfo.getReturnCode())) {
                    String content = resp.getData().getContent();
                    byte[] content_hr = new BASE64Decoder().decodeBuffer(content);
                    String contentXml = new String(content_hr);
                    logger.debug("contentXml is {}", contentXml);
                    XStreamManager.getXstream().alias("RESPONSE", FpkjResp.class);
                    fpkjResp = (FpkjResp) XStreamManager.getXstream().fromXML(contentXml);
                    fpkjResp.setIssueSuccess(true);
                    logger.debug("fpkjResp is {}", fpkjResp);
                }else{
                    fpkjResp = new FpkjResp();
                }
                fpkjResp.setReturnStateInfo(returnStateInfo);
                logger.info("buildFpkjResp without exception");
            }
        } catch (Exception e) {
            returnStateInfo = new ReturnStateInfo();
            returnStateInfo.setReturnCode(InvoiceSoapErrorCode.FAIL_KP);
            returnStateInfo.setReturnMessage(e.getMessage());
            fpkjResp = new FpkjResp();
            fpkjResp.setReturnStateInfo(returnStateInfo);
            logger.error("issue invoice by https connect to baiwang(vendor) occurs error {}", e);
        }finally {
            logger.info("in buildFpkjResp fpkjResp {}", fpkjResp);
            return fpkjResp;
        }
    }

    /**
     * 组装红票请求
     * @param invoice
     * @return
     */
    private CommonFPKJReq buildRedInvoiceReq(InvoiceBo invoice) {
        CommonFPKJReq fpkjReq = new CommonFPKJReq();
        String sequence;
        if (StringUtils.isBlank(invoice.getLastRedSequence())) {//第一次冲红
            sequence = new StringBuilder(series_redinvoice)
                    .append(InvoiceSequenceGenerator.seperator)
                    .append(InvoiceSequenceGenerator.newSequence(invoice.getOrderId())).toString();
        } else {//不是第一次冲红
            sequence = InvoiceSequenceGenerator.nextSequence(invoice.getLastRedSequence(), 1);
        }

        //发票请求流水号	20	是 ,直接使用订单号
        fpkjReq.setFpReqSeriseNum(sequence);
        //开票类型	1	是 0-蓝字发票；1-红字发票
        fpkjReq.setKpType(issue_invoice_type_red);
        //填充基本信息
        buildInvoiceReqBaseInfo(fpkjReq, invoice);

        //原发票代码	12
        fpkjReq.setOriginalInvoiceCode(invoice.getInvoiceCode());
        //原发票号码	8
        fpkjReq.setOriginalInvoiceNum(invoice.getInvoiceNum());

        CommonFPKJProject fpkjProject = new CommonFPKJProject();
        List<GoodsItemBo> goodsItemList = invoice.getOrderInvoice().getGoodsItemList();

        //移除打折项目
        removeDiscountItem(goodsItemList);

        InvoiceCalculator.calculateRedInvoiceAmount(goodsItemList,
                invoice.getInvoiceAmount(), tax_rate);

        double actualAmount = invoice.getInvoiceAmount().getActualAmount();

        //价税合计		是
        fpkjReq.setTotalValoremTax(-actualAmount);

        InvoiceAmount invoiceAmount = invoice.getInvoiceAmount();
        //合计金额		是
        fpkjReq.setCombinedAmount(-invoiceAmount.getTotalProjectAmount());
        //合计税额		是
        fpkjReq.setCombinedTax(-invoiceAmount.getTotalTaxAmount());
        List<ProjectDetail> projectDetails = buildRedProjectDetails(goodsItemList);
        fpkjProject.setSize(projectDetails.size());
        fpkjProject.setProjectDetails(projectDetails);
        //
        fpkjReq.setProject(fpkjProject);
        return fpkjReq;
    }

    /**
     * 生成发票的请求消息体的基本信息
     *
     * @param fpkjReq
     * @param invoice
     */
    private void buildInvoiceReqBaseInfo(CommonFPKJReq fpkjReq, InvoiceBo invoice) {
        //销售方纳税人识别号	20	是
        fpkjReq.setSalesTaxpayerId(sales_tax_payers);
        //销售方名称	100	是
        fpkjReq.setSalesName(seller_name);
        //销售方地址、电话	100	是
        fpkjReq.setSalesAddrTel(seller_contact_info);
        //销售方银行账号	100	否
        //购买方纳税人识别号	20	否

        //购买方名称	100	是
        if (StringUtils.isBlank(invoice.getTitle())) {
            logger.error("InvoiceServiceImpl.issueInvoice queryByOrderId {}", invoice);
            throw new ServiceException(ServiceError.ORDER_INVOICE_HEADER_BLANK);
        }
        String buyerName = invoice.getTitle().length() > 100 ? invoice.getTitle().substring(0, 100) : invoice.getTitle();
        fpkjReq.setBuyerName(buyerName);
        //购买方地址、电话	100	否
        //购买方银行账号	100	否
        //购买方手机号	48	否 购买方手机号与电子邮箱不能同时为空
        fpkjReq.setBuyerTel(invoice.getMobilePhone());
        //购买方电子邮箱	100	否
        //购买方发票通平台账户	100	否
        //微信openId 50 否
        //开票人	8	是
        fpkjReq.setBiller(issuer_name);
        //收款人	8	否
        //复核人	8	否

        //原发票代码	12
        //原发票号码	8

        //备注	130	否
        //行业类型		是
        fpkjReq.setIndustryType("0");
    }

    /**
     * convert entity
     * GoodsItemBo -> ProjectDetail
     *
     * @param goodsItemList
     * @return
     */
    private List<ProjectDetail> buildProjectDetails(final List<GoodsItemBo> goodsItemList, boolean hasDiscount) {
        List<ProjectDetail> projectDetails = Lists.newLinkedList();
        ProjectDetail projectDetail;
        int invoiceLineNature_item = 0;
        if (hasDiscount) {
            invoiceLineNature_item = 2;
        }
        for (GoodsItemBo goodsItem : goodsItemList) {
            projectDetail = new ProjectDetail();
            if (goodsItem.getSkn().equals(InvoiceCalculator.discountLine_skn)) {//折扣行
                //发票行性质	1	是 0正常行、1折扣行、2被折扣行
                projectDetail.setInvoiceLineNature(1);
                //项目名称	90	是
                projectDetail.setProjectName(goodsItem.getPrductName());
                //税额 是 单位：元（2位小数）
                projectDetail.setTaxAmount(goodsItem.getTaxAmount());
                //项目金额 是 不含税，单位：元（2位小数）
                projectDetail.setProjectAmount(goodsItem.getAmountWithoutTax());
                //税率		是
                projectDetail.setTaxRate(tax_rate);
                projectDetails.add(projectDetail);
                continue;
            }
            //发票行性质	1	是 0正常行、1折扣行、2被折扣行
            projectDetail.setInvoiceLineNature(invoiceLineNature_item);
            //项目名称	90	是
            projectDetail.setProjectName(goodsItem.getPrductName());
            //计量单位	20	否
            //规格型号	40	否
            //项目数量		否
            projectDetail.setAmount(goodsItem.getBuyNumber());

            //税额 是 单位：元（2位小数）
            projectDetail.setTaxAmount(goodsItem.getTaxAmount());
            //项目单价 否 小数点后6位 不含税
            projectDetail.setUnitPrice(goodsItem.getUnitPrice());
            //项目金额 是 不含税，单位：元（2位小数）
            projectDetail.setProjectAmount(goodsItem.getAmountWithoutTax());
            //税率		是
            projectDetail.setTaxRate(tax_rate);
            //add to list
            projectDetails.add(projectDetail);
        }
        return projectDetails;
    }


    /**
     * convert entity
     * GoodsItemBo -> ProjectDetail
     *
     * @param goodsItemList
     * @return
     */
    private List<ProjectDetail> buildRedProjectDetails(final List<GoodsItemBo> goodsItemList) {
        List<ProjectDetail> projectDetails = Lists.newLinkedList();
        ProjectDetail projectDetail;
        for (GoodsItemBo goodsItem : goodsItemList) {
            projectDetail = new ProjectDetail();

            //发票行性质	1	是 0正常行、1折扣行、2被折扣行
            projectDetail.setInvoiceLineNature(0);
            //项目名称	90	是
            projectDetail.setProjectName(goodsItem.getPrductName());
            //计量单位	20	否
            //规格型号	40	否
            //项目数量		否
            projectDetail.setAmount(goodsItem.getBuyNumber());

            //税额 是 单位：元（2位小数）
            projectDetail.setTaxAmount(goodsItem.getTaxAmount());
            //项目单价 否 小数点后6位 不含税
            projectDetail.setUnitPrice(goodsItem.getUnitPrice());
            //项目金额 是 不含税，单位：元（2位小数）
            projectDetail.setProjectAmount(goodsItem.getAmountWithoutTax());
            //税率		是
            projectDetail.setTaxRate(tax_rate);
            //add to list
            projectDetails.add(projectDetail);
        }
        return projectDetails;
    }

    /**
     * 移除退货商品，或重新计算skn数量
     *
     * @param all
     * @param refundGoods
     */
    private void removeRefoundGoods(List<GoodsItemBo> all, List<GoodsItemBo> refundGoods) {
        List<GoodsItemBo> removingList = Lists.newArrayList();
        Map<Integer, GoodsItemBo> goodsItemBoMap = MapUtil.transformMap(refundGoods,
                new MapUtil.Function<GoodsItemBo, Integer>() {
                    @Override
                    public Integer apply(GoodsItemBo input) {
                        return input.getSkn();
                    }
                }
        );
        int buyNumber;
        for (GoodsItemBo item : all) {
            buyNumber = item.getBuyNumber() < 0 ? -item.getBuyNumber() : item.getBuyNumber();
            if (goodsItemBoMap.containsKey(item.getSkn())) {
                buyNumber = new BigDecimal(buyNumber).abs()
                        .subtract(new BigDecimal(goodsItemBoMap.get(item.getSkn()).getBuyNumber()))
                        .intValue();
                if (buyNumber <= 0) {
                    removingList.add(item);
                }
            }
            item.setBuyNumber(buyNumber);
        }

        if (CollectionUtils.isNotEmpty(removingList)) {
            all.removeAll(removingList);
        }
    }


    private void removeDiscountItem(List<GoodsItemBo> goodsItemList) {
        List<GoodsItemBo> ignoreItems = Lists.newArrayList();
        for (GoodsItemBo item : goodsItemList) {
            if (InvoiceCalculator.discountLine_skn == item.getSkn()) {
                ignoreItems.add(item);
                break;
            }
        }
        if (CollectionUtils.isNotEmpty(ignoreItems)) {
            goodsItemList.removeAll(ignoreItems);
        }
    }

    int getCurrentTimeSeconds() {
        long longTime = new Date().getTime();
        return (int) (longTime / 1000);
    }



    public static void main(String[] args) {
        InvoiceBo invoiceBo = new InvoiceBo();
        System.out.println("show me the default value :" + JSONObject.toJSONString(invoiceBo));
        double totalMarketPrice = 51.2 + 18.72,
        actualAmount = 73.7-5.13-0.87,
        tax_rate = 0.17;
        BigDecimal a = new BigDecimal(actualAmount).divide(new BigDecimal(1).add(new BigDecimal(tax_rate)), 8, BigDecimal.ROUND_HALF_UP);
        System.out.println(a);
        System.out.println(new BigDecimal(totalMarketPrice).subtract(a)
                .divide(new BigDecimal(totalMarketPrice), 5, BigDecimal.ROUND_HALF_UP));
        String errorMsg = "nothing";
        try{
            throw new Exception("test a bomb");
        }catch (Exception e){
            System.out.println("catch exception");
            errorMsg = e.getMessage();
            logger.error("",e);
        }finally {
            System.out.println("reach finally, error is " + errorMsg);
        }
    }

}
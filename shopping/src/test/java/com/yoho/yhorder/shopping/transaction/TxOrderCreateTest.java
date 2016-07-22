package com.yoho.yhorder.shopping.transaction;

import com.yoho.core.transaction.YHTxCoordinator;
import com.yoho.core.transaction.annoation.YHTransaction;
import com.yoho.yhorder.shopping.transaction.service.*;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by fruwei on 2016/6/6.
 */

@Component
public class TxOrderCreateTest {
    Logger logger= LoggerFactory.getLogger(TxOrderCreateTest.class);
    @Autowired
    private TxStorageService txStorageService;

    @Autowired
    private TxCouponService txCouponService;

    @Autowired
    private TxPromotionService txPromotionService;

    @Autowired
    private TxRedEnvelopeService txRedEnvelopeService;
    @Autowired
    private TxYohocoinService txYohocoinService;

    @Autowired
    private YHTxCoordinator tx;



    @YHTransaction
    public void testCreateOrder() {
        logger.info("init...");
        Assert.assertNotNull(tx);
        //Assert.assertNotNull(tx.steps);

        try {

            txStorageService.decreaseStorage(11111111, 1);

            txCouponService.useCoupon("a123456");

            txPromotionService.usePromotion("p111111");

            txRedEnvelopeService.useRedEnvelope(100.0);

            txYohocoinService.useYohocoin(1000);

        }catch (Exception e ){
            tx.rollback();
            logger.warn("create order ",e);
        }
        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}

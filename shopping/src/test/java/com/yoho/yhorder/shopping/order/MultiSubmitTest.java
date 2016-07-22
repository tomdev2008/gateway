package com.yoho.yhorder.shopping.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.service.model.order.request.ShoppingSubmitRequest;
import com.yoho.service.model.order.response.shopping.ShoppingSubmitResponse;
import com.yoho.yhorder.shopping.charge.ChargerService;
import com.yoho.yhorder.shopping.service.IShoppingCartService;
import com.yoho.yhorder.shopping.service.impl.ShoppingCartAddService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by fruwei on 2016/5/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})
public class MultiSubmitTest {

    @Autowired
    IShoppingCartService shoppingCartService;
    @Autowired
    ShoppingCartAddService shoppingCartAddService;


    Executor executor;

    static int THREAD_NUM=5;
    CyclicBarrier cyclicBarrier;

    CyclicBarrier allcyclicBarrier;

    @Before
    public void init(){
        executor=Executors.newFixedThreadPool(THREAD_NUM);
        cyclicBarrier=new CyclicBarrier(THREAD_NUM);
        allcyclicBarrier=new CyclicBarrier(THREAD_NUM+1);
    }

    /*
        sql 准备 清空测试用户购物车
        delete from  shopping_cart_items where uid in (8041880,8041884,8041886,8041890,8041894,8041896,8041898,8041900,8041904,8041906);

     */
    @Test
    public void testMultiSubmit(){

        int[] uids={8041880,8041884,8041886,8041890,8041894,8041896,8041898,8041900,8041904,8041906};
        for(int i=0;i<1;i++){
            BuyThread threads=new BuyThread(uids[i]);
            executor.execute(threads);
        }

        try {
            allcyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }


    }

    class BuyThread implements Runnable{


        int uid;
        public BuyThread(int uid){
            this.uid=uid;
        }
        @Override
        public void run() {

            //添加到购物车商品
            ShoppingCartRequest req=new ShoppingCartRequest();
            req.setUid(uid);
            req.setBuy_number(10);
            req.setSelected("Y");
            req.setProduct_sku(131874);
            try {
                shoppingCartAddService.add(req);
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }


            System.out.println(uid+" start to buy ######################################################");
            //购买
            ShoppingSubmitRequest request=new ShoppingSubmitRequest();
            request.setUid(uid);
            request.setPayment_type(2);

            try {
                ShoppingSubmitResponse rsp = shoppingCartService.submit(request);
                System.out.println(uid + " end  buy #################################################" + rsp);
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try {
                    allcyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }


        }
    }



}

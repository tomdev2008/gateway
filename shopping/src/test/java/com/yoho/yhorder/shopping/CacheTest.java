package com.yoho.yhorder.shopping;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by xjipeng on 16/1/23.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:META-INF/spring/spring-mybatis-datasource.xml",
        "classpath*:META-INF/spring/spring*.xml"})

public class CacheTest {

    private static Executor executor = Executors.newSingleThreadExecutor();

    public static void init(){

        System.out.println("..... enter init......");

        LoadingCache<String,String> cahceBuilder=CacheBuilder
                .newBuilder()
                .refreshAfterWrite(5, TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        System.out.println("...begin load and sleep 5s, key:" + key + " time:" + (new Date()));
                        String strProValue = "hello " + key + "!";

                        Thread.sleep(5000);

                        System.out.println("...after load, key:" + key + " value:" + strProValue + " time:" + (new Date()));
                        return strProValue;
                    }

                    @Override
                    public ListenableFuture<String> reload(String key, String oldValue) throws Exception {
                        System.out.println("...begin reload and sleep 5s, key:" + key + " time:" + (new Date()));
                        String strProValue = "hello " + key + "!";

                        //Thread.sleep(10000);

                        System.out.println("...after reload, key:" + key + " value:" + strProValue + " time:" + (new Date()));

                        // asynchronous!
                        ListenableFutureTask<String> task = ListenableFutureTask.create(new Callable<String>(){
                            public String call() throws InterruptedException {
                                System.out.println("#### begin reload, old value :"+ oldValue);
                                Thread.sleep(5000);
                                System.out.println("#### after reload");
                                return " new value for keyb";
                            }
                        });

                        executor.execute(task);
                        return task;
//                        return Futures.immediateFuture(load(key));
                    }
                });

        try {
            System.out.println("---get value:"+cahceBuilder.get("keyb") + " time:" + (new Date()) );
            Thread.sleep(15000);
            System.out.println("---begin get value by key:keyb, time:" + (new Date()) );
            System.out.println("---after get value by key:keyb, value"+cahceBuilder.get("keyb") + " time:" + (new Date()) );

            Thread.sleep(6000);
            System.out.println("---lastest get value by key:keyb, value"+cahceBuilder.get("keyb") + " time:" + (new Date()) );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test1(){

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(" .... thread run ......");
                CacheTest.init();
            }
        });

        t1.start();
    }


}

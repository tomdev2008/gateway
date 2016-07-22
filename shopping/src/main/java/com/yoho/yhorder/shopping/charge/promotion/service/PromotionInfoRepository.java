package com.yoho.yhorder.shopping.charge.promotion.service;

import java.util.*;
import java.util.concurrent.*;

import javax.annotation.PostConstruct;

import com.google.common.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yoho.service.model.order.model.promotion.PromotionCondition;
import com.yoho.service.model.order.model.promotion.PromotionInfo;
import com.yoho.yhorder.dal.IPromotionDAO;

/**
 * 促销信息，从数据库中获取活跃可用的促销
 * <p/>
 * Created by chunhua.zhang@yoho.cn on 2015/12/17.
 */

@Component
public class PromotionInfoRepository {

    public static final String ANY_KEY = "any_key_is_ok";
    private final static Logger logger = LoggerFactory.getLogger(PromotionInfoRepository.class);

    //cache的促销信息，时间为[PROMOTION_CACHE_IN_MIN]分钟
    private final int PROMOTION_CACHE_IN_MIN = 5;

    @Autowired
    private IPromotionDAO promotionDAO;


    //guava cache, 支持线程定时刷新
    private LoadingCache<String, List<PromotionInfo>> cachedPromotions;

    /**
     * 初始化cache。 nerver call it
     */
    @PostConstruct
    public void intPromotions() {

        //reload的线程池
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("promotion-cache-reload-pool-%d").setDaemon(true).build();
        ExecutorService parentExecutor = Executors.newSingleThreadExecutor(threadFactory);
        final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(parentExecutor);

        cachedPromotions = CacheBuilder.newBuilder()
                //5 分钟更新一次
                .refreshAfterWrite(PROMOTION_CACHE_IN_MIN, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, List<PromotionInfo>>() {
                            @Override
							public List<PromotionInfo> load(String key) {
                                return PromotionInfoRepository.this.getFromDB();
                            }

                            //reload
                            @Override
                            public ListenableFuture<List<PromotionInfo>> reload(final String key, List<PromotionInfo> prePromotions) {
                                // asynchronous!
                                return executorService.submit((Callable)() -> PromotionInfoRepository.this.getFromDB());
                            }

                        });

        logger.info("init promotion infos from database success.");
    }


    /**
     * 根据promotion id 获取 promotion详细信息
     *
     * @param promotionId promotion id
     * @return 详细信息 如果找不到，返回 null
     */
    public PromotionInfo getPromotionById(int promotionId) {

        List<PromotionInfo> all = this.getAllActivePromotions();

        for (PromotionInfo info : all) {
            if (info.getId().equals(String.valueOf(promotionId))) {
                return info;
            }
        }

        return null;

    }


    /**
     * 根据 channel获取所有可用的促销
     *
     * @param chanelIds 通道信息
     * @return 可用的促销列表
     */
    public List<PromotionInfo> getActivePromotionsByFitChannel(int... chanelIds) {

        List<PromotionInfo> all = this.getAllActivePromotions();
        if (chanelIds == null || chanelIds.length == 0) {
            return all;
        }

        List<PromotionInfo> filted = new LinkedList<>();
        for (PromotionInfo info : all) {

            boolean fit = true;
            //1,2,3
            String fitChannels = info.getFitChannel();
            for (int channelId : chanelIds) {
                if (!fitChannels.contains(String.valueOf(channelId))) {
                    fit = false;
                    break;
                }
            }
            if (fit) {
                filted.add(info);
            }
        }

        return filted;
    }



    private List<PromotionInfo> getAllActivePromotions() {

        List<PromotionInfo> promotionInfoList = null;
        try {
            promotionInfoList = this.cachedPromotions.get(ANY_KEY);
        } catch (Exception e) {
            logger.error("exception happened when fetch", e);
        }

        //如果娶不到，从db中查找
        if (promotionInfoList == null) {
            promotionInfoList = this.getFromDB();
        }

        logger.info("get active promotion infos success. total: {}", promotionInfoList.size());

        return promotionInfoList;

    }


    /**
     * 从数据库中获取所有活跃可用的促销
     */
    private List<PromotionInfo> getFromDB() {

        List<PromotionInfo> promotionInfos = new LinkedList<>();

        List<Map> allPromotions = this.promotionDAO.selectActivePromotions();

        logger.debug("find all active promotions from db:{}", allPromotions);

        if (allPromotions == null) {
            return null;
        }

        for (Map promotion : allPromotions) {

            PromotionInfo promotionInfo = new PromotionInfo();

            promotionInfo.setId(String.valueOf(promotion.get("id")));
            promotionInfo.setTitle(String.valueOf(promotion.get("title")));

            if (promotion.get("promotion_type") != null) {
                promotionInfo.setPromotionType(String.valueOf(promotion.get("promotion_type")));
            }
            if (promotion.get("action_param") != null) {
                promotionInfo.setActionParam(String.valueOf(promotion.get("action_param")));
            }


            if (promotion.get("limit_param") != null) {
                promotionInfo.setLimitParam(String.valueOf(promotion.get("limit_param")));
            }

            if (promotion.get("reject_param") != null) {
                promotionInfo.setRejectParam(String.valueOf(promotion.get("reject_param")));
            }

            if (promotion.get("fit_channel") != null) {
                promotionInfo.setFitChannel(String.valueOf(promotion.get("fit_channel")));
            }


            //condition param
            if (promotion.get("condition_param") != null) {
                String condition = String.valueOf(promotion.get("condition_param"));
                PromotionCondition promotionCondition = new PromotionCondition(condition);
                promotionInfo.setCondition(promotionCondition);
            }


            if (promotion.get("priority") != null) {
                int priority = 0;
                try {
                    priority = Integer.parseInt(String.valueOf(promotion.get("priority")));
                } catch (Exception ex) {
                    logger.warn("promotion priority is not int,promotion id {}", promotionInfo.getId(), ex);
                }
                promotionInfo.setPriority(priority);
            }

            if (promotion.get("start_time") != null) {
                promotionInfo.setStartTime(String.valueOf(promotion.get("start_time")));
            }

            if (promotion.get("end_time") != null) {
                promotionInfo.setEndTime(String.valueOf(promotion.get("end_time")));
            }

            promotionInfos.add(promotionInfo);
        }

        logger.info("get all active promotions from database success. size:{}", promotionInfos.size());

        return promotionInfos;


    }
}

package com.yoho.yhorder.shopping.promotion.test;

import com.yoho.yhorder.shopping.charge.promotion.service.CartPromotionService;
import com.yoho.yhorder.shopping.charge.promotion.service.PromotionInfoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * test cash reduce
 * <p/>
 * Created by chunhua.zhang@yoho.cn on 2015/12/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/spring/spring*.xml"})
public class TestRedis {


    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PromotionInfoRepository repository;


    @Before
    public void setup() throws IOException {


    }


    /**
     * 满599， brand = 517 ，减少100
     */
    @Test
    public void testRedis() {


    }

}

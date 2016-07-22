package com.yoho.yhorder.shopping.promotion.test;

import com.yoho.core.common.utils.YHMath;
import com.yoho.yhorder.shopping.charge.promotion.impl.BigDecimalHelper;
import com.yoho.yhorder.shopping.utils.MathUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by chunhua.zhang@yoho.cn on 2015/12/24.
 */
public class TestCash {


    @Test
    public void test(){


        BigDecimal bigDecimal = new BigDecimal("12.363");

         BigDecimal new1 = BigDecimalHelper.up(bigDecimal);

        BigDecimal new2 = BigDecimalHelper.up(new1);

        Assert.assertEquals(new1.doubleValue(), 12.37d, 0.0001d);
        Assert.assertEquals(new2.doubleValue(), 12.37d, 0.0001d);

    }



    @Test
    public void test2(){


        BigDecimal bigDecimal = new BigDecimal("12.4501");

        BigDecimal new1 = BigDecimalHelper.up(bigDecimal);

        BigDecimal new2 = BigDecimalHelper.up(new1);

        Assert.assertEquals(new1.doubleValue(), 12.01d, 0.0001d);
        Assert.assertEquals(new2.doubleValue(), 12.01d, 0.0001d);

    }


    @Test
    public void test3(){


        double d1 = 12.35;

        double number = 77;

        double total = d1 * number;

        double yohui =  60.0d;


        System.out.println(total);
        Assert.assertEquals(148.2d, total, 0.00001d);

    }


    @Test
    public void test4(){


        double d1 = 218.68;

        double number = 1.8;

        double total = d1 + number;


        System.out.println(YHMath.add(d1, number));

    }


}

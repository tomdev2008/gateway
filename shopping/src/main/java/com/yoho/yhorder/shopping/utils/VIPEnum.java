package com.yoho.yhorder.shopping.utils;

/**
 * Created by JXWU on 2015/11/22.
 */
public enum VIPEnum {
    VIP_0("0","普通会员",0,1,0,1,1,null),
    VIP_1("1","银卡会员",1,2,600,0.95,0.95,new int[]{1,4,10,8,9}),
    VIP_2("2","金卡会员",2,3,2000,0.9,0.9,new int[]{2,4,10,5,8,9}),
    VIP_3("3","白金会员",3,null,5000,0.88,0.88,new int[]{3,4,10,5,6,7,8,9});

    public String name;
    public String title;
    public Integer curLevel;
    public Integer nextLevel;
    public int needCost;
    public double commonDiscount;
    public double promotionDiscount;
    public int[] premiumScop;

    private VIPEnum(String name,String title,Integer curLevel,Integer nextLevel,int needCost,double commonDiscount,double promotionDiscount,int[] premiumScop)
    {
        this.name = name;
        this.title = title;
        this.curLevel = curLevel;
        this.nextLevel = nextLevel;
        this.needCost  = needCost;
        this.commonDiscount = commonDiscount;
        this.promotionDiscount = promotionDiscount;
        this.premiumScop = premiumScop;
    }

    public static  VIPEnum valueOf(int userLevel)
    {
        switch (userLevel)
        {
            case 0:
                return VIP_0;
            case 1:
                return VIP_1;
            case 2:
                return VIP_2;
            case 3:
                return VIP_3;
            default:
                return VIP_0;
        }
    }
}

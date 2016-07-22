package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.YohoodProduct;

public interface YohoodProductMapper {

    YohoodProduct selectByPrimaryKey(Integer productSku);

    int updateByPrimaryKeySelective(YohoodProduct record);
}
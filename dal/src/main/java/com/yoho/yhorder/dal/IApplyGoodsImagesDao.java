package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.ApplyGoodsImages;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IApplyGoodsImagesDao {
    int deleteByPrimaryKey(Integer id);

    int insert(ApplyGoodsImages record);

    int insertBatch(List<ApplyGoodsImages> record);

    /**
     * 添加图片
     * @param record
     * @return
     */
    int insertImages(ApplyGoodsImages record);

    ApplyGoodsImages selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ApplyGoodsImages record);

    int updateByPrimaryKey(ApplyGoodsImages record);

    /**
     * 获得图片
     */
    List<ApplyGoodsImages> selectImages(@Param("applyId")Integer applyId,@Param("applyGoodsId")Integer applyGoodsId);


    /**
     * 批量获得图片
     */
    List<ApplyGoodsImages> selectImagesBatch(List<ApplyGoodsImages> applyGoodsImagesList);
}










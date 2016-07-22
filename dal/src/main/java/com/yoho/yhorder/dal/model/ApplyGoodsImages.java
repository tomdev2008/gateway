package com.yoho.yhorder.dal.model;

import java.util.List;

public class ApplyGoodsImages extends Base {
    private static final long serialVersionUID = 2649852425862588265L;
    private Integer id;

    private Integer applyId;

    private Integer applyGoodsId;

    private Byte imageType;

    private String imagePath;

    //用户业务处理
    @Deprecated
    private List<String> imagePathList;


    public List<String> getImagePathList() {
        return imagePathList;
    }

    public void setImagePathList(List<String> imagePathList) {
        this.imagePathList = imagePathList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getApplyId() {
        return applyId;
    }

    public void setApplyId(Integer applyId) {
        this.applyId = applyId;
    }

    public Integer getApplyGoodsId() {
        return applyGoodsId;
    }

    public void setApplyGoodsId(Integer applyGoodsId) {
        this.applyGoodsId = applyGoodsId;
    }

    public Byte getImageType() {
        return imageType;
    }

    public void setImageType(Byte imageType) {
        this.imageType = imageType;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? null : imagePath.trim();
    }
}
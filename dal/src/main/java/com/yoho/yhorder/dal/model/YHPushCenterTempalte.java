package com.yoho.yhorder.dal.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by JXWU on 2016/2/24.
 */
public class YHPushCenterTempalte {
    private int sceneId;
    //
    private String sceneName;

    private String pushType;

    private String title;

    private String content;

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.Gate;

public interface IGateDAO {
    Gate selectByMetaKey(String metaKey);
}
package com.yoho.yhorder.dal;

import com.yoho.yhorder.dal.model.UnionUsers;

public interface IUnionUsersDAO {
    UnionUsers selectByPrimaryKey(Integer id);
}
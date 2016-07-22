package com.yoho.yhorder.shopping.model;

import com.yoho.product.model.ProductBo;
import com.yoho.service.model.order.request.ShoppingCartRequest;
import com.yoho.yhorder.shopping.utils.Action;

/**
 * Created by JXWU on 2015/11/21.
 */
public class ActionContext {

    //用户请求数据对象，包括用户uid，shoppingkey等
    private ShoppingCartRequest request;

    //购物车操作枚举对象
    private Action action;

    //临时变量
    private ProductBo productBo;

    public ActionContext(ShoppingCartRequest request, Action action)
    {
        this.request = request;
        this.action = action;
    }

    public ShoppingCartRequest getRequest() {
        return request;
    }

    public void setRequest(ShoppingCartRequest request) {
        this.request = request;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ProductBo getProductBo() {
        return productBo;
    }

    public void setProductBo(ProductBo productBo) {
        this.productBo = productBo;
    }
}

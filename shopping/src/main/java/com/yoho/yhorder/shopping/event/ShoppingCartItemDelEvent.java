package com.yoho.yhorder.shopping.event;

import com.yoho.yhorder.dal.model.ShoppingCartItems;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;

/**
 * Created by wujiexiang on 16/3/14.
 */
public class ShoppingCartItemDelEvent {

    private List<ShoppingCartItems> items;

    public ShoppingCartItemDelEvent(List<ShoppingCartItems> items) {
        this.items= items;
    }

    public ShoppingCartItemDelEvent() {

    }


    public List<ShoppingCartItems> getItems() {
        return items;
    }

    public void setItems(List<ShoppingCartItems> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

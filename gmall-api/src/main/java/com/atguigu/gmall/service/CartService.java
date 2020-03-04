package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem ifItemExistsInUserCart(String memberId, String skuId);

    void addCartItem(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem cartItemFromDb);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String userId);

    void checkCart(OmsCartItem omsCartItem);

    void delCart(String memberId);
}

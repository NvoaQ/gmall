package com.chendong.gmall.service;

import com.chendong.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem isCartExistByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDb);

    void synCartCache(String memberId);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);
}

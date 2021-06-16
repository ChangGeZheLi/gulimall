package com.syong.gulimall.cart.service;

import com.syong.gulimall.cart.vo.Cart;
import com.syong.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Description:
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getCurrentUserCartItems();
}

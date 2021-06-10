package com.syong.gulimall.cart.service;

import com.syong.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @Description:
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);
}

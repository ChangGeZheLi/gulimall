package com.syong.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syong.common.utils.R;
import com.syong.gulimall.cart.feign.ProductFeignService;
import com.syong.gulimall.cart.interceptor.CartInterceptor;
import com.syong.gulimall.cart.service.CartService;
import com.syong.gulimall.cart.to.UserInfoTo;
import com.syong.gulimall.cart.vo.Cart;
import com.syong.gulimall.cart.vo.CartItem;
import com.syong.gulimall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description:
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private ThreadPoolExecutor executor;

    private static final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String key = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(key)){
            //如果redis中没有该购物项，才要把数据存入
            //需要进行远程调用，使用异步编排方式
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                //商品添加到购物车
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setPrice(data.getPrice());
                cartItem.setSkuId(skuId);
                cartItem.setTitle(data.getSkuTitle());
            }, executor);

            //远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttr = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            //等两个方法都完成，往redis中存数据
            CompletableFuture.allOf(getSkuInfo,getSkuSaleAttr).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);

            return cartItem;
        }else{
            //购物车已经有该商品了，只需要改变数量
            CartItem cartItem = JSON.parseObject(key, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));

            return cartItem;
        }
    }

    /**
     * 获取购物车中的某个购物项
     **/
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String str  = (String) cartOps.get(skuId.toString());

        CartItem cartItem = JSON.parseObject(str, CartItem.class);

        return cartItem;
    }

    /**
     * 获取到需要操作的购物车
     **/
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //判断拦截器拦截数据，决定key值
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey = "";
        if (userInfoTo.getUserId() != null){
            //用户登录
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else {
            //用户没登陆，使用临时user-key
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        //绑定key，operations所有操作都是针对该key
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);

        return operations;
    }
}

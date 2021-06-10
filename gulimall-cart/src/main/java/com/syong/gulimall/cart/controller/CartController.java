package com.syong.gulimall.cart.controller;

import com.syong.common.constant.AuthServerConstant;
import com.syong.gulimall.cart.interceptor.CartInterceptor;
import com.syong.gulimall.cart.service.CartService;
import com.syong.gulimall.cart.to.UserInfoTo;
import com.syong.gulimall.cart.vo.CartItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

/**
 * @Description:
 */
@Controller
public class CartController {

    @Resource
    private CartService cartService;

    @GetMapping("/cart.html")
    public String cartListPage(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        return "cartList";
    }

    /**
     * 添加商品到购物车
     **/
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);

        redirectAttributes.addAttribute("skuId",skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 添加商品到购物车成功，success应该只是做一个展示的作用，再次刷新不会重复添加商品
     **/
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId")Long skuId,Model model){

        //重定向到成功页面，再次查询购物车数据
        CartItem cartItem = cartService.getCartItem(skuId);

        model.addAttribute("item",cartItem);

        return "success";
    }
}

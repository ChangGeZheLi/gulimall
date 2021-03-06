package com.syong.gulimall.cart.controller;

import com.syong.common.constant.AuthServerConstant;
import com.syong.gulimall.cart.interceptor.CartInterceptor;
import com.syong.gulimall.cart.service.CartService;
import com.syong.gulimall.cart.to.UserInfoTo;
import com.syong.gulimall.cart.vo.Cart;
import com.syong.gulimall.cart.vo.CartItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Description:
 */
@Controller
public class CartController {

    @Resource
    private CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems(){

        return cartService.getCurrentUserCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId){
        cartService.deleteItem(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }


    @GetMapping("/countItem")
    public String countIem(@RequestParam("skuId")Long skuId,@RequestParam("num")Integer num){
        cartService.countItem(skuId,num);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("check")Integer check){

        cartService.checkItem(skuId,check);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart = cartService.getCart();

        model.addAttribute("cart",cart);

        return "cartList";
    }

    /**
     * ????????????????????????
     **/
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);

        redirectAttributes.addAttribute("skuId",skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * ?????????????????????????????????success???????????????????????????????????????????????????????????????????????????
     **/
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId")Long skuId,Model model){

        //??????????????????????????????????????????????????????
        CartItem cartItem = cartService.getCartItem(skuId);

        model.addAttribute("item",cartItem);

        return "success";
    }
}

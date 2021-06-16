package com.syong.gulimall.order.web;

import com.syong.gulimall.order.service.OrderService;
import com.syong.gulimall.order.vo.OrderConfirmVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @Description:
 */
@Controller
public class OrderWebController {

    @Resource
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        System.out.println(confirmVo);

        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }
}

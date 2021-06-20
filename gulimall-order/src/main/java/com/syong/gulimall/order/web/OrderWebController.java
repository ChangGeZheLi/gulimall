package com.syong.gulimall.order.web;

import com.syong.gulimall.order.service.OrderService;
import com.syong.gulimall.order.vo.OrderConfirmVo;
import com.syong.gulimall.order.vo.OrderSubmitVo;
import com.syong.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){

        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

        //通过状态码确定是否下单成功
        if (responseVo.getCode() == 0){
            //成功
            model.addAttribute("submitOrderResp",responseVo);
            return "pay";
        }else {
            //失败
            String msg = "下单失败；";
            switch (responseVo.getCode()){
                case 1: msg+="订单信息已过期，请刷新后再次提交";break;
                case 2: msg+="订单商品价格发生变化，请确认后再次提交";break;
                case 3: msg+="库存锁定失败，商品库存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }


}

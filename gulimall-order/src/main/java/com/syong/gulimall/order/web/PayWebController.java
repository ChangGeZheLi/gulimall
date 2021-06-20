package com.syong.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.syong.gulimall.order.config.AlipayTemplate;
import com.syong.gulimall.order.service.OrderService;
import com.syong.gulimall.order.vo.PayVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Description:
 */
@Controller
public class PayWebController {

    @Resource
    private AlipayTemplate alipayTemplate;
    @Resource
    private OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPay(orderSn);

        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);

        return pay;
    }
}

package com.syong.gulimall.member.web;

import com.syong.common.utils.R;
import com.syong.gulimall.member.feign.OrderFeignService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 */
@Controller
public class MemberWebController {

    @Resource
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                  Model model){

        //获取支付宝给我们的所有数据

        Map<String,Object> page = new HashMap<>();
        page.put("pageNum",pageNum.toString());

        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders",r);

        return "orderList";
    }
}

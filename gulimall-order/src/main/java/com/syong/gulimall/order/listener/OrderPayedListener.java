package com.syong.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.syong.gulimall.order.config.AlipayTemplate;
import com.syong.gulimall.order.service.OrderService;
import com.syong.gulimall.order.vo.PayAsyncVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description: 接收支付宝支付成功之后的请求
 */
@RestController
public class OrderPayedListener {

    @Resource
    private OrderService orderService;
    @Resource
    private AlipayTemplate alipayTemplate;


    /**
     * 收到支付宝的异步通知，需要返回给支付宝success，否则支付宝会以最大努力通知的方式不断重试发送支付成功通知
     **/
    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
//        Map<String, String[]> map = request.getParameterMap();
//        for (String key : map.keySet()) {
//            System.out.println("参数名："+key+"==>参数值: "+request.getParameter(key));
//        }

        //验证签名
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名
        //签名验证结果
        if (signVerified){
            System.out.println("签名验证成功。。。。");
            String result = orderService.handlePayResult(vo);

            return result;
        }else {
            System.out.println("签名验证失败。。。。");
            return "error";
        }
    }
}

package com.syong.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.syong.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000117676171";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCn6IxDgaAIs5q8Xsv4Ayv9plQIuPgKkODrIqJvzhIAoJhxS/jxsIp9fbC8XdOOmqko6xPB+vH6SqpGuUSDik5FEMOIK5KN9GRzXfYk596DeeL5q+1nMYyzzMTeLIW1yZfVuynhmXt2XPnBMNNNNsktBFp9HbJSemqeclovkXcmynbBe7RmkxqnM+PTXz4nM8aPiIh4anke1ywNbgcEYSxBrHPjmPmfSr4hUVtjCkc6ykDwlyajj+LzxVGawxjxB3AuHvj6Pt2EwB2f0KpymCVByb+MIBpf1liPZmef37m7u2CarefYElpzm/amhf0nF7NgAKNWwz9kPbMXxFjvBcPbAgMBAAECggEBAIXppdRBEY1fEM2jHTGT9gW6HdNHEiv7GCbv3HUm+JyfeHUDjmvTzOAA2m+gtRVKrBcCRSO0LEDeQ6dcIxR1va6/i/KSLbQhcevAyIrukjxxfWB6ikB++CR975TyFUHCoeHH/8L5Uco4pMg7VIqEYzYP/stCX/H/yADOWrcNYOYBz3FwicZUcW0020uxVYoaSnlp3zeq0gRW/wpJ9NoAhWb6eFFxahQPd/08joMtd2ZdTuVfgl8QSilPpRcHijttBtxUuft24KeLb4Od6dsUaFQzP2tmmjjVJc5/ZKEnhtRTYf0AF6r5PLWwv63HovQWNPkcsMywAXWWv/A9U3QCh0ECgYEA4lsIwUbz+cPaY44dCM8YNsd6/Bd+PLGjqyrgwwhg/rVtPgMEDkW0OqGV6w3AlCHpQyypg4G8oautlDI2sVjX7UBv6EF7vngtVU52+FBrZ41y6tiLNwQ3yUw2A18RNVo3RSn28AjcfUB02SoLbZnM/AjWKxknED48QM7+wSFjWPMCgYEAveX4GKV/eFXyn95hpXzcoaZzu9I/qtPu5cssqjZRP/JRgyBX0PkIxSVk06SX/lkslouIWe0hjjqB0LFYxnr6gXPT58Fby8YpYr3IC+9xFBlpuAG9umUp11I1r4A0GSiDliOSzJNuzEw4+cxMFiVQoKg1BSKC5p0PqYlqxOBao3kCgYEAi0NY+pjtyDAwb+nigSXxDtriFrFZkHv2Z3wk9rlz+6GhyXJPj3xhK2V4+DnozvqiKhsTW/55ELqwO6o9LhWsG6L2dt27BW+o7IAvAA1yVy6WkliSJlIpBGcoICDgVIPh12K/M+UvyrvaaDXO/CqRDEtfgPqjpwGzTmhMIoSpO/MCgYBGCwX4qUqcKy+D0jW4IZcLtOapV4KOUv+iGM/PtJtBd/Ki6BIcDU8z0HghLGu3sFKm8K6JZNLksCXjwRZal0/A4eU+bW6beesX9aJM7LOL01fQPCwsDVZiinss0Z/ly7DFdzeVZ6gNiOvXD4jc/kSADasIytL3luUJBpuwJISqGQKBgBQj/Dx4AFnCEiYJNb5F9aX+OW2rz/XV1nTYa9huttLhrGv3BDcTp8rBtWfpM6RscUYBT9SkKHmMuUXk/S1KIyXK/PRqxV8uichmmKAas01BaAFWB28IETHjQDtU4Y/zzCH87Rv7b00gjoGATE4CLipaGlxkhhXuVAHFFu9fl8/N";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiEY06q9xlFJq+Hgk27U79k1ItsotBrMaP0qYjj1zmQCIVA8z5GXM6ZwBdfUOKjZNLamb9Ny8I5lB9aPJHDtsL9xrXRORQBekk7cuEe5RtcMVTjDP7DaqO0vkyWeC3Lw1dpH/Hd4Yxo05gDDJWxUHH2DDTFMRGWknvKCeqYve+1rnWJr/HMyAUA3V5mkfyTHP2RQipoLIOjqAB7c/BxD7ghmKChLaO5IZbCi00vujuqhsrVQEXLATUGIZ3fAbNI2u4VtfhCX+uthcIb3EXPRrHEMYbV7kL0hl5tgC2ElXy/aHfCZ2NDra6t7LIYyp6za5M+3+MZ5buOwq7IiwpXgbtQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}

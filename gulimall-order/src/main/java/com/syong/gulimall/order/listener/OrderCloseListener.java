package com.syong.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.syong.gulimall.order.entity.OrderEntity;
import com.syong.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Description: 订单关闭监听器
 */
@Service
@RabbitListener(queues = "order.release.queue")
public class OrderCloseListener {

    @Resource
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {

        try{
            orderService.closeOrder(entity);
            //手动调用支付宝收单，防止因为延时问题


            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}

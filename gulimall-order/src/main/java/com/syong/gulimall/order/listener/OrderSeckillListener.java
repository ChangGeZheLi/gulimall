package com.syong.gulimall.order.listener;

import com.syong.common.to.mq.SeckillOrderTo;
import com.syong.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.channels.Channel;

/**
 * @Description:
 */
@Service
@RabbitListener(queues = "order.seckill.queue")
public class OrderSeckillListener {

    @Resource
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo to, Channel channel, Message message){

        orderService.createSeckillOrder(to);
    }

}

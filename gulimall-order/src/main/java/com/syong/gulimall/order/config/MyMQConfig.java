package com.syong.gulimall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 在rabbitmq中使用注解方式创建队列、交换机、绑定关系
 */
@Configuration
public class MyMQConfig {

    /**
     * 容器中的队列、交换机、绑定关系都会自动创建
     **/
    @Bean
    public Queue orderDelayQueue(){
        /**
         Queue(String name,  队列名字
         boolean durable,  是否持久化
         boolean exclusive,  是否排他
         boolean autoDelete, 是否自动删除
         Map<String, Object> arguments) 属性
         设置队列参数
         */
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);

        Queue queue = new Queue("order.delay.queue",true,false,false,arguments);

        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue("order.release.queue",true,false,false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange(){
        TopicExchange topicExchange = new TopicExchange("order-event-exchange", true, false);
        return topicExchange;
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        Binding binding = new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order",null);
        return binding;
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        Binding binding = new Binding("order.release.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order",null);
        return binding;
    }
}

package com.syong.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
    public Queue stockDelayQueue(){
        /**
         Queue(String name,  队列名字
         boolean durable,  是否持久化
         boolean exclusive,  是否排他
         boolean autoDelete, 是否自动删除
         Map<String, Object> arguments) 属性
         设置队列参数
         */
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        // 消息过期时间 2分钟
        arguments.put("x-message-ttl", 120000);

        Queue queue = new Queue("stock.delay.queue",true,false,false,arguments);

        return queue;
    }

    @Bean
    public Queue stockReleaseOrderQueue(){
        Queue queue = new Queue("stock.release.queue",true,false,false);
        return queue;
    }

    @Bean
    public Exchange stockEventExchange(){
        TopicExchange topicExchange = new TopicExchange("stock-event-exchange", true, false);
        return topicExchange;
    }

    @Bean
    public Binding stockLockedBinding(){
        Binding binding = new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.locked",null);
        return binding;
    }

    @Bean
    public Binding stockReleaseBinding(){
        Binding binding = new Binding("stock.release.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.release.#",null);
        return binding;
    }
}

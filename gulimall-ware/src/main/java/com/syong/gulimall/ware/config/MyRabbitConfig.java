package com.syong.gulimall.ware.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @Description: 给容器中指定rabbitmq的json的转换器
 */
@Configuration
public class MyRabbitConfig {

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 如果容器中没有指定MessageConverter，就会用默认的转换器，也就是WhiteListDeserializingMessageConverter转换器
     **/
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

//    /**
//     * 定制RabbitTemplate
//     * @PostConstruct MyRabbitConfig对象创建完成之后，执行这个方法
//     **/
//    @PostConstruct
//    public void initRabbitTemplate(){
//        //设置发布者确认回调
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            /**
//             * 只要发布者的消息抵达了中间件，这个回调的ConfirmCallback就会是true
//             * correlationData 当前消息的唯一关联数据（消息的唯一id）
//             * ack 消息是否被成功接收到
//             * cause 没有被成功接收到的原因
//             **/
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                //回调处理
//            }
//        });
//
//        //设置消息抵达队列的确认回调
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            /**
//             * 只有当消息没有正确发送到队列，才会执行回到
//             * message 发送失败的消息的详细信息
//             * replyCode 回复的状态码
//             * replyText 回复的文本内容
//             * exchange 当时消息发给哪个交换机
//             * routingKey 当时消息使用的路由键
//             **/
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//
//            }
//        });
//    }
}

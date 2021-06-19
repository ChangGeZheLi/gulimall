package com.syong.gulimall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.syong.common.to.mq.StockLockedTo;
import com.syong.common.utils.R;
import com.syong.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.syong.gulimall.ware.entity.WareOrderTaskEntity;
import com.syong.gulimall.ware.feign.OrderFeignService;
import com.syong.gulimall.ware.service.WareOrderTaskDetailService;
import com.syong.gulimall.ware.service.WareOrderTaskService;
import com.syong.gulimall.ware.service.WareSkuService;
import com.syong.gulimall.ware.vo.OrderVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Description:
 */
@Service
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseListener {

    @Resource
    private WareSkuService wareSkuService;
    /**
     * 库存自动解锁
     **/
    @RabbitHandler
    public void handleStockRelease(StockLockedTo to, Message message, Channel channel) throws IOException {

        try{
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

}

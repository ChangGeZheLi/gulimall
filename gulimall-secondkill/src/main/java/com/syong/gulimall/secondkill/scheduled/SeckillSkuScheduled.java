package com.syong.gulimall.secondkill.scheduled;

import com.syong.gulimall.secondkill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 秒杀商品定时上架
 * 每天晚上三点上架最近三天需要秒杀的商品
 */
@Service
@Slf4j
public class SeckillSkuScheduled {

    @Resource
    private SeckillService seckillService;
    @Resource
    private RedissonClient redissonClient;

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days(){
        log.info("上架商品秒杀信息。。。。");
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        try{
            lock.lock(10, TimeUnit.SECONDS);
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }

}

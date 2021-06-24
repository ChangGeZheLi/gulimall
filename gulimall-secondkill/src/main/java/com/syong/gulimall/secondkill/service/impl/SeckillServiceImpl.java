package com.syong.gulimall.secondkill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.syong.common.to.mq.SeckillOrderTo;
import com.syong.common.utils.R;
import com.syong.common.vo.MemberEntity;
import com.syong.gulimall.secondkill.feign.CouponFeignService;
import com.syong.gulimall.secondkill.feign.ProductFeignService;
import com.syong.gulimall.secondkill.interceptor.LoginInterceptor;
import com.syong.gulimall.secondkill.service.SeckillService;
import com.syong.gulimall.secondkill.to.SeckillSkuRedisTo;
import com.syong.gulimall.secondkill.vo.SeckillSessionsWithSkusVo;
import com.syong.gulimall.secondkill.vo.SeckillSkuVo;
import com.syong.gulimall.secondkill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description:
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";   // 后面跟上每个商品的随机码

    /**
     * 上架最近三天的秒杀商品
     **/
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //扫描最近三天所有需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0){
            //上架商品
            List<SeckillSessionsWithSkusVo> data = r.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {});

            if (data != null && data.size() > 0){
                //缓存都redis中
                //缓存活动信息
                saveSessionInfos(data);
                //缓存活动的关联商品信息
                saveSessionSkuInfos(data);
            }
        }
    }

    /**
     * 获取当前参与秒杀的信息
     **/
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //确定当前时间属于哪个场次
        long time = new Date().getTime();

        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);

            //判断当前时间在哪个去间
            if (time <= end && time>= start){
                //获取这个秒杀场次商品信息
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = ops.multiGet(range);
                if (list != null){
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redis = JSON.parseObject((String) item, SeckillSkuRedisTo.class);

                        return redis;
                    }).collect(Collectors.toList());
                    return  collect;
                }
                break;
            }
        }
        return null;
    }

    /**
     * 返回当前sku是否参与秒杀优惠
     **/
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {

        //找到所有需要参加秒杀的商品的key
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        Set<String> keys = ops.keys();
        if (keys != null && keys.size() >0){
            //使用正则表达式匹配是否有当前skuid的商品参与秒杀
            String regx = "\\d-" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)){
                    String json = ops.get(key);
                    SeckillSkuRedisTo to = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    //如果符合当前场次的秒杀，则需要发挥随机码
                    long currentTime = new Date().getTime();
                    if (currentTime >= to.getStartTime() && currentTime<= to.getEndTime()){
                        return to;
                    }else {
                        to.setRandomCode(null);
                        return to;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 秒杀流程
     **/
    @Override
    public String kill(String killId, String key, Integer num) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        MemberEntity memberEntity = LoginInterceptor.threadLocal.get();

        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)){
            return null;
        }else {
            SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long time = new Date().getTime();

            //redis过期时间
            long ttl = endTime - time;

            //校验时间合法性
            if (time <= endTime && time>= startTime){
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId() + "-" + redisTo.getSkuId();

                //校验商品随机码和商品id
                if (randomCode.equals(key) && killId.equals(skuId)){
                    //校验购物数量是否合理
                    if (redisTo.getSeckillLimit().intValue() >= num){
                        //验证当前用户是否已经购买过，通过在redis中占位的形式
                        //setNX
                        String redisKey = memberEntity.getId() + "-" + skuId ;
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean){
                            //占位成功，用户没有购买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            try {
                                //使用tryAcquire。防止没有拿到信号量一直在阻塞状态
                                boolean b = semaphore.tryAcquire(100, TimeUnit.MILLISECONDS);
                                //只有拿到信号量才能进行秒杀
                                if (b){
                                    //秒杀成功
                                    String orderSn = IdWorker.getTimeId();

                                    SeckillOrderTo orderTo = new SeckillOrderTo();
                                    orderTo.setMemberId(memberEntity.getId());
                                    orderTo.setNum(num);
                                    orderTo.setOrderSn(orderSn);
                                    orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                    orderTo.setSkuId(redisTo.getSkuId());
                                    orderTo.setSeckillPrice(redisTo.getSeckillPrice());

                                    //使用mq进行流量削峰，因此秒杀服务只要给mq发送消息就行，监听并且消费消息在订单服务完成
                                    rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);

                                    return orderSn;
                                }
                                return null;

                            } catch (InterruptedException e) {
                                return null;
                            }
                        }else {
                            //占位失败
                        }

                    }
                }

            }
        }

        return null;
    }

    /**
     * 缓存活动信息
     **/
    private void saveSessionInfos(List<SeckillSessionsWithSkusVo> vos){
        if (vos!= null && vos.size()>0){
            for (SeckillSessionsWithSkusVo vo : vos) {
                long start = vo.getStartTime().getTime();
                long end = vo.getEndTime().getTime();

                String key = SESSIONS_CACHE_PREFIX +  start + "_" + end;

                if (!stringRedisTemplate.hasKey(key)){
                    List<String> collect = vo.getRelationSkus().stream().map(item ->item.getPromotionSessionId().toString()+"-"+ item.getSkuId().toString()).collect(Collectors.toList());
                    stringRedisTemplate.opsForList().leftPushAll(key,collect);
                }
            }
        }
    }

    /**
     * 缓存活动的关联商品信息
     **/
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkusVo> vos){
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        if (vos!= null && vos.size()>0){
            for (SeckillSessionsWithSkusVo vo : vos) {
                //设置商品随机码
                String token = UUID.randomUUID().toString().replace("-", "");
                //缓存商品
                for (SeckillSkuVo relationSkus : vo.getRelationSkus()) {
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    if (!ops.hasKey(relationSkus.getPromotionSessionId().toString()+"-"+relationSkus.getSkuId().toString())){
                        //sku的基本数据
                        R r = productFeignService.skuInfo(relationSkus.getSkuId());
                        if (r.getCode() == 0){
                            SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            redisTo.setSkuInfo(skuInfo);
                        }

                        //sku的秒杀信息
                        BeanUtils.copyProperties(relationSkus,redisTo);


                        //设置开始时间结束时间
                        redisTo.setStartTime(vo.getStartTime().getTime());
                        redisTo.setEndTime(vo.getEndTime().getTime());

                        //设置商品随机码
                        redisTo.setRandomCode(token);
                        //为每一个商品都设置一个信号量
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                        //商品可以秒杀的库存作为信号量
                        semaphore.trySetPermits(relationSkus.getSeckillCount().intValue());

                        //给redis中存数据
                        String s = JSON.toJSONString(redisTo);
                        ops.put(relationSkus.getPromotionSessionId().toString()+"-"+relationSkus.getSkuId().toString(),s);
                    }
                }
            }
        }
    }
}

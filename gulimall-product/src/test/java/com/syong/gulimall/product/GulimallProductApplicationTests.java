package com.syong.gulimall.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void upload(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        ops.set("hello","world"+ UUID.randomUUID().toString());

        System.out.println(ops.get("hello"));
    }

    @Test
    public void upload2(){
        System.out.println(redissonClient);
    }
}

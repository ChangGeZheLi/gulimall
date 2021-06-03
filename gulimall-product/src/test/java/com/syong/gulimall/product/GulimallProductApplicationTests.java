package com.syong.gulimall.product;

import com.syong.gulimall.product.dao.AttrGroupDao;
import com.syong.gulimall.product.dao.SkuSaleAttrValueDao;
import com.syong.gulimall.product.vo.foregroundVo.SkuItemSaleAttrVo;
import com.syong.gulimall.product.vo.foregroundVo.SkuItemVo;
import com.syong.gulimall.product.vo.foregroundVo.SpuItemAttrGroupVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private AttrGroupDao attrGroupDao;
    @Resource
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

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

    @Test
    public void upload3(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(100L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId.toString());
    }

    @Test
    public void upload4(){
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(1L);
        System.out.println(saleAttrsBySpuId.toString());
    }
}

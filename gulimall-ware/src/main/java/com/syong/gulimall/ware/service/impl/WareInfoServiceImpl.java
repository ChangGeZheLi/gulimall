package com.syong.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.syong.common.utils.R;
import com.syong.gulimall.ware.feign.MemberFeignService;
import com.syong.gulimall.ware.vo.FareVo;
import com.syong.gulimall.ware.vo.MemberAddressVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.ware.dao.WareInfoDao;
import com.syong.gulimall.ware.entity.WareInfoEntity;
import com.syong.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.eq("id",key)
                    .or().like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据收货地址计算运费
     *
     * @return*/
    @Override
    public FareVo getFare(Long addrId) {

        FareVo fareVo = new FareVo();

        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });

        if (data != null){
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());

            fareVo.setAddress(data);
            fareVo.setFare(new BigDecimal(substring));

            return fareVo ;
        }

        return null;
    }

}
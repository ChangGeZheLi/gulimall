package com.syong.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.ware.entity.WareInfoEntity;
import com.syong.gulimall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:34:14
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);
}


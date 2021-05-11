package com.syong.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.ware.entity.PurchaseEntity;
import com.syong.gulimall.ware.vo.MergeVo;
import com.syong.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:34:14
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo vo);
}


package com.syong.gulimall.ware.service.impl;

import com.syong.common.constant.WareConstant;
import com.syong.gulimall.ware.entity.PurchaseDetailEntity;
import com.syong.gulimall.ware.feign.ProductFeignService;
import com.syong.gulimall.ware.service.PurchaseDetailService;
import com.syong.gulimall.ware.service.WareSkuService;
import com.syong.gulimall.ware.vo.MergeVo;
import com.syong.gulimall.ware.vo.PurchaseDoneVo;
import com.syong.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.Query;

import com.syong.gulimall.ware.dao.PurchaseDao;
import com.syong.gulimall.ware.entity.PurchaseEntity;
import com.syong.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService detailService;
    @Resource
    private WareSkuService skuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryUnreceivePurchase(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();

        //如果没有指定采购单，则需要自动新建采购单
        if (purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        //TODO 确认采购单状态是新建或已分配才能合并

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        detailService.updateBatchById(collect);

        //设置采购单更新时间
        PurchaseEntity purchaseEntity =  new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());

        this.updateById(purchaseEntity);

    }

    /**
     * ids: 采购人员领取的所有采购单id
     **/
    @Override
    public void received(List<Long> ids) {
        //确认当前采购单是新建或已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            //根据ids中的id查询出详细信息
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            //过滤掉不是新建和已分配状态的信息
            return item.getStatus() <= 1;
        }).map(item->{
            //更新所有的状态和更新时间
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());

            return item;
        }).collect(Collectors.toList());

        //改变采购单的状态
        this.updateBatchById(collect);

        //改变采购项的状态
        collect.forEach(item->{
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());

            //将wms_purchase_detail表中的status更新
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());

                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            detailService.updateBatchById(detailEntities);

        });

    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo vo) {

        //改变采购项选项，当所有的采购项状态都为已完成时，采购单状态才能更新为已完成
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = vo.getItems();

        ArrayList<PurchaseDetailEntity> entities = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            //如果状态为4，则不更新采购单，并且设置采购项的状态为4
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            }else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                //将成功采购的进行入库
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                //将详细信息存入wms_ware_sku
                skuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }

            purchaseDetailEntity.setId(item.getItemId());
            entities.add(purchaseDetailEntity);
        }

        //批量更新
        detailService.updateBatchById(entities);

        //改变采购单状态
        Long id = vo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}
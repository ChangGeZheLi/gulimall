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

        //????????????????????????????????????????????????????????????
        if (purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        //TODO ??????????????????????????????????????????????????????

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

        //???????????????????????????
        PurchaseEntity purchaseEntity =  new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());

        this.updateById(purchaseEntity);

    }

    /**
     * ids: ????????????????????????????????????id
     **/
    @Override
    public void received(List<Long> ids) {
        //????????????????????????????????????????????????
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            //??????ids??????id?????????????????????
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            //????????????????????????????????????????????????
            return item.getStatus() <= 1;
        }).map(item->{
            //????????????????????????????????????
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());

            return item;
        }).collect(Collectors.toList());

        //????????????????????????
        this.updateBatchById(collect);

        //????????????????????????
        collect.forEach(item->{
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());

            //???wms_purchase_detail?????????status??????
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

        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = vo.getItems();

        ArrayList<PurchaseDetailEntity> entities = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            //???????????????4????????????????????????????????????????????????????????????4
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            }else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                //??????????????????????????????
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                //?????????????????????wms_ware_sku
                skuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }

            purchaseDetailEntity.setId(item.getItemId());
            entities.add(purchaseDetailEntity);
        }

        //????????????
        detailService.updateBatchById(entities);

        //?????????????????????
        Long id = vo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}
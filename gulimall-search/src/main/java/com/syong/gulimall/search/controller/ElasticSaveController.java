package com.syong.gulimall.search.controller;

import com.syong.common.exception.BizCodeEnum;
import com.syong.common.to.es.SkuESModel;
import com.syong.common.utils.R;
import com.syong.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @Description:
 */
@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSaveController {

    @Resource
    private ProductSaveService productSaveService;

    /**
     * 上架商品
     **/
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels){

        Boolean b = false;

        try {
            b = productSaveService.productStatusUp(skuESModels);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误:{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());

        }

        if (b){
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }else {
            return R.ok();
        }
    }
}

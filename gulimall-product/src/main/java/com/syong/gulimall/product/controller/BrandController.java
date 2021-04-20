package com.syong.gulimall.product.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.syong.gulimall.product.entity.BrandEntity;
import com.syong.gulimall.product.service.BrandService;
import com.syong.common.utils.PageUtils;
import com.syong.common.utils.R;

import javax.annotation.Resource;


/**
 * 品牌
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 12:21:40
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    /**
     * 按类型注入
     **/
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /*
     * 阿里云对象存储
     * 1、导入oss-starter
     * 2、配置key，endpoint信息
     * 3、注入OSSClient
     **/
//    @RequestMapping("/oss")
//    //@RequiresPermissions("product:brand:list")
//    public R oss() throws FileNotFoundException {
//        InputStream inputStream = new FileInputStream("D:\\FILES\\ccd1077b985c7150.jpg");
//        ossClient.putObject("gulimall-syong","test.jpg",inputStream);
//        System.out.println("上传完成。。。。");
//
//        return R.ok();
//    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@RequestBody BrandEntity brand){
		brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}

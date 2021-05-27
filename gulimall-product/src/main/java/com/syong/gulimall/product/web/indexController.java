package com.syong.gulimall.product.web;

import com.syong.gulimall.product.entity.CategoryEntity;
import com.syong.gulimall.product.service.CategoryService;
import com.syong.gulimall.product.vo.foregroundVo.Catalog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 */
@Controller
public class indexController {

    @Resource
    private CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        //查出所有的一级分类
        List<CategoryEntity> entityList = categoryService.getLevel1Categories();

        //视图解析器进行拼串
        //前缀：classpath:/templates/ 后缀：.html
        model.addAttribute("categories",entityList);
        return "index";
    }

    @ResponseBody
    @GetMapping("index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson(){

        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
}

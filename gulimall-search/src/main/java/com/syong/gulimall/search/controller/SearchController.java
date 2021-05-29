package com.syong.gulimall.search.controller;

import com.syong.gulimall.search.service.SearchService;
import com.syong.gulimall.search.vo.SearchParam;
import com.syong.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;

/**
 * @Description:
 */
@Controller
public class SearchController {

    @Resource
    private SearchService searchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model){

        SearchResult result = searchService.search(param);
        model.addAttribute("result",result);

        return "list";
    }
}

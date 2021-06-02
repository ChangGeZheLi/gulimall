package com.syong.gulimall.search.controller;

import com.syong.gulimall.search.service.SearchService;
import com.syong.gulimall.search.vo.SearchParam;
import com.syong.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Description:
 */
@Controller
public class SearchController {

    @Resource
    private SearchService searchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){

        //获取查询条件
        String queryString = request.getQueryString();
        param.set_queryString(queryString);

        SearchResult result = searchService.search(param);
        model.addAttribute("result",result);

        return "list";
    }
}

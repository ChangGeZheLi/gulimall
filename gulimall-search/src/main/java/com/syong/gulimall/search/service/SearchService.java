package com.syong.gulimall.search.service;

import com.syong.gulimall.search.vo.SearchParam;
import com.syong.gulimall.search.vo.SearchResult;

/**
 * @Description:
 */
public interface SearchService {
    SearchResult search(SearchParam param);
}

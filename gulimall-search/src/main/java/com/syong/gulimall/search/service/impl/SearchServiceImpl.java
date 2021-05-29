package com.syong.gulimall.search.service.impl;

import com.syong.gulimall.search.service.SearchService;
import com.syong.gulimall.search.vo.SearchParam;
import com.syong.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Service;

/**
 * @Description:
 */
@Service("SearchService")
public class SearchServiceImpl implements SearchService {

    /**
     * 根据传递来的检索条件到es中查询数据
     **/
    @Override
    public SearchResult search(SearchParam param) {
        return null;
    }
}

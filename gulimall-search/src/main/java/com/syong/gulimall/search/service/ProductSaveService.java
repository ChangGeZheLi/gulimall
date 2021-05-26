package com.syong.gulimall.search.service;

import com.syong.common.to.es.SkuESModel;

import java.io.IOException;
import java.util.List;

/**
 * @Description:
 */
public interface ProductSaveService {

    Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException;
}

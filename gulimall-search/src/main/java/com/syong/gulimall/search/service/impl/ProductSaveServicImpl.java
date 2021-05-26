package com.syong.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.syong.common.to.es.SkuESModel;
import com.syong.gulimall.search.config.GulimallElasticSearchConfig;
import com.syong.gulimall.search.constant.EsConstant;
import com.syong.gulimall.search.service.ProductSaveService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @Description:
 */
@Service
public class ProductSaveServicImpl implements ProductSaveService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException {

        //在es中建立索引：product，建立映射关系

        //在es中保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel skuESModel : skuESModels) {
            //构造保存请求
            IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
            request.id(skuESModel.getSkuId().toString());

            String s = JSON.toJSONString(skuESModel);

            request.source(s, XContentType.JSON);

            bulkRequest.add(request);
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        return bulk.hasFailures();

    }
}

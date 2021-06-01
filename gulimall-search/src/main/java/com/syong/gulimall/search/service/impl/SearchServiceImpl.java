package com.syong.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.syong.common.to.es.SkuESModel;
import com.syong.gulimall.search.config.GulimallElasticSearchConfig;
import com.syong.gulimall.search.constant.EsConstant;
import com.syong.gulimall.search.service.SearchService;
import com.syong.gulimall.search.vo.SearchParam;
import com.syong.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 */
@Service("SearchService")
public class SearchServiceImpl implements SearchService {

    @Resource
    private RestHighLevelClient highLevelClient;

    /**
     * 根据传递来的检索条件到es中查询数据
     **/
    @Override
    public SearchResult search(SearchParam param) {

        SearchResult result = null;

        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            SearchResponse response = highLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //根据response结果封装成SearchResult
            result = buildSearchResult(param,response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 根据es的响应解雇，封装成SearchResult对象
     *
     * @param param
     * @param response*/
    private SearchResult buildSearchResult(SearchParam param, SearchResponse response) {
        SearchResult result = new SearchResult();

        SearchHits hits = response.getHits();

        //所有商品信息
        if (hits.getHits()!=null && hits.getHits().length > 0) {
            List<SkuESModel> skuESModels = new ArrayList<>();
            for (SearchHit hit : hits.getHits()) {
                String source = hit.getSourceAsString();
                SkuESModel skuESModel = JSON.parseObject(source, SkuESModel.class);

                //如果有高亮字段就设置
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = highlightField.getFragments()[0].string();

                    skuESModel.setSkuTitle(skuTitle);
                }
                skuESModels.add(skuESModel);
            }
            result.setProducts(skuESModels);
        }

        //根据response和param封装返回结果
        //属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrsAgg = response.getAggregations().get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            //查询属性id
            long attrId = bucket.getKeyAsNumber().longValue();

            Aggregations subAttrAgg = bucket.getAggregations();
            //查询属性名
            ParsedStringTerms attrNameAgg = subAttrAgg.get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            //查询属性值
            ParsedStringTerms attrValueAgg = subAttrAgg.get("attrValueAgg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item -> item.getKeyAsString()
            ).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValues(attrValues);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        //品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();

        ParsedLongTerms brandAgg = response.getAggregations().get("brandAgg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //品牌id
            long brandId = bucket.getKeyAsNumber().longValue();

            //品牌name
            Terms brandNameAgg = bucket.getAggregations().get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();

            //品牌图片
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brandImgAgg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }

        result.setBrands(brandVos);

        //分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalogAgg");

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //分类id
            long catalogId = bucket.getKeyAsNumber().longValue();

            //分类名
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalogNameAgg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();

            catalogVo.setCatalogId(catalogId);
            catalogVo.setCatalogName(catalogName);

            catalogVos.add(catalogVo);
        }

        result.setCatalogs(catalogVos);

        //当前页码
        result.setPageNum(param.getPageNum());

        //总记录数
        result.setTotal(hits.getTotalHits().value);
        //总页数:
        long total = hits.getTotalHits().value;
        Integer totalPages = (int)total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int)total / EsConstant.PRODUCT_PAGESIZE : (int)total / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPages);

        return result;
    }

    /**
     * 根据查询条件动态构建查询请求
     *
     * @param param*/
    private SearchRequest buildSearchRequest(SearchParam param) {
        //通过sourceBuilder构建所有的查询语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //查询条件构造
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //must条件匹配
        if (!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //构建filter
        if (param.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //根据品牌id查询
        if (param.getBrandId()!=null && param.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }
        //根据指定属性查询
        if (param.getAttrs()!=null && param.getAttrs().size()>0){
            for (String attrStr : param.getAttrs()) {
                //页面传递的数据格式：attrs=1_5寸:8寸 表示一号属性，值为5寸&8寸
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");

                //构建到查询器中,每一个必须得生成一个nested查询
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs",nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //根据是否有库存查询
        if (param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock",param.getHasStock()==1));
        }
        //根据价格区间查询
        if (!StringUtils.isEmpty(param.getSkuPrice())){
            //价格区间格式1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            //根据传递的range参数进行分割
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2){
                rangeQuery.gte(s[0]);
            }else if (s.length == 1){
                if (param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }else {
                    rangeQuery.gte(s[0]);
                }
            }

            boolQuery.filter(rangeQuery);
        }

        sourceBuilder.query(boolQuery);

        //排序
        if (!StringUtils.isEmpty(param.getSort())){
            //sort字段格式：sort=hostScore_asc/desc
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0],order);
        }

        //分页
        //from = (pageNum - 1) * pageSize
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //只有在搜索框输入得搜索才进行高亮
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();

            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");

            sourceBuilder.highlighter(builder);
        }

        //聚合分析
        //品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg");
        brandAgg.field("brandId").size(50);
        //品牌子类聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImgAgg").field("brandImg").size(1));

        sourceBuilder.aggregation(brandAgg);

        //分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg");
        catalogAgg.field("catalogId").size(20);

        //分类子聚合
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogNameAgg").field("catalogName").size(1));

        sourceBuilder.aggregation(catalogAgg);

        //属性聚合
        NestedAggregationBuilder nested = AggregationBuilders.nested("attrsAgg", "attrs");
        //子聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(1);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(50));

        nested.subAggregation(attrIdAgg);

        sourceBuilder.aggregation(nested);


        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }
}

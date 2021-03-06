package com.syong.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.syong.common.to.es.SkuESModel;
import com.syong.common.utils.R;
import com.syong.gulimall.search.config.GulimallElasticSearchConfig;
import com.syong.gulimall.search.constant.EsConstant;
import com.syong.gulimall.search.feign.ProductFeignService;
import com.syong.gulimall.search.service.SearchService;
import com.syong.gulimall.search.vo.AttrResponseVo;
import com.syong.gulimall.search.vo.BrandVo;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    @Resource
    private ProductFeignService productFeignService;

    /**
     * ?????????????????????????????????es???????????????
     **/
    @Override
    public SearchResult search(SearchParam param) {

        SearchResult result = null;

        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            SearchResponse response = highLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //??????response???????????????SearchResult
            result = buildSearchResult(param,response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * ??????es???????????????????????????SearchResult??????
     *
     * @param param
     * @param response*/
    private SearchResult buildSearchResult(SearchParam param, SearchResponse response) {
        SearchResult result = new SearchResult();

        SearchHits hits = response.getHits();

        //??????????????????
        if (hits.getHits()!=null && hits.getHits().length > 0) {
            List<SkuESModel> skuESModels = new ArrayList<>();
            for (SearchHit hit : hits.getHits()) {
                String source = hit.getSourceAsString();
                SkuESModel skuESModel = JSON.parseObject(source, SkuESModel.class);

                //??????????????????????????????
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = highlightField.getFragments()[0].string();

                    skuESModel.setSkuTitle(skuTitle);
                }
                skuESModels.add(skuESModel);
            }
            result.setProducts(skuESModels);
        }


        //??????response???param??????????????????
        //????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrsAgg = response.getAggregations().get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            //????????????id
            long attrId = bucket.getKeyAsNumber().longValue();

            Aggregations subAttrAgg = bucket.getAggregations();
            //???????????????
            ParsedStringTerms attrNameAgg = subAttrAgg.get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            //???????????????
            ParsedStringTerms attrValueAgg = subAttrAgg.get("attrValueAgg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item -> item.getKeyAsString()
            ).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValues(attrValues);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        //????????????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();

        ParsedLongTerms brandAgg = response.getAggregations().get("brandAgg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //??????id
            long brandId = bucket.getKeyAsNumber().longValue();

            //??????name
            Terms brandNameAgg = bucket.getAggregations().get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();

            //????????????
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brandImgAgg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }

        result.setBrands(brandVos);

        //????????????
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalogAgg");

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //??????id
            long catalogId = bucket.getKeyAsNumber().longValue();

            //?????????
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalogNameAgg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();

            catalogVo.setCatalogId(catalogId);
            catalogVo.setCatalogName(catalogName);

            catalogVos.add(catalogVo);
        }

        result.setCatalogs(catalogVos);

        //????????????
        result.setPageNum(param.getPageNum());

        //????????????
        result.setTotal(hits.getTotalHits().value);
        //?????????:
        long total = hits.getTotalHits().value;
        Integer totalPages = (int)total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int)total / EsConstant.PRODUCT_PAGESIZE : (int)total / EsConstant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPages);

        //??????????????????
        List<Integer> pageNavs = new ArrayList<>();
        for (Integer i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //???????????????????????????
        if (param.getAttrs()!=null && param.getAttrs().size()>0){
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();

                //????????????attrs????????????????????????:attrs=2_5???:6???
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0){
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                }else {
                    navVo.setNavName(s[0]);
                }

                //?????????????????????????????????????????????
                //???????????????????????????????????????
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(navVos);
        }

        //????????????????????????????????????
        if (param.getBrandId() != null && param.getBrandId().size() > 0){
            List<SearchResult.NavVo> navVos = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("??????");
            //????????????????????????
            R r = productFeignService.brandInfo(param.getBrandId());
            if (r.getCode() == 0){
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName()+"");
                    replace = replaceQueryString(param,brandVo.getBrandId()+"","brandId");
                }
                navVo.setNavName(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
            }
            navVos.add(navVo);
        }

        return result;
    }

    private String replaceQueryString(SearchParam param, String attr,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
            //?????????????????????java??????????????????
            encode = encode.replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&"+key+ "=" + encode, "");
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param param*/
    private SearchRequest buildSearchRequest(SearchParam param) {
        //??????sourceBuilder???????????????????????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //??????????????????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //must????????????
        if (!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //??????filter
        if (param.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //????????????id??????
        if (param.getBrandId()!=null && param.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }
        //????????????????????????
        if (param.getAttrs()!=null && param.getAttrs().size()>0){
            for (String attrStr : param.getAttrs()) {
                //??????????????????????????????attrs=1_5???:8??? ???????????????????????????5???&8???
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");

                //?????????????????????,??????????????????????????????nested??????
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs",nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //???????????????????????????
        if (param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock",param.getHasStock()==1));
        }
        //????????????????????????
        if (!StringUtils.isEmpty(param.getSkuPrice())){
            //??????????????????1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            //???????????????range??????????????????
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

        //??????
        if (!StringUtils.isEmpty(param.getSort())){
            //sort???????????????sort=hostScore_asc/desc
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0],order);
        }

        //??????
        //from = (pageNum - 1) * pageSize
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //????????????????????????????????????????????????
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();

            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");

            sourceBuilder.highlighter(builder);
        }

        //????????????
        //????????????
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg");
        brandAgg.field("brandId").size(50);
        //??????????????????
        brandAgg.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImgAgg").field("brandImg").size(1));

        sourceBuilder.aggregation(brandAgg);

        //????????????
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg");
        catalogAgg.field("catalogId").size(20);

        //???????????????
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogNameAgg").field("catalogName").size(1));

        sourceBuilder.aggregation(catalogAgg);

        //????????????
        NestedAggregationBuilder nested = AggregationBuilders.nested("attrsAgg", "attrs");
        //?????????
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(1);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(50));

        nested.subAggregation(attrIdAgg);

        sourceBuilder.aggregation(nested);

        System.out.println("?????????DSL??????"+ sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }
}

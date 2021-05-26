package com.syong.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.syong.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.naming.directory.SearchResult;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallSearchApplicationTests {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 搜索数据
     **/
    public void searchData() throws IOException{
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");

        SearchSourceBuilder builder = new SearchSourceBuilder();

        searchRequest.source(builder);

        SearchResponse response = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        

    }

    /**
     * 测试存储数据到es
     **/
    @Test
    public void indexData() throws IOException {
        IndexRequest request = new IndexRequest("posts");
        request.id("1");

        User user = new User();
        user.setAge(20);
        user.setGender("M");
        user.setName("lisi");

        String jsonString = JSON.toJSONString(user);
        request.source(jsonString, XContentType.JSON);

        IndexResponse index = restHighLevelClient.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    @Data
    class User{
        private String name;
        private String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {

        System.out.println(restHighLevelClient);
    }

}

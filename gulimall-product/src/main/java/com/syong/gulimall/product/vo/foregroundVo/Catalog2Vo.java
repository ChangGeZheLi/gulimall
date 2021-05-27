package com.syong.gulimall.product.vo.foregroundVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 二级分类vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2Vo {
    /**
     * 一级父分类id
     **/
    private String catalog1Id;
    /**
     * 三级子分类
     **/
    private List<Catalog3Vo> catalog3List;
    /**
     * 二级分类id
     **/
    private String id;
    /**
     * 二级分类name
     **/
    private String name;

    /**
     * 三级分类Vo
     **/
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo{
        /**
         * 父分类id
         **/
        private String catalog2Id;

        private String id;
        private String name;
    }
}

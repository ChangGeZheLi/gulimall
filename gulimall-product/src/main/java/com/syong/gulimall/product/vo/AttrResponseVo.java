package com.syong.gulimall.product.vo;

import lombok.Data;

/**
 * @Description: 封装响应给前端的分类规格参数的数据
 */
@Data
public class AttrResponseVo extends AttrVo{
    /**
     * "catelogName": "手机/数码/手机", //所属分类名字
     **/
    private String catelogName;
    /**
     * "groupName": "主体", //所属分组名字
     **/
    private String groupName;
    /**
     * "catelogPath": [2, 34, 225] //分类完整路径
     **/
    private Long[] catelogPath;
}

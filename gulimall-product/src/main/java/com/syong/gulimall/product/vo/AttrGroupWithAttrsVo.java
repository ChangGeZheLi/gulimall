package com.syong.gulimall.product.vo;

import com.syong.gulimall.product.entity.AttrEntity;
import com.syong.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 */
@Data
public class AttrGroupWithAttrsVo extends AttrGroupEntity {

    /**
     * 封装所有属性分组下的所有属性
     **/
    private List<AttrEntity> attrs;
}

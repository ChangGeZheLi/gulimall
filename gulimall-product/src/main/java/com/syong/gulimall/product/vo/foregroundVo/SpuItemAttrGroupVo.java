package com.syong.gulimall.product.vo.foregroundVo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description:
 */
@Data
@ToString
public class SpuItemAttrGroupVo{
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}

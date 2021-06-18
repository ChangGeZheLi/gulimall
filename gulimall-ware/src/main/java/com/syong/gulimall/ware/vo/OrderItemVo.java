package com.syong.gulimall.ware.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 购物项内容
 */
@Data
@ToString
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;


    private boolean hasStock;
    private BigDecimal weight;
}

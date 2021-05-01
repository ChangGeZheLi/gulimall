package com.syong.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}

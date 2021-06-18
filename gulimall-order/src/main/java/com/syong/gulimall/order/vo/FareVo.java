package com.syong.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}

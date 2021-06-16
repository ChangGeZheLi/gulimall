package com.syong.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 封装订单确认页需要的数据
 */
@ToString
public class OrderConfirmVo {

    /**
     * 收货地址：ums_member_receive_address
     **/
    @Setter
    @Getter
    private List<MemberAddressVo> addresses;
    /**
     * 所有选中的购物项
     **/
    @Setter
    @Getter
    private List<OrderItemVo> items;
    /**
     * 优惠券信息
     **/
    @Setter
    @Getter
    private Integer integration;
    /**
     * 订单总额
     **/
    private BigDecimal total;
    /**
     * 应付总额，也就是减去优惠券
     **/
    @Setter
    private BigDecimal payPrice;
    /**
     * 防止重复提交令牌
     **/
    @Getter
    @Setter
    private String orderToken;


    public BigDecimal getTotal() {

        BigDecimal sum = new BigDecimal("0");
        if (items != null){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount()));
                sum = sum.add(multiply);
            }
        }

        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    public Integer getCount(){
        return items.size();
    }
}

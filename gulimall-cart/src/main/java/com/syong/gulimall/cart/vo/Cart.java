package com.syong.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 */
public class Cart {

    /**
     * 商品数量
     **/
    private List<CartItem> items;
    /**
     * 商品数量
     **/
    private Integer countNum;
    /**
     * 商品类型数量
     **/
    private Integer countType;
    /**
     * 商品总价
     **/
    private BigDecimal totalAmount;
    /**
     * 减免价格
     **/
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;

        if (items != null && items.size()>0){
            for (CartItem item : items) {
                count++;
            }
        }

        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        return countType;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        //计算所有购物项的总价
        if (items!=null && items.size()>0){
            List<CartItem> collect = items.stream().filter(CartItem::getCheck).collect(Collectors.toList());
            for (CartItem cartItem : collect) {
                amount = amount.add(cartItem.getTotalPrice());
            }
        }
        //减去优惠价
        BigDecimal subtract = amount.subtract(getReduce());

        return subtract;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}

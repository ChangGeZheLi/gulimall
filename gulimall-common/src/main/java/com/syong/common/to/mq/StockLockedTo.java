package com.syong.common.to.mq;

import lombok.Data;


/**
 * @Description: 库存锁定成功，封装传递给mq的数据
 */
@Data
public class StockLockedTo {
    /**
     * 库存工作单id
     **/
    private Long id;
    /**
     * 工作详情所有id
     **/
    private StockDetailTo to;
}

package com.zslin.model;

import lombok.Data;

import javax.persistence.*;

/**
 * 订单中使用套餐抵扣的详情
 */
@Entity
@Table(name = "t_order_bag_detail")
@Data
public class OrderBagDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "bag_id")
    private Integer bagId;

    @Column(name = "food_id")
    private Integer foodId;

    @Column(name = "food_name")
    private String foodName;

    private Integer amount = 0;

    /** 单价 */
    private Float price;

    /** 总价 */
    private Float money;
}

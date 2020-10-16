package com.zslin.model;

import lombok.Data;

import javax.persistence.*;

/**
 * 订单详情
 *  - 菜品详情
 *  - 每一样菜品一开始不能合并，否则会导致打印出问题
 */
@Entity
@Table(name = "t_food_order_detail")
@Data
public class FoodOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    /** 批次编号，每一次新增就有一个批次 */
    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "order_no")
    private String orderNo;

    /** 创建时间，yyyy-MM-dd HH:mm:ss */
    @Column(name = "create_time")
    private String createTime;

    /** 创建时间，Long类型 */
    @Column(name = "create_long")
    private Long createLong;

    /** 创建日期，yyyy-MM-dd */
    @Column(name = "create_day")
    private String createDay;

    /** 打印状态 0-未打印；1-已打印 */
    @Column(name = "print_status")
    private String printStatus;

    /** 打印次数 */
    @Column(name = "print_count")
     private Integer printCount = 0;

   @Column(name = "cate_id")
    private Integer cateId;

    @Column(name = "cate_name")
    private String cateName;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "food_name_letter")
    private String foodNameLetter;

    @Column(name = "food_id")
    private Integer foodId;

    @Column(name = "food_data_id")
    private Integer foodDataId;

    /** 单次下单的单品数量 */
    private Integer amount;

    /** 单价 */
    private Float price;

    /** 小计 */
    private Float subtotal=0f;

    /** 套餐抵价数量 */
    @Column(name = "bag_count")
    private Integer bagCount = 0;

    /** 打印标记，1-吧台；2-厨房；3-甜品店；all-全部 */
    @Column(name = "print_flag")
    private String printFlag;
}

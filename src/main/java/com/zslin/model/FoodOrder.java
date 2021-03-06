package com.zslin.model;

import lombok.Data;

import javax.persistence.*;

/**
 * 点餐订单
 */
@Entity
@Table(name = "t_food_order")
@Data
public class FoodOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String no;

    /** 收银员姓名 */
    @Column(name = "cashier_name")
    private String cashierName;

    /** 收银员电话 */
    @Column(name = "cashier_phone")
    private String cashierPhone;

    /** 创建时间，yyyy-MM-dd HH:mm:ss */
    @Column(name = "create_time")
    private String createTime;

    /** 创建时间，Long类型 */
    @Column(name = "create_long")
    private Long createLong;

    /** 创建日期，yyyy-MM-dd */
    @Column(name = "create_day")
    private String createDay;

    /**
     * 状态
     * -1-取消订单
     * 0-用餐中
     * 1-已结账
     *
     */
    private String status;

    /** 结束时间，yyyy-MM-dd HH:mm:ss，退票或退压金都表示结束 */
    @Column(name = "end_time")
    private String endTime;

    /** 结束时间，Long类型 */
    @Column(name = "end_long")
    private Long endLong;

    /** 订单类型；1-普通订单；2-美团订单；3-套餐订单 */
    private String type;

    /** 优惠金额 */
    @Column(name = "discount_money")
    private Float discountMoney=0f;

    /** 优惠原因，如果是友情价，则是股东电话号码 */
    @Column(name = "discount_reason")
    private String discountReason;

    /** 优惠类型；0-无优惠；1-积分抵价；2-友情价；3-抵价券；4-微信扣款订单；5-会员扣款订单；6-美团扣款订单；9-飞凡扣款订单；10-时段折扣，12-折扣日，13-套餐 */
    @Column(name = "discount_type")
    private String discountType;

    /** 付款方式，只有到店下单才会有此值；1-现金；2-微信；3-支付宝支付；4-刷卡；5-商场支付 */
    @Column(name = "pay_type")
    private String payType;

    /** 是否有退菜情况 */
    @Column(name = "has_refund")
    private String hasRefund = "0";

    /** 退菜金额 */
    @Column(name = "refund_money")
    private Float refundMoney = 0f;

    /** 收银金额，只是餐费，不包含压金 */
    @Column(name = "total_money")
    private Float totalMoney=0f;

    /** 收银金额，挂零后的金额 */
    private Float totalMoney2=0f;

    /** 是否去除小数点 */
    @Column(name = "remove_dot")
    private String removeDot="0";

    /** 抹零金额 */
    @Column(name = "dot_money")
    private Float dotMoney=0f;

    /** 餐桌ID */
    @Column(name = "table_id")
    private Integer tableId;

    /** 餐桌名称 */
    @Column(name = "table_name")
    private String tableName;

    /** 用餐人数 */
    private Integer amount;

    /** 菜品数量 */
    private Integer foodCount =0;

    /** 总的菜品多少份 */
    private Integer unitCount=0;
}

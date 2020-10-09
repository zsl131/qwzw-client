package com.zslin.qwzw.model;

import lombok.Data;

import javax.persistence.*;

/**
 * 食品包，即套餐详情，具体的食品
 */
@Entity
@Table(name = "t_food_bag_detail")
@Data
public class FoodBagDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "bag_name")
    private String bagName;

    @Column(name = "bag_id")
    private Integer bagId;

    @Column(name = "food_id")
    private Integer foodId;

    @Column(name = "food_name")
    private String foodName;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    /** 是否定位到食品，如果只定位到分类，可以选择该分类下几选几 */
    @Column(name = "is_food")
    private String isFood;

    /** 数量 */
    private Integer amount = 0;

    /** 备注 */
    private String remark;

    /** 一串菜品ID */
    @Column(name = "food_ids")
    private String foodIds;

    /** 一串菜品名称，用于显示 */
    @Column(name = "food_names")
    private String foodNames;
}

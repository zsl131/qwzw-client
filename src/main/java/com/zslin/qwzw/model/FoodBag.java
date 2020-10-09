package com.zslin.qwzw.model;

import lombok.Data;

import javax.persistence.*;

/**
 * 食品包，即套餐
 */
@Entity
@Table(name = "t_food_bag")
@Data
public class FoodBag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    private String sn;

    private String remark;
}

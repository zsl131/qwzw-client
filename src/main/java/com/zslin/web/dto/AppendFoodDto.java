package com.zslin.web.dto;

import com.zslin.model.Food;
import lombok.Data;

/**
 * 新增菜品DTO对象
 */
@Data
public class AppendFoodDto {

    /** 对应菜品 */
    private Food food;

    /** 对应数量 */
    private Integer amount;

    public AppendFoodDto(Food food, Integer amount) {
        this.food = food;
        this.amount = amount;
    }
}

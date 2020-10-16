package com.zslin.web.dto;

import com.zslin.model.Food;
import com.zslin.qwzw.model.FoodBag;
import com.zslin.qwzw.model.FoodBagDetail;
import lombok.Data;

import java.util.List;

@Data
public class FoodBagDto {

    private FoodBag bag;

    private List<FoodBagDetail> detailList;

    private List<Food> foodList;

    public FoodBagDto(FoodBag bag, List<FoodBagDetail> detailList, List<Food> foodList) {
        this.bag = bag;
        this.detailList = detailList;
        this.foodList = foodList;
    }
}

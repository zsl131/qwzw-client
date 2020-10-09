package com.zslin.qwzw.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.qwzw.model.FoodBag;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IFoodBagService extends BaseRepository<FoodBag, Integer>, JpaSpecificationExecutor<FoodBag> {

    FoodBag findBySn(String sn);
}

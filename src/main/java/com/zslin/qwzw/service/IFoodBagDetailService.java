package com.zslin.qwzw.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.qwzw.model.FoodBagDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IFoodBagDetailService extends BaseRepository<FoodBagDetail, Integer>, JpaSpecificationExecutor<FoodBagDetail> {
}

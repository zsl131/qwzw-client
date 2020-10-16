package com.zslin.qwzw.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.qwzw.model.FoodBagDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IFoodBagDetailService extends BaseRepository<FoodBagDetail, Integer>, JpaSpecificationExecutor<FoodBagDetail> {

    List<FoodBagDetail> findByBagId(Integer bagId);
}

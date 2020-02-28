package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.Food;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IFoodService extends BaseRepository<Food, Integer>, JpaSpecificationExecutor<Food> {

    Food findByDataId(Integer dataId);

    /** 通过分类获取 */
    List<Food> findByCateId(Integer cateId, Sort sort);

    @Modifying
    @Transactional
    void deleteByDataId(Integer id);
}

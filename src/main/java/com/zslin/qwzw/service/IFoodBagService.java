package com.zslin.qwzw.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.qwzw.model.FoodBag;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IFoodBagService extends BaseRepository<FoodBag, Integer>, JpaSpecificationExecutor<FoodBag> {

    FoodBag findBySn(String sn);

    @Modifying
    @Transactional
    @Query("UPDATE FoodBag f SET f.status=?2 WHERE f.id=?1")
    void updateStatus(Integer id, String status);

    List<FoodBag> findByStatus(String status);
}

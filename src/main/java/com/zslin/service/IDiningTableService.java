package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.DiningTable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IDiningTableService extends BaseRepository<DiningTable, Integer>, JpaSpecificationExecutor<DiningTable> {

    DiningTable findByDataId(Integer dataId);

    @Modifying
    @Transactional
    void deleteByDataId(Integer dataId);

    @Query("SELECT t FROM DiningTable t WHERE t.id NOT IN (SELECT o.tableId FROM FoodOrder o WHERE o.status='0')")
    List<DiningTable> findEmptyTableIds();
}

package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.DiningTable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface IDiningTableService extends BaseRepository<DiningTable, Integer>, JpaSpecificationExecutor<DiningTable> {

    DiningTable findByDataId(Integer dataId);

    @Modifying
    @Transactional
    void deleteByDataId(Integer dataId);
}

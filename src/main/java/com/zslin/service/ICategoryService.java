package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.Category;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ICategoryService extends BaseRepository<Category, Integer>, JpaSpecificationExecutor<Category> {

    Category findByDataId(Integer dataId);

    @Modifying
    @Transactional
    void deleteByDataId(Integer id);
}

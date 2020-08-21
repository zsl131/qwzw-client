package com.zslin.qwzw.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.qwzw.model.PrintConfig;
import org.springframework.data.jpa.repository.Query;

public interface IPrintConfigService extends BaseRepository<PrintConfig, Integer> {

    @Query("FROM PrintConfig ")
    PrintConfig loadOne();
}

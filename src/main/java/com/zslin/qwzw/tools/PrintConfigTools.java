package com.zslin.qwzw.tools;

import com.zslin.qwzw.model.PrintConfig;
import com.zslin.qwzw.service.IPrintConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrintConfigTools {

    @Autowired
    private IPrintConfigService printConfigService;

    private static PrintConfig config = null;

    public PrintConfig getConfig() {
        if(config==null) {config = printConfigService.loadOne();}
        return config;
    }
}

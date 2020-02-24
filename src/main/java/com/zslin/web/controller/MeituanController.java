package com.zslin.web.controller;

import com.zslin.meituan.tools.MeituanHandlerTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by 钟述林 393156105@qq.com on 2017/7/5 16:37.
 */
@Controller
@RequestMapping(value = "meituan")
public class MeituanController {

    @Autowired
    private MeituanHandlerTools meituanHandlerTools;
}

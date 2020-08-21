package com.zslin.web.controller;

import com.zslin.basic.tools.NormalTools;
import com.zslin.service.IFoodOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "web/settlement")
public class WebSettlementController {

    @Autowired
    private IFoodOrderService foodOrderService;

    @GetMapping(value = {"", "/", "index"})
    public String index(Model model, String day) {
        if(day==null) {
            day = NormalTools.curDate("yyyy-MM-dd"); //默认为当天
        } else {
            day = day.replace("eq-", "");
        }

        return "/web/settlement/index";
    }
}

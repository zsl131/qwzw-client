package com.zslin.web.controller;

import com.zslin.model.DiningTable;
import com.zslin.service.IDiningTableService;
import com.zslin.service.IFoodOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(value = "web/table")
public class WebTableController {

    @Autowired
    private IDiningTableService diningTableService;

    @Autowired
    private IFoodOrderService foodOrderService;

    @GetMapping(value = "index")
    public String index(Model model, String type, HttpServletRequest request) {
        type = type==null||"".equals(type)?"1":type;
        List<DiningTable> tableList = diningTableService.findAll();
        model.addAttribute("tableList", tableList);
        model.addAttribute("type", type);
        model.addAttribute("mealingOrders", foodOrderService.findByMealing()); //用餐中的订单
        return "web/table/index";
    }
}

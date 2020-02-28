package com.zslin.web.controller;

import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.model.Category;
import com.zslin.model.DiningTable;
import com.zslin.model.Food;
import com.zslin.service.ICategoryService;
import com.zslin.service.IDiningTableService;
import com.zslin.service.IFoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(value = "web/food")
public class WebFoodController {

    @Autowired
    private IFoodService foodService;

    @Autowired
    private IDiningTableService diningTableService;

    @Autowired
    private ICategoryService categoryService;

//    public String

    /**
     *
     * @param model
     * @param tableId
     * @param cateId 分类ID，实际是分类的dataId
     * @param request
     * @return
     */
    @GetMapping(value = "index")
    public String index(Model model, Integer tableId, Integer cateId, Integer count, HttpServletRequest request) {
        DiningTable table = diningTableService.findOne(tableId);
        if(table==null) {return "direct:/web/table/index";} //如果没有选择餐桌则跳转
        model.addAttribute("table", table);
        Sort sort = SimpleSortBuilder.generateSort("orderNo_a");
        List<Food> foodList = foodService.findAll(sort);
        /*if(cateId!=null && cateId>0) {
            foodList = foodService.findByCateId(cateId, sort);
        } else {
            foodList = foodService.findAll(sort);
        }*/
        List<Category> categoryList = categoryService.findAll(sort);
        model.addAttribute("foodList", foodList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("count", count);
        return "web/food/index";
    }
}

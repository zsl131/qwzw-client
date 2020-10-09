package com.zslin.web.controller;

import com.zslin.basic.annotations.Token;
import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.basic.tools.PinyinToolkit;
import com.zslin.basic.tools.TokenTools;
import com.zslin.model.Category;
import com.zslin.model.Food;
import com.zslin.qwzw.model.FoodBag;
import com.zslin.qwzw.model.FoodBagDetail;
import com.zslin.qwzw.service.IFoodBagDetailService;
import com.zslin.qwzw.service.IFoodBagService;
import com.zslin.service.ICategoryService;
import com.zslin.service.IFoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(value = "web/foodBag")
public class FoodBagController {

    @Autowired
    private IFoodBagService foodBagService;

    @Autowired
    private IFoodService foodService;

    @Autowired
    private IFoodBagDetailService foodBagDetailService;

    @Autowired
    private ICategoryService categoryService;

    @GetMapping(value = "index")
    public String index(Model model, HttpServletRequest request) {
        List<FoodBag> bagList = foodBagService.findAll();
        model.addAttribute("bagList", bagList);
        return "web/foodBag/index";
    }

    @Token(flag= Token.READY)
    @GetMapping(value = "add")
    public String add(Model model, HttpServletRequest request) {
        FoodBag bag = new FoodBag();
        model.addAttribute("bag", bag);
        return "web/foodBag/add";
    }

    @Token(flag= Token.CHECK)
    @PostMapping(value="add")
    public String add(FoodBag bag, HttpServletRequest request) {
        if(TokenTools.isNoRepeat(request)) {
            bag.setSn(PinyinToolkit.cn2Spell(bag.getName(), ""));
            foodBagService.save(bag);
        }
        return "redirect:/web/foodBag/index";
    }

    @GetMapping(value = "onAddDetail")
    public String onAddDetail(Integer bagId, Integer cateId, String isFood, Model model, HttpServletRequest request) {
        FoodBag bag = foodBagService.findOne(bagId);
        Sort sort = SimpleSortBuilder.generateSort("orderNo");
        List<Category> categoryList = categoryService.findAll(sort);
        List<Food> foodList = foodService.findAll(sort);

        model.addAttribute("foodList", foodList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("bag", bag);
        return "web/foodBag/onAddDetail";
    }

    @PostMapping(value = "onAddDetail")
    public @ResponseBody String onAddDetail(Integer bagId, String bagName, String ids, String names, Integer amount) {
//        FoodBag bag = foodBagService.findOne(bagId);
        FoodBagDetail fbd = new FoodBagDetail();
        fbd.setAmount(amount);
        fbd.setBagId(bagId);
        fbd.setBagName(bagName);
        fbd.setFoodIds(ids);
        fbd.setFoodNames(names);
        foodBagDetailService.save(fbd);
        return "1";
    }

    /** 删除 */
    @PostMapping(value = "deleteDetail")
    public @ResponseBody String deleteDetail(Integer detailId) {
        foodBagDetailService.delete(detailId);
        return "1";
    }
}

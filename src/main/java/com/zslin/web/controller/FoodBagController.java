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
import com.zslin.web.dto.FoodBagDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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

        List<FoodBagDetail> detailList = foodBagDetailService.findByBagId(bagId);

        model.addAttribute("foodList", foodList);
        model.addAttribute("detailList", detailList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("bag", bag);
        return "web/foodBag/onAddDetail";
    }

    @PostMapping(value = "onAddDetail")
    public @ResponseBody String onAddDetail(Integer bagId, String bagName, String ids, Integer totalCount,
                                            String names, Integer amount, Integer cateId, String cateName) {
//        FoodBag bag = foodBagService.findOne(bagId);
        FoodBagDetail fbd = new FoodBagDetail();
        fbd.setAmount(amount);
        fbd.setBagId(bagId);
        fbd.setBagName(bagName);
        fbd.setFoodIds(ids);
        fbd.setFoodNames(names);
        fbd.setTotalCount(totalCount);
        fbd.setCategoryId(cateId);
        fbd.setCategoryName(cateName);
        foodBagDetailService.save(fbd);
        return "1";
    }

    /** 修改状态 */
    @PostMapping(value = "updateStatus")
    public @ResponseBody String updateStatus(Integer id, String status) {
        foodBagService.updateStatus(id, status);
        return "1";
    }

    /** 删除 */
    @PostMapping(value = "deleteDetail")
    public @ResponseBody String deleteDetail(Integer detailId) {
        foodBagDetailService.delete(detailId);
        return "1";
    }

    /** 获取所有可使用的套餐 */
    @PostMapping(value = "findAll")
    public @ResponseBody List<FoodBag> findAll() {
        List<FoodBag> bagList = foodBagService.findByStatus("1");
        return bagList;
    }

    @PostMapping(value = "loadOne")
    public @ResponseBody
    FoodBagDto loadOne(Integer id) {
        FoodBag bag = foodBagService.findOne(id);
        List<FoodBagDetail> detailList = foodBagDetailService.findByBagId(id);
        return new FoodBagDto(bag, detailList, foodService.findByIds(buildFoodIds(detailList)));
    }

    private Integer [] buildFoodIds(List<FoodBagDetail> detailList) {
        List<Integer> ids = new ArrayList<>();
        for(FoodBagDetail fbd:detailList) {
            ids.addAll(buildIds(fbd.getFoodIds()));
        }
        List<Integer> res = new ArrayList<>();
        for(Integer id : ids) {
            if(!res.contains(id)) {res.add(id);}
        }

        return res.toArray(new Integer[res.size()]);
    }

    private List<Integer> buildIds(String ids) {
        List<Integer> res = new ArrayList<>();
        String [] array = ids.split("-");
        for(String str : array) {
            try {
                Integer id = Integer.parseInt(str);
                if(!res.contains(id)) {res.add(id);} ;
            } catch (Exception e) {
            }
        }
        return res;
    }
}

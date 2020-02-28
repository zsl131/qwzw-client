package com.zslin.web.controller;

import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.basic.tools.NormalTools;
import com.zslin.model.*;
import com.zslin.service.ICategoryService;
import com.zslin.service.IDiningTableService;
import com.zslin.service.IFoodOrderService;
import com.zslin.service.IFoodService;
import com.zslin.tools.OrderNoTools;
import com.zslin.tools.WorkerCookieTools;
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
@RequestMapping("web/foodOrder")
public class WebFoodOrderController {

    @Autowired
    private IFoodOrderService foodOrderService;

    @Autowired
    private IDiningTableService diningTableService;

    @Autowired
    private OrderNoTools orderNoTools;

    @Autowired
    private WorkerCookieTools workerCookieTools;

    @Autowired
    private IFoodService foodService;

    @Autowired
    private ICategoryService categoryService;

    @GetMapping(value = "onOrder")
    public String onOrder(Model model, String orderNo, HttpServletRequest request) {
        FoodOrder order = foodOrderService.findByNo(orderNo);
        if(order==null) {return  "redirect:/web/table/index";} //如果订单不存在，则跳转
        DiningTable table = diningTableService.findOne(order.getTableId());
        if(table==null) {return "direct:/web/table/index";} //如果没有选择餐桌则跳转
        model.addAttribute("table", table);
        Sort sort = SimpleSortBuilder.generateSort("orderNo_a");
        List<Food> foodList = foodService.findAll(sort);
        List<Category> categoryList = categoryService.findAll(sort);
        model.addAttribute("foodList", foodList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("order", order);

        return "web/foodOrder/order";
    }

    /**
     * 点击餐桌下单
     * @param count 用餐人数
     * @param tableId 餐桌ID
     * @param request
     * @return
     */
    @PostMapping(value = "onOrder")
    public @ResponseBody
    String onOrder(Integer count, Integer tableId, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        FoodOrder fo = foodOrderService.findByTableIdAndStatus(tableId, "0"); //获取在就餐中的餐桌
        if(fo!=null) {return "redirect:web/foodOrder/onOrder?orderNo="+fo.getNo();} //如果在就餐中则跳转
        DiningTable table = diningTableService.findOne(tableId);
        FoodOrder order = new FoodOrder();
        order.setNo(orderNoTools.getOrderNo(""));
        order.setTableId(tableId);
        order.setStatus("0");
        order.setCreateDay(NormalTools.curDate("yyyy-MM-dd"));
        order.setCreateTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
        order.setCreateLong(System.currentTimeMillis());
        order.setTableName(table.getName());
        order.setAmount(count);

        order.setCashierName(w.getName());
        order.setCashierPhone(w.getPhone());

        foodOrderService.save(order);

        return order.getNo(); //返回订单编号
    }

    @PostMapping(value = "updatePeopleAmount")
    public @ResponseBody Integer updatePeopleAmount(String orderNo, Integer amount) {
        Integer count = foodOrderService.updatePeopleAmount(amount, orderNo);
        if(count>=1) {return amount;}
        else {return 0;}
    }
}

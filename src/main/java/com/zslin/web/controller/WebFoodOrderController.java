package com.zslin.web.controller;

import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.basic.tools.NormalTools;
import com.zslin.model.*;
import com.zslin.service.*;
import com.zslin.tools.OrderNoTools;
import com.zslin.tools.WorkerCookieTools;
import com.zslin.web.dto.AppendFoodDto;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private IFoodOrderDetailService foodOrderDetailService;

    /** 订单详情 */
    @GetMapping(value = "show")
    public String show(Model model, String orderNo) {
        FoodOrder order = foodOrderService.findByNo(orderNo);
        List<FoodOrderDetail> detailList = foodOrderDetailService.findByOrderNo(orderNo, SimpleSortBuilder.generateSort("id"));
        detailList = rebuildFoodDetail(detailList);
        model.addAttribute("order", order);
        model.addAttribute("detailList", detailList);
        return "web/foodOrder/show";
    }

    @PostMapping(value = "settlement")
    public @ResponseBody String settlement(String orderNo, Float totalMoney, String removeDot, String payType) {
        FoodOrder order = foodOrderService.findByNo(orderNo);
        float  orderMoney = buildTotalMoney(orderNo);
        if(order==null || !"0".equals(order.getStatus())) {return "-1";} //订单不存在或不在就餐中
        else if(orderMoney!=totalMoney) { return "-2"; } //订单金额和传入金额不一致
        float dotMoney = 0f;
        order.setTotalMoney(orderMoney);
        order.setTotalMoney2(orderMoney);
        if("1".equals(removeDot)) { //如果需要抹零
            float resMoney = rebuildMoney((float)Math.floor(orderMoney));
            dotMoney = rebuildMoney(orderMoney - resMoney);
            order.setTotalMoney2(resMoney);
            order.setDotMoney(dotMoney);
        }
        order.setRemoveDot(removeDot);
        order.setPayType(payType);
        order.setEndTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
        order.setEndLong(System.currentTimeMillis());
        order.setStatus("1");
        foodOrderService.save(order);
        //TODO 收银完成需要打印消费单
        return "1";
    }

    private float buildTotalMoney(String orderNo) {
        try {
            List<FoodOrderDetail> detailList = foodOrderDetailService.findByOrderNo(orderNo, SimpleSortBuilder.generateSort("id"));
            float money = 0f;
            for(FoodOrderDetail d : detailList) {
                money += (d.getPrice()*d.getAmount());
            }
            return rebuildMoney(money);
        } catch (Exception e) {
            return 0f;
        }
    }

    //保留2小数
    private float rebuildMoney(float money) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Float.parseFloat(df.format(money));
    }

    /** 关闭订单 */
    @PostMapping(value = "closeOrder")
    public @ResponseBody String closeOrder(String orderNo) {
        FoodOrder order = foodOrderService.findByNo(orderNo);
        if(order==null || !"0".equals(order.getStatus()) || order.getFoodCount()>0) {  //如果订单不存在或状态不为用餐中或有菜品
            return "-1";
        } else {
            order.setStatus("-1");
            order.setEndLong(System.currentTimeMillis());
            order.setEndTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            foodOrderService.save(order);
            return "1";
        }
    }

    /** 换桌 */
    @PostMapping(value = "changeTable")
    public @ResponseBody String changeTable(String orderNo, Integer tableId, String tableName) {
        FoodOrder order = foodOrderService.findByNo(orderNo);
        if(order==null || !"0".equals(order.getStatus())) { //如果订单不存在或状态不为用餐中
            return "-1";
        } else {
            order.setTableName(tableName);
            order.setTableId(tableId);
            foodOrderService.save(order);
            return "1";
        }
    }

    /** 获取空桌，用于换桌 */
    @PostMapping(value = "queryEmptyTables")
    public @ResponseBody List<DiningTable> queryEmptyTables() {
        List<DiningTable> emptyTables = diningTableService.findEmptyTableIds();
        return emptyTables;
    }

    /** 当准备结算订单 */
    @GetMapping(value = "onSettle")
    public String onSettle(Model model, String orderNo, HttpServletRequest request) {
        FoodOrder order = foodOrderService.findByNo(orderNo); //订单
        if(order==null || !"0".equals(order.getStatus())) {return buildShowUrl(orderNo);}
        List<FoodOrderDetail> detailList = foodOrderDetailService.findByOrderNo(orderNo, SimpleSortBuilder.generateSort("id"));
        detailList = rebuildFoodDetail(detailList);
        model.addAttribute("order", order);
        model.addAttribute("detailList", detailList);
        return "web/foodOrder/onSettle";
    }

    private List<FoodOrderDetail> rebuildFoodDetail(List<FoodOrderDetail> detailList) {
        if(detailList==null) {return new ArrayList<>();}
        Map<Integer, List<FoodOrderDetail>> map = new HashMap<>();
        for(FoodOrderDetail fod : detailList) {
            setData(map, fod.getFoodId(), fod);
        }
        return rebuildFood(map);
    }

    private void setData(Map<Integer, List<FoodOrderDetail>> map, Integer key, FoodOrderDetail value) {
        if(!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(value);
    }

    private List<FoodOrderDetail> rebuildFood(Map<Integer, List<FoodOrderDetail>> detailMap) {
        List<FoodOrderDetail> res = new ArrayList<>();
        for (Map.Entry<Integer, List<FoodOrderDetail>> entry : detailMap.entrySet()) {
            List<FoodOrderDetail> list = entry.getValue();
            Integer amount = 0; FoodOrderDetail detail=null;
            for(FoodOrderDetail fod : list) {amount += fod.getAmount(); detail = fod;}
            if(detail!=null) {
                detail.setAmount(amount);
                res.add(detail);
            }
        }
        return res;
    }

    @PostMapping(value = "appendFood")
    public @ResponseBody String appendFood(String orderNo, String foodData, HttpServletRequest request) {
        try {
            FoodOrder order = foodOrderService.findByNo(orderNo); //订单信息
            List<AppendFoodDto> foodList = buildFoodList(foodData); //菜品列表

            Integer unitAmount = order.getUnitCount();
            String batchNo = RandomUtils.nextLong(1000, 9999)+""; //太长不易显示
            for(AppendFoodDto dto : foodList) {
                FoodOrderDetail fod = new FoodOrderDetail();
                Food food = dto.getFood();
                fod.setBatchNo(batchNo);
                fod.setOrderNo(orderNo);
                fod.setCateId(food.getCateId());
                fod.setCateName(food.getCateName());
                fod.setCreateDay(NormalTools.curDate("yyyy-MM-dd"));
                fod.setCreateLong(System.currentTimeMillis());
                fod.setCreateTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
                fod.setFoodId(food.getId());
                fod.setFoodName(food.getName());
                fod.setFoodNameLetter(food.getNameLetter());
                fod.setOrderId(order.getId());
                fod.setPrintStatus("0");
                fod.setAmount(dto.getAmount());
                fod.setPrice(food.getPrice());
                fod.setSubtotal(food.getPrice()*dto.getAmount());
                foodOrderDetailService.save(fod);

                unitAmount += fod.getAmount();
            }
            foodOrderService.updateUnitCount(unitAmount, orderNo);
        } catch (Exception e) {
            e.printStackTrace();
            return "出错："+e.getMessage();
        }
        return "1";
    }

    //foodData ::9-1_5-2_2-1_4-2_
    private List<AppendFoodDto> buildFoodList(String foodData) {
        List<AppendFoodDto> result = new ArrayList<>();
        String [] array = foodData.split("_");
        for(String str : array) {
            if(str!=null && str.contains("-")) { //如果不为空并包含-
                String [] data = str.split("-");
                Integer foodId = getIntegerValue(data[0]);
                Integer amount = getIntegerValue(data[1]);
                if(foodId!=null && foodId>0 && amount!=null && amount>0) {
                    result.add(new AppendFoodDto(foodService.findOne(foodId), amount));
                }
            }
        }
        return result;
    }

    private Integer getIntegerValue(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping(value = "onOrder")
    public String onOrder(Model model, String orderNo, HttpServletRequest request) {
        FoodOrder order = foodOrderService.findByNo(orderNo);
        if(order==null || !"0".equals(order.getStatus())) {return buildShowUrl(orderNo);}
        //if(order==null) {return  "redirect:/web/table/index";} //如果订单不存在，则跳转
        DiningTable table = diningTableService.findOne(order.getTableId());
        if(table==null) {return "direct:/web/table/index";} //如果没有选择餐桌则跳转
        model.addAttribute("table", table);
        Sort sort = SimpleSortBuilder.generateSort("orderNo_a");
        List<Food> foodList = foodService.findAll(sort);
        List<Category> categoryList = categoryService.findAll(sort);
        model.addAttribute("foodList", foodList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("detailList", foodOrderDetailService.findByOrderNo(orderNo, SimpleSortBuilder.generateSort("createTime_d")));
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

    private String buildShowUrl(String orderNo) {
        return "redirect:/web/foodOrder/show?orderNo="+orderNo;
    }
}

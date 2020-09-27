package com.zslin.web.controller;

import com.zslin.basic.repository.SimplePageBuilder;
import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.basic.tools.NormalTools;
import com.zslin.basic.utils.ParamFilterUtil;
import com.zslin.dto.ResDto;
import com.zslin.model.*;
import com.zslin.service.*;
import com.zslin.tools.WorkerCookieTools;
import com.zslin.upload.tools.UploadFileTools;
import com.zslin.upload.tools.UploadJsonTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by 钟述林 393156105@qq.com on 2017/4/1 22:58.
 * 收银台，新版本
 */
@Controller
@RequestMapping(value = "web/newOrders")
public class NewOrdersController {

    @Autowired
    private ICommodityService commodityService;

    @Autowired
    private IBuffetOrderDetailService buffetOrderDetailService;

    @Autowired
    private IBuffetOrderService buffetOrderService;

    @Autowired
    private WorkerCookieTools workerCookieTools;


    @Autowired
    private IRulesService rulesService;

    @Autowired
    private IPriceService priceService;

    @Autowired
    private UploadFileTools uploadFileTools;

    @Autowired
    private IDiscountTimeService discountTimeService;

    @Autowired
    private IFoodOrderService foodOrderService;

    @GetMapping(value = "index")
    public String index(Model model, String type, HttpServletRequest request) {
        type = type == null || "".equalsIgnoreCase(type) ? "1" : type;
        List<Commodity> commodityList;
        if ("1".equalsIgnoreCase(type)) {
            commodityList = commodityService.listByTicket("");
            model.addAttribute("rules", rulesService.loadOne());
        } else {
            commodityList = commodityService.listByType("3");
        }

        model.addAttribute("commodityList", commodityList);
        model.addAttribute("type", type);
        model.addAttribute("discountTime", discountTimeService.findByTime(Integer.parseInt((new SimpleDateFormat("HHmm").format(new Date())))));
        return "web/newOrders/index";
    }

    /**
     * 订单列表
     */
    @GetMapping(value = "list")
    public String list(Model model, Integer page, HttpServletRequest request) {
        Page<FoodOrder> datas = foodOrderService.findAll(ParamFilterUtil.getInstance().buildSearch(model, request),
                SimplePageBuilder.generate(page, SimpleSortBuilder.generateSort("createLong_d")));
        model.addAttribute("datas", datas);
        return "web/newOrders/list";
    }

    @PostMapping(value = "removeOrder")
    public
    @ResponseBody
    ResDto removeOrder(String no, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-1", "未检测到收银员");
        } //未检测到收银员，刷新重新登陆
        BuffetOrder order = buffetOrderService.findByNo(no);
        if (order == null) {
            return new ResDto("-2", "未检查到订单信息");
        }
        if (!"0".equals(order.getStatus())) { //只要status为0时可删除订单
            return new ResDto("-3", "只有在刚下单状态下才可取消订单");
        }
        order.setStatus("-2");
        order.setRetreatName(w.getName());
        order.setRetreatReason("下单错误");
        order.setEndLong(System.currentTimeMillis());
        order.setEndTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
        order.setRetreatTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
        buffetOrderService.save(order);
        return new ResDto("1", "操作成功");
    }

    //发送友情价订单到服务器
    @PostMapping(value = "sendFriendOrder")
    public
    @ResponseBody
    ResDto sendFriendOrder(String no, String bossPhone, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-10", "无权操作");
        }
        BuffetOrder orders = buffetOrderService.findByNo(no);
        if (orders == null) {
            return new ResDto("-1", "订单不存在"); //订单不存在
        } else {
            if (!"0".equals(orders.getStatus())) {
                return new ResDto("-2", "当前状态不可再进行亲情折扣");
            }
            orders.setType("4");
            Float totalMoney = buildFriendMoney(no);
            Float discountMoney = orders.getTotalMoney() - totalMoney;
            orders.setTotalMoney(totalMoney);
            orders.setDiscountMoney(discountMoney);
            orders.setDiscountReason(bossPhone);
            orders.setDiscountType("2");
            buffetOrderService.save(orders);
            sendBuffetOrder2Server(orders);
            return new ResDto("0", "提交成功等待确认");
        }
    }

    private Float buildFriendMoney(String no) {
        List<BuffetOrderDetail> list = buffetOrderDetailService.listByOrderNo(no);
        Price price = priceService.findOne();
        Float result = 0f;
        for (BuffetOrderDetail d : list) {
            if ("88888".equals(d.getCommodityNo())) { //如果是午餐券
                result += price.getFriendBreakfastPrice();
            } else if ("99999".equals(d.getCommodityNo())) { //如果是晚餐券
                result += price.getFriendDinnerPrice();
            } else {
                result += d.getPrice();
            }
        }
        return result;
    }


    public void sendBuffetOrder2Server(BuffetOrder order) {
        String content = UploadJsonTools.buildDataJson(UploadJsonTools.buildBuffetOrder(order));
        uploadFileTools.setChangeContext(content, true);
    }

}

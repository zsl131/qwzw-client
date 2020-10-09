package com.zslin.web.controller;

import com.zslin.basic.annotations.AdminAuth;
import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.basic.tools.NormalTools;
import com.zslin.model.*;
import com.zslin.qwzw.tools.FoodDataTools;
import com.zslin.service.*;
import com.zslin.upload.tools.UploadFileTools;
import com.zslin.upload.tools.UploadJsonTools;
import com.zslin.web.dto.MyTicketDto;
import com.zslin.web.dto.MyTimeDto;
import com.zslin.web.tools.MyTicketTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 钟述林 393156105@qq.com on 2017/6/7 0:48.
 */
@Controller
@RequestMapping(value = "web/newOrders")
@AdminAuth(name = "订单管理", psn = "订单管理", orderNum = 10, porderNum = 1, pentity = 0, icon = "fa fa-list")
public class WebNewOrdersController {

    @Autowired
    private IBuffetOrderService buffetOrderService;

    @Autowired
    private IBuffetOrderDetailService buffetOrderDetailService;

    @Autowired
    private ICommodityService commodityService;

    @Autowired
    private IRulesService rulesService;

    @Autowired
    private IPrizeService prizeService;

    @Autowired
    private IMemberChargeService memberChargeService;

    @Autowired
    private IIncomeService incomeService;

    @Autowired
    private UploadFileTools uploadFileTools;

    @Autowired
    private IFoodOrderService foodOrderService;

    @Autowired
    private IFoodOrderDetailService foodOrderDetailService;

    @Autowired
    private IRefundOrderFoodService refundOrderFoodService;

    //会员充值统计
    private void calMemberCharge(Model model, String day) {
        Float mCash = memberChargeService.queryMoneyByPayType(day, "1"); //会员现金
        Float mWeixin = memberChargeService.queryMoneyByPayType(day, "3"); //会员微信
        Float mAlipay = memberChargeService.queryMoneyByPayType(day, "2"); //会员支付宝
        model.addAttribute("mCash", mCash==null?0:mCash);
        model.addAttribute("mWeixin", mWeixin==null?0:mWeixin);
        model.addAttribute("mAlipay", mAlipay==null?0:mAlipay);
    }

    @GetMapping(value = "cal")
    public String cal(Model model, String day, HttpServletRequest request) {
        if(day==null) {
            day = NormalTools.curDate("yyyy-MM-dd"); //默认为当天
        } else {
            day = day.replace("eq-", "");
        }

        MyTimeDto mtd = new MyTimeDto(day);

        //Integer deskCount = foodOrderService.deskCount(day); //人数
        Integer peopleCount = foodOrderService.peopleCount(day); //桌数

        Integer dotCount = foodOrderService.dotCount(day); //抹零数量
        Float dotMoney = foodOrderService.dotMoney(day); //抹零金额

        List<FoodOrderDetail> detailList = foodOrderDetailService.findByCreateDay(day, SimpleSortBuilder.generateSort("foodId"));
        detailList = FoodDataTools.rebuildFoodDetail(detailList);
        Integer deskCount = foodOrderService.deskCount(day, "-1");

        Float discountMoney = foodOrderService.discountMoney(day); //抵扣金额

        model.addAttribute("deskCount", deskCount==null?0:deskCount);
        model.addAttribute("deskCount", deskCount==null?0:deskCount); //关闭桌数
        model.addAttribute("peopleCount", peopleCount==null?0:peopleCount);
        model.addAttribute("dotCount", dotCount==null?0:dotCount);
        model.addAttribute("dotMoney", dotMoney==null?0:dotMoney);
        Integer unEndCount = foodOrderService.unEndCount(day);
        model.addAttribute("unEndCount", unEndCount==null?0:unEndCount); //未结束数量
        model.addAttribute("discountMoney", discountMoney==null?0:discountMoney); //抵扣金额

        Float totalMoney = foodOrderService.totalMoney(day);
        model.addAttribute("totalMoney", totalMoney==null?0:totalMoney); //总金额，含抹零金额

        Float wxMoney = foodOrderService.payMoney(day, "2");
        Float alipayMoney = foodOrderService.payMoney(day, "3");
        model.addAttribute("wxMoney", wxMoney==null?0:wxMoney); //微信金额
        model.addAttribute("alipayMoney", alipayMoney==null?0:alipayMoney); //支付宝金额

        model.addAttribute("detailList", detailList);

        List<RefundOrderFood> refundList = refundOrderFoodService.findByCreateDay(day);
        model.addAttribute("refundList", refundList);
        model.addAttribute("day", day);

        model.addAttribute("income", incomeService.findByComeDay(day.replaceAll("-", "")));
        return "web/newOrders/cal";
    }

    @PostMapping(value = "addIncome")
    public @ResponseBody String addIncome(String day, Float money, Integer peopleCount, Integer todayTotalDesk) {
        day = day.replaceAll("-", "");
        Income income = incomeService.findByComeDay(day);
        if(income==null) {
            income = new Income();
            income.setComeDay(day);
        }
        income.setMoney(money);
        income.setPeopleCount(peopleCount);
        income.setDeskCount(todayTotalDesk);
        incomeService.save(income);
//        sendIncome2Server(income); //TODO 先暂时不提交到服务端
        return "1";
    }

    private void sendIncome2Server(Income income) {
        String content = UploadJsonTools.buildDataJson(UploadJsonTools.buildIncome(income));
        uploadFileTools.setChangeContext(content, true);
    }

    private void buildBondMoney(MyTimeDto mtd, Model model) {
        Float bondMoneyAM = buffetOrderService.queryBondMoney(mtd.getStartTimeAM(), mtd.getEndTimeAM());
        model.addAttribute("bondMoneyAM", bondMoneyAM==null?0:bondMoneyAM); //押金
        Float bondMoneyPM = buffetOrderService.queryBondMoney(mtd.getStartTimePM(), mtd.getEndTimePM());
        model.addAttribute("bondMoneyPM", bondMoneyPM==null?0:bondMoneyPM); //押金

        Float unBackBondAM = buffetOrderService.queryBondByStatus(mtd.getStartTimeAM(), mtd.getEndTimeAM(), "2"); //未退押金
        Float surplusBondAM = buffetOrderService.queryBondByStatus(mtd.getStartTimeAM(), mtd.getEndTimeAM(), "5"); //已扣押金
        model.addAttribute("unBackBondAM", unBackBondAM==null?0:unBackBondAM);
        model.addAttribute("surplusBondAM", surplusBondAM==null?0:surplusBondAM);

        Float unBackBondPM = buffetOrderService.queryBondByStatus(mtd.getStartTimePM(), mtd.getEndTimePM(), "2"); //未退押金
        Float surplusBondPM = buffetOrderService.queryBondByStatus(mtd.getStartTimePM(), mtd.getEndTimePM(), "5"); //已扣押金
        model.addAttribute("unBackBondPM", unBackBondPM==null?0:unBackBondPM);
        model.addAttribute("surplusBondPM", surplusBondPM==null?0:surplusBondPM);
    }

    private void calMeituan(MyTimeDto mtd, Model model) {
        List<BuffetOrder> list = buffetOrderService.findByMeiTuan(mtd.getStartTimeAM(), mtd.getEndTimeAM()); //上午
        model.addAttribute("meituanAmountAM", buildMeituanAmount(list));

        List<BuffetOrder> listPM = buffetOrderService.findByMeiTuan(mtd.getStartTimePM(), mtd.getEndTimePM()); //下午
        model.addAttribute("meituanAmountPM", buildMeituanAmount(listPM));
    }

    private void calFfan(MyTimeDto mtd, Model model) {
        List<BuffetOrder> list = buffetOrderService.findByFfan(mtd.getStartTimeAM(), mtd.getEndTimeAM()); //上午
        model.addAttribute("ffanAmountAM", buildFfanAmount(list));

        List<BuffetOrder> listPM = buffetOrderService.findByFfan(mtd.getStartTimePM(), mtd.getEndTimePM()); //下午
        model.addAttribute("ffanAmountPM", buildFfanAmount(listPM));
    }

    private Integer buildFfanAmount(List<BuffetOrder> list) {
        Integer res = 0;
        for(BuffetOrder order : list) {
            String datas = order.getDiscountReason();
            String [] array = datas.split(",");
            for(String d : array) {
                if(d!=null) {
                    res ++;
                }
            }
        }
        return res;
    }

    private Integer buildMeituanAmount(List<BuffetOrder> list) {
        Integer res = 0;
        for(BuffetOrder order : list) {
            String datas = order.getDiscountReason();
            String [] array = datas.split(",");
            for(String d : array) {
                if(d!=null && d.length()==12) {
                    res ++;
                }
            }
        }
        return res;
    }

    private void calTicket(MyTimeDto mtd, Model model) {
        List<BuffetOrder> list = buffetOrderService.findByTicket(mtd.getStartTimeAM(), mtd.getEndTimeAM()); //上午
        model.addAttribute("ticketAmountAM", buildTicketAmount(list));

        List<BuffetOrder> listPM = buffetOrderService.findByTicket(mtd.getStartTimePM(), mtd.getEndTimePM()); //下午
        model.addAttribute("ticketAmountPM", buildTicketAmount(listPM));
    }

    private void calScoreMoney(MyTimeDto mtd, Model model) {
        Float scoreMoneyAM = buffetOrderService.queryScoreDiscount(mtd.getStartTimeAM(), mtd.getEndTimeAM()); //会员抵价金额
        Float scoreMoneyPM = buffetOrderService.queryScoreDiscount(mtd.getStartTimePM(), mtd.getEndTimePM()); //会员抵价金额

        model.addAttribute("scoreMoneyAM", scoreMoneyAM==null?0:scoreMoneyAM);
        model.addAttribute("scoreMoneyPM", scoreMoneyPM==null?0:scoreMoneyPM);
    }

    private Map<MyTicketDto, Integer> buildTicketAmount(List<BuffetOrder> list) {
        MyTicketTools mtt = new MyTicketTools();
        for(BuffetOrder order : list) {
            String [] array = order.getDiscountReason().split("_");
            for(String single : array) {
                if(single!=null && single.indexOf(":")>0) {
                    String [] s_a = single.split(":");
                    Integer dataId = Integer.parseInt(s_a[0]); //Prize的id
                    Integer amount = Integer.parseInt(s_a[1]); //对应数量
                    Prize prize = prizeService.findByDataId(dataId);
                    mtt.add(prize.getId(), prize.getName(), amount);
                }
            }
        }
        return mtt.getDatas();
    }

    private void buildMemberMoney(MyTimeDto mtd, Model model) {
        Float memberMoneyAM = buffetOrderService.queryMemberDiscount(mtd.getStartTimeAM(), mtd.getEndTimeAM()); //会员抵价金额
        Float memberMoneyPM = buffetOrderService.queryMemberDiscount(mtd.getStartTimePM(), mtd.getEndTimePM()); //会员抵价金额

        model.addAttribute("memberMoneyAM", memberMoneyAM==null?0:memberMoneyAM);
        model.addAttribute("memberMoneyPM", memberMoneyPM==null?0:memberMoneyPM);
    }

    private void buildBond(MyTimeDto mtd, Model model) {
        Float weixinBondAM = buffetOrderService.queryBondByType(mtd.getStartTimeAM(), mtd.getEndTimeAM(), "3"); //微信押金
        Float weixinBondPM = buffetOrderService.queryBondByType(mtd.getStartTimePM(), mtd.getEndTimePM(), "3"); //微信押金

        Float alipayBondAM = buffetOrderService.queryBondByType(mtd.getStartTimeAM(), mtd.getEndTimeAM(), "4"); //支付宝押金
        Float alipayBondPM = buffetOrderService.queryBondByType(mtd.getStartTimePM(), mtd.getEndTimePM(), "4"); //支付宝押金

        Float cashBondAM = buffetOrderService.queryBondByType(mtd.getStartTimeAM(), mtd.getEndTimeAM(), "1"); //现金押金
        Float cashBondPM = buffetOrderService.queryBondByType(mtd.getStartTimePM(), mtd.getEndTimePM(), "1"); //现金押金

        Float returnedBondAM = buffetOrderService.queryReturnedBond(mtd.getStartTimeAM(), mtd.getEndTimeAM()); //已退押金
        Float returnedBondPM = buffetOrderService.queryReturnedBond(mtd.getStartTimePM(), mtd.getEndTimePM()); //已退押金

        model.addAttribute("weixinBondAM", weixinBondAM==null?0:weixinBondAM);
        model.addAttribute("weixinBondPM", weixinBondPM==null?0:weixinBondPM);
        model.addAttribute("alipayBondAM", alipayBondAM==null?0:alipayBondAM);
        model.addAttribute("alipayBondPM", alipayBondPM==null?0:alipayBondPM);
        model.addAttribute("cashBondAM", cashBondAM==null?0:cashBondAM);
        model.addAttribute("cashBondPM", cashBondPM==null?0:cashBondPM);
        model.addAttribute("returnedBondAM", returnedBondAM==null?0:returnedBondAM);
        model.addAttribute("returnedBondPM", returnedBondPM==null?0:returnedBondPM);
    }

    private void queryTotalMoney(MyTimeDto mtd, Model model) {
        Float totalAM = buffetOrderService.queryTotalMoneyByDay(mtd.getStartTimeAM(), mtd.getEndTimeAM());
        Float totalPM = buffetOrderService.queryTotalMoneyByDay(mtd.getStartTimePM(), mtd.getEndTimePM());
        model.addAttribute("totalAM", totalAM==null?0:totalAM);
        model.addAttribute("totalPM", totalPM==null?0:totalPM);
    }

    private void queryCount(String day, Model model) {
        Integer halfAm = buffetOrderDetailService.queryCount(day, "66666"); //午餐半票人数
        Integer fullAm = buffetOrderDetailService.queryCount(day, "88888"); //午餐全票人数
        Integer halfPm = buffetOrderDetailService.queryCount(day, "77777"); //晚餐半票人数
        Integer fullPm = buffetOrderDetailService.queryCount(day, "99999"); //晚餐全票人数
        Integer speCount = buffetOrderDetailService.queryCount(day, "33333"); //简餐人籹
        Integer speHalfCount = buffetOrderDetailService.queryCount(day, "22222"); //简餐人籹,半票
        model.addAttribute("speHalfCount", speHalfCount);

        model.addAttribute("halfAm", halfAm);
        model.addAttribute("fullAm", fullAm);
        model.addAttribute("halfPm", halfPm);
        model.addAttribute("fullPm", fullPm);
        model.addAttribute("speCount", speCount);
    }
}

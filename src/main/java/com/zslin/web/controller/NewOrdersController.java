package com.zslin.web.controller;

import com.zslin.basic.repository.SimplePageBuilder;
import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.basic.tools.NormalTools;
import com.zslin.basic.utils.ParamFilterUtil;
import com.zslin.card.model.Card;
import com.zslin.card.service.ICardService;
import com.zslin.card.tools.CardWriteOffTools;
import com.zslin.dto.ResDto;
import com.zslin.meituan.dto.ReturnDto;
import com.zslin.meituan.tools.MeituanHandlerTools;
import com.zslin.model.*;
import com.zslin.service.*;
import com.zslin.tools.*;
import com.zslin.upload.tools.UploadFileTools;
import com.zslin.upload.tools.UploadJsonTools;
import com.zslin.web.tools.ScoreTools;
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
import java.util.ArrayList;
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
    private OrderNoTools orderNoTools;

    @Autowired
    private IRulesService rulesService;

    @Autowired
    private IPriceService priceService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private UploadFileTools uploadFileTools;

    @Autowired
    private IPrizeService prizeService;

    @Autowired
    private PrintTicketTools printTicketTools;

    @Autowired
    private IDiscountTimeService discountTimeService;

    @Autowired
    private RestdayTools restdayTools;

    @Autowired
    private IWalletService walletService;

    @Autowired
    private CardWriteOffTools cardWriteOffTools;

    @Autowired
    private ICardService cardService;

    @Autowired
    private IDiscountConfigService discountConfigService;

    @Autowired
    private DiscountDayTools discountDayTools;

    @GetMapping(value = "index")
    public String index(Model model, String type, HttpServletRequest request) {
        type = type == null || "".equalsIgnoreCase(type) ? "1" : type;
        List<Commodity> commodityList;
        if ("1".equalsIgnoreCase(type)) {
            if(restdayTools.isWorkday()) {
                commodityList = commodityService.listByTicket("");
            } else {
                commodityList = commodityService.listByTicket("33333","22222");
            }
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
        Page<BuffetOrder> datas = buffetOrderService.findAll(ParamFilterUtil.getInstance().buildSearch(model, request),
                SimplePageBuilder.generate(page, SimpleSortBuilder.generateSort("createLong_d")));
        model.addAttribute("datas", datas);
        return "web/newOrders/list";
    }

    @PostMapping(value = "addBuffetOrder")
    public
    @ResponseBody
    ResDto addBuffetOrder(String commodityNo, HttpServletRequest request) {
        try {
            Worker w = workerCookieTools.getWorker(request);
            if (w == null) {
                return new ResDto("-1", "未检测到收银员");
            } //未检测到收银员，刷新重新登陆
            List<BuffetOrderDetail> detailList = buildDetail(commodityNo);
            if (detailList == null || detailList.size() <= 0) {
                return new ResDto("-2", "订单中无商品");
            }
            BuffetOrder order = new BuffetOrder();
            order.setCashierName(w.getName());
            order.setIsSelf("3".equals(detailList.get(0).getCommodityType()) ? "0" : "1"); //如果商品类型为3则表示是外卖单品
            order.setCashierPhone(w.getPhone());
            order.setNo(orderNoTools.getOrderNo("1")); //前台订单以1开头
            order.setCreateLong(System.currentTimeMillis());
            order.setCreateTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setCreateDay(NormalTools.curDate("yyyy-MM-dd"));
            order.setStatus("0"); //表示下单状态，未付款
            order.setType("1");
            order.setCommodityCount(detailList.size()); //订单中商品数量
            order.setTotalMoney(buildTotalMoney(detailList)); //订单总价
            order.setDiscountType("0");

            DiscountTime discountTime = discountTimeService.findByTime(Integer.parseInt((new SimpleDateFormat("HHmm").format(new Date()))));
            if (discountTime != null && discountTime.getDiscountMoney() > 0) {
                Float needDiscountMoney = buildDiscountMoney(detailList, discountTime.getDiscountMoney());
                if (needDiscountMoney > 0) {
                    order.setDiscountMoney(needDiscountMoney);
                    //order.setTotalMoney(order.getTotalMoney() - needDiscountMoney);
                    order.setDiscountType("10");
                    order.setDiscountReason("时段折扣");
                }
            }
            Float discountDayMoney = buildDiscountDayMoney(detailList);
            if(discountDayTools.isDiscountDay() && discountDayMoney>0) { //如果是折扣日
                order.setDiscountType("12");
                order.setDiscountReason("折扣日");
                order.setDiscountMoney(discountDayMoney);
            }
            buffetOrderService.save(order);

            addOrderDetail(detailList, order); //保存订单商品信息
            return new ResDto(order.getNo(), "下单成功"); //表示下单成功
        } catch (Exception e) {
            return new ResDto("-3", "订单错误");
        }
    }

    private Float buildDiscountDayMoney(List<BuffetOrderDetail> detailList) {
        Float res = 0f;
        DiscountConfig config = discountConfigService.loadOne();
        if(config!=null && "1".equals(config.getStatus())) {
            for (BuffetOrderDetail bod : detailList) {
                if ("88888".equalsIgnoreCase(bod.getCommodityNo())) {
                    res += config.getDiscountAM();
                } else if("99999".equalsIgnoreCase(bod.getCommodityNo())) {
                    res += config.getDiscountPM();
                } else if("66666".equals(bod.getCommodityNo())) {
                    res += config.getDiscountHalfAM();
                } else if("77777".equals(bod.getCommodityNo())) {
                    res += config.getDiscountHalfPM();
                }
            }
        }
        return res;
    }

    //计算时段折扣应折扣的金额
    private Float buildDiscountMoney(List<BuffetOrderDetail> detailList, Float price) {
        Float res = 0f;
        for (BuffetOrderDetail bod : detailList) {
            if ("88888".equalsIgnoreCase(bod.getCommodityNo()) || "99999".equalsIgnoreCase(bod.getCommodityNo())) {
                res += price;
            }
        }
        return res;
    }

    private void addOrderDetail(List<BuffetOrderDetail> detailList, BuffetOrder order) {
        for (BuffetOrderDetail bod : detailList) {
            bod.setOrderId(order.getId());
            bod.setOrderNo(order.getNo());
            buffetOrderDetailService.save(bod);
        }
    }

    //订单总价
    private Float buildTotalMoney(List<BuffetOrderDetail> detailList) {
        Float res = 0f;
        for (BuffetOrderDetail bod : detailList) {
            res += bod.getPrice();
        }
        return res;
    }

    private List<BuffetOrderDetail> buildDetail(String commodityNo) {
        List<BuffetOrderDetail> res = new ArrayList<>();
        if (commodityNo == null || "".equalsIgnoreCase(commodityNo) || commodityNo.indexOf(",") < 0) {
            return res;
        }
        String[] nos = commodityNo.split(",");
        for (String no : nos) {
            if (no != null && !"".equalsIgnoreCase(no) && !"0".equalsIgnoreCase(no)) {
                Commodity c = commodityService.findByNo(no);
                if (c != null) {
                    BuffetOrderDetail bod = new BuffetOrderDetail();
                    bod.setPrice(c.getPrice());
                    bod.setCommodityId(c.getId());
                    bod.setCommodityName(c.getName());
                    bod.setCommodityNo(c.getNo());
                    bod.setCommodityType(c.getType());
                    bod.setCreateLong(System.currentTimeMillis());
                    bod.setCreateTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
                    bod.setCreateDay(NormalTools.curDate("yyyy-MM-dd"));
                    res.add(bod);
                }
            }
        }
        return res;
    }

    //订单支付页面
    @GetMapping(value = "payBuffetOrder")
    public String payBuffetOrder(Model model, String orderNo, HttpServletRequest request) {
        BuffetOrder order = buffetOrderService.findByNo(orderNo);
        if ("0".equals(order.getIsSelf())) { //如果是外卖单品，则跳转
            return "redirect:/web/newOrders/payOutOrder?orderNo=" + orderNo;
        }
        String status = order.getStatus();
        if (!"0".equalsIgnoreCase(status) && !"1".equalsIgnoreCase(status) && !"6".equals(status)) {
            return "redirect:/web/newOrders/showOrder?orderNo=" + orderNo;
        }
        model.addAttribute("order", order);
        model.addAttribute("commodityList", buffetOrderDetailService.listByOrderNo(orderNo));
        model.addAttribute("price", priceService.findOne());
        model.addAttribute("rules", rulesService.loadOne());
        model.addAttribute("discountConfig", discountConfigService.loadOne());
        model.addAttribute("isDiscountDay", discountDayTools.isDiscountDay()); //今天是否为折扣日
        return "web/newOrders/payBuffetOrder";
    }

    @GetMapping(value = "payOutOrder")
    public String payOutOrder(Model model, String orderNo, HttpServletRequest request) {
        BuffetOrder order = buffetOrderService.findByNo(orderNo);
        if ("1".equals(order.getIsSelf())) { //如果是自助餐则跳转
            return "redirect:/web/newOrders/payBuffetOrder?orderNo=" + orderNo;
        }
        String status = order.getStatus();
        if (!"0".equalsIgnoreCase(status)) {
            return "redirect:/web/newOrders/showOrder?orderNo=" + orderNo;
        }
        model.addAttribute("order", order);
        model.addAttribute("commodityList", buffetOrderDetailService.listByOrderNo(orderNo));
        return "web/newOrders/payOutOrder";
    }

    /**
     * 提交外卖订单
     */
    @PostMapping(value = "postOutOrder")
    public
    @ResponseBody
    ResDto postOutOrder(String no, String payType,
                        String specialType, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-1", "未检测到收银员");
        } //未检测到收银员，刷新重新登陆
        BuffetOrder order = buffetOrderService.findByNo(no);
        if (order == null) {
            return new ResDto("-2", "未检查到订单信息");
        }
        if ("1".equals(specialType)) { //普通订单
            order.setSurplusBond(0f); //剩余压金金额
            order.setType(specialType); //订单类型
            order.setPayType(payType);
            order.setDiscountType("0");
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //确认收款

            order.setEndLong(System.currentTimeMillis());
            order.setEndTime(NormalTools.curDate());
            //TODO 需要打印小票
        } else if ("7".equals(specialType)) { //如果是货到付款
            order.setSurplusBond(0f); //剩余压金金额
            order.setType(specialType); //订单类型
            order.setDiscountType("0");
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("1"); //已下单，配货送货中……
            //TODO 需要打印小票
        }
        buffetOrderService.save(order);

        //TODO 生成小票
        printTicketTools.printOutOrder(order);

        sendBuffetOrder2Server(order); //发送数据到服务器
        sendBuffetOrderDetail2Server(order.getNo());
        return new ResDto("1", "操作成功");
    }

    @GetMapping(value = "queryOrder")
    public
    @ResponseBody
    BuffetOrder queryOrder(String orderNo, HttpServletRequest request) {
        BuffetOrder order = buffetOrderService.findByNo(orderNo);
        return order;
    }

    //订单显示页面
    @GetMapping(value = "showOrder")
    public String showOrder(Model model, String orderNo, HttpServletRequest request) {
        BuffetOrder order = buffetOrderService.findByNo(orderNo);
        model.addAttribute("order", order);
        model.addAttribute("commodityList", buffetOrderDetailService.listByOrderNo(orderNo));
        model.addAttribute("price", priceService.findOne());
        model.addAttribute("rules", rulesService.loadOne());
        return "web/newOrders/showOrder";
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

    @Autowired
    private MeituanHandlerTools meituanHandlerTools;

    @Autowired
    private ScoreTools scoreTools;

    /**
     * 提交订单
     */
    @PostMapping(value = "postOrder")
    public
    @ResponseBody
    ResDto postOrder(String no, Float bondMoney, Integer bondCount, String payType,
                     String bondPayType, String specialType, String reserve, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-1", "未检测到收银员");
        } //未检测到收银员，刷新重新登陆
        BuffetOrder order = buffetOrderService.findByNo(no);
        if (order == null) {
            return new ResDto("-2", "未检查到订单信息");
        }
        Float totalBondMoney = (bondCount >= 1) ? bondMoney : 0f;
        if ("5".equalsIgnoreCase(specialType)) { //如果是会员订单
            Member m = memberService.findByPhone(reserve);
            if (m == null) {
                return new ResDto("-3", "未检测到会员信息");
            }
//            order.setSurplusBond(bondMoney*bondCount); //剩余压金金额
            order.setSurplusBond(totalBondMoney); //剩余压金金额
            order.setType(specialType); //订单类型
            order.setPayType(payType);
            Float memberSurplus = m.getSurplus() * 1.0f / 100;
            Float curSurplus = memberSurplus - order.getTotalMoney(); //消费后剩余
            order.setDiscountMoney(curSurplus >= 0 ? order.getTotalMoney() : memberSurplus);
            order.setDiscountReason(reserve);
            order.setTotalMoney(order.getTotalMoney() - order.getDiscountMoney());
            order.setDiscountType("5");
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //就餐中……
            memberService.plusMoneyByPhone(0 - (int) (order.getDiscountMoney() * 100), reserve);
            //TODO 服务器端需要自行对会员账户进行消费处理
            //TODO 需要打印小票
        } else if("11".equalsIgnoreCase(specialType)) { //如果是积分订单
            Wallet wallet = walletService.findByPhone(reserve);
            if (wallet == null) {
                return new ResDto("-3", "未检测到账户信息");
            }
            order.setSurplusBond(totalBondMoney); //剩余压金金额
            order.setType(specialType); //订单类型
            order.setPayType(payType);
            Rules rules = rulesService.loadOne();
            Float totalMoney = order.getTotalMoney(); //订单总金额
            Integer score = wallet.getScore(); //用户剩余积分
            Integer scoreMoney = score / rules.getScoreMoney();
            Integer discountMost = (int) (totalMoney * rules.getScoreDeductible() / 100);
            Integer discountReal = discountMost>scoreMoney?scoreMoney:discountMost; //实际抵扣金额

            order.setDiscountMoney(discountReal*1f);
            order.setDiscountReason(reserve);
            order.setTotalMoney(totalMoney - discountReal);
            order.setDiscountType("1"); //积分抵扣

            /*Float memberSurplus = m.getSurplus() * 1.0f / 100;
            Float curSurplus = memberSurplus - order.getTotalMoney(); //消费后剩余
            order.setDiscountMoney(curSurplus >= 0 ? order.getTotalMoney() : memberSurplus);
            order.setDiscountReason(reserve);
            order.setTotalMoney(order.getTotalMoney() - order.getDiscountMoney());
            order.setDiscountType("5");*/
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //就餐中……
//            memberService.plusMoneyByPhone(0 - (int) (order.getDiscountMoney() * 100), reserve);

            scoreTools.scoreConsume(wallet.getOpenid(), discountReal*rules.getScoreMoney()); //这里需要传openid，服务端需要使用openid
            //TODO 服务器端需要自行对会员账户进行消费处理
            //TODO 需要打印小票
        } else if ("1".equals(specialType)) { //普通订单
//            order.setSurplusBond(bondMoney*bondCount); //剩余压金金额
            order.setSurplusBond(totalBondMoney); //剩余压金金额
            order.setType(specialType); //订单类型
            order.setPayType(payType);
//            order.setDiscountType("0");
            order.setEntryLong(System.currentTimeMillis());
            if("10".equalsIgnoreCase(order.getDiscountType())) { //如果是时段折扣
                order.setTotalMoney(order.getTotalMoney()-order.getDiscountMoney());
            } else if("12".equals(order.getDiscountType())) { //折扣日
                order.setTotalMoney(order.getTotalMoney()-order.getDiscountMoney());
            }
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //就餐中……
            //TODO 需要打印小票
        } else if ("4".equals(specialType)) { //如果是亲情折扣订单
//            order.setSurplusBond(bondMoney*bondCount); //剩余压金金额
            order.setSurplusBond(totalBondMoney); //剩余压金金额
            order.setPayType(payType);
            order.setType(specialType); //订单类型
            order.setDiscountType("2");
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //就餐中……
            //TODO 需要打印小票
        } else if ("6".equals(specialType)) { //如果是卡券订单
//            Float discountMoney = buildDiscountMoney(no, reserve);
            Float discountMoney = buildCardDiscountMoney(no, reserve);
            order.setDiscountMoney(discountMoney);
            order.setTotalMoney(order.getTotalMoney() - discountMoney);
            order.setType(specialType); //订单类型
            order.setDiscountReason(reserve);
            order.setDiscountType("3"); //抵价券
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //就餐中……
            order.setPayType(payType);
//            order.setSurplusBond(bondMoney*bondCount); //剩余压金金额
            order.setSurplusBond(totalBondMoney); //剩余压金金额
        } else if ("3".equalsIgnoreCase(specialType)) { //美团订单
            Float discountMoney = buildDiscountMoneyByMt(no, reserve);
            order.setDiscountMoney(discountMoney);
            order.setTotalMoney(order.getTotalMoney() - discountMoney);
            order.setType(specialType); //订单类型
            order.setMeituanNum(reserve);
            String discountReason = buildDiscountReasonByMt(no, reserve);
            if (discountReason != null && discountReason.length() > 11) {
                order.setDiscountReason(discountReason);
                order.setMtStatus("1");
            } else {
                order.setDiscountReason(reserve);
                order.setMtStatus("0");
            }
            order.setDiscountType("6"); //美团
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //就餐中……
            order.setPayType(payType);
//            order.setSurplusBond(bondMoney*bondCount); //剩余压金金额
            order.setSurplusBond(totalBondMoney); //剩余压金金额
        } else if ("9".equals(specialType)) { //飞凡订单
            Float discountMoney = buildDiscountMoneyByFfan(no, reserve);
            order.setDiscountMoney(discountMoney);
            order.setTotalMoney(order.getTotalMoney() - discountMoney);
            order.setType(specialType); //订单类型
            order.setMeituanNum(reserve);
            String discountReason = buildDiscountReasonByMt(no, reserve);
            order.setDiscountReason(reserve);
            order.setMtStatus("0");
            order.setDiscountType("9"); //飞凡
            order.setEntryLong(System.currentTimeMillis());
            order.setEntryTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
            order.setStatus("2"); //就餐中……
            order.setPayType(payType);
//            order.setSurplusBond(bondMoney*bondCount); //剩余压金金额
            order.setSurplusBond(totalBondMoney); //剩余压金金额
        }
        order.setBondPayType(bondPayType);
        buffetOrderService.save(order);

        //TODO 生成小票
        printTicketTools.printBuffetOrder(order);

        sendBuffetOrder2Server(order); //发送数据到服务器
        sendBuffetOrderDetail2Server(order.getNo());
        return new ResDto("1", "操作成功");
    }

    private String buildDiscountReasonByMt(String orderNo, String reserve) {
//        meituanHandlerTools.h
        StringBuffer sb = new StringBuffer();
        String[] array = reserve.split(",");
        for (String single : array) {
            if (single == null) {
                continue;
            }
            if (single.indexOf("_") >= 0) {
                String[] tmpArray = single.split("_");
                Integer count = Integer.parseInt(tmpArray[1]); //美团券张数
//                ReturnDto dto = meituanHandlerTools.handlerCheck(tmpArray[0], count, orderNo);
                ReturnDto dto = null;
                if (dto != null && dto.getCode() > 0 && dto.getData() != null) {
                    sb.append(dto.getData().toString());
                }
            }
        }
        return sb.toString();
    }

    private Float buildDiscountMoneyByMt(String orderNo, String reserve) {
        Float res = 0f;
        String[] array = reserve.split(",");
        List<BuffetOrderDetail> details = buffetOrderDetailService.listByOrderNo(orderNo);
        int flag = 0;
        for (String single : array) {
            if (single == null) {
                continue;
            }
            if (single.indexOf("_") >= 0) {
                String[] tmpArray = single.split("_");
                Integer count = Integer.parseInt(tmpArray[1]); //美团券张数
                Integer amount = Integer.parseInt(tmpArray[2]); //一张券所抵人数
                for (int i = 0; i < count * amount; i++) {
                    res += details.get(flag).getPrice();
                    flag++;
                }
            } else if (single != null && single.length() == 12 && flag < details.size()) {
                res += details.get(flag).getPrice();
                flag++;
            }
        }
        return res;
    }


    private Float buildDiscountMoneyByFfan(String orderNo, String reserve) {
        Float res = 0f;
        String[] array = reserve.split(",");
        List<BuffetOrderDetail> details = buffetOrderDetailService.listByOrderNo(orderNo);
        int flag = 0;
        for (String single : array) {
            if (single == null) {
                continue;
            }
            if (single.indexOf("_") >= 0) {
                String[] tmpArray = single.split("_");
                Integer count = Integer.parseInt(tmpArray[1]); //飞凡券张数
                Integer amount = Integer.parseInt(tmpArray[2]); //一张券所抵人数
                for (int i = 0; i < count * amount; i++) {
                    res += details.get(flag).getPrice();
                    flag++;
                }
            } else if (single != null && flag < details.size()) {
                res += details.get(flag).getPrice();
                flag++;
            }
        }
        return res;
    }

    private Float buildDiscountMoney(String orderNo, String reserve) {
        Float res = 0f;
        String[] array = reserve.split("_");
        for (String single : array) {
            if (single != null && single.indexOf(":") > 0) {
                String[] s_a = single.split(":");
                Integer dataId = Integer.parseInt(s_a[0]); //Prize的id
                Integer amount = Integer.parseInt(s_a[1]); //对应数量
                Prize prize = prizeService.findByDataId(dataId);
                String prizeType = prize.getType();
                if ("3".equals(prizeType)) {
                    Float price = buffetOrderDetailService.findPrice(orderNo, "88888");
                    res += price * amount;
                } else if ("4".equals(prizeType)) {
                    Float price = buffetOrderDetailService.findPrice(orderNo, "99999");
                    res += price * amount;
                } else if ("2".equals(prizeType)) {
                    res += ((1.0f * prize.getWorth() * amount) / 100);
                }
            }
        }
        return res;
    }

    /** 生成卡券抵价金额 */
    private Float buildCardDiscountMoney(String orderNo, String reserve) {
        Float res = 0f;
        String isDinner = reserve.split("-")[0]; //1-晚餐；0-午餐
        String[] array = reserve.split("-")[1].split("_");
        for (String single : array) {
            Integer cardNo = Integer.parseInt(single);
            Card card = cardService.findByNo(cardNo);
            if(card!=null && "0".equals(card.getStatus())) {
                if("1".equals(card.getType())) {res += 10;}
                else if("2".equals(card.getType())) {res += 48;}
                else if("3".equals(card.getType())) {
                    if("1".equals(isDinner)) {res += 58;}
                    else {res += 48;}
                }

                cardWriteOffTools.writeOff(card, orderNo);
            }
        }
        return res;
    }

    //退票
    @PostMapping(value = "retreatOrders")
    public
    @ResponseBody
    ResDto retreatOrders(String no, String reason, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-10", "无权操作");
        }
        Rules rules = rulesService.loadOne(); //先获取规则
        BuffetOrder orders = buffetOrderService.findByNo(no);
        if (orders == null) {
            return new ResDto("-1", "订单不存在"); //订单不存在
        } else {
            if (orders.getEndTime() != null) {
                return new ResDto("-2", "订单已完结，不可退票");
            } else if (!"1".equalsIgnoreCase(orders.getType()) && !"4".equalsIgnoreCase(orders.getType())) {
                return new ResDto("-3", "此订单类型不可退票");
            } else if (!OrdersOperateTools.canRetreat(rules.getRefundMin(), orders.getEntryLong())) {
                return new ResDto("-4", "已超过退票时间");
            } else if (!"2".equalsIgnoreCase(orders.getStatus())) {
                return new ResDto("-5", "只能在就餐状态才可退票");
            } else {
                orders.setStatus("-1");
                orders.setRetreatName(w.getName());
                orders.setRetreatReason(reason);
                orders.setEndLong(System.currentTimeMillis());
                orders.setEndTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
                orders.setRetreatTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
                buffetOrderService.save(orders);

                //TODO 需要将结果发送至服务器
                sendBuffetOrder2Server(orders);
                return new ResDto("0", "退票完成");
            }
        }
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

    @PostMapping(value = "cancelOrders")
    public
    @ResponseBody
    ResDto cancelOrders(String no, String reason, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-10", "无权操作");
        }
        BuffetOrder orders = buffetOrderService.findByNo(no);
        if (orders == null) {
            return new ResDto("-1", "订单不存在"); //订单不存在
        } else { //在就餐前都可取消
            if (!"0".equalsIgnoreCase(orders.getStatus()) && !"6".equalsIgnoreCase(orders.getStatus())) {
                return new ResDto("-2", "当前状态不允许取消");
            } else if (!"4".equalsIgnoreCase(orders.getType())) {
                return new ResDto("-3", "此类型的订单不允许取消");
            } else {
                orders.setStatus("-2");
                orders.setRetreatName(w.getName());
                orders.setRetreatReason(reason);
                orders.setEndLong(System.currentTimeMillis());
                orders.setEndTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
                orders.setRetreatTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));
                buffetOrderService.save(orders);

                //TODO 需要将结果发送至服务器
                sendBuffetOrder2Server(orders);
                return new ResDto("0", "取消完成");
            }
        }
    }

    @GetMapping(value = "receiveMoney")
    public String receiveMoney(Model model, String no, HttpServletRequest request) {
        if (no != null && !"".equalsIgnoreCase(no)) {
            model.addAttribute("order", buffetOrderService.findByNo(no));
        }
        return "web/newOrders/receiveMoney";
    }

    @PostMapping(value = "receiveMoney")
    public
    @ResponseBody
    ResDto receiveMoney(String no, String payType, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-1", "未检测到收银员");
        }
        BuffetOrder orders = buffetOrderService.findByNo(no);
        if (orders == null) {
            return new ResDto("-2", "订单不存在");
        }
        if (!"1".equalsIgnoreCase(orders.getStatus())) { //只有在配送状态才可收款
            return new ResDto("-3", "只有在就餐状态才可退压金");
        }
        orders.setStatus("2"); //已完成
        orders.setPayType(payType);
        orders.setEndLong(System.currentTimeMillis());
        orders.setEndTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));

        buffetOrderService.save(orders);

        sendBuffetOrder2Server(orders);
        return new ResDto("0", "操作完成！");
    }

    @GetMapping(value = "returnBond")
    public String returnBond(Model model, String no, HttpServletRequest request) {
        if (no != null && !"".equalsIgnoreCase(no)) {
            model.addAttribute("order", buffetOrderService.findByNo(no));
        }
        return "web/newOrders/returnBond";
    }

    /**
     * 提交退还压金
     *
     * @param no      订单编号
     * @param money   扣除压金金额，一般为0
     * @param request
     * @return
     */
    @PostMapping(value = "returnBond")
    public
    @ResponseBody
    ResDto returnBond(String no, Float money, HttpServletRequest request) {
        Worker w = workerCookieTools.getWorker(request);
        if (w == null) {
            return new ResDto("-1", "未检测到收银员");
        }
        BuffetOrder orders = buffetOrderService.findByNo(no);
        if (orders == null) {
            return new ResDto("-2", "订单不存在");
        }
        if (!"2".equalsIgnoreCase(orders.getStatus()) && !"3".equalsIgnoreCase(orders.getStatus())) { //只有在就餐状态才可退压金
            return new ResDto("-3", "只有在就餐状态才可退压金");
        }
        if (money > 0) { //有扣压金
            orders.setStatus("5");
        } else {
            orders.setStatus("4");
        }
        orders.setBackBond(orders.getSurplusBond() - money); //已退还的押金
        orders.setSurplusBond(money); //扣下来的压金
        orders.setEndLong(System.currentTimeMillis());
        orders.setEndTime(NormalTools.curDate("yyyy-MM-dd HH:mm:ss"));

        buffetOrderService.save(orders);

        sendBuffetOrder2Server(orders);
        return new ResDto("0", "操作完成！");
    }

    /**
     * 修改支付方式
     */
    @PostMapping(value = "updatePayType")
    public
    @ResponseBody
    ResDto updatePayType(String orderNo, String field, String payType) {
        BuffetOrder order = buffetOrderService.findByNo(orderNo);
        if (order != null && "2".equalsIgnoreCase(order.getStatus())) {
            if ("payType".equalsIgnoreCase(field)) {
                order.setPayType(payType);
            } else if ("bondPayType".equalsIgnoreCase(field)) {
                order.setBondPayType(payType);
            }
            buffetOrderService.save(order);
            sendBuffetOrder2Server(order);
            return new ResDto("1", "操作完成！");
        } else {
            return new ResDto("0", "只有就餐中的订单才可修改支付方式");
        }
    }

    public void sendBuffetOrder2Server(BuffetOrder order) {
        String content = UploadJsonTools.buildDataJson(UploadJsonTools.buildBuffetOrder(order));
        uploadFileTools.setChangeContext(content, true);
    }

    public void sendBuffetOrderDetail2Server(String no) {
        List<BuffetOrderDetail> list = buffetOrderDetailService.listByOrderNo(no);
        for (BuffetOrderDetail detail : list) {
            String content = UploadJsonTools.buildDataJson(UploadJsonTools.buildBuffetOrderDetail(detail));
            uploadFileTools.setChangeContext(content, true);
        }
    }
}

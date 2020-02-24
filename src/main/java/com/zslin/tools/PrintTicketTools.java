package com.zslin.tools;

import com.zslin.basic.tools.NormalTools;
import com.zslin.model.BuffetOrder;
import com.zslin.model.BuffetOrderDetail;
import com.zslin.model.Company;
import com.zslin.service.IBuffetOrderDetailService;
import com.zslin.service.ICompanyService;
import com.zslin.service.IOrderNoService;
import com.zslin.web.dto.DetailDto;
import com.zslin.web.dto.MyTimeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by 钟述林 393156105@qq.com on 2017/4/12 16:01.
 * 下单后打印票据工具类
 */
@Component
public class PrintTicketTools {

    @Autowired
    private IBuffetOrderDetailService buffetOrderDetailService;

    @Autowired
    private WordTemplateTools wordTemplateTools;

    @Autowired
    private ICompanyService companyService;

    @Autowired
    private IOrderNoService orderNoService;

    /**
     * 打印自助券票据
     * @param order 订单对象
     */
    public void printBuffetOrder(BuffetOrder order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Company com = companyService.loadOne();
                List<BuffetOrderDetail> detailList = buffetOrderDetailService.listByOrderNo(order.getNo());
                printTicket(order, detailList, com.getName(), com.getPhone(), com.getAddress());
                String level = getLevel(detailList);
                printBond(order, buildDetailDto(detailList), com.getName(), com.getPhone(), com.getAddress(), com.getHaveTime(), level);
            }
        }).start();
    }

    private DetailDto buildDetailDto(List<BuffetOrderDetail> list) {
        Integer fullCount = 0, halfCount = 0, totalCount = 0;
        for(BuffetOrderDetail bod : list) {
            String no = bod.getCommodityNo();
            if("99999".equals(no) || "88888".equals(no)) {fullCount ++;}
            else if("66666".equals(no) || "77777".equals(no)) {halfCount++;}
            totalCount++;
        }
        return new DetailDto(halfCount, fullCount, totalCount);
    }

    //打印消费单
    public void printVoucher(Integer count, String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Company com = companyService.loadOne();
                printVoucher(com.getName(), com.getPhone(), com.getAddress(), count, name);
            }
        }).start();
    }

    //打印消费单
    public void printVoucher(BuffetOrder order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Company com = companyService.loadOne();
//                printVoucher(com.getName(), com.getPhone(), com.getAddress(), count, name);
                printVoucherNew(com.getName(), com.getPhone(), com.getAddress(), order);
            }
        }).start();
    }

    /**
     * 打印外卖票据
     * @param order 订单对象
     */
    public void printOutOrder(BuffetOrder order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                Long start = System.currentTimeMillis();
                Company com = companyService.loadOne();
                List<BuffetOrderDetail> detailList = buffetOrderDetailService.listByOrderNo(order.getNo());
                printOut(order, buildCommodity(detailList), order.getTotalMoney(), com.getName(), com.getPhone(), com.getAddress());
//                Long end = System.currentTimeMillis();
//                System.out.println("耗时============="+((end-start)/1000));
            }
        }).start();
    }

    private String buildCommodity(List<BuffetOrderDetail> detailList) {
        StringBuffer sb = new StringBuffer();
        for(BuffetOrderDetail detail : detailList) {
            sb.append(buildBlank(detail.getCommodityName()));
        }
        return sb.toString();
    }
    private String buildBlank(String commodityName) {
        StringBuffer sb = new StringBuffer(commodityName);
        //24-
        for(int i=0;i<23-calNameLen(commodityName); i++) {
            sb.append("·");
        }
        sb.append(" 1 ");
        return sb.toString();
    }

    private int calNameLen(String commodityName) {
        int res = 0;
        char [] array = commodityName.toCharArray();
        for(char c : array) {
            if(isChinese(c)) {
                res += 2;
            } else {res ++;}
        }
        return res;
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    private void printOut(BuffetOrder order, String commodity, Float money, String shopName, String phone, String address) {
        File f = wordTemplateTools.buildOutFile(shopName, commodity, money,
                order.getEntryTime()==null?order.getCreateTime():order.getEntryTime(), order.getNo(), phone, address);

        PrintTools.print(f.getAbsolutePath());

        f.delete();
    }

    //打印押金单
    private void printBond(BuffetOrder order, DetailDto dto, String shopName, String phone, String address, Integer haveTime, String level) {
        if(order.getSurplusBond()>0) { //当压金金额大于0时才需要出单
            StringBuffer sb = new StringBuffer();
            sb.append(dto.getFullCount()).append("+").append(dto.getHalfCount()).append("=").append(dto.getTotalCount()); //生成人数
            File f = wordTemplateTools.buildBondFile(shopName, sb.toString(), order.getSurplusBond(),
                    order.getEntryTime() == null ? order.getCreateTime() : order.getEntryTime(), order.getNo(), phone, address, order.getPayType(),
                    order.getBondPayType(), order.getType(), buildTime(haveTime), level);

            PrintTools.print(f.getAbsolutePath());

            f.delete();
        }
    }

    private void printVoucher(String shopName, String phone, String address, Integer count, String name) {
//        Integer [] array = new Integer[]{3,3,3,3,4,4,4,5,5,6,7,3,3};
//        Integer count = array[(int)(Math.random()*array.length)];
        if(count==null || count<=0) {
            Integer [] array = new Integer[]{3,3,3,3,4,4,4,5,5,6,7,3,3};
            count = array[(int)(Math.random()*array.length)];
        }
//        Integer count = (int)(Math.random()*10)+3;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 15);
        String curDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        //Integer curNo = orderNoService.findOrderNo(curDate, "1");
        //curNo = curNo==null?150:curNo;
        Integer curNo = (int)(150*Math.random());

        String code = curDate+"10"+curNo;
        Float price = (new Date()).before(cal.getTime())?49f:59f;
        File f = wordTemplateTools.buildVoucherFile(shopName, count+"", count*price, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), phone, address, code, name);

        PrintTools.print(f.getAbsolutePath());

        f.delete();
    }

    private void printVoucherNew(String shopName, String phone, String address, BuffetOrder order) {
//        Integer [] array = new Integer[]{3,3,3,3,4,4,4,5,5,6,7,3,3};
//        Integer count = array[(int)(Math.random()*array.length)];
//        Integer count = (int)(Math.random()*10)+3;
        String com = buildCommodityCount(order.getNo());
        System.out.println("============"+com);
        File f = wordTemplateTools.buildVoucherFile(shopName, buildCommodityCount(order.getNo()), order.getTotalMoney()+order.getDiscountMoney(), order.getCreateTime(), phone, address, order.getNo(), order.getCashierName());

        PrintTools.print(f.getAbsolutePath());

        f.delete();
    }

    private String buildCommodityCount(String orderNo) {
        List<BuffetOrderDetail> list = buffetOrderDetailService.listByOrderNo(orderNo);
        Integer countAll = 0, countHalf = 0;
        for(BuffetOrderDetail bod : list) {
            String no = bod.getCommodityNo();
            if("88888".equals(no) || "99999".equals(no) || "33333".equals(no)) {countAll ++;}
            else if("66666".equals(no) || "77777".equals(no) ||"22222".equals(no)){countHalf ++;}
        }
        return countAll+"+"+countHalf;
    }

    //生成用餐时间时长
    private String buildTime(Integer haveTime) {
        StringBuffer sb = new StringBuffer();
        haveTime = (haveTime==null||haveTime<=0)?120:haveTime;
        Integer hour = haveTime/60;
        Integer min = haveTime%60;
        if(hour>0) {
            sb.append(hour).append("小时");
        }
        if(min>0) {
            sb.append(min).append("分钟");
        }
        return sb.toString();
    }

    //生成就餐时间截止
    private String buildTime(String createTime, Integer haveTime) {
        try {
            haveTime = (haveTime==null||haveTime<=0)?120:haveTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date cDate = sdf.parse(createTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(cDate);
            cal.add(Calendar.MINUTE, haveTime);
            String [] array = sdf.format(cal.getTime()).split(" ");
            return array[1];
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 打印入场券
     * @param order 订单信息
     * @param detailList 订单详情列表
     * @param shopName 公司名称
     */
    private void printTicket(BuffetOrder order, List<BuffetOrderDetail> detailList, String shopName, String phone, String address) {
        String level = getLevel(detailList);
        for(BuffetOrderDetail detail : detailList) {
            if(detail.getPrice()>0) {
                File f = wordTemplateTools.buildTicketFile(shopName,
                        level + ("77777".equals(detail.getCommodityNo()) || "66666".equals(detail.getCommodityNo()) ? "儿童" : ""),
                        order.getEntryTime() == null ? order.getCreateTime() : order.getEntryTime(), order.getNo(), phone, address, order.getType());

                PrintTools.print(f.getAbsolutePath());
                f.delete();
            }
        }
    }

    private String getLevel(List<BuffetOrderDetail> detailList) {
        String level = "午餐";
        for(BuffetOrderDetail detail : detailList) {
            if(detail.getPrice()>0 && ("99999".equals(detail.getCommodityNo()) || "77777".equals(detail.getCommodityNo()))) {
                level = "晚餐";
                break;
            } else if(detail.getPrice()>0 && ("33333".equals(detail.getCommodityNo()))) {
                level = "【简餐】";
                break;
            }
        }
        return level;
    }
}

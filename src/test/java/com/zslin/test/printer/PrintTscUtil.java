package com.zslin.test.printer;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class PrintTscUtil {

        public interface TscLibDll extends Library {
                TscLibDll INSTANCE = (TscLibDll) Native.loadLibrary("TSCLIB", TscLibDll.class);
                int about();
                int openport(String pirnterName);
                int closeport();
                int sendcommand(String printerCommand);
                int setup(String width, String height, String speed, String density, String sensor, String vertical, String
                        offset);
                int downloadpcx(String filename, String image_name);
                int barcode(String x, String y, String type, String height, String readable, String rotation, String narrow,
                            String wide, String code);

                int printerfont(String x, String y, String fonttype, String rotation, String xmul, String ymul, String text);
                int clearbuffer();
                int printlabel(String set, String copy);
                int formfeed();
                int nobackfeed();
                int windowsfont(int x, int y, int fontheight, int rotation, int fontstyle, int fontunderline, String
                        szFaceName, String content);
        }

        public static void printBoxCode(String boxCode,String barCode,String name,String idCard,String age,String sex,String number) {
                try {
                        //加载驱动
                        System.loadLibrary("TSCLIB");
                        System.setProperty("jna.encoding", "GBK");
                        //Gprinter  GP-1324D
                        PrintTscUtil.TscLibDll.INSTANCE.openport("XP-58C (副本 1)");
                        PrintTscUtil.TscLibDll.INSTANCE.sendcommand("SIZE 60 mm,40 mm");//指定标签的宽度
                        PrintTscUtil.TscLibDll.INSTANCE.sendcommand("CLS");
                        PrintTscUtil.TscLibDll.INSTANCE.sendcommand("QRCODE 20,30,L,6,A,0,\""+boxCode+"\"");// 打印二维码
                        PrintTscUtil.TscLibDll.INSTANCE.printerfont ("170","30", "TSS24.BF2", "0", "1", "1", "姓  名: " + name);//姓  名
                        PrintTscUtil.TscLibDll.INSTANCE.printerfont ("170","65", "TSS24.BF2", "0", "1", "1", "年  龄: " + age+"岁");//年  龄
                        PrintTscUtil.TscLibDll.INSTANCE.printerfont ("170","100", "TSS24.BF2", "0", "1", "1", "性  别: " + sex);//性  别
                        PrintTscUtil.TscLibDll.INSTANCE.printerfont("170","135", "TSS24.BF2", "0", "1", "1", "流水号：" + number);
                        PrintTscUtil.TscLibDll.INSTANCE.barcode("20", "190", "128", "50", "0", "0", "2", "2", barCode);
                        PrintTscUtil.TscLibDll.INSTANCE.printerfont ("20","260", "TSS24.BF2", "0", "1", "1", "" + idCard);
                        PrintTscUtil.TscLibDll.INSTANCE.printlabel("1", "1");
                        PrintTscUtil.TscLibDll.INSTANCE.closeport();
                } catch (Exception e) {
                        e.printStackTrace();
                }

        }
}

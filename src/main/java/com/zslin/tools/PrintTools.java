package com.zslin.tools;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import java.io.File;
import java.io.IOException;

/**
 * Created by 钟述林 393156105@qq.com on 2017/4/8 23:42.
 */
public class PrintTools {

    /**
     * 打印测试页
     * @param printerName
     */
    public static void printTest(String printerName) {
        //E:\idea\2020\qwzw-client\src\main\resources
        String sep = File.separator;
        String path = System.getProperty("user.dir") + sep + "src" + sep + "main" + sep +
                "resources" + sep + "word-temp" + sep + "test-print.docx";
        print(path, printerName);
    }

    public static void print(String path) {
        print(path, null);
    }

    public static void print(String path, String printerName) {
        try {
            new PrintTools().printWord(path, printerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printWord(String path, String printerName) {
        ComThread.InitSTA();
//        ActiveXComponent word=new ActiveXComponent("Word.Application");
        ActiveXComponent word;
        try {
            word = new ActiveXComponent("KWPS.Application");
        } catch (Exception e) {
            word = new ActiveXComponent("Word.Application");
        }
        //设置打印机
        if(printerName!=null && !"".equals(printerName)) {
            word.setProperty("ActivePrinter", new Variant(printerName));
        }
        Dispatch doc=null;
        Dispatch.put(word, "Visible", new Variant(false));
        Dispatch docs=word.getProperty("Documents").toDispatch();
        doc=Dispatch.call(docs, "Open", path).toDispatch();
//        System.out.println("++++++++"+word.getProgramId()+"+==="+docs.getProgramId());
//        System.out.println("=========="+doc.getProgramId()+"===="+doc.m_pDispatch+"+++");
        try {
            Dispatch.call(doc, "PrintOut");//打印
            word.invoke("Quit", new Variant[] {});
            ComThread.Release();
            killWordTask();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("打印失败");
        }finally{
            try {
                if(doc!=null){
                    //Dispatch.call(doc, "Close",new Variant(0));
                }
                doc = null;
//                killWordTask();
                ComThread.Release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            //释放资源
            ComThread.Release();
        }

        ComThread.Release();
        ComThread.quitMainSTA();
    }

    private static void killWordTask() {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = "taskkill /F /IM WINWORD.EXE";
            rt.exec(command);
        } catch (IOException e) {
        }
    }
}

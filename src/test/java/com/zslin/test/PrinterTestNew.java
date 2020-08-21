package com.zslin.test;

import com.zslin.test.printer.PrintTscUtil;
import com.zslin.test.printer.Tools1;
import com.zslin.tools.PrintTemplateTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

/**
 * 打印机测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("zsl")
public class PrinterTestNew {

    @Autowired
    private PrintTemplateTools printTemplateTools;

    @Test
    public void test04() {
        String details = "这奇才阿斯蒂芬工\\n阿斯蒂芬琪苦";
        printTemplateTools.buildFoodFile("签王之王昭通店", "厨房",
                "4号桌", 6, "2020031001", "001", details);
    }

    @Test
    public void test01() {
        Tools1 tools1 = new Tools1();
        tools1.list();
    }

    @Test
    public void test02() {
        Tools1 tools1 = new Tools1();
        tools1.print("XP-58C (副本 1)", new File("D:/temp/qwzw-print.doc"));
    }

    @Test
    public void test03() {
        PrintTscUtil.printBoxCode("李四","340621198906154567","张三依依","340621********198x","60","男","201806190101");
        PrintTscUtil.printBoxCode("http://www.baidu.com","340621198906154567","张三依依","340621********198x","60","男","201806190101");
    }
}

package com.zslin.web.controller;

import com.zslin.basic.tools.ConfigTools;
import com.zslin.qwzw.model.PrintConfig;
import com.zslin.qwzw.service.IPrintConfigService;
import com.zslin.tools.PrintTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "web/printConfig")
public class WebPrintConfigController {

    @Autowired
    private IPrintConfigService printConfigService;

    @Autowired
    private ConfigTools configTools;

    @RequestMapping(value = "index")
    public String index(Model model, PrintConfig printConfig, HttpServletRequest request) {
        String method = request.getMethod();
        if("GET".equalsIgnoreCase(method)) { //如果是Get请求
            PrintConfig pc = printConfigService.loadOne();
            if(pc==null) {pc = new PrintConfig();}
            model.addAttribute("printConfig", pc);
        } else {
            PrintConfig pc = printConfigService.loadOne();
            if(pc==null) {pc=new PrintConfig();}
            pc.setPrintName1(printConfig.getPrintName1());
            pc.setPrintName2(printConfig.getPrintName2());
            pc.setPrintName3(printConfig.getPrintName3());
            printConfigService.save(pc);
        }
        return "web/printConfig/index";
    }

    @PostMapping(value = "testPrint")
    public @ResponseBody String testPrint(String name) {
        String path = configTools.getUploadPath("word-temp") + "test-print.docx";
        System.out.println(path);
        PrintTools.printTest(name, path);
        return "如果打印正常打印，则表示连接正常";
    }
}

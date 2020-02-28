package com.zslin.basic.controller;

import com.zslin.basic.annotations.AdminAuth;
import com.zslin.basic.tools.MyBeanUtils;
import com.zslin.model.Company;
import com.zslin.service.ICompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value="admin/company")
@AdminAuth(name="服务端配置", orderNum=10, psn="系统管理", pentity=0, porderNum=20)
public class CompanyController {

    @Autowired
    private ICompanyService companyService;

    @AdminAuth(name="服务端配置", orderNum=1, icon="fa fa-cog", type="1")
    @RequestMapping(value="index", method= RequestMethod.GET)
    public String index(Model model, HttpServletRequest request) {
        Company c = companyService.loadOne();
        if(c==null) {c = new Company();}
        model.addAttribute("company", c);
        return "admin/basic/company/index";
    }

    @RequestMapping(value="index", method=RequestMethod.POST)
    public String index(Model model, Company company, HttpServletRequest request) {

        Company c = companyService.loadOne();
        if(c==null) {
            companyService.save(company);
        } else {
            MyBeanUtils.copyProperties(company, c, new String[]{"id"});
            companyService.save(c);
        }

        request.getSession().setAttribute("company", company); //修改后需要修改一次Session中的值
        return "redirect:/admin/company/index";
    }
}

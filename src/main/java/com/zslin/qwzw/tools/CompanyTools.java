package com.zslin.qwzw.tools;

import com.zslin.model.Company;
import com.zslin.service.ICompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompanyTools {

    @Autowired
    private ICompanyService companyService;

    private static Company com = null;

    public Company getCompany() {
        if(com==null) {com = companyService.loadOne();}
        return com;
    }
}

package com.zslin.qwzw.model;

import javax.persistence.*;

/**
 * 打印机配置
 */
@Entity
@Table(name = "t_print_config")
public class PrintConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    /** 吧台打印机名称 */
    @Column(name = "print_name1")
    private String printName1;

    /** 厨房打印机名称 */
    @Column(name = "print_name2")
    private String printName2;

    /** 甜品店打印机名称 */
    @Column(name = "print_name3")
    private String printName3;

    public String getPrintName3() {
        return printName3;
    }

    public void setPrintName3(String printName3) {
        this.printName3 = printName3;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPrintName1() {
        return printName1;
    }

    public void setPrintName1(String printName1) {
        this.printName1 = printName1;
    }

    public String getPrintName2() {
        return printName2;
    }

    public void setPrintName2(String printName2) {
        this.printName2 = printName2;
    }
}

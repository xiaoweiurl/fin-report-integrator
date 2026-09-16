package com.cairui.finreport.master;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    private Long id;
    private String code;
    private String name;
    private String category;
    private String balanceSide;
    private String parentCode;
    private Integer levelNo = 1;
    private Boolean enabled = true;
}

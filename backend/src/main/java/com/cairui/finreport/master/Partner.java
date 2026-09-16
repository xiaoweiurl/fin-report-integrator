package com.cairui.finreport.master;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Partner {
    private Long id;
    private String code;
    private String name;
    private String partnerType;
    private Boolean enabled = true;
}

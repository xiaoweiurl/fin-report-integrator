package com.cairui.finreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@MapperScan(basePackages = "com.cairui.finreport", annotationClass = org.apache.ibatis.annotations.Mapper.class)
public class FinReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinReportApplication.class, args);
    }
}

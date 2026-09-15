package com.cairui.finreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class FinReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinReportApplication.class, args);
    }
}

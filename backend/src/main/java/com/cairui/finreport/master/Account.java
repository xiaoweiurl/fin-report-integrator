package com.cairui.finreport.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(name = "balance_side", nullable = false, length = 16)
    private String balanceSide;

    @Column(name = "parent_code", length = 32)
    private String parentCode;

    @Column(name = "level_no", nullable = false)
    private Integer levelNo = 1;

    @Column(nullable = false)
    private Boolean enabled = true;
}

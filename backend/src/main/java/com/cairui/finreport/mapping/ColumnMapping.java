package com.cairui.finreport.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "column_mappings")
public class ColumnMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_type", nullable = false, length = 40)
    private String importType;

    @Column(nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "mapping_json", nullable = false)
    private String mappingJson;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}

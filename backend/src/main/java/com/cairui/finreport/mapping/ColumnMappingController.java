package com.cairui.finreport.mapping;

import com.cairui.finreport.audit.AuditService;
import com.cairui.finreport.common.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mappings")
public class ColumnMappingController {
    private final ColumnMappingRepository repo;
    private final AuditService audit;

    public ColumnMappingController(ColumnMappingRepository repo, AuditService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    @GetMapping
    public List<ColumnMapping> list() {
        return repo.findAllByOrderByImportTypeAsc();
    }

    @PutMapping("/{id}")
    public ColumnMapping update(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        ColumnMapping m = repo.findById(id).orElseThrow(() -> ApiException.notFound("映射不存在"));
        if (body.containsKey("name")) {
            m.setName(body.get("name"));
        }
        if (body.containsKey("mappingJson")) {
            m.setMappingJson(body.get("mappingJson"));
        }
        ColumnMapping saved = repo.save(m);
        audit.log("MAPPING_UPDATE", "MAPPING", String.valueOf(id), m.getImportType(), auth.getName());
        return saved;
    }
}

package com.onair.hearit.admin.presentation.api;

import com.onair.hearit.admin.application.AdminElasticSearchMigrationService;
import com.onair.hearit.admin.dto.response.AdminElasticSearchMigrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/elasticsearch")
public class AdminElasticSearchMigrationController {

    private final AdminElasticSearchMigrationService elasticSearchMigrationService;

    @PostMapping("/migration")
    public ResponseEntity<AdminElasticSearchMigrationResponse> migrate() {
        AdminElasticSearchMigrationResponse response = elasticSearchMigrationService.migrate();
        return ResponseEntity.ok(response);
    }
}

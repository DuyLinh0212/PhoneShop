package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.admin.AdminCatalogDtos.ResourceData;
import com.example.webbanphone.dto.admin.AdminCatalogDtos.ResourceSummary;
import com.example.webbanphone.services.AdminCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/catalog")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    public AdminCatalogController(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<ResourceSummary>> getResources() {
        return ResponseEntity.ok(adminCatalogService.getResources());
    }

    @GetMapping("/{resource}")
    public ResponseEntity<ResourceData> getResource(@PathVariable String resource) {
        return ResponseEntity.ok(adminCatalogService.getResourceData(resource));
    }

    @PostMapping("/{resource}")
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String resource,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminCatalogService.create(resource, payload));
    }

    @PutMapping("/{resource}/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String resource,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(adminCatalogService.update(resource, id, payload));
    }

    @DeleteMapping("/{resource}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String resource, @PathVariable Integer id) {
        adminCatalogService.delete(resource, id);
        return ResponseEntity.noContent().build();
    }
}

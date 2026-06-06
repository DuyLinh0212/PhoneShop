package com.example.webbanphone.dto.admin;

import java.util.List;
import java.util.Map;

public final class AdminCatalogDtos {
    private AdminCatalogDtos() {
    }

    public record ResourceSummary(
            String key,
            String title,
            String description
    ) {
    }

    public record ColumnMeta(
            String key,
            String label,
            String type,
            boolean editable,
            boolean required
    ) {
    }

    public record ResourceData(
            String key,
            String title,
            String description,
            List<ColumnMeta> columns,
            List<Map<String, Object>> rows,
            Map<String, Object> stats
    ) {
    }
}

package com.example.webbanphone.services;

import com.example.webbanphone.dto.admin.AdminCatalogDtos.ColumnMeta;
import com.example.webbanphone.dto.admin.AdminCatalogDtos.ResourceData;
import com.example.webbanphone.dto.admin.AdminCatalogDtos.ResourceSummary;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminCatalogService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, ResourceDefinition> resources;

    public AdminCatalogService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.passwordEncoder = passwordEncoder;
        this.resources = buildResources();
    }

    public List<ResourceSummary> getResources() {
        return resources.values().stream()
                .map(resource -> new ResourceSummary(resource.key(), resource.title(), resource.description()))
                .toList();
    }

    public ResourceData getResourceData(String key) {
        ResourceDefinition resource = resource(key);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(resource.selectSql());
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", rows.size());
        if (resource.activeColumn() != null) {
            long active = rows.stream().filter(row -> truthy(row.get(resource.activeColumn()))).count();
            stats.put("active", active);
            stats.put("inactive", rows.size() - active);
        }
        return new ResourceData(
                resource.key(),
                resource.title(),
                resource.description(),
                resource.columns(),
                rows,
                stats
        );
    }

    @Transactional
    public Map<String, Object> create(String key, Map<String, Object> payload) {
        ResourceDefinition resource = resource(key);
        if (!resource.allowCreate()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource does not support create");
        }

        Map<String, Object> values = writableValues(resource, payload, false);
        if ("users".equals(resource.key()) || "customers".equals(resource.key())) {
            String password = stringValue(payload.getOrDefault("password", "123456"));
            values.put("password_hash", passwordEncoder.encode(password == null ? "123456" : password));
        }
        if ("customers".equals(resource.key())) {
            values.put("role_id", roleId("user"));
        }
        if (resource.activeColumn() != null && !values.containsKey(resource.activeColumn())) {
            values.put(resource.activeColumn(), true);
        }

        validateRequired(resource, values);
        String columns = values.keySet().stream().map(this::quote).collect(Collectors.joining(", "));
        String params = values.keySet().stream().map(column -> ":" + column).collect(Collectors.joining(", "));
        namedJdbcTemplate.update(
                "INSERT INTO " + resource.table() + " (" + columns + ") VALUES (" + params + ")",
                new MapSqlParameterSource(values)
        );
        writeLog("create", resource.key(), values.toString());
        return latestRow(resource);
    }

    @Transactional
    public Map<String, Object> update(String key, Integer id, Map<String, Object> payload) {
        ResourceDefinition resource = resource(key);
        Map<String, Object> values = writableValues(resource, payload, true);
        if (values.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No editable fields were provided");
        }

        MapSqlParameterSource params = new MapSqlParameterSource(values).addValue("id", id);
        String assignments = values.keySet().stream()
                .map(column -> quote(column) + " = :" + column)
                .collect(Collectors.joining(", "));
        int updated = namedJdbcTemplate.update(
                "UPDATE " + resource.table() + " SET " + assignments + " WHERE id = :id",
                params
        );
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        }
        writeLog("update", resource.key() + "#" + id, values.toString());
        return rowById(resource, id);
    }

    @Transactional
    public void delete(String key, Integer id) {
        ResourceDefinition resource = resource(key);
        int updated;
        if (resource.activeColumn() != null) {
            updated = jdbcTemplate.update(
                    "UPDATE " + resource.table() + " SET " + quote(resource.activeColumn()) + " = 0 WHERE id = ?",
                    id
            );
        } else {
            updated = jdbcTemplate.update("DELETE FROM " + resource.table() + " WHERE id = ?", id);
        }
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        }
        writeLog("delete", resource.key() + "#" + id, resource.activeColumn() == null ? "deleted" : "deactivated");
    }

    private ResourceDefinition resource(String key) {
        ResourceDefinition resource = resources.get(key == null ? "" : key.trim().toLowerCase(Locale.ROOT));
        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin resource not found");
        }
        return resource;
    }

    private Map<String, Object> writableValues(ResourceDefinition resource, Map<String, Object> payload, boolean partial) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (payload == null) {
            return values;
        }
        Set<String> editableColumns = resource.columns().stream()
                .filter(ColumnMeta::editable)
                .map(ColumnMeta::key)
                .collect(Collectors.toSet());
        for (ColumnMeta column : resource.columns()) {
            if (!editableColumns.contains(column.key()) || !payload.containsKey(column.key())) {
                continue;
            }
            Object value = convertValue(payload.get(column.key()), column.type());
            if (!partial || value != null || payload.containsKey(column.key())) {
                values.put(column.key(), value);
            }
        }
        return values;
    }

    private void validateRequired(ResourceDefinition resource, Map<String, Object> values) {
        for (ColumnMeta column : resource.columns()) {
            if (column.required() && column.editable() && blank(values.get(column.key()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, column.label() + " is required");
            }
        }
    }

    private Map<String, Object> latestRow(ResourceDefinition resource) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT TOP 1 * FROM " + resource.table() + " ORDER BY id DESC"
        );
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private Map<String, Object> rowById(ResourceDefinition resource, Integer id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + resource.table() + " WHERE id = ?",
                id
        );
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        }
        return rows.get(0);
    }

    private Integer roleId(String role) {
        return jdbcTemplate.queryForObject(
                "SELECT TOP 1 id FROM roles WHERE LOWER(name) = ?",
                Integer.class,
                role.toLowerCase(Locale.ROOT)
        );
    }

    private void writeLog(String action, String target, String detail) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO activity_logs (actor, action, target, detail) VALUES (?, ?, ?, ?)",
                    "admin-api",
                    action,
                    target,
                    detail
            );
        } catch (RuntimeException ignored) {
            // Logging must never break the business action.
        }
    }

    private Object convertValue(Object value, String type) {
        if (value == null) {
            return null;
        }
        if ("boolean".equals(type)) {
            return truthy(value);
        }
        if ("number".equals(type)) {
            if (value instanceof Number number) {
                return number;
            }
            String text = stringValue(value);
            return text == null || text.isBlank() ? null : Double.parseDouble(text);
        }
        if ("datetime".equals(type)) {
            if (value instanceof Timestamp || value instanceof LocalDateTime) {
                return value;
            }
            String text = stringValue(value);
            return text == null || text.isBlank() ? null : text;
        }
        return stringValue(value);
    }

    private boolean truthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = stringValue(value);
        return text != null && Set.of("true", "1", "yes", "active").contains(text.toLowerCase(Locale.ROOT));
    }

    private boolean blank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String quote(String column) {
        return "[" + column + "]";
    }

    private Map<String, ResourceDefinition> buildResources() {
        List<ResourceDefinition> definitions = new ArrayList<>();
        definitions.add(resource("categories", "Danh muc", "Nhom san pham hien tren website", "categories", "is_active", true,
                col("id", "ID", "number", false, false),
                col("parent_id", "Danh muc cha", "number", true, false),
                col("name", "Ten danh muc", "text", true, true),
                col("slug", "Slug", "text", true, true),
                col("description", "Mo ta", "textarea", true, false),
                col("is_active", "Dang hien thi", "boolean", true, false)));
        definitions.add(resource("brands", "Thuong hieu", "Quan ly hang san xuat", "brands", "is_active", true,
                col("id", "ID", "number", false, false),
                col("name", "Ten thuong hieu", "text", true, true),
                col("logo", "Logo", "text", true, false),
                col("is_active", "Dang hien thi", "boolean", true, false)));
        definitions.add(resource("variants", "Bien the", "Mau sac, RAM, ROM, gia va ton kho", "product_variants", "is_active", true,
                col("id", "ID", "number", false, false),
                col("product_id", "Ma san pham", "number", true, true),
                col("color", "Mau", "text", true, false),
                col("storage", "Dung luong", "text", true, false),
                col("ram", "RAM", "text", true, false),
                col("price", "Gia", "number", true, true),
                col("sale_price", "Gia giam", "number", true, false),
                col("stock", "Ton kho", "number", true, false),
                col("sku", "SKU", "text", true, false),
                col("is_active", "Dang ban", "boolean", true, false)));
        definitions.add(resource("specs", "Thong so ky thuat", "Cau hinh chi tiet cua san pham", "product_specs", null, true,
                col("id", "ID", "number", false, false),
                col("product_id", "Ma san pham", "number", true, true),
                col("spec_key", "Thong so", "text", true, true),
                col("spec_value", "Gia tri", "text", true, true),
                col("sort_order", "Sap xep", "number", true, false)));
        definitions.add(resource("images", "Hinh anh", "Anh san pham va thu tu hien thi", "product_images", null, true,
                col("id", "ID", "number", false, false),
                col("product_id", "Ma san pham", "number", true, true),
                col("variant_id", "Ma bien the", "number", true, false),
                col("image_url", "URL anh", "text", true, true),
                col("alt_text", "Mo ta anh", "text", true, false),
                col("sort_order", "Sap xep", "number", true, false)));
        definitions.add(resource("customers", "Khach hang", "Tai khoan khach su dung website", "users", "is_active", true,
                col("id", "ID", "number", false, false),
                col("role_id", "Ma vai tro", "number", false, false),
                col("full_name", "Ho ten", "text", true, true),
                col("email", "Email", "text", true, true),
                col("phone", "Dien thoai", "text", true, false),
                col("avatar", "Avatar", "text", true, false),
                col("is_active", "Dang hoat dong", "boolean", true, false)));
        definitions.add(resource("promotions", "Khuyen mai", "Ma giam gia va chien dich uu dai", "promotions", "is_active", true,
                col("id", "ID", "number", false, false),
                col("code", "Ma", "text", true, true),
                col("name", "Ten khuyen mai", "text", true, true),
                col("discount_percent", "Phan tram giam", "number", true, true),
                col("start_at", "Bat dau", "datetime", true, false),
                col("end_at", "Ket thuc", "datetime", true, false),
                col("is_active", "Dang chay", "boolean", true, false)));
        definitions.add(resource("posts", "Bai viet", "Noi dung tin tuc va tu van", "posts", "is_published", true,
                col("id", "ID", "number", false, false),
                col("author_id", "Tac gia", "number", true, false),
                col("title", "Tieu de", "text", true, true),
                col("slug", "Slug", "text", true, true),
                col("thumbnail", "Anh dai dien", "text", true, false),
                col("summary", "Tom tat", "textarea", true, false),
                col("content", "Noi dung", "textarea", true, false),
                col("status", "Trang thai", "text", true, false),
                col("is_published", "Da xuat ban", "boolean", true, false),
                col("published_at", "Ngay xuat ban", "datetime", true, false),
                col("created_at", "Ngay tao", "datetime", false, false),
                col("updated_at", "Ngay cap nhat", "datetime", false, false)));
        definitions.add(resource("banners", "Banner", "Banner hien thi tren trang nguoi dung", "banners", "is_active", true,
                col("id", "ID", "number", false, false),
                col("title", "Tieu de", "text", true, true),
                col("image_url", "URL anh", "text", true, true),
                col("link_url", "Lien ket", "text", true, false),
                col("position", "Vi tri", "text", true, false),
                col("sort_order", "Sap xep", "number", true, false),
                col("is_active", "Dang hien thi", "boolean", true, false)));
        definitions.add(resource("reviews", "Binh luan", "Danh gia san pham cua khach", "reviews", "is_approved", false,
                col("id", "ID", "number", false, false),
                col("product_id", "Ma san pham", "number", false, false),
                col("user_id", "Ma khach", "number", false, false),
                col("rating", "Sao", "number", false, false),
                col("title", "Tieu de", "text", true, false),
                col("content", "Noi dung", "textarea", true, false),
                col("is_approved", "Da duyet", "boolean", true, false),
                col("created_at", "Ngay tao", "datetime", false, false)));
        definitions.add(resource("settings", "Cau hinh", "Thiet lap he thong cua cua hang", "store_settings", null, true,
                col("id", "ID", "number", false, false),
                col("setting_key", "Khoa", "text", true, true),
                col("setting_value", "Gia tri", "text", true, false),
                col("note", "Ghi chu", "text", true, false)));
        definitions.add(resource("users", "Nguoi dung", "Tai khoan admin va user", "users", "is_active", true,
                col("id", "ID", "number", false, false),
                col("role_id", "Ma vai tro", "number", true, true),
                col("full_name", "Ho ten", "text", true, true),
                col("email", "Email", "text", true, true),
                col("phone", "Dien thoai", "text", true, false),
                col("avatar", "Avatar", "text", true, false),
                col("is_active", "Dang hoat dong", "boolean", true, false)));
        definitions.add(resource("roles", "Vai tro va phan quyen", "Nhom quyen truy cap he thong", "roles", null, true,
                col("id", "ID", "number", false, false),
                col("name", "Ten vai tro", "text", true, true),
                col("description", "Mo ta", "text", true, false)));
        definitions.add(resource("logs", "Nhat ky hoat dong", "Cac thao tac quan tri gan day", "activity_logs", null, false,
                col("id", "ID", "number", false, false),
                col("actor", "Nguoi thuc hien", "text", false, false),
                col("action", "Thao tac", "text", false, false),
                col("target", "Doi tuong", "text", false, false),
                col("detail", "Chi tiet", "text", false, false),
                col("created_at", "Thoi gian", "datetime", false, false)));

        return definitions.stream().collect(Collectors.toMap(ResourceDefinition::key, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private ResourceDefinition resource(
            String key,
            String title,
            String description,
            String table,
            String activeColumn,
            boolean allowCreate,
            ColumnMeta... columns
    ) {
        return new ResourceDefinition(key, title, description, table, activeColumn, allowCreate, List.of(columns));
    }

    private ColumnMeta col(String key, String label, String type, boolean editable, boolean required) {
        return new ColumnMeta(key, label, type, editable, required);
    }

    private record ResourceDefinition(
            String key,
            String title,
            String description,
            String table,
            String activeColumn,
            boolean allowCreate,
            List<ColumnMeta> columns
    ) {
        String selectSql() {
            return "SELECT * FROM " + table + " ORDER BY id DESC";
        }
    }
}

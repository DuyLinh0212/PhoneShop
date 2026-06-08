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
    private final AdminActivityLogService activityLogService;
    private final Map<String, ResourceDefinition> resources;

    public AdminCatalogService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            AdminActivityLogService activityLogService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chức năng này không hỗ trợ tạo mới");
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
        writeLog("Tạo mới", resource.key(), values.toString());
        return latestRow(resource);
    }

    @Transactional
    public Map<String, Object> update(String key, Integer id, Map<String, Object> payload) {
        ResourceDefinition resource = resource(key);
        Map<String, Object> values = writableValues(resource, payload, true);
        if (values.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chưa có trường dữ liệu nào được phép chỉnh sửa");
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi");
        }
        writeLog("Cập nhật", resource.key() + "#" + id, values.toString());
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi");
        }
        writeLog("Xóa", resource.key() + "#" + id, resource.activeColumn() == null ? "Đã xóa" : "Đã tắt");
    }

    private ResourceDefinition resource(String key) {
        ResourceDefinition resource = resources.get(key == null ? "" : key.trim().toLowerCase(Locale.ROOT));
        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu quản trị");
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, column.label() + " là bắt buộc");
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi");
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
        activityLogService.writeAsync(action, target, detail);
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
        definitions.add(resource("categories", "Danh mục", "Nhóm sản phẩm hiển thị trên website", "categories", "is_active", true,
                col("id", "ID", "number", false, false),
                col("parent_id", "Danh mục cha", "number", true, false),
                col("name", "Tên danh mục", "text", true, true),
                col("slug", "Slug", "text", true, true),
                col("description", "Mô tả", "textarea", true, false),
                col("is_active", "Đang hiển thị", "boolean", true, false)));
        definitions.add(resource("brands", "Thương hiệu", "Quản lý hãng sản xuất", "brands", "is_active", true,
                col("id", "ID", "number", false, false),
                col("name", "Tên thương hiệu", "text", true, true),
                col("logo", "Logo", "text", true, false),
                col("is_active", "Đang hiển thị", "boolean", true, false)));
        definitions.add(resource("variants", "Biến thể", "Màu sắc, RAM, ROM, giá và tồn kho", "product_variants", "is_active", true,
                col("id", "ID", "number", false, false),
                col("product_id", "Mã sản phẩm", "number", true, true),
                col("color", "Màu", "text", true, false),
                col("storage", "Dung lượng", "text", true, false),
                col("ram", "RAM", "text", true, false),
                col("price", "Giá bán", "number", true, true),
                col("discount_percent", "Khuyến mãi (%)", "number", true, false),
                col("sale_price", "Giá sau giảm", "number", false, false),
                col("cost_price", "Giá nhập", "number", true, false),
                col("stock", "Tồn kho", "number", true, false),
                col("sku", "SKU", "text", true, false),
                col("is_active", "Đang bán", "boolean", true, false)));
        definitions.add(resource("specs", "Thông số kỹ thuật", "Cấu hình chi tiết của sản phẩm", "product_specs", null, true,
                col("id", "ID", "number", false, false),
                col("product_id", "Mã sản phẩm", "number", true, true),
                col("spec_key", "Thông số", "text", true, true),
                col("spec_value", "Giá trị", "text", true, true),
                col("sort_order", "Sắp xếp", "number", true, false)));
        definitions.add(resource("images", "Hình ảnh", "Ảnh sản phẩm và thứ tự hiển thị", "product_images", null, true,
                col("id", "ID", "number", false, false),
                col("product_id", "Mã sản phẩm", "number", true, true),
                col("variant_id", "Mã biến thể", "number", true, false),
                col("image_url", "URL ảnh", "text", true, true),
                col("alt_text", "Mô tả ảnh", "text", true, false),
                col("sort_order", "Sắp xếp", "number", true, false)));
        definitions.add(resource("customers", "Khách hàng", "Tài khoản khách sử dụng website", "users", "is_active", true,
                col("id", "ID", "number", false, false),
                col("role_id", "Mã vai trò", "number", false, false),
                col("full_name", "Họ tên", "text", true, true),
                col("email", "Email", "text", true, true),
                col("phone", "Điện thoại", "text", true, false),
                col("avatar", "Avatar", "text", true, false),
                col("is_active", "Đang hoạt động", "boolean", true, false)));
        definitions.add(resource("promotions", "Khuyến mãi", "Mã giảm giá và chiến dịch ưu đãi", "promotions", "is_active", true,
                col("id", "ID", "number", false, false),
                col("code", "Mã", "text", true, true),
                col("name", "Tên khuyến mãi", "text", true, true),
                col("discount_percent", "Phần trăm giảm", "number", true, true),
                col("start_at", "Bắt đầu", "datetime", true, false),
                col("end_at", "Kết thúc", "datetime", true, false),
                col("is_active", "Đang chạy", "boolean", true, false)));
        definitions.add(resource("posts", "Bài viết", "Nội dung tin tức và tư vấn", "posts", "is_published", true,
                col("id", "ID", "number", false, false),
                col("author_id", "Tác giả", "number", true, false),
                col("title", "Tiêu đề", "text", true, true),
                col("slug", "Slug", "text", true, true),
                col("thumbnail", "Ảnh đại diện", "text", true, false),
                col("summary", "Tóm tắt", "textarea", true, false),
                col("content", "Nội dung", "textarea", true, false),
                col("status", "Trạng thái", "text", true, false),
                col("is_published", "Đã xuất bản", "boolean", true, false),
                col("published_at", "Ngày xuất bản", "datetime", true, false),
                col("created_at", "Ngày tạo", "datetime", false, false),
                col("updated_at", "Ngày cập nhật", "datetime", false, false)));
        definitions.add(resource("banners", "Banner", "Banner hiển thị trên trang người dùng", "banners", "is_active", true,
                col("id", "ID", "number", false, false),
                col("title", "Tiêu đề", "text", true, true),
                col("image_url", "URL ảnh", "text", true, true),
                col("link_url", "Liên kết", "text", true, false),
                col("position", "Vị trí", "text", true, false),
                col("sort_order", "Sắp xếp", "number", true, false),
                col("is_active", "Đang hiển thị", "boolean", true, false)));
        definitions.add(resource("reviews", "Bình luận", "Đánh giá sản phẩm của khách", "reviews", "is_approved", false,
                col("id", "ID", "number", false, false),
                col("product_id", "Mã sản phẩm", "number", false, false),
                col("user_id", "Mã khách", "number", false, false),
                col("rating", "Sao", "number", false, false),
                col("title", "Tiêu đề", "text", true, false),
                col("content", "Nội dung", "textarea", true, false),
                col("is_approved", "Đã duyệt", "boolean", true, false),
                col("created_at", "Ngày tạo", "datetime", false, false)));
        definitions.add(resource("settings", "Cấu hình", "Thiết lập hệ thống của cửa hàng", "store_settings", null, true,
                col("id", "ID", "number", false, false),
                col("setting_key", "Khóa", "text", true, true),
                col("setting_value", "Giá trị", "text", true, false),
                col("note", "Ghi chú", "text", true, false)));
        definitions.add(resource("users", "Người dùng", "Tài khoản quản trị và người dùng", "users", "is_active", true,
                col("id", "ID", "number", false, false),
                col("role_id", "Mã vai trò", "number", true, true),
                col("full_name", "Họ tên", "text", true, true),
                col("email", "Email", "text", true, true),
                col("phone", "Điện thoại", "text", true, false),
                col("avatar", "Avatar", "text", true, false),
                col("is_active", "Đang hoạt động", "boolean", true, false)));
        definitions.add(resource("roles", "Vai trò và phân quyền", "Nhóm quyền truy cập hệ thống", "roles", null, true,
                col("id", "ID", "number", false, false),
                col("name", "Tên vai trò", "text", true, true),
                col("description", "Mô tả", "text", true, false)));
        definitions.add(resource("logs", "Nhật ký hoạt động", "Các thao tác quản trị gần đây", "activity_logs", null, false,
                col("id", "ID", "number", false, false),
                col("actor", "Người thực hiện", "text", false, false),
                col("action", "Thao tác", "text", false, false),
                col("target", "Đối tượng", "text", false, false),
                col("detail", "Chi tiết", "text", false, false),
                col("created_at", "Thời gian", "datetime", false, false)));

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

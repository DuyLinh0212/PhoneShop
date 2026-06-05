package com.example.webbanphone.services;

import com.example.webbanphone.dto.admin.AdminStatisticsResponse;
import com.example.webbanphone.entities.Category;
import com.example.webbanphone.entities.Order;
import com.example.webbanphone.entities.OrderItem;
import com.example.webbanphone.entities.Product;
import com.example.webbanphone.entities.ProductVariant;
import com.example.webbanphone.entities.Review;
import com.example.webbanphone.repositories.CategoryRepository;
import com.example.webbanphone.repositories.OrderItemRepository;
import com.example.webbanphone.repositories.OrderRepository;
import com.example.webbanphone.repositories.ProductRepository;
import com.example.webbanphone.repositories.ProductVariantRepository;
import com.example.webbanphone.repositories.ReviewRepository;
import com.example.webbanphone.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminStatisticsService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public AdminStatisticsService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            ReviewRepository reviewRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public AdminStatisticsResponse getStatistics() {
        List<Order> orders = orderRepository.findAllByOrderByIdDesc();
        List<OrderItem> orderItems = orderItemRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<ProductVariant> variants = variantRepository.findAll();
        List<Review> approvedReviews = reviewRepository.findAll().stream()
                .filter(review -> Boolean.TRUE.equals(review.getIsApproved()))
                .toList();

        BigDecimal totalRevenue = orders.stream()
                .filter(order -> !isCancelled(order.getStatus()))
                .map(order -> value(order.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingOrders = orders.stream().filter(order -> isPending(order.getStatus())).count();
        long outOfStockProducts = products.stream()
                .filter(product -> stockForProduct(product.getId(), variants) <= 0)
                .count();
        double averageRating = approvedReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        return new AdminStatisticsResponse(
                totalRevenue,
                orders.size(),
                userRepository.count(),
                products.size(),
                pendingOrders,
                outOfStockProducts,
                approvedReviews.size(),
                Math.round(averageRating * 10.0) / 10.0,
                buildCategoryRevenue(orderItems, variants, products, totalRevenue),
                buildRecentOrders(orders),
                buildBestSellers(orderItems)
        );
    }

    private List<AdminStatisticsResponse.CategoryRevenue> buildCategoryRevenue(
            List<OrderItem> orderItems,
            List<ProductVariant> variants,
            List<Product> products,
            BigDecimal totalRevenue
    ) {
        Map<Integer, ProductVariant> variantById = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity(), (left, right) -> left));
        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (left, right) -> left));
        Map<Integer, Category> categoryById = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), (left, right) -> left));
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();

        for (OrderItem item : orderItems) {
            ProductVariant variant = variantById.get(item.getVariantId());
            Product product = variant == null ? null : productById.get(variant.getProductId());
            Category category = product == null ? null : categoryById.get(product.getCategoryId());
            String name = category == null ? "Khac" : category.getName();
            revenueByCategory.merge(name, value(item.getSubtotal()), BigDecimal::add);
        }

        BigDecimal denominator = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? totalRevenue : BigDecimal.ONE;
        return revenueByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(6)
                .map(entry -> new AdminStatisticsResponse.CategoryRevenue(
                        entry.getKey(),
                        entry.getValue(),
                        entry.getValue().multiply(BigDecimal.valueOf(100))
                                .divide(denominator, 1, RoundingMode.HALF_UP)
                                .doubleValue()
                ))
                .toList();
    }

    private List<AdminStatisticsResponse.RecentOrder> buildRecentOrders(List<Order> orders) {
        return orders.stream()
                .limit(5)
                .map(order -> new AdminStatisticsResponse.RecentOrder(
                        order.getId(),
                        "#DH" + String.format(Locale.ROOT, "%05d", order.getId()),
                        order.getShippingName(),
                        value(order.getTotalAmount()),
                        order.getStatus(),
                        order.getPaymentStatus(),
                        order.getCreatedAt() == null ? "" : DATE_TIME_FORMAT.format(order.getCreatedAt())
                ))
                .toList();
    }

    private List<AdminStatisticsResponse.BestSeller> buildBestSellers(List<OrderItem> orderItems) {
        Map<String, Long> soldByName = orderItems.stream()
                .filter(item -> item.getProductName() != null)
                .collect(Collectors.groupingBy(OrderItem::getProductName, Collectors.summingLong(item -> item.getQuantity() == null ? 0 : item.getQuantity())));
        Map<String, BigDecimal> revenueByName = new HashMap<>();
        orderItems.stream()
                .filter(item -> item.getProductName() != null)
                .forEach(item -> revenueByName.merge(item.getProductName(), value(item.getSubtotal()), BigDecimal::add));

        return soldByName.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new AdminStatisticsResponse.BestSeller(
                        entry.getKey(),
                        entry.getValue(),
                        revenueByName.getOrDefault(entry.getKey(), BigDecimal.ZERO)
                ))
                .toList();
    }

    private int stockForProduct(Integer productId, List<ProductVariant> variants) {
        return variants.stream()
                .filter(variant -> Objects.equals(variant.getProductId(), productId))
                .mapToInt(variant -> variant.getStock() == null ? 0 : variant.getStock())
                .sum();
    }

    private boolean isPending(String status) {
        String normalized = normalize(status);
        return normalized.contains("pending") || normalized.contains("cho") || normalized.contains("xac nhan");
    }

    private boolean isCancelled(String status) {
        String normalized = normalize(status);
        return normalized.contains("cancel") || normalized.contains("huy");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}

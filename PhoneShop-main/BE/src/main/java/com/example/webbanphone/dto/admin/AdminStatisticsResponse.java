package com.example.webbanphone.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminStatisticsResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        long totalCustomers,
        long totalProducts,
        long pendingOrders,
        long outOfStockProducts,
        long totalReviews,
        double averageRating,
        List<CategoryRevenue> categoryRevenue,
        List<RecentOrder> recentOrders,
        List<BestSeller> bestSellers
) {
    public record CategoryRevenue(String name, BigDecimal revenue, double percent) {
    }

    public record RecentOrder(
            Integer id,
            String code,
            String customer,
            BigDecimal total,
            String status,
            String paymentStatus,
            String createdAt
    ) {
    }

    public record BestSeller(String name, long sold, BigDecimal revenue) {
    }
}

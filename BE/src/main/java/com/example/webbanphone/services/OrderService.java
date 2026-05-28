package com.example.webbanphone.services;

import com.example.webbanphone.dto.cart.CartItemResponse;
import com.example.webbanphone.dto.cart.CartResponse;
import com.example.webbanphone.dto.order.AdminOrderResponse;
import com.example.webbanphone.dto.order.CheckoutRequest;
import com.example.webbanphone.dto.order.OrderItemResponse;
import com.example.webbanphone.dto.order.OrderResponse;
import com.example.webbanphone.dto.order.UpdateOrderStatusRequest;
import com.example.webbanphone.entities.Order;
import com.example.webbanphone.entities.OrderItem;
import com.example.webbanphone.entities.OrderStatusLog;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.OrderItemRepository;
import com.example.webbanphone.repositories.OrderRepository;
import com.example.webbanphone.repositories.OrderStatusLogRepository;
import com.example.webbanphone.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class OrderService {

    private static final BigDecimal FREE_SHIP_FROM = BigDecimal.valueOf(500000);
    private static final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(30000);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderStatusLogRepository orderStatusLogRepository,
            UserRepository userRepository,
            CartService cartService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusLogRepository = orderStatusLogRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    public List<OrderResponse> getOrders(User user) {
        return orderRepository.findByUserIdOrderByIdDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(User user, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .filter(item -> item.getUserId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!Set.of("pending", "confirmed").contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending orders can be cancelled");
        }

        String oldStatus = order.getStatus();
        order.setStatus("cancelled");
        writeStatusLog(order.getId(), user.getId(), oldStatus, "cancelled", "Customer cancelled order");
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        if (request == null || isBlank(request.shippingName()) || isBlank(request.shippingPhone())
                || isBlank(request.shippingAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shipping information is required");
        }

        CartResponse cart = cartService.getCart(user);
        if (cart.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        BigDecimal shippingFee = cart.subtotal().compareTo(FREE_SHIP_FROM) >= 0 ? BigDecimal.ZERO : SHIPPING_FEE;
        BigDecimal totalAmount = cart.subtotal().add(shippingFee);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setShippingName(request.shippingName().trim());
        order.setShippingPhone(request.shippingPhone().trim());
        order.setShippingAddress(request.shippingAddress().trim());
        order.setSubtotal(cart.subtotal());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");
        order.setPaymentMethod(isBlank(request.paymentMethod()) ? "cod" : request.paymentMethod().trim());
        order.setPaymentStatus("unpaid");
        order.setNote(trimToNull(request.note()));
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        for (CartItemResponse cartItem : cart.items()) {
            OrderItem item = new OrderItem();
            item.setOrderId(savedOrder.getId());
            item.setVariantId(cartItem.variantId());
            item.setProductName(cartItem.productName());
            item.setVariantInfo(variantInfo(cartItem));
            item.setUnitPrice(cartItem.unitPrice());
            item.setQuantity(cartItem.quantity());
            item.setSubtotal(cartItem.subtotal());
            orderItemRepository.save(item);
        }

        cartService.clearCart(user);
        return toResponse(savedOrder);
    }

    public List<AdminOrderResponse> getAdminOrders(String status) {
        List<Order> orders = isBlank(status) || "all".equalsIgnoreCase(status)
                ? orderRepository.findAllByOrderByIdDesc()
                : orderRepository.findByStatusOrderByIdDesc(normalizeStatus(status));

        return orders.stream().map(this::toAdminResponse).toList();
    }

    @Transactional
    public AdminOrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request, User changedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (request != null && !isBlank(request.status())) {
            String oldStatus = order.getStatus();
            String newStatus = normalizeStatus(request.status());
            validateStatusTransition(oldStatus, newStatus);
            if (!oldStatus.equals(newStatus)) {
                order.setStatus(newStatus);
                writeStatusLog(order.getId(), changedBy == null ? null : changedBy.getId(), oldStatus, newStatus, trimToNull(request.note()));
            }
        }

        if (request != null && !isBlank(request.paymentStatus())) {
            String paymentStatus = request.paymentStatus().trim().toLowerCase(Locale.ROOT);
            if (!Set.of("unpaid", "paid", "refunded").contains(paymentStatus)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment status");
            }
            order.setPaymentStatus(paymentStatus);
        }

        return toAdminResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getVariantId(),
                        item.getProductName(),
                        item.getVariantInfo(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getShippingName(),
                order.getShippingPhone(),
                order.getShippingAddress(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getNote(),
                order.getCreatedAt(),
                items
        );
    }

    private AdminOrderResponse toAdminResponse(Order order) {
        User user = userRepository.findById(order.getUserId()).orElse(null);
        OrderResponse base = toResponse(order);

        return new AdminOrderResponse(
                order.getId(),
                order.getUserId(),
                user == null ? order.getShippingName() : user.getFullName(),
                user == null ? null : user.getEmail(),
                user == null ? order.getShippingPhone() : user.getPhone(),
                order.getShippingName(),
                order.getShippingPhone(),
                order.getShippingAddress(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getNote(),
                order.getCreatedAt(),
                base.items()
        );
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("pending", "confirmed", "processing", "shipping", "delivered", "cancelled", "refunded")
                .contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status");
        }
        return normalized;
    }

    private void validateStatusTransition(String oldStatus, String newStatus) {
        if (oldStatus == null || oldStatus.equals(newStatus)) {
            return;
        }

        boolean valid = switch (oldStatus) {
            case "pending" -> Set.of("confirmed", "cancelled").contains(newStatus);
            case "confirmed" -> Set.of("processing", "cancelled").contains(newStatus);
            case "processing" -> "shipping".equals(newStatus);
            case "shipping" -> "delivered".equals(newStatus);
            case "delivered" -> "refunded".equals(newStatus);
            default -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status transition");
        }
    }

    private void writeStatusLog(Integer orderId, Integer changedBy, String oldStatus, String newStatus, String note) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setChangedBy(changedBy);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setNote(note);
        orderStatusLogRepository.save(log);
    }

    private String variantInfo(CartItemResponse item) {
        return Stream.of(item.color(), item.storage(), item.ram())
                .filter(value -> value != null && !value.trim().isEmpty())
                .reduce((left, right) -> left + " / " + right)
                .orElse(null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}

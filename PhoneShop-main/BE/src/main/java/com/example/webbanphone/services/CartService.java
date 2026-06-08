package com.example.webbanphone.services;

import com.example.webbanphone.dto.cart.AddCartItemRequest;
import com.example.webbanphone.dto.cart.CartItemResponse;
import com.example.webbanphone.dto.cart.CartResponse;
import com.example.webbanphone.dto.cart.UpdateCartItemRequest;
import com.example.webbanphone.entities.CartItem;
import com.example.webbanphone.entities.Product;
import com.example.webbanphone.entities.ProductVariant;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.CartItemRepository;
import com.example.webbanphone.repositories.ProductRepository;
import com.example.webbanphone.repositories.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CartService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductVariantRepository variantRepository,
            ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getCart(User user) {
        List<CartItemResponse> items = cartItemRepository.findByUserIdOrderByIdDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return toCartResponse(items);
    }

    @Transactional
    public CartResponse addItem(User user, AddCartItemRequest request) {
        if (request == null || request.variantId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "variantId is required");
        }

        ProductVariant variant = findActiveVariant(request.variantId());
        int quantity = normalizeQuantity(request.quantity());
        int stock = variant.getStock() == null ? 0 : variant.getStock();
        if (stock <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product variant is out of stock");
        }

        CartItem item = cartItemRepository.findByUserIdAndVariantId(user.getId(), variant.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUserId(user.getId());
                    newItem.setVariantId(variant.getId());
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(Math.min(item.getQuantity() + quantity, stock));
        cartItemRepository.save(item);
        return getCart(user);
    }

    @Transactional
    public CartResponse updateItem(User user, Integer itemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
                .filter(cartItem -> cartItem.getUserId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        int quantity = normalizeQuantity(request == null ? null : request.quantity());
        ProductVariant variant = findActiveVariant(item.getVariantId());
        int stock = variant.getStock() == null ? 0 : variant.getStock();
        item.setQuantity(Math.min(quantity, stock));
        cartItemRepository.save(item);
        return getCart(user);
    }

    @Transactional
    public CartResponse removeItem(User user, Integer itemId) {
        cartItemRepository.deleteByUserIdAndId(user.getId(), itemId);
        return getCart(user);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUserId(user.getId());
    }

    private ProductVariant findActiveVariant(Integer variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found"));
        if (!Boolean.TRUE.equals(variant.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product variant is inactive");
        }
        return variant;
    }

    private CartResponse toCartResponse(List<CartItemResponse> items) {
        int totalQuantity = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, totalQuantity, subtotal);
    }

    private CartItemResponse toResponse(CartItem item) {
        ProductVariant variant = variantRepository.findById(item.getVariantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found"));
        Product product = productRepository.findById(variant.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        BigDecimal unitPrice = effectivePrice(variant);
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                variant.getId(),
                product.getName(),
                product.getThumbnail(),
                variant.getColor(),
                variant.getStorage(),
                variant.getRam(),
                variant.getSku(),
                unitPrice,
                item.getQuantity(),
                variant.getStock(),
                subtotal
        );
    }

    private int normalizeQuantity(Integer quantity) {
        if (quantity == null) {
            return 1;
        }
        return Math.max(1, Math.min(quantity, 99));
    }

    private BigDecimal effectivePrice(ProductVariant variant) {
        BigDecimal price = variant.getPrice() == null ? BigDecimal.ZERO : variant.getPrice();
        BigDecimal discountPercent = variant.getDiscountPercent() == null
                ? BigDecimal.ZERO
                : variant.getDiscountPercent();

        if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            return price.multiply(ONE_HUNDRED.subtract(discountPercent))
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        }

        BigDecimal salePrice = variant.getSalePrice();
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) >= 0 && salePrice.compareTo(price) < 0) {
            return salePrice;
        }

        return price;
    }
}

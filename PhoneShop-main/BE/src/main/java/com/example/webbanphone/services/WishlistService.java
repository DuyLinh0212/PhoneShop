package com.example.webbanphone.services;

import com.example.webbanphone.dto.product.ProductDetailResponse;
import com.example.webbanphone.dto.wishlist.FavoriteStatusResponse;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.entities.Wishlist;
import com.example.webbanphone.repositories.ProductRepository;
import com.example.webbanphone.repositories.WishlistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public WishlistService(
            WishlistRepository wishlistRepository,
            ProductRepository productRepository,
            ProductService productService
    ) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public List<ProductDetailResponse> getFavorites(User user) {
        return wishlistRepository.findByUserIdOrderByIdDesc(user.getId())
                .stream()
                .filter(item -> productRepository.existsById(item.getProductId()))
                .map(item -> productService.getProductDetail(item.getProductId()))
                .toList();
    }

    public FavoriteStatusResponse getStatus(User user, Integer productId) {
        return new FavoriteStatusResponse(
                wishlistRepository.existsByUserIdAndProductId(user.getId(), productId),
                wishlistRepository.countByUserId(user.getId())
        );
    }

    @Transactional
    public FavoriteStatusResponse addFavorite(User user, Integer productId) {
        if (productId == null || !productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUserId(user.getId());
                    wishlist.setProductId(productId);
                    wishlist.setAddedAt(LocalDateTime.now());
                    return wishlistRepository.save(wishlist);
                });

        return getStatus(user, productId);
    }

    @Transactional
    public FavoriteStatusResponse removeFavorite(User user, Integer productId) {
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
        return getStatus(user, productId);
    }
}

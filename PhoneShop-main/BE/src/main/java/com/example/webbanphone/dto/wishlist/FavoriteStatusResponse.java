package com.example.webbanphone.dto.wishlist;

public record FavoriteStatusResponse(
        Boolean favorite,
        Long wishlistCount
) {
}

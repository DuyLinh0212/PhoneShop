package com.example.webbanphone.dto.profile;

public record ProfileResponse(
        Integer userId,
        String fullName,
        String email,
        String phone,
        String role,
        long orderCount,
        long wishlistCount,
        Integer rewardPoints,
        AddressResponse defaultAddress
) {
}

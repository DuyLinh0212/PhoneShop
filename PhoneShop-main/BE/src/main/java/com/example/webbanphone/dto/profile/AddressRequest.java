package com.example.webbanphone.dto.profile;

public record AddressRequest(
        String fullName,
        String phone,
        String province,
        String district,
        String ward,
        String street
) {
}

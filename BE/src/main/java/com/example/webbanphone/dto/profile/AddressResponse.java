package com.example.webbanphone.dto.profile;

public record AddressResponse(
        Integer id,
        String fullName,
        String phone,
        String province,
        String district,
        String ward,
        String street,
        Boolean isDefault
) {
}

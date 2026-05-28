package com.example.webbanphone.services;

import com.example.webbanphone.dto.profile.AddressResponse;
import com.example.webbanphone.dto.profile.ProfileResponse;
import com.example.webbanphone.dto.profile.UpdateProfileRequest;
import com.example.webbanphone.entities.Address;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.AddressRepository;
import com.example.webbanphone.repositories.OrderRepository;
import com.example.webbanphone.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;

    public ProfileService(
            UserRepository userRepository,
            AddressRepository addressRepository,
            OrderRepository orderRepository
    ) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
    }

    public ProfileResponse getProfile(User user) {
        return toResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request == null || isBlank(request.fullName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }

        user.setFullName(request.fullName().trim());
        user.setPhone(trimToNull(request.phone()));
        return toResponse(userRepository.save(user));
    }

    private ProfileResponse toResponse(User user) {
        AddressResponse defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrueOrderByIdDesc(user.getId())
                .map(this::toAddressResponse)
                .orElse(null);
        String role = user.getRole() != null ? user.getRole().getName() : "user";
        long orderCount = orderRepository.countByUserId(user.getId());

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                role,
                orderCount,
                0,
                (int) Math.min(orderCount * 100, 999999),
                defaultAddress
        );
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getProvince(),
                address.getDistrict(),
                address.getWard(),
                address.getStreet(),
                address.getIsDefault()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}

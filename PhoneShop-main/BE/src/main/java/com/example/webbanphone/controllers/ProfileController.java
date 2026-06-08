package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.profile.AddressRequest;
import com.example.webbanphone.dto.profile.AddressResponse;
import com.example.webbanphone.dto.profile.ProfileResponse;
import com.example.webbanphone.dto.profile.UpdateProfileRequest;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.services.CurrentUserService;
import com.example.webbanphone.services.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    public ProfileController(ProfileService profileService, CurrentUserService currentUserService) {
        this.profileService = profileService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(currentUser(authentication)));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProfile(currentUser(authentication), request));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(Authentication authentication) {
        return ResponseEntity.ok(profileService.getAddresses(currentUser(authentication)));
    }

    @PutMapping("/default-address")
    public ResponseEntity<AddressResponse> saveDefaultAddress(
            Authentication authentication,
            @RequestBody AddressRequest request
    ) {
        return ResponseEntity.ok(profileService.saveDefaultAddress(currentUser(authentication), request));
    }

    private User currentUser(Authentication authentication) {
        return currentUserService.getByEmail(authentication == null ? null : authentication.getName());
    }
}

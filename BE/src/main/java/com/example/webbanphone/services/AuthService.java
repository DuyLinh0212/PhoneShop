package com.example.webbanphone.services;
import com.example.webbanphone.dto.auth.LoginRequest;
import com.example.webbanphone.dto.auth.LoginResponse;
import com.example.webbanphone.dto.auth.MeResponse;
import com.example.webbanphone.dto.auth.RegisterRequest;
import com.example.webbanphone.dto.auth.RegisterResponse;
import com.example.webbanphone.entities.Role;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.RoleRepository;
import com.example.webbanphone.repositories.UserRepository;
import com.example.webbanphone.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String role = user.getRole() != null ? user.getRole().getName() : "user";
        String token = jwtService.generateToken(user.getId(), user.getEmail(), role);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                role
        );
    }

    public RegisterResponse register(RegisterRequest request) {
        if (request == null
                || isBlank(request.fullName())
                || isBlank(request.email())
                || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fullName, email and password are required");
        }

        if (request.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Role userRole = roleRepository.findByNameIgnoreCase("user")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Role 'user' not found"));

        User user = new User();
        user.setRole(userRole);
        user.setFullName(request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(isBlank(request.phone()) ? null : request.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                userRole.getName()
        );
    }

    public MeResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String role = user.getRole() != null ? user.getRole().getName() : "user";
        return new MeResponse(user.getId(), user.getFullName(), user.getEmail(), role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

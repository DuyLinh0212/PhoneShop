package com.example.webbanphone.services;

import com.example.webbanphone.entities.RefreshToken;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Thu hồi toàn bộ refresh token cũ của user trước khi tạo mới
        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshToken.setIsRevoked(false);
        refreshToken.setCreatedAt(Instant.now());
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"));

        if (Boolean.TRUE.equals(refreshToken.getIsRevoked())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token đã bị thu hồi");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.revokeByToken(token);
    }

    @Transactional
    public void revokeAllByUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }
}

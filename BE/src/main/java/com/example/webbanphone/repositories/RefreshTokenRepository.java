package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.RefreshToken;
import com.example.webbanphone.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.user = :user AND r.isRevoked = false")
    void revokeAllByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.token = :token")
    void revokeByToken(@Param("token") String token);
}

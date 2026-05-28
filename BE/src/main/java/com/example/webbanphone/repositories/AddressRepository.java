package com.example.webbanphone.repositories;

import com.example.webbanphone.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUserIdOrderByIsDefaultDescIdDesc(Integer userId);

    Optional<Address> findFirstByUserIdAndIsDefaultTrueOrderByIdDesc(Integer userId);
}

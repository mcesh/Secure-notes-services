package com.secure.notes.repositories;

import com.secure.notes.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserName(String username);

    Boolean existsByUserName(String username);

    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);
}

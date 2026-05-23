package com.example.prathiphala_family_league.user.repository;

import com.example.prathiphala_family_league.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByDeletedFalseAndActiveTrue();

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName AND u.deleted = false AND u.active = true")
    List<User> findActiveUsersByRoleName(@Param("roleName") String roleName);
    Optional<User> findByEmailAndDeletedFalse(String email);
    Optional<User> findByIdAndDeletedFalse(Long id);
    Page<User> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchByNameOrEmail(@Param("search") String search, Pageable pageable);
}

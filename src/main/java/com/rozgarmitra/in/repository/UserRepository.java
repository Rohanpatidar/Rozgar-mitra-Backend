package com.rozgarmitra.in.repository;

import com.rozgarmitra.in.Entity.User;
import com.rozgarmitra.in.Enum.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);

    List<User> findByRole(Role role);

    boolean existsByUsername(String username);

    long countByRoleAndIsOnlineTrue(Role role);
}

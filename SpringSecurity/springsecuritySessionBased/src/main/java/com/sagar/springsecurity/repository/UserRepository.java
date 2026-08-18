package com.sagar.springsecurity.repository;

import com.sagar.springsecurity.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

}

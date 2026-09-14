package com.taskmanagement.repository;

import com.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JpaRepository gives us: save(), findById(), findAll(), delete(), count()
 * for free — no SQL needed.
 *
 * findByEmail is a Spring Data derived query — Spring generates the SQL
 * from the method name at startup. Used by UserDetailsService for JWT auth.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}

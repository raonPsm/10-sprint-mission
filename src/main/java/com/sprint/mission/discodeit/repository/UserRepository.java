package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// JpaRepository<엔티티 타입, PK 타입> 상속
public interface UserRepository extends JpaRepository<User, UUID> {
    // JpaRepository에서 기본적인 CRUD 메서드 제공 (save, findById, findAll, existsById, deleteById)

    // Spring Data JPA 쿼리 메소드
    // exists + By + Username
    // JPQL -> SELECT COUNT(u) FROM User u WHERE u.username = :username
    // SQL -> SELECT count(*) >= 1 FROM users WHERE username = ?
    boolean existsByUsername(String username);
    // exists + By + Email
    // JPQL -> SELECT COUNT(u) FROM User u WHERE u.email = :email
    // SQL -> SELECT count(*) >= 1 FROM users WHERE email = ?
    boolean existsByEmail(String email);
}

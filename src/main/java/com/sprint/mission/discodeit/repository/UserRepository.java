package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// JpaRepository<엔티티 타입, PK 타입> 상속
public interface UserRepository extends JpaRepository<User, UUID> {
    // JpaRepository에서 기본적인 CRUD 메서드 제공
        // boolean existsById(ID id);
        // boolean existsById(UUID id);
        // List<User> findAllById(Iterable<UUID> ids);
        // Optional<User> findById(UUID id);
        // User save(User user);
        // List<User> findAll();
        // void delete(User user);
        // User getReferenceById(UUID id);

    // Spring Data JPA 쿼리 메소드

    // exists + By + Username
    // JPQL -> SELECT COUNT(u) FROM User u WHERE u.username = :username
    // SQL -> SELECT count(*) >= 1 FROM users WHERE username = ?
    boolean existsByUsername(String username);

    // exists + By + Email
    // JPQL -> SELECT COUNT(u) FROM User u WHERE u.email = :email
    // SQL -> SELECT count(*) >= 1 FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // find + By + Username
    // JPQL -> SELECT u FROM User u WHERE u.username = :username
    // SQL -> SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);

    // @EntityGraph(attributePaths = {"profile", "userStatus"})
    // @Query("SELECT u FROM User u")
    @Query("SELECT u " +
            "FROM User u " +
            "LEFT JOIN FETCH u.profile " +
            "LEFT JOIN FETCH u.userStatus")
    List<User> findAllWithProfileAndUserStatus();
}

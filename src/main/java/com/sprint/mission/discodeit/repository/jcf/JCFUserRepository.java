package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
// name -> 확인할 설정 속성 이름
// havingValue -> 설정값이 이 값과 일치할 때만 Bean으로 등록
// matchIfMissing = true -> application.yaml에 해당 설정이 아예 없을 경우에도 이 클래스를 Bean으로 등록
public class JCFUserRepository implements UserRepository {
    private final Map<UUID, User> data;

    public JCFUserRepository() {
        data = new ConcurrentHashMap<>();
    }

    @Override
    public User save(User user) {
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(data.get(userId));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public boolean existsById(UUID userId) {
        return data.containsKey(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return data.values().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    @Override
    public boolean existsByEmail(String email) {
        return data.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public void deleteById(UUID userId) {
        data.remove(userId);
    }
}

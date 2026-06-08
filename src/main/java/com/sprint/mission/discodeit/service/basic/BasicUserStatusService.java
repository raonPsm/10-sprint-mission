package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserStatusDto create(UserStatusCreateRequest request) {
        // 관련된 User가 존재하지 않으면 예외를 발생
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다. userId: " + request.userId()));

        // 같은 User와 관련된 객체가 이미 존재하면 예외를 발생
        if(userStatusRepository.existsByUser_Id(request.userId())) {
            throw new IllegalArgumentException("이미 해당 유저의 상태 정보(UserStatus)가 존재합니다. userId: " + request.userId());
        }

        UserStatus userStatus = new UserStatus(user, Instant.now());

        return userStatusMapper.toDto(userStatusRepository.save(userStatus));
    }

    @Transactional(readOnly = true)
    @Override
    public UserStatusDto find(UUID id) {
        return userStatusMapper.toDto(
                userStatusRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다."))
                );
    }


    @Transactional(readOnly = true)
    @Override
    public List<UserStatusDto> findAll() {
        return userStatusMapper.toDtoList(userStatusRepository.findAll());
    }

    @Transactional
    @Override
    public UserStatusDto update(UUID id, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다."));
        userStatus.update(request.newLastActiveAt());
        return userStatusMapper.toDto(userStatus);
    }

    @Transactional
    @Override
    public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NoSuchElementException("수정할 UserStatus를 찾을 수 없습니다."));
        userStatus.update(request.newLastActiveAt());
        return userStatusMapper.toDto(userStatus);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("UserStatus를 찾을 수 없습니다.")); // 엔티티를 영속성 컨텍스트에 올려놓고
        userStatusRepository.delete(userStatus); // 삭제
    }
}
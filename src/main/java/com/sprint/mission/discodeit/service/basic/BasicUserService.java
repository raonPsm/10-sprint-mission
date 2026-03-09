package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    // binaryContentRepo, userStatusRepo -> 의존성 필요 없음 / 변경 감지 및 영속성 전이 활용
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;

    @Transactional
    @Override
    public UserDto create(UserCreateRequest userCreateRequest,
                          Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String username = userCreateRequest.username();
        String email = userCreateRequest.email();
        String password = userCreateRequest.password();

        // email, username 중복 검증
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일(email)입니다: " + email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름(username)입니다.: " + username);
        }

        // 프로필 이미지 엔티티 생성
        BinaryContent profileImage = optionalProfileCreateRequest
                .map(req -> {
                    BinaryContent binaryContent = new BinaryContent(
                            req.fileName(),
                            (long)req.bytes().length,
                            req.contentType()
                    );

                    // DB에 메타데이터 저장
                    binaryContentRepository.save(binaryContent);

                    // 실제 파일 데이터를 스토리지에 저장
                    binaryContentStorage.put(binaryContent.getId(), req.bytes());

                    return binaryContent;
                })
                .orElse(null);

        // User 엔티티 생성
        User user = new User(username, email, password, profileImage);

        // UserStatus 엔티티 생성 + 양방향 연관관계(1:1) 설정
        // TODO: 단방향 연관관계가 맞는 건지 확인 요망
        UserStatus userStatus = new UserStatus(user, Instant.now());
        user.assignUserStatus(userStatus); // TODO: 필요한 것인지 검토 요망

        User savedUser = userRepository.save(user);
        // 영속성 전이
        //   JPA에서 엔티티를 저장할 때, 내부적으로는 EntityManager.persist(entity) 메서드가 호출되어
        // 해당 객체를 영속성 컨텍스트에 등록한다. (Managed 상태로 변경)
        //   cascade = CascadeType.ALL -> 부모 엔티티에서 수행되는 영속성 컨텍스트 상태 변화를,
        // 참조하고 있는 자식 엔티티에도 동일하게 전이
        //
        // user 객체를 영속성 컨텍스트에 등록 -> JPA가 user 객체의 필드들을 스캔하면서 cascade 옵션이 설정된 연관 엔티티가 있는지 확인
        // -> JPA는 부모인 user를 영속화하기 전에, 참조된 자식 객체인 profile과 userStatus에도 persist() 명령을 전파하여 영속성 컨텍스트에 함께 등록
        // -> @Transactional에 의해 메서드가 종료되고 커밋되는 시점(Flush)에,
        // 영속성 컨텍스트에 새로 등록된 3개의 엔티티에 대한 INSERT 쿼리가 한번에 진행
        // * Flush -> 영속성 컨텍스트의 변경 내용을 DB에 반영하는 것 / 영속성 컨텍스트의 변경 내용을 DB에 동기화

        return userMapper.toDto(savedUser);
        // TODO: username 유효성 검사 로직 추가
        // TODO: email 중복 불가능 검사 로직 추가
        // TODO: password 유효성 검사 로직 추가
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 유저가 존재하지 않습니다. (userId: " + userId + " )"));

        // UserStatus 무결성 검증
        if (user.getUserStatus() == null) {
            throw new IllegalStateException("유저 상태 데이터가 누락되었습니다. (userId: " + userId + ")");
        }

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAll() {
        return userMapper.toDtoList(userRepository.findAll());
        // TODO: N+1 문제 -> Fetch Join
    }

    @Transactional
    @Override
    public UserDto update(UUID userId,
                       UserUpdateRequest userUpdateRequest,
                       Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        // user 존재하는지 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 유저가 존재하지 않습니다. (userId: " + userId + " )"));

        String newUsername = userUpdateRequest.newUsername();
        String newEmail = userUpdateRequest.newEmail();

        // 본인의 현재 정보와 다를 때만 중복 검사 실시
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용중인 이메일(email)입니다.: " + newEmail);
        }
        if (!user.getUsername().equals(newUsername) && userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름(username)입니다.: " + newUsername);
        }


        // 회원가입 시 사진을 등록했다면 기본 프로필로 돌아갈 수 있는 기능 없음
        // -> 업데이트 시 파일이 존재하면 기존 사진 삭제 후 새로 저장, 파일이 없으면 기존 사진 유지
        BinaryContent newProfileImage = optionalProfileCreateRequest
                .map(req -> new BinaryContent(req.fileName(), (long)req.bytes().length, req.contentType()))
                .orElse(user.getProfile());

        // orphanRemoval = true 설정으로 새로운 프로필 참조가 할당되면 기존 프로필은 자동으로 DELETE 수행됨
        user.update(newUsername, newEmail, userUpdateRequest.newPassword(), newProfileImage);

        optionalProfileCreateRequest.ifPresent(req ->
                binaryContentStorage.put(user.getProfile().getId(), req.bytes())
        );

        // (Dirty Checking) 메서드 종료 시 트랜잭션이 커밋되면서 변경된 엔티티에 대한 UPDATE 쿼리 자동 발생. save() 호출 불필요
        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 유저가 존재하지 않습니다. (userId: " + userId + " )"));
        // cascade = CascadeType.ALL, orphanRemoval = true 적용
        // User만 삭제해도 연관된 UserStatus, BinaryContent에 대한 DELETE 쿼리가 자동으로 발생
        userRepository.delete(user);
    }
}

package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Transactional
    @Override
    public ReadStatus create(ReadStatusCreateRequest request) {
        // 관련된 Channel, User 있는지 확인
        if (!userRepository.existsById(request.userId())) {
            throw new NoSuchElementException("존재하지 않는 사용자입니다. id: " + request.userId());
        }
        if (!channelRepository.existsById(request.channelId())) {
            throw new NoSuchElementException("존재하지 않는 채널입니다. id: " + request.channelId());
        }

        // 같은 Channel과 User와 관련된 객체가 이미 존재하면 예외 발생
        if (readStatusRepository.existsByUser_IdAndChannel_Id(request.userId(), request.channelId())) {
            throw new IllegalArgumentException("해당 유저의 readStatus가 이미 존재합니다.");
        }

        // 지연 로딩 활용 (프록시)
        User userProxy = userRepository.getReferenceById(request.userId());
        Channel channelProxy = channelRepository.getReferenceById(request.channelId());

        ReadStatus readStatus = new ReadStatus(userProxy, channelProxy, request.lastReadAt());

        return readStatusRepository.save(readStatus);
    }

    @Transactional(readOnly = true)
    @Override
    public ReadStatus find(UUID readStatusId) {
        return readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException("읽기 상태(readStatus)를 찾을 수 없습니다. id: " + readStatusId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return readStatusRepository.findAllByUserId(userId);
    }

    @Transactional
    @Override
    public ReadStatus update(UUID readStatusId, ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException("수정할 읽기 상태(readStatus)를 찾을 수 없습니다. id: " + readStatusId));

        // 읽은 시간 갱신
        readStatus.updateLastReadAt(request.newLastReadAt());

        return readStatus;
    }

    @Transactional
    @Override
    public void delete(UUID readStatusId) {
        // FIXME: 내부 메서드 사용하지 않도록 수정
        ReadStatus readStatus = find(readStatusId);
        readStatusRepository.delete(readStatus);
    }
}

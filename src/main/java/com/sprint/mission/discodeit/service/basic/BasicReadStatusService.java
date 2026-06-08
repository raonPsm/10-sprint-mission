package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final ReadStatusMapper readStatusMapper;

  @Transactional
  @Override
  public ReadStatusDto create(ReadStatusCreateRequest request) {
    // 관련된 Channel, User 있는지 확인
    if (!userRepository.existsById(request.userId())) {
      throw new UserNotFoundException(Map.of("userId", request.userId()));
    }
    if (!channelRepository.existsById(request.channelId())) {
      throw new ChannelNotFoundException(Map.of("channelId", request.channelId()));
    }

    // 같은 Channel과 User와 관련된 객체가 이미 존재하면 예외 발생
    if (readStatusRepository.existsByUser_IdAndChannel_Id(request.userId(), request.channelId())) {
      throw new ReadStatusAlreadyExistsException(
          Map.of("userId", request.userId(), "channelId", request.channelId()));
    }

    // 지연 로딩 활용 (프록시)
    User userProxy = userRepository.getReferenceById(request.userId());
    Channel channelProxy = channelRepository.getReferenceById(request.channelId());

    ReadStatus readStatus = new ReadStatus(userProxy, channelProxy, request.lastReadAt());

    return readStatusMapper.toDto(readStatusRepository.save(readStatus));
  }

  @Transactional(readOnly = true)
  @Override
  public ReadStatusDto find(UUID readStatusId) {
    return readStatusMapper.toDto(readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(Map.of("readStatusId", readStatusId)))
    );
  }

  @Transactional(readOnly = true)
  @Override
  public List<ReadStatusDto> findAllByUserId(UUID userId) {
    return readStatusMapper.toDtoList(readStatusRepository.findAllByUserId(userId));
  }

  @Transactional
  @Override
  public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(Map.of("readStatusId", readStatusId)));

    // 읽은 시간 갱신
    readStatus.updateLastReadAt(request.newLastReadAt());

    return readStatusMapper.toDto(readStatus);
  }

  @Transactional
  @Override
  public void delete(UUID readStatusId) {
    ReadStatus readStatus = readStatusRepository.findById(readStatusId)
        .orElseThrow(() -> new ReadStatusNotFoundException(Map.of("readStatusId", readStatusId)));
    readStatusRepository.delete(readStatus);
  }
}

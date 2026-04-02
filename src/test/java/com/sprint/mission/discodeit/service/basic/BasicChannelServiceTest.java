package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.Dto.ChannelDto;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNameAlreadyExistsException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelAlreadyExistsException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelParticipantsEmptyException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private ReadStatusRepository readStatusRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelMapper channelMapper;

  @InjectMocks
  private BasicChannelService channelService;

  // === Helper Method ===

  private Channel publicChannelInit(String name) {
    Channel channel = Channel.createPublicChannel(name, "description");
    ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());
    return channel;
  }

  private Channel privateChannelInit() {
    Channel channel = Channel.createPrivateChannel();
    ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());
    return channel;
  }

  private User userInit(String username) {
    User user = new User(username, username + "@example.com", "password1", null);
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  private ChannelDto publicDtoFrom(Channel channel) {
    return new ChannelDto(
        channel.getId(),
        ChannelType.PUBLIC,
        channel.getName(),
        channel.getDescription(),
        List.of(),
        Instant.now()
    );
  }

  private ChannelDto privateDtoFrom(Channel channel) {
    return new ChannelDto(
        channel.getId(),
        ChannelType.PRIVATE,
        null,
        null,
        List.of(),
        Instant.now()
    );
  }

  // === create(PUBLIC) ===
  @Nested
  @DisplayName("create(PUBLIC)")
  class CreatePublicChannel {

    @Test
    @DisplayName("성공 - PUBLIC 채널 생성")
    void createPublic_shouldCreatePublicChannel_whenValidRequestProvided() {
      // given
      PublicChannelCreateRequest request =
          new PublicChannelCreateRequest("Public Channel", "description");
      Channel savedChannel = publicChannelInit("Public Channel");
      ChannelDto expectedDto = publicDtoFrom(savedChannel);

      given(channelRepository.existsByName("Public Channel")).willReturn(false);
      given(channelRepository.save(any(Channel.class))).willReturn(savedChannel);
      given(channelMapper.toDto(savedChannel)).willReturn(expectedDto);

      // when
      ChannelDto result = channelService.create(request);

      // then
      assertThat(result.type()).isEqualTo(ChannelType.PUBLIC);
      assertThat(result.name()).isEqualTo("Public Channel");
      assertThat(result.description()).isEqualTo("description");
    }

    @Test
    @DisplayName("실패 - 이미 존재하는 채널 이름으로 생성 시 ChannelNameAlreadyExistsException 발생")
    void createPublic_shouldThrowChannelNameAlreadyExistsException_whenPublicChannelNameAlreadyExists() {
      // given
      PublicChannelCreateRequest request =
          new PublicChannelCreateRequest("Duplicated Channel", "description");

      given(channelRepository.existsByName("Duplicated Channel")).willReturn(true);
      // when & then
      assertThatThrownBy(() -> channelService.create(request))
          .isInstanceOf(ChannelNameAlreadyExistsException.class);

      then(channelRepository).should(never()).save(any());
    }
  }

  // === create(PRIVATE) ===
  @Nested
  @DisplayName("create(PRIVATE)")
  class CreatePrivate {

    @Test
    @DisplayName("성공 - PRIVATE 채널 생성 및 참여자 ReadStatus 저장")
    void createPrivate_shouldCreatePrivateChannelAndSaveReadStatuses_whenValidParticipantsProvided() {
      // given
      User testUser1 = userInit("testUser1");
      User testUser2 = userInit("testUser2");
      Set<UUID> participantsIds = Set.of(testUser1.getId(), testUser2.getId());
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantsIds);

      Channel savedChannel = privateChannelInit();
      ChannelDto expectedDto = privateDtoFrom(savedChannel);

      given(channelRepository.findPrivateChannelByParticipantsIds(participantsIds))
          .willReturn(Optional.empty());
      given(userRepository.existsById(testUser1.getId())).willReturn(true);
      given(userRepository.existsById(testUser2.getId())).willReturn(true);
      given(channelRepository.save(any(Channel.class))).willReturn(savedChannel);
      given(userRepository.findAllById(participantsIds)).willReturn(List.of(testUser1, testUser2));
      given(channelMapper.toDto(savedChannel)).willReturn(expectedDto);

      // when
      ChannelDto result = channelService.create(request);

      // then
      assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);
      // PRIVATE 채널 생성 과정에서 ReadStatus들 저장되었는지 확인
      then(readStatusRepository).should().saveAll(any());
    }

    @Test
    @DisplayName("실패 - 참여자 목록이 비어 있을 경우 PrivateChannelParticipantsEmptyException 발생")
    void createPrivate_shouldThrowPrivateChannelParticipantsEmptyException_whenParticipantsAreEmpty() {
      // given
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(Set.of());

      // when & then
      assertThatThrownBy(() -> channelService.create(request))
          .isInstanceOf(PrivateChannelParticipantsEmptyException.class);

      then(channelRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 동일 참여자 구성의 PRIVATE 채널이 이미 존재하면 PrivateChannelAlreadyExistsException 발생")
    void create_shouldThrowPrivateChannelAlreadyExistsException_whenPrivateChannelWithSameParticipantsAlreadyExists() {
      // given
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      Set<UUID> participantIds = Set.of(userId1, userId2);
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);

      Channel existingChannel = privateChannelInit();
      given(channelRepository.findPrivateChannelByParticipantsIds(participantIds))
          .willReturn(Optional.of(existingChannel));

      // when & then
      assertThatThrownBy(() -> channelService.create(request))
          .isInstanceOf(PrivateChannelAlreadyExistsException.class);

      then(channelRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 참여자 ID가 포함된 경우 UserNotFoundException 발생")
    void create_shouldThrowUserNotFoundException_whenUnknownParticipantIncluded() {
      // TODO: 구현
    }
  }

  // === update() ===
  @Nested
  @DisplayName("update()")
  class Update {

    @Test
    @DisplayName("성공 - PUBLIC 채널 이름과 설명 수정")
    void update_shouldUpdatePublicChannel_whenValidRequest() {
      // given
      Channel existingChannel = publicChannelInit("oldName");
      UUID channelId = existingChannel.getId();
      PublicChannelUpdateRequest request =
          new PublicChannelUpdateRequest("newName", "newDescription");
      ChannelDto expectedDto = new ChannelDto(
          channelId,
          ChannelType.PUBLIC,
          "newName",
          "newDescription",
          List.of(),
          Instant.now()
      );

      given(channelRepository.findById(channelId)).willReturn(Optional.of(existingChannel));
      given(channelRepository.existsByName("newName")).willReturn(false);
      given(channelMapper.toDto(existingChannel)).willReturn(expectedDto);

      // when
      ChannelDto result = channelService.update(channelId, request);

      // then
      assertThat(result.name()).isEqualTo("newName");
      assertThat(result.description()).isEqualTo("newDescription");
      assertThat(result.name()).isEqualTo("newName");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 채널 수정 시 ChannelNotFoundException 발생")
    void update_shouldThrowChannelNotFoundException_whenChannelDoesNotExist() {
      // given
      UUID unknownId = UUID.randomUUID();
      PublicChannelUpdateRequest request =
          new PublicChannelUpdateRequest("newName", "newDescription");

      given(channelRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.update(unknownId, request))
          .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("실패 - PRIVATE 채널 수정 시도 시 PrivateChannelUpdateNotAllowedException 발생")
    void update_shouldThrowPrivateChannelUpdateNotAllowedException_whenChannelIsPrivate() {
      // given
      Channel privateChannel = privateChannelInit();
      UUID channelId = privateChannel.getId();
      // request 인자를 위한 세팅
      PublicChannelUpdateRequest request =
          new PublicChannelUpdateRequest("newName", "newDescription");

      given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

      // when & then
      assertThatThrownBy(() -> channelService.update(channelId, request))
          .isInstanceOf(PrivateChannelUpdateNotAllowedException.class);
    }

    @Test
    @DisplayName("실패 - 다른 채널이 사용 중인 이름으로 수정 시 ChannelNameAlreadyExistsException 발생")
    void update_shouldThrowChannelNameAlreadyExistsException_whenNewNameAlreadyExists() {
      // given
      Channel existingChannel = publicChannelInit("name");
      UUID channelId = existingChannel.getId();
      PublicChannelUpdateRequest request =
          new PublicChannelUpdateRequest("duplicatedName", "duplicatedDescription");

      given(channelRepository.findById(channelId)).willReturn(Optional.of(existingChannel));
      given(channelRepository.existsByName("duplicatedName")).willReturn(true);

      // when & then
      assertThatThrownBy(() -> channelService.update(channelId, request))
          .isInstanceOf(ChannelNameAlreadyExistsException.class);
    }
  }

  // === delete() ===
  @Nested
  @DisplayName("delete()")
  class Delete {

    @Test
    @DisplayName("성공 - 채널 삭제 시 메시지,ReadStatus 먼저 삭제 후 채널 삭제")
    void delete_shouldDeleteMessagesReadStatusesAndChannel_whenChannelExists() {
      // given
      Channel existingChannel = publicChannelInit("channelName");
      UUID channelId = existingChannel.getId();

      given(channelRepository.findById(channelId)).willReturn(Optional.of(existingChannel));

      // when
      channelService.delete(channelId);

      // then
      then(messageRepository).should().deleteAllByChannel_Id(channelId);
      then(readStatusRepository).should().deleteAllByChannel_Id(channelId);
      then(channelRepository).should().delete(existingChannel);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 채널 삭제 시 ChannelNotFoundException 발생")
    void delete_shouldThrowChannelNotFoundException_whenChannelDoesNotExist() {
      // given
      UUID unknownId = UUID.randomUUID();

      given(channelRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.delete(unknownId))
          .isInstanceOf(ChannelNotFoundException.class);

      then(channelRepository).should(never()).delete(any());
    }
  }

  // === findAllByUserId() ===
  @Nested
  @DisplayName("findAllByUserId()")
  class FindAllByUserId {

    @Test
    @DisplayName("성공 - PUBLIC 전체 + 본인이 속한 PRIVATE 채널 목록 반환")
    void findAllByUserId_shouldReturnPublicAndAccessiblePrivateChannels_whenUserHasAccess() {
      // given
      UUID userId = UUID.randomUUID();

      Channel publicChannel = publicChannelInit("Public Channel");
      Channel privateChannel = privateChannelInit();

      ReadStatus readStatus = new ReadStatus(userInit("user1"), privateChannel, Instant.now());

      List<Channel> accessibleChannels = List.of(publicChannel, privateChannel);
      List<ChannelDto> expectedDtos = List.of(
          publicDtoFrom(publicChannel),
          privateDtoFrom(privateChannel)
      );

      given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of(readStatus));
      given(channelRepository.findAllByAccessible(anySet())).willReturn(accessibleChannels);
      given(channelMapper.toDtoList(accessibleChannels)).willReturn(expectedDtos);

      // when
      List<ChannelDto> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).hasSize(2);
      assertThat(result).extracting(ChannelDto::type)
          .containsExactlyInAnyOrder(ChannelType.PUBLIC, ChannelType.PRIVATE);
    }

    // TODO: +a?
  }
}
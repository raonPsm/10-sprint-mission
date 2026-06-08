package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.MessageDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private MessageMapper messageMapper;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private PageResponseMapper pageResponseMapper;

  @InjectMocks
  private BasicMessageService messageService;

  private Channel channelInit() {
    Channel channel = Channel.createPublicChannel("testChannel", "description");
    ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());
    return channel;
  }

  private User userInit(String username) {
    User user = new User(username, username + "@example.com", "password1", null);
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }

  private Message messageInit(String content, Channel channel, User author) {
    Message message = new Message(content, channel, author, List.of());
    ReflectionTestUtils.setField(message, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(message, "createdAt", Instant.now());
    return message;
  }

  private MessageDto dtoFrom(Message message) {
    return new MessageDto(
        message.getId(),
        message.getCreatedAt(),
        null,
        message.getContent(),
        message.getChannel().getId(),
        null,
        List.of()
    );
  }
}
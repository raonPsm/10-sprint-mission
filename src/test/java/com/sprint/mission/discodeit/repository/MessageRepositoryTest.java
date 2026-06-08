package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaAuditingTestConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingTestConfig.class)
public class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private TestEntityManager em;

  // === Helper Method ===
  private User persistUserWithStatus(String username) {
    User user = new User(username, username + "@example.com", "password", null);
    em.persist(user);
    em.persist(new UserStatus(user, Instant.now()));
    em.flush();
    em.clear();
    return user;
  }

  private Channel persistPublicChannel(String name) {
    Channel channel = Channel.createPublicChannel(name, "Public Channel");
    em.persist(channel);
    em.flush();
    em.clear();
    return channel;
  }

  private Message persistMessage(String content, Channel channel, User author) {
    Message message = new Message(content, channel, author, List.of());
    em.persist(message);
    em.flush();
    em.clear();
    return message;
  }

  // findAllByChannel_IdWithAuthor
  @Nested
  @DisplayName("findAllByChannel_IdWithAuthor")
  class findAllByChannel_IdWithAuthor {

    @Test
    @DisplayName("성공 - 작성자 정보를 함께 로딩하여 반환")
    void findAllWithAuthor_returnMessagesWithAuthor() {
      // given
      User author = persistUserWithStatus("author");
      Channel channel = persistPublicChannel("publicChannel");
      persistMessage("message1", channel, author);

      // when - 미래 시각을 커서로 사용하여 채널의 모든 메시지를 조회
      Instant futureCursor = Instant.now().plusSeconds(10);
      Slice<Message> result = messageRepository.findAllByChannel_IdWithAuthor(
          channel.getId(), futureCursor, PageRequest.of(0, 10)
      );

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getAuthor().getUsername()).isEqualTo("author");
    }

    @Test
    @DisplayName("실패 - 채널에 메시지가 없으면 빈 Slice 반환")
    void findAllWithAuthor_emptyChannel_returnEmpty() {
      // given
      Channel channel = persistPublicChannel("empty-publicChannel");

      // when
      Instant futureCursor = Instant.now().plusSeconds(10);
      Slice<Message> result = messageRepository.findAllByChannel_IdWithAuthor(
          channel.getId(), futureCursor, PageRequest.of(0, 10)
      );

      // then
      assertThat(result.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("findTopByChannel_IdOrderByCreatedAtDesc")
  class findTopByChannel_IdOrderByCreatedAtDesc {

    @Test
    @DisplayName("성공 - 채널의 가장 최신 메시지 1개 반환")
    void findTopByChannel_returnsLatestMessage() {
      // given
      User author = persistUserWithStatus("author");
      Channel channel = persistPublicChannel("publicChannel");

      persistMessage("firstMessage", channel, author);
      persistMessage("latestMessage", channel, author);

      // when
      Optional<Message> result
          = messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(channel.getId());

      // then
      assertThat(result).isPresent();
      assertThat(result.get().getContent()).isEqualTo("latestMessage");
    }

    @Test
    @DisplayName("실패 - 메시지가 없는 채널은 empty 반환")
    void findTopByChannelId_noMessages_returnsEmpty() {
      // given
      Channel channel = persistPublicChannel("empty-publicChannel");

      // when
      Optional<Message> result
          = messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(channel.getId());

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("deleteAllByChannel_Id")
  class deleteAllByChannel_Id {

    @Test
    @DisplayName("성공 - 채널의 모든 메시지 삭제")
    void deleteAllByChannelId_removesAllMessages() {
      // given
      User author = persistUserWithStatus("delete-author");
      Channel channel = persistPublicChannel("delete-publicChannel");

      persistMessage("Message1", channel, author);
      persistMessage("Message2", channel, author);

      // when
      messageRepository.deleteAllByChannel_Id(channel.getId());
      em.flush();
      em.clear();

      // then
      Slice<Message> result = messageRepository.findAllByChannel_IdWithAuthor(
          channel.getId(), Instant.now().plusSeconds(10), PageRequest.of(0, 10)
      );
      assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("성공 - 다른 채널의 메시지는 영향 없음")
    void deleteAllByChannelId_doesNotAffectOtherChannels() {
      // given
      User author = persistUserWithStatus("delete-author");
      Channel targetChannel = persistPublicChannel("target-publicChannel");
      Channel otherChannel = persistPublicChannel("other-publicChannel");

      persistMessage("Message-will-deleted", targetChannel, author);
      persistMessage("Message-will-maintained", otherChannel, author);
      // when
      messageRepository.deleteAllByChannel_Id(targetChannel.getId());
      em.flush();
      em.clear();

      // then
      Slice<Message> otherResult = messageRepository.findAllByChannel_IdWithAuthor(
          otherChannel.getId(), Instant.now().plusSeconds(10), PageRequest.of(0, 10)
      );

      assertThat(otherResult.getContent()).hasSize(1);
      assertThat(otherResult.getContent().get(0).getContent()).isEqualTo("Message-will-maintained");
    }
  }
}

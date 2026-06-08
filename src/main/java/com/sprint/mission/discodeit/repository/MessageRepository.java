package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    // save(Message entity);
    // findById(UUID id);
    // delete(Message entity);
    // deleteAllByChannel_Id(UUID channelId);

    // 특정 채널의 최신 메시지 페이지 1개를 가져옴 -> 첫 페이지 조회에 사용
    // JPQL -> SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC
    // SQL  -> SELECT m.* FROM messages m WHERE m.channel_id = ? ORDER BY m.created_at DESC LIMIT ? OFFSET ?
    // Slice는 hasNext 판단 하에 내부적으로 size+1 조회 전략을 사용할 수 있다.
    Slice<Message> findAllByChannel_IdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    // createdAt 보다 오래된 메시지들만 다음 페이지로 가져옴 -> 커서 기반 더보기
    // JPQL -> SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC
    // SQL  -> SELECT m.* FROM messages m WHERE m.channel_id = ? ORDER BY m.created_at DESC LIMIT ? OFFSET ?
    Slice<Message> findAllByChannel_IdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID channelId, Instant createdAt, Pageable pageable);

    // 특정 채널의 createdAt 커서 이전 메시지를 최신순으로 슬라이스 조회하면서, author, userStatus, profile을 한 번에 조회
    @Query("SELECT m FROM Message m "
            + "LEFT JOIN FETCH m.author a "
            + "LEFT JOIN FETCH a.userStatus "
            + "LEFT JOIN FETCH a.profile "
            + "WHERE m.channel.id = :channelId AND m.createdAt < :createdAt "
            + "ORDER BY m.createdAt DESC"
    )
    Slice<Message> findAllByChannel_IdWithAuthor(@Param("channelId") UUID channelId,
                                                @Param("createdAt") Instant createdAt,
                                                Pageable pageable);

    // 채널의 최신 메시지 1개 조회
    // JPQL -> SELECT m FROM Message M WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC + setMaxResults(1)
    // SQL  -> SELECT m.* FROM message m WHERE m.channel_id = ? ORDER BY m.created_at DESC LIMIT 1;
    Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(UUID channelId);

    void deleteAllByChannel_Id(UUID channelId);
}

/**
 * size+1 조회 전략 -> 사용자가 요청한 데이터 개수보다 하나 더 조회해서 다음 페이지의 존재 여부를 확인
 *
 * 페이징 처리에서 중요한 정보 중 하나는 다음 페이지가 존재하는지 여부(hasNext)이다. 이를 확인하는 방법은 2가지가 있다.
 *   전체 카운트 쿼리 실행 (Page 방식): 전체 데이터 개수를 세어서 현재 위치와 비교. 데이터가 많을수록 COUNT 쿼리가 매우 느려짐
 *   하나 더 가져오기 (Slice 방식): 10개 요청 시 DB에서 11개를 가져와서 확인. 11번째 데이터가 존재한다면 뒤에 데이터가 더 존재한다고 판단.
 */
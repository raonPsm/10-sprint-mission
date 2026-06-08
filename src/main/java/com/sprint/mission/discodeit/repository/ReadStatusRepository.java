package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    // ReadStatus save(ReadStatus entity);
    // List<ReadStatus> saveAll(Iterable<ReadStatus> entities);
    // Optional<ReadStatus> findById(UUID id);
    // void delete(ReadStatus entity;

    // 특정 유저의 ReadStatus 목록 조회
    // JPQL -> SELECT rs FROM ReadStatus rs WHERE rs.user.id = :userId
    // SQL  -> SELECT rs.* FROM read_statuses rs WHERE user_id = ?
    List<ReadStatus> findAllByUserId(UUID userId);

    // 채널 삭제 시 해당 채널의 ReadStatus 일괄 삭제
    // JPQL -> DELETE FROM ReadStatus rs WHERE rs.channel.id = :channelId
    // SQL  -> DELETE FROM read_statuses WHERE channel_id = ?
    void deleteAllByChannel_Id(UUID channelId);

    // (유저, 채널) -> ReadStatus 존재 여부 확인
    // JQPL -> SELECT COUNT(rs) > 0 FROM ReadStatus rs WHERE rs.user.id = :userId AND rs.channel.id = :channelId
    // SQL  -> SELECT COUNT(*) > 0 FROM read_statuses WHERE user_id = ? AND channel_id = ?
    boolean existsByUser_IdAndChannel_Id(UUID userId, UUID channelId);

    // 특정 채널의 ReadStatus 목록 조회
    // JPQL -> SELECT rs FROM ReadStatus rs WHERE rs.channel.id = :channelId
    // SQL  -> SELECT rs.* FROM read_statuses rs WHERE channel_id = ?
    List<ReadStatus> findAllByChannel_Id(UUID channelId);
}

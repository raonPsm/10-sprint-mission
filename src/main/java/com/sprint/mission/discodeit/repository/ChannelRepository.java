package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    boolean existsByName(String channelName);

    // PUBLIC + 내가 속한 PRIVATE 채널 조회
    // JPQL 사용
    @Query("SELECT c " +
            "FROM Channel c " +
            "WHERE c.type  = 'PUBLIC' OR c.id IN :accessiblePrivateChannelIds")
    // @Param : 메서드 파라미터 <-> 쿼리문 변수 매핑 역할
    List<Channel> findAllByAccessible(@Param("accessiblePrivateChannelIds")Set<UUID> accessiblePrivateChannelIds);

    // 동일 참여자 구성의 비공개 채널 조회
    @Query("SELECT rs.channel FROM ReadStatus rs " +
            "WHERE rs.channel.type = 'PRIVATE' " + // PRIVATE 채널에서
            "GROUP BY rs.channel " + // 채널 단위로 그룹화
            "HAVING COUNT(rs.user.id) = :size " + // 채널에 속한 전체 유저의 수가 :size랑 같고
            "AND SUM(CASE WHEN rs.user.id IN :participantIds THEN 1 ELSE 0 END) = :size")
            // 채널에 속한 유저들을 하나씩 순회하며, 그 유저의 ID가 :participantIds에 포함되어 있으면 1, 아니면 0을 더해서 그 합이 :size랑 같아야 함
    Optional<Channel> findPrivateChannelByParticipantsIds(Set<UUID> participantIds);
}

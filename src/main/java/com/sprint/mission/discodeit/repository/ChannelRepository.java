package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ChannelRepository {
    Channel save(Channel channel);
    Optional<Channel> findById(UUID id);
    List<Channel> findAll();
    List<Channel> findAllByAccessible(UUID userId, Set<UUID> accessiblePrivateChannelIds);
    boolean existsById(UUID id);
    void deleteById(UUID id);

    Optional<Channel> findPrivateChannelByParticipantsIds(Set<UUID> participantIds);
    boolean existsByName(String channelName);
}

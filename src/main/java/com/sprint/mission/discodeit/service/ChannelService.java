package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.Dto.ChannelDto;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    // PUBLIC 채널 생성
    ChannelDto create(PublicChannelCreateRequest request);
    // PRIVATE 채널 생성
    ChannelDto create(PrivateChannelCreateRequest request);

    // 단건 조회
    ChannelDto findByChannelId(UUID channelId);
    // 특정 유저가 볼 수 있는 Channel 목록을 조회 - 유저별 채널 목록 조회 (PUBLIC 전체 + 본인이 속한 PRIVATE)
    List<ChannelDto> findAllByUserId(UUID userId);

    // 채널 수정r
    ChannelDto update(UUID channelId, PublicChannelUpdateRequest request);

    // 채널 삭제
    void delete(UUID channelId);
}

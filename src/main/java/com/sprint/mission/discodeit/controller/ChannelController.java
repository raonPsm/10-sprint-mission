package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.requestRespose.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController implements ChannelApi {
    private final ChannelService channelService;
    private final ChannelMapper channelMapper;

    /// POST /api/channels/public - Public Channel 생성
    @PostMapping("/public")
    public ResponseEntity<ChannelResponse> createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
        Channel createdPublicChannel = channelService.create(request);
        ChannelResponse response = channelMapper.toResponse(createdPublicChannel);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /// POST /api/channels/private - Private Channel 생성
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
        Channel createdPrivateChannel = channelService.create(request);
        ChannelResponse response = channelMapper.toResponse(createdPrivateChannel);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /// DELETE /api/channels/{channelId} - Channel 삭제
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

    /// PATCH /api/channels/{channelId} - Channel 정보 수정
    // 공개 채널의 정보 수정 (비공개 채널의 정보는 수정 불가능)
    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> update(
            @PathVariable UUID channelId,
            @RequestBody PublicChannelUpdateRequest request
    ) {
        Channel updatedChannel = channelService.update(channelId, request);
        ChannelResponse response = channelMapper.toResponse(updatedChannel);
        return ResponseEntity.ok(response);
    }

    /// GET /api/channels - User가 참여 중인 Channel 목록 조회
    @GetMapping
    public ResponseEntity<List<ChannelResponse>> findAllByUserId(@RequestParam UUID userId) {
        List<Channel> channels = channelService.findAllByUserId(userId);
        List<ChannelResponse> responses = channelMapper.toListResponse(channels);
        return ResponseEntity.ok(responses);
    }
}
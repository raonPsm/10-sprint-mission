package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Repeatable;
import java.util.List;
import java.util.UUID;

@RestController
// channel -> channels
@RequestMapping("/api/channels")
public class ChannelController {
    private final ChannelService channelService;

    @Autowired
    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    /// POST /api/channels/public - Public Channel 생성
    @PostMapping("/public")
    public ResponseEntity<ChannelResponse> createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
        ChannelResponse createdPublicChannel = channelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPublicChannel);
    }

    /// POST /api/channels/private - Private Channel 생성
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
        ChannelResponse createdPrivateChannel = channelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPrivateChannel);
    }

    /// DELETE /api/channels/{channelId} - Channel 삭제
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

    // 공개 채널의 정보를 수정할 수 있다.
    @PatchMapping(value = "/{channelId}")
    public ResponseEntity<ChannelResponse> update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest request
    ) {
        ChannelResponse response = channelService.update(channelId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    // 특정 사용자가 볼 수 있는 모든 채널 목록을 조회할 수 있다.
    @GetMapping()
    public ResponseEntity<List<ChannelResponse>> findAllByUserId(@RequestParam UUID userId) {
        List<ChannelResponse> responses = channelService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}

package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    /// POST /api/channels/public - Public Channel 생성
    @PostMapping("/public")
    public ResponseEntity<ChannelResponse> createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
        ChannelResponse createdPublicChannel = channelService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdPublicChannel);
    }

    /// POST /api/channels/private - Private Channel 생성
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
        ChannelResponse createdPrivateChannel = channelService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdPrivateChannel);
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
        ChannelResponse updatedChannel = channelService.update(channelId, request);
        return ResponseEntity.ok().body(updatedChannel);
    }


    /// GET /api/channels - User가 참여 중인 Channel 목록 조회
    // FIXME: ChannelDto -> ChannelResponse로 통일
    @GetMapping
    public ResponseEntity<List<ChannelDto>> findAllByUserId(@RequestParam UUID userId) {
        List<ChannelDto> channelListResponse = channelService.findAllByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(channelListResponse);
    }
}

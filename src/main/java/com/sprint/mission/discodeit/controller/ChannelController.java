package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.Dto.ChannelDto;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  /// POST /api/channels/public - Public Channel 생성
  @PostMapping("/public")
  public ResponseEntity<ChannelDto> createPublicChannel(
      @Valid @RequestBody PublicChannelCreateRequest request) {
    ChannelDto createdPublicChannel = channelService.create(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdPublicChannel);
  }

  /// POST /api/channels/private - Private Channel 생성
  @PostMapping("/private")
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @Valid @RequestBody PrivateChannelCreateRequest request) {
    ChannelDto createdPrivateChannel = channelService.create(request);
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
  public ResponseEntity<ChannelDto> update(
      @PathVariable UUID channelId,
      @Valid @RequestBody PublicChannelUpdateRequest request
  ) {
    ChannelDto updatedChannel = channelService.update(channelId, request);
    return ResponseEntity.ok(updatedChannel);
  }

  /// GET /api/channels - User가 참여 중인 Channel 목록 조회
  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAllByUserId(@RequestParam UUID userId) {
    List<ChannelDto> channels = channelService.findAllByUserId(userId);
    return ResponseEntity.ok(channels);
  }
}
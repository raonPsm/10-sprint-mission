package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.Dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
@Slf4j
public class ReadStatusController implements ReadStatusApi {

  private final ReadStatusService readStatusService;

  /// GET /api/readStatuses - User의 Message 읽음 상태 목록 조회
  @GetMapping
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam UUID userId) {
    log.debug("[READ_STATUS_FIND] 읽음 상태 목록 조회 API 요청: userId={}", userId);

    List<ReadStatusDto> readStatuses = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(readStatuses);
  }

  /// POST /api/readStatuses - Message 읽음 상태 생성
  @PostMapping
  public ResponseEntity<ReadStatusDto> create(@Valid @RequestBody ReadStatusCreateRequest request) {
    log.debug("[READ_STATUS_CREATE] 읽음 상태 생성 API 요청: userId={}, channelId={}",
        request.userId(), request.channelId()
    );

    ReadStatusDto readStatus = readStatusService.create(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(readStatus);
  }

  /// PATCH /api/readStatuses/{readStatusId} - Message 읽음 상태 수정
  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto> update(
      @PathVariable UUID readStatusId,
      @Valid @RequestBody ReadStatusUpdateRequest request
  ) {
    log.debug("[READ_STATUS_UPDATE] 읽음 상태 수정 API 요청: readStatusId={}", readStatusId);

    ReadStatusDto readStatus = readStatusService.update(readStatusId, request);

    return ResponseEntity.ok(readStatus);
  }
}

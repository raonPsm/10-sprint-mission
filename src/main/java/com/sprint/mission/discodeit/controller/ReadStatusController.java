package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController implements ReadStatusApi {
    private final ReadStatusService readStatusService;
    private final ReadStatusMapper readStatusMapper;

    /// GET /api/readStatuses - User의 Message 읽음 상태 목록 조회
    @GetMapping
    public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(@RequestParam UUID userId) {
        List<ReadStatus> readStatuses = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatusMapper.toListResponse(readStatuses));
    }

    /// POST /api/readStatuses - Message 읽음 상태 생성
    @PostMapping
    public ResponseEntity<ReadStatusResponse> create(@RequestBody ReadStatusCreateRequest request) {
        ReadStatus readStatus = readStatusService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(readStatusMapper.toResponse(readStatus));
    }

    /// PATCH /api/readStatuses/{readStatusId} - Message 읽음 상태 수정
    @PatchMapping("/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> update(@PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request) {
        ReadStatus readStatus = readStatusService.update(readStatusId, request);
        return ResponseEntity.ok(readStatusMapper.toResponse(readStatus));
    }
}

package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.Dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusUpdateRequest;
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
    public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam UUID userId) {
        List<ReadStatusDto> readStatuses = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatuses);
    }

    /// POST /api/readStatuses - Message 읽음 상태 생성
    @PostMapping
    public ResponseEntity<ReadStatusDto> create(@RequestBody ReadStatusCreateRequest request) {
        ReadStatusDto readStatus = readStatusService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(readStatus);
    }

    /// PATCH /api/readStatuses/{readStatusId} - Message 읽음 상태 수정
    @PatchMapping("/{readStatusId}")
    public ResponseEntity<ReadStatusDto> update(@PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request) {
        ReadStatusDto readStatus = readStatusService.update(readStatusId, request);
        return ResponseEntity.ok(readStatus);
    }
}

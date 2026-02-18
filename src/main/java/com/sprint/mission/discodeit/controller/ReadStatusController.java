package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
public class ReadStatusController implements ReadStatusApi {
    private final ReadStatusService readStatusService;

    @Autowired
    public ReadStatusController(ReadStatusService readStatusService) {
        this.readStatusService = readStatusService;
    }

    /// GET /api/readStatuses - User의 Message 읽음 상태 목록 조회
    @GetMapping
    public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(@RequestParam UUID userId) {
        List<ReadStatusResponse> readStatusListResponse = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatusListResponse);
    }

    /// POST /api/readStatuses - Message 읽음 상태 생성
    @PostMapping
    public ResponseEntity<ReadStatusResponse> create(@RequestBody ReadStatusCreateRequest request) {
        ReadStatusResponse response = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// PATCH /api/readStatuses/{readStatusId} - Message 읽음 상태 수정
    @PatchMapping("{readStatusId}")
    public ResponseEntity<ReadStatusResponse> update(@PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request) {
        ReadStatusResponse response = readStatusService.update(readStatusId, request);
        return ResponseEntity.ok(response);
    }
}

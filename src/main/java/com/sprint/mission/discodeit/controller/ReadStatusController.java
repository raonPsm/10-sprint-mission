package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    @Autowired
    public ReadStatusController(ReadStatusService readStatusService) {
        this.readStatusService = readStatusService;
    }

    // 특정 채널의 메시지 수신 정보를 생성할 수 있다.
    @PostMapping()
    public ResponseEntity<ReadStatusResponse> create(@RequestBody ReadStatusCreateRequest request) {
        ReadStatusResponse response = readStatusService.create(request);
        return ResponseEntity.ok(response);
    }

    // 특정 채널의 메시지 수신 정보를 수정할 수 있다.
    @PatchMapping(value = "/{readStatusId}")
    public ResponseEntity<ReadStatusResponse> update(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request
    ) {
        // 서비스 메서드 호출 시 request 객체 전달 필요 (서비스 로직 확인 필요)
        ReadStatusResponse response = readStatusService.update(readStatusId, request);
        return ResponseEntity.ok(response);
    }

    // User의 Message 읽음 상태 목록 조회
    @GetMapping
    public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(@PathVariable UUID userId) {
        List<ReadStatusResponse> response = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(response);
    }
}

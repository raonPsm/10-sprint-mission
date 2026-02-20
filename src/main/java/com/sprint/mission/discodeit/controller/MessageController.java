package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// FIXME: 서비스 계층에서는 entity return하고 컨트롤러에서 DTO로 변환하는 방식으로 리펙토링 (+MapStruct 라이브러리 사용 고려
@RestController
@RequestMapping("/api/messages")
public class MessageController implements MessageApi {
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    // TODO: @RequiredArgsConstructor로 리펙토링 고려

    /// GET /api/messages - Channel의 Message 목록 조회
    @GetMapping
    public ResponseEntity<List<MessageResponse>> findAllByChannelId(@RequestParam UUID channelId) {
        List<MessageResponse> messageListResponse = messageService.findAllByChannelId(channelId);
        return ResponseEntity.ok(messageListResponse);
    }

    /// POST /api/messages - Message 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> create(
            @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        List<BinaryContentCreateRequest> fileRequests = Optional.ofNullable(attachments)
                .map(files -> files.stream()
                        .map(file -> {
                            try {
                                return new BinaryContentCreateRequest(
                                        file.getOriginalFilename(),
                                        file.getContentType(),
                                        file.getBytes()
                                );
                            } catch (IOException e) {
                                throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
                            }
                        })
                        .toList())
                .orElse(new ArrayList<>());
        MessageResponse response = messageService.create(messageCreateRequest, fileRequests);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /// DELETE /api/messages/{messageId} - Message 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    /// PATCH /api/messages/{messageId} - Message 내용 수정
    @PatchMapping(value = "/{messageId}")
    public ResponseEntity<MessageResponse> update(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest request
    ) {
        MessageResponse response = messageService.update(messageId, request);
        return ResponseEntity.ok(response);
    }
}
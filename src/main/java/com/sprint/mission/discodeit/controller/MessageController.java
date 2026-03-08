package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.Dto.MessageDto;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
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

// FIXME: 메서드 내부 기타 부수효과 문제 확인 후 수정
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController implements MessageApi {
    private final MessageService messageService;
    private final MessageMapper messageMapper;

    /// GET /api/messages - Channel의 Message 목록 조회
    @GetMapping
    public ResponseEntity<List<MessageDto>> findAllByChannelId(@RequestParam UUID channelId) {
        List<MessageDto> responses = messageService.findAllByChannelId(channelId).stream()
                .map(messageMapper::toDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /// POST /api/messages - Message 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDto> create(
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
        Message message = messageService.create(messageCreateRequest, fileRequests);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(messageMapper.toDto(message));
    }

    /// DELETE /api/messages/{messageId} - Message 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    /// PATCH /api/messages/{messageId} - Message 내용 수정
    @PatchMapping(value = "/{messageId}")
    public ResponseEntity<MessageDto> update(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest request
    ) {
        Message message = messageService.update(messageId, request);
        return ResponseEntity.ok(messageMapper.toDto(message));
    }
}
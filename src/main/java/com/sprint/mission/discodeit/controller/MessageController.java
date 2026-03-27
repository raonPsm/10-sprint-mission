package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.Dto.MessageDto;
import com.sprint.mission.discodeit.dto.requestRespose.PageResponse;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// FIXME: 메서드 내부 기타 부수효과 문제 확인 후 수정
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController implements MessageApi {

  private final MessageService messageService;

  /// GET /api/messages - Channel의 Message 목록 조회
  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @RequestParam("channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor,
      @PageableDefault(
          size = 50, // 한 번에 50개 -> 명세
          page = 0, // 0 페이지
          sort = "createdAt", // createdAt 기준으로
          direction = Sort.Direction.DESC // 내림차순 정렬
      ) Pageable pageable
  ) {
    log.debug("[MESSAGE_FIND_BY_CHANNEL] Channel의 Message 목록 조회 API 요청: channelId={}, cursor={}",
        channelId,
        cursor
    );

    PageResponse<MessageDto> messages = messageService.findAllByChannelId(
        channelId, cursor, pageable
    );
    return ResponseEntity.ok(messages);
  }

  /// POST /api/messages - Message 생성
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto> create(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    log.info("[MESSAGE_CREATE] Message 생성 API 요청: channelId={}, authorId={}, attachmentCount={}",
        messageCreateRequest.channelId(),
        messageCreateRequest.authorId(),
        attachments == null ? 0 : attachments.size()
    );

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
    MessageDto message = messageService.create(messageCreateRequest, fileRequests);

    log.info("[MESSAGE_CREATE] Message 생성 API 응답: messageId={}", message.id());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(message);
  }

  /// DELETE /api/messages/{messageId} - Message 삭제
  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    log.info("[MESSAGE_DELETE] Message 삭제 API 요청: messageId={}", messageId);

    messageService.delete(messageId);

    log.info("[MESSAGE_DELETE] Message 삭제 API 응답: messageId={}", messageId);
    return ResponseEntity.noContent().build();
  }

  /// PATCH /api/messages/{messageId} - Message 내용 수정
  @PatchMapping(value = "/{messageId}")
  public ResponseEntity<MessageDto> update(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest request
  ) {
    log.info("[MESSAGE_UPDATE] Message 내용 수정 API 요청: messageId={}", messageId);

    MessageDto message = messageService.update(messageId, request);

    log.info("[MESSAGE_UPDATE] Message 내용 수정 API 응답: messageId={}", messageId);
    return ResponseEntity.ok(message);
  }
}
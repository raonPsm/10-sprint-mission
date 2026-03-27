package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.Dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
@Slf4j
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  // GET /api/binaryContents - 여러 첨부 파일 조회
  @GetMapping
  public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds
  ) {
    log.info("[FILE_LIST_FIND] 여러 첨부 파일 조회 API 요청: count={}", binaryContentIds.size());

    List<BinaryContentDto> binaryContents = binaryContentService.findAllByIdIn(binaryContentIds);
    return ResponseEntity.ok(binaryContents);
  }

  // GET /api/binaryContents/{binaryContentId} - 첨부 파일 조회
  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentDto> find(@PathVariable UUID binaryContentId) {
    log.info("[FILE_FIND] 첨부 파일 조회 API 요청: binaryContentId={}", binaryContentId);

    BinaryContentDto binaryContent = binaryContentService.find(binaryContentId);
    return ResponseEntity.ok(binaryContent);
  }

  // GET /api/{binaryContentId}/download - 파일 다운로드
  @GetMapping("{binaryContentId}/download")
  public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
    log.info("[FILE_DOWNLOAD] 파일 다운로드 API 요청: binaryContentId={}", binaryContentId);

    BinaryContentDto binaryContentDto = binaryContentService.find(binaryContentId);
    return binaryContentStorage.download(binaryContentDto);
  }
}
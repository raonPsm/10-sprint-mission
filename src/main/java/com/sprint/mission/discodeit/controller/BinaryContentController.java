package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.Dto.BinaryContentDto;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentApi {
    private final BinaryContentService binaryContentService;
    private final BinaryContentMapper binaryContentMapper;

    // GET /api/binaryContents - 여러 첨부 파일 조회
    @GetMapping
    public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
            @RequestParam("binaryContentIds") List<UUID> binaryContentIds
    ) {
        List<BinaryContentDto> binaryContents = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.ok(binaryContents);
    }

    // GET /api/binaryContents/{binaryContentId} - 첨부 파일 조회
    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContentDto> find(@PathVariable UUID binaryContentId) {
        BinaryContentDto binaryContent = binaryContentService.find(binaryContentId);
        return ResponseEntity.ok(binaryContent);
    }
}
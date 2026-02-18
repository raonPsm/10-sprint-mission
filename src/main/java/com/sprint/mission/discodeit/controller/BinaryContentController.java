package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentIdsRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
// Kebab case 로 변경
@RequestMapping("/api/binaryContents")
public class BinaryContentController implements BinaryContentApi {
    private final BinaryContentService binaryContentService;

    @Autowired
    public BinaryContentController(BinaryContentService binaryContentService) {
        this.binaryContentService = binaryContentService;
    }

    // GET /api/binaryContents - 여러 첨부 파일 조회
    @GetMapping
    public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
            @RequestParam("binaryContentIds") List<UUID> binaryContentIds
    ) {
        List<BinaryContentResponse> binaryContents = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.ok(binaryContents);
    }

    // GET /api/binaryContents/{binaryContentId} - 첨부 파일 조회
    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContentResponse> find(@PathVariable UUID binaryContentId) {
        BinaryContentResponse response = binaryContentService.find(binaryContentId);
        return ResponseEntity.ok(response);
    }
}

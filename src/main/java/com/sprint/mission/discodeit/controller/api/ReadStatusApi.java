package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.requestRespose.readstatus.ReadStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
public interface ReadStatusApi {

    /// GET /api/readStatuses - User의 Message 읽음 상태 목록 조회
    @Operation(summary = "User의 Message 읽음 상태 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message 읽음 상태 목록 조회 성공",
                    content = @Content(
                            mediaType = "*/*",
                            array = @ArraySchema(schema = @Schema(implementation = ReadStatusResponse.class))
                    )
            )
    })
    ResponseEntity<List<ReadStatusResponse>> findAllByUserId(
            @Parameter(description = "조회할 User ID", required = true) UUID userId
    );

    /// POST /api/readStatuses - Message 읽음 상태 생성
    @Operation(summary = "Message 읽음 상태 생성")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Message 읽음 상태가 성공적으로 생성됨",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = ReadStatusResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 읽음 상태가 존재함",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "ReadStatus with userId {userId} and channelId {channelId} already exists")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Channel 또는 User를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "Channel | User with id {channelId | userId} not found")
                    )
            )
    })
    ResponseEntity<ReadStatusResponse> create(
            @RequestBody ReadStatusCreateRequest request
    );

    /// PATCH /api/readStatuses/{readStatusId} - Message 읽음 상태 수정
    @Operation(summary = "Message 읽음 상태 수정")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message 읽음 상태가 성공적으로 수정됨",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = ReadStatusResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Message 읽음 상태를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "ReadStatus with id {readStatusId} not found")
                    )
            )
    })
    ResponseEntity<ReadStatusResponse> update(
            @Parameter(description = "수정할 읽음 상태 ID", required = true) UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request
    );
}
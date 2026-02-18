package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Message", description = "Message API")
public interface MessageApi {

    /// GET /api/messages - Channel의 Message 목록 조회
    @Operation(summary = "Channel의 Message 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message 목록 조회 성공",
                    content = @Content(
                            mediaType = "*/*",
                            array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))
                    )
            )
    })
    ResponseEntity<List<MessageResponse>> findAllByChannelId(
            @Parameter(description = "조회할 Channel ID", required = true) UUID channelId
    );

    /// POST /api/messages - Message 생성
    @Operation(summary = "Message 생성")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Message가 성공적으로 생성됨",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Channel 또는 User를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "Channel | Author with id {channelId | authorId} not found")
                    )
            )
    })
    ResponseEntity<MessageResponse> create(
            @Parameter(description = "Message 생성 정보", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,

            @Parameter(description = "Message 첨부 파일들", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    );

    /// DELETE /api/messages/{messageId} - Message 삭제
    @Operation(summary = "Message 삭제")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Message가 성공적으로 삭제됨"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Message를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "Message with id {messageId} not found")
                    )
            )
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 Message ID", required = true) UUID messageId
    );

    /// PATCH /api/messages/{messageId} - Message 내용 수정
    @Operation(summary = "Message 내용 수정")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message가 성공적으로 수정됨",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Message를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "Message with id {messageId} not found")
                    )
            )
    })
    ResponseEntity<MessageResponse> update(
            @Parameter(description = "수정할 Message ID", required = true) UUID messageId,
            @RequestBody MessageUpdateRequest request
    );
}

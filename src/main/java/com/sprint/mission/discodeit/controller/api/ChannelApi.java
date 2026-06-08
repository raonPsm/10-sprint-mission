package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Channel", description = "Channel API")
public interface ChannelApi {

    /// POST /api/channels/public - Public Channel 생성
    @Operation(summary = "Public Channel 생성")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Public Channel이 성공적으로 생성됨",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = ChannelResponse.class)
                    )
            )
    })
    ResponseEntity<ChannelResponse> createPublicChannel(
            @RequestBody PublicChannelCreateRequest request
    );
    
    /// POST /api/channels/private - Private Channel 생성
    @Operation(summary = "Private Channel 생성")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Private Channel이 성공적으로 생성됨",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = ChannelResponse.class)
                    )
            )
    })
    ResponseEntity<ChannelResponse> createPrivateChannel(
            @RequestBody PrivateChannelCreateRequest request
    );

    /// DELETE /api/channels/{channelId} - Channel 삭제
    @Operation(summary = "Channel 삭제")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Channel이 성공적으로 삭제됨",
                    content = @Content // 204는 Body가 없으므로 content 비워둠
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Channel을 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "Channel with id {channelId} not found")
                    )
            )
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 Channel ID") UUID channelId
    );

    /// PATCH /api/channels/{channelId} - Channel 정보 수정
    @Operation(summary = "Channel 정보 수정")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "404",
                    description = "Channel을 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "Channel with id {channelId} not found")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Private Channel은 수정할 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            examples = @ExampleObject(value = "Private channel cannot be updated")
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Channel 정보가 성공적으로 수정됨",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = ChannelResponse.class)
                    )
            )
    })
    ResponseEntity<ChannelResponse> update(
            @Parameter(description = "수정할 Channel ID") UUID channelId,
            @RequestBody PublicChannelUpdateRequest request
    );
    
    /// GET /api/channels - User가 참여 중인 Channel 목록 조회
    @Operation(summary = "User가 참여 중인 Channel 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Channel 목록 조회 성공",
                    content = @Content(
                            mediaType = "*/*",
                            array = @ArraySchema(schema = @Schema(implementation = ChannelResponse.class))
                    )
            )
    })
    ResponseEntity<List<ChannelResponse>> findAllByUserId(
            @Parameter(description = "조회할 User ID", required = true) UUID userId
    );
}

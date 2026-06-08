package com.sprint.mission.discodeit.dto.requestRespose.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MessageCreateRequest(
    @NotBlank(message = "메시지 내용은 필수입니다.")
    String content,

    @NotNull(message = "채널 ID는 필수입니다.")
    UUID channelId,

    @NotNull(message = "작성자 ID는 필수입니다.")
    UUID authorId
    // List<BinaryContentRequest> attachments
    // MultipartFile로 별도로 받으므로 제거
) {

}

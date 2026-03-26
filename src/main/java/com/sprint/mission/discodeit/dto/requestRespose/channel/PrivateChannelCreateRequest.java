package com.sprint.mission.discodeit.dto.requestRespose.channel;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record PrivateChannelCreateRequest(
    @NotNull(message = "참여자 목록은 필수입니다.")
    @NotEmpty(message = "참여자는 최소 1명 이상이어야 합니다.")
    Set<UUID> participantIds // 초대할 유저 id 목록
) {

}

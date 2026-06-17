package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;

// 파일 업로드 상태 변경 이벤트
public record BinaryContentStatusChangedEvent(
    BinaryContentDto binaryContent
) {

}

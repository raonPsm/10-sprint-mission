package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.data.NotificationDto;

// 새로운 알림 이벤트
public record NotificationCreatedEvent(
    NotificationDto notification
) {

}

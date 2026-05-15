package com.sprint.mission.discodeit.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// @ConfigurationProperties - 바인딩 실패 시 앱 시작 자체가 실패 (<-> @Value)
@ConfigurationProperties(prefix = "discodeit.admin")
public record AdminProperties(
    String username,
    String email,
    String password
) {

}

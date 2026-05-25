package com.sprint.mission.discodeit.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discodeit.jwt")
public record JwtProperties(
    String secretKey,
    long accessTokenExpiry,
    long refreshTokenExpiry
) {

}
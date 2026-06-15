package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.security.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/*
메시지 브로커 구성 - 구독/발행 prefix 지정 및 인메모리 브로커 활성화
STOMP 엔드포인트 등록 - 클라이언트가 WebSocket 연결을 맺는 진입점 정의
인바운드 채널 인터셉터 등록 - 수신 메시지에 대한 JWT 인증 및 SecurityContext 전파

클라이언트 발행 (/pub/***)
  -> 인바운드 채널 인터셉터(JWT 인증 -> SecurityContext 전파)
  -> @MessageMapping 핸들러
  -> SimpleBroker (/sub/***)
  -> 구독 중인 클라이언트들에게 전달
 */
@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 메시지 브로커를 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  // JWT 인증 처리를 담당하는 커스텀 STOMP 채널 인터셉터.
  // CONNECT 프레임 수신 시 헤더의 JWT 토큰을 검증하고 Principal을 세팅
  private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

  // 메시지 브로커 구성
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // 클라이언트가 구독할 때 사용하는 prefix
    config.enableSimpleBroker("/sub");
    // 클라이언트가 발행할 때 사용하는 prefix (@MessageMapping으로 라우팅됨)
    config.setApplicationDestinationPrefixes("/pub");
  }

  // 클라이언트가 WebSocket 연결을 맺는 STOMP 엔드포인트를 등록
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // /ws 경로를 STOMP 핸드셰이크 엔드포인트로 등록
    // 클라이언트는 이 엔드 포인트로 최초 HTTP 업그레이드 요청을 보내 WebSocket 연결을 수립한 뒤 STOMP 프로토콜로 통신 시작
    // .withSockJS(): WebSocket을 지원하지 않는 환경을 위한 SockJS 폴백 방식을 활성화
    registry.addEndpoint("/ws").withSockJS();
  }

  // 클라이언트 -> 서버 방향의 인바운드 채널에 인터셉터를 등록
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        stompAuthChannelInterceptor,
        // Spring Security 제공 SecurityContext 전파 인터셉터
        // 매 STOMP 프레임마다 메시지에 담긴 Principal을 꺼내 SecurityContextHolder에 Authentication으로 설정
        new SecurityContextChannelInterceptor(),
        // 전파된 인증 정보로 메시지 권한 검사 (인증 -> 전파 -> 인가 순서 보장 필수)
        authorizationChannelInterceptor());
  }

  // 모든 STOMP 메시지에 대해 ROLE_USER 권한을 요구하는 인가 인터셉터
  private AuthorizationChannelInterceptor authorizationChannelInterceptor() {
    return new AuthorizationChannelInterceptor(
        // 인가 규칙을 선언적으로 정의할 수 있는 매니저,
        // 내부적으로 등록된 규칙을 순서대로 순회하며 첫 번째로 매칭되는 규칙을 적용함
        MessageMatcherDelegatingAuthorizationManager.builder()
            .anyMessage().hasRole(UserRole.USER.name())
            .build());
  }
}

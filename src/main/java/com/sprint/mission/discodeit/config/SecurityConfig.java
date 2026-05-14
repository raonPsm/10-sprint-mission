package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import com.sprint.mission.discodeit.security.SpaCsrfTokenRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, LoginSuccessHandler loginSuccessHandler,
      LoginFailureHandler loginFailureHandler) throws Exception {
    http
        .csrf(csrf -> csrf
            // CookieCsrfTokenRepository -> 클라이언트 쿠기에 저장 (<-> HttpSessionCsrfTokenRepository - SSR 환경)
            // withHttpOnlyFalse() -> JS에서 document.cookie로 읽을 수 있게 허용
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // 들어오는 요청에서 CSRF 토큰을 어떻게 추출하고 검증할지 설정
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        // Spring Security 기본 제공 폼 로그인 기능을 활성화하고 설정을 커스터마이징
        .formLogin(login -> login
            // 클라이언트가 POST /api/auth/login으로 username, password를 전송하면 Spring Security가 가로채서 인증 처리
            .loginProcessingUrl("/api/auth/login") // 로그인 요청을 처리할 URL을 지정
            // CSR 방식에서는 JSON 응답을 해야 하므로 리다이렉트를 사용하지 않는다
            .successHandler(loginSuccessHandler) // 인증 성공 시 호출되는 핸들러
            .failureHandler(loginFailureHandler) // 인증 실패 시 호출되는 핸들러
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

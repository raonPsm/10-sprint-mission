package com.sprint.mission.discodeit.config;

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
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            // CookieCsrfTokenRepository -> 클라이언트 쿠기에 저장 (<-> HttpSessionCsrfTokenRepository - SSR 환경)
            // withHttpOnlyFalse() -> JS에서 document.cookie로 읽을 수 있게 허용
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // 들어오는 요청에서 CSRF 토큰을 어떻게 추출하고 검증할지 설정
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

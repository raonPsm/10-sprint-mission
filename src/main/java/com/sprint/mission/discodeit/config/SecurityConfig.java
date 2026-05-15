package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.LoginSuccessHandler;
import com.sprint.mission.discodeit.security.SpaCsrfTokenRequestHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@Configuration
@EnableMethodSecurity // 메서드 보안 활성화
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
        )
        // 어떤 요청을 허용하고 막을지 인가(Authorization) 규칙을 정의하는 설정
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                // GET /api/auth/csrf-token 허용
                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/auth/csrf-token"),
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/users"), // 회원가입
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/login"), // 로그인
                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/auth/logout"), // 로그아웃
                // /api/** 가 아닌 모든 요청 허용 (정적 리소스 등)
                new NegatedRequestMatcher(AntPathRequestMatcher.antMatcher("/api/**"))
            ).permitAll() // <- 위 조건들은 인증 없이 허용
            .anyRequest().authenticated() // <- 나머지는 모두 인증 필요
        )
        .exceptionHandling(ex -> ex
            // CSR 환경: 비인증 요청에 HTML redirect 대신 401 JSON 반환
            // authenticationEntryPoint - 인증되지 않은 사용자가 보호된 리소스에 접근할 때 발생하는 예외를 처리하는 인터페이스
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.setCharacterEncoding("UTF-8");
              response.getWriter().write("{\"message\":\"인증이 필요합니다.\"}");
            })
            // 인증은 완료되었으나 특정 리소스에 접근할 권한이 없는 사용자가 요청을 보냈을 때 발생하는 인가(Authorization) 예외를 처리하는 인터페이스
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              response.setContentType(MediaType.APPLICATION_JSON_VALUE);
              response.setCharacterEncoding("UTF-8");
              response.getWriter().write("{\"message\":\"접근 권한이 없습니다.\"}");
            })
        )
        .logout(logout -> logout
                // POST 요청을 이 URL로 받으면 Spring Security LogoutFilter가 처리
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler( // 기본값(200)을 204로 교체
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)
                )
            // 세션 무효화, SecurityContext 초기화 등 나머지 로그아웃 동작은 Spring Security 기본값 그대로 유지
            // <Spring Security 로그아웃 기본값>
            //  1. logoutUrl                -> POST /logout
            //  2. invalidateHttpSession    -> true  (세션 무효화)
            //  3. deleteCookies            -> (기본 없음, 명시해야 삭제)
            //  4. clearAuthentication      -> true  (SecurityContext에서 인증 제거)
            //  5. logoutSuccessUrl         -> /login?logout  (리다이렉트)
            //  6. logoutSuccessHandler     -> SimpleUrlLogoutSuccessHandler
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 역할 계층 구조를 정의하는 RoleHierarchy 빈을 등록
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix() // 'ROLE_' 접두사를 자동으로 붙여주는 빌더를 시작
        // ADMIN은 CHANNEL_MANAGER의 모든 권한을 자동으로 포함
        .role("ADMIN").implies("CHANNEL_MANAGER")
        // CHANNEL_MANAGER는 USER의 모든 권한을 자동으로 포함
        .role("CHANNEL_MANAGER").implies("USER")
        .build();
  }

  /*
  - RoleHierarchy를 Bean으로 등록해도 Method Security에는 자동으로 적용되지 않는다 -> 명시적 주입 필요
  - @EnableMethodSecurity가 동작할 때 필요하므로 static 메서드로 선언하여 SecurityConfig 인스턴스 생성 전에 실행 되도록 한다
  - Spring 컨텍스트 초기화 순서
    - @EnableMethodSecurity 처리 시작 <- 이때 MethodSecurityExpressionHandler가 필요함
    - SecurityConfig 인스턴스 생성
    - 일반 @Bean 메서드 실행
   */
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) { // Spring이 자동으로 주입해줌
    // Method Security용 표현식 핸들러 생성
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    // RoleHierarchy 주입 - @PreAuthorize에서 계층 구조 인식을 위해 필요함
    handler.setRoleHierarchy(roleHierarchy);
    // Bean으로 반환 -> Spring Security가 자동으로 감지해서 사용
    return handler;
  }
}

package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/*
BREACH 공격
- HTTPS 암호화 통신 내에 포함된 민감한 정보(세션 토큰, CSRF 토큰 등)를 유출하는 사이드 채널 공격(Side-channel attack)
- HTTP body의 데이터 압축 알고리즘(GZIP, DEFLATE) 특성을 악용
  - 본문에 동일한 문자열이 반복되면 압축 후의 전체 데이터 크기가 줄어드는 점을 이용
  - 공격자는 웹 페이지의 요청 파라미터(텍스트)를 계속 변경하며 주입한다.
    주입한 텍스트가 본문 속 실제 비밀키와 일치할수록 압축 결과 파일의 크기가 작아지므로
    패킷 크기의 변화를 관찰하면 한 글자씩 비밀키를 유추해 낸다. (brute-force)

- CSRF 토큰 값을 매 요청 마다 XOR 연산으로 난수화하여 고정된 비밀값이 노출되지 않도록 해 해당 공격을 방어할 수 있다.
 */
public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

  // 토큰을 있는 그대로 사용
  private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
  // 토큰을 XOR 마스킹해서 사용 (BREACH 공격 방어)
  private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

  // 응답 시 호출
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      Supplier<CsrfToken> csrfToken) {
    /*
     * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
     * the CsrfToken when it is rendered in the response body.
     */
    this.xor.handle(request, response, csrfToken); // XOR 마스킹 적용
    /*
     * Render the token value to a cookie by causing the deferred token to be loaded.
     */
    csrfToken.get(); // 쿠키에 토큰 강제 기록 (이렇게 안하면 지연 로딩 때문에 쿠키 발급이 안됨)
  }
  /*
  Spring Security 5.8부터 CsrfFilter가 토큰을 CsrfToken 객체가 아니라 Supplier<CsrfToken> 로 request attribute에 저장함
  (모든 요청마다 저장소(쿠키/세션)에서 토큰을 읽어오는 비용을 줄이기 위해서 변경됨)
  -> 이로 인해 지연 로딩이 되기 때문에 모든 요청에 대해서 .get()을 명시적 호출하여 쿠키 발급을해야 함
   */

  // 요청 검증 시 호출
  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    String headerValue = request.getHeader(csrfToken.getHeaderName()); // X-XSRF-TOKEN 헤더 확인
    /*
     * If the request contains a request header, use CsrfTokenRequestAttributeHandler
     * to resolve the CsrfToken. This applies when a single-page application includes
     * the header value automatically, which was obtained via a cookie containing the
     * raw CsrfToken.
     *
     * In all other cases (e.g. if the request contains a request parameter), use
     * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
     * when a server-side rendered form includes the _csrf request parameter as a
     * hidden input.
     */
    return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
        .resolveCsrfTokenValue(request, csrfToken);
    /*
    X-XSRF-TOKEN 헤더 있음 (CSR 요청) -> plain으로 검증 (CSR은 토큰을 헤더로 보내기 때문 - 헤더는 압축을 하지 않음)
    X-XSRF-TOKEN 헤더 없음 (SSR 요청) -> xor로 검증 (SSR은 토큰을 Body 안에 포함하여 보내기 때문 - xor로 방어 필요)
     */
  }
}



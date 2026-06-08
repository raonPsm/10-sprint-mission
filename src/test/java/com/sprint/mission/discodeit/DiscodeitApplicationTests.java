package com.sprint.mission.discodeit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// @SpringBootTest: 스프링 컨테이너를 실제로 띄워서, 애플리케이션 정상적으로 시작되는지 확인
@SpringBootTest
@ActiveProfiles("test")
class DiscodeitApplicationTests {

  @Test
  void contextLoads() { // 스프링 컨텍스트 문제 없이 로드되는지 검사
  }
  // 빈 등록 오류, 의존성 주입 실패, 설정 파일 문제... 등이 있으면 테스트 컨텍스트 로딩 중 실패
}

package com.sprint.mission.discodeit.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 테스트 전용 설정 클래스임을 선언
// @DataJpaTest 의 컴포넌트 스캔에 자동으로 잡히지 않음 -> 명시적으로 @Import해야 사용 가능
@TestConfiguration
// JPA에서 엔티티의 생성 및 수정 시점을 자동으로 기록해주는 Auditing 기능을 활성화하기 위한 어노테이션
// 이 어노테이션이 있어야 엔티티의 @CreatedDate, @LastModifiedDate가 자동으로 값이 채워짐
@EnableJpaAuditing
// @DataJpaTest 컴포넌트 스캔에 자동으로 잡히지 않으므로 -> 명시적으로 @Import해야 사용 가능
public class JpaAuditingTestConfig {

}

/*
TODO: 관련해서 더 조사 후 이해하기
별도의 설정 클래스로 분리하는 이유
@SpringBootApplicatio 에 @EnableJpaAuditing를 추가해주면 Mock 테스트 시, 문제가 발생할 수 있다.
@WebMvcTest(xxx.class) 의 Controller Test를 진행할 때 문제가 될 수 있다.
왜냐하면 모든 테스트는 Application 클래스가 항상 로드되면서 실행이 되는데,
Auditing 어노테이션이 등록되어 있으면 모든 테스트가 항상 JPA 관련된 빈을 필요로 하고 있는 상태가 되기 때문이다.
@WebMvcTest 어노테이션은 JPA 관련 빈들은 관련 빈들을 로드하지 않기 때문에 문제가 발생
 */
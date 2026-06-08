package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserCreateRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
통합테스트: 전체 ApplicationContext를 띄워 Controller → Service → Repository → DB까지 실제 빈이 연결된 상태에서 테스트
실제 운영 환경과 가장 유사한 조건에서 API의 전체 흐름을 검증할 수 있다.
 */

// 전체 ApplicationContext를 로드하되, 실제 HTTP 서버(Tomcat 등)을 띄우지 않고 MockMvc 기반의 모의 서블릿 환경을 사용
// 실제 서버를 띄우는 RANDOM_PORT 보다 빠르면서도 전체 빈 연동을 테스트할 수 있다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
// SpringBootTest는 기본적으로 MockMvc를 자동 구성하지 않으므로,
// 이 어노테이션을 추가하여 MockMvc 빈을 생성하고 주입받을 수 있게 한다.
@AutoConfigureMockMvc
// application-test-yml 설정을 사용하여 H2 인메모리 DB에 연결
@ActiveProfiles("test")
// 각 테스트 메서드가 끝날 때 자동으로 롤백하도록 설정
@Transactional
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  // === Helper Method ===

  // 객체를 JSON으로 직렬화하여 MockPart 형태로 반환
  private MockPart jsonPart(String name, Object body) throws Exception {
    // 전달받은 객체를 JSON 바이트 배열로 직렬화하여 MockPart 생성
    MockPart part = new MockPart(name, objectMapper.writeValueAsBytes(body));
    // 해당 파트의 Content-Type을 application/json으로 설정
    part.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return part;
  }

  // 사용자 생성 API를 호출하고 응답 본문을 문자열로 반환
  private String createUserAndGetResponse(String username, String email) throws Exception {
    // UserCreateRequest DTO 생성
    UserCreateRequest request = new UserCreateRequest(username, email, "password");
    // MockMvc를 통해 multipart POST 요청을 /api/users 엔드포인트로 전송
    return mockMvc.perform(multipart("/api/users")
        // 요청 객체를 JSON 파트로 변환하여 멀티파트 요청에 추가
            .part(jsonPart("userCreateRequest", request)))
        .andExpect(status().isCreated()) // HTTP 상태코드가 201 Created인지 검증
        .andReturn().getResponse().getContentAsString(); // 응답 본문을 문자열로 추출하여 반환
  }

  // 사용자를 생성하고 생성된 사용자의 UUID만 추출하여 반환
  private UUID createUserAndGetId(String username, String email) throws Exception {
    // 전달받은 객체를 JSON
    String response = createUserAndGetResponse(username, email);
    return UUID.fromString(objectMapper.readTree(response).get("userId").asText());
  }

  // === 사용자 생성 ===
  @Nested
  @DisplayName("POST /api/users - 사용자 생성")
  class CreateUser {

    @Test
    @DisplayName("성공 - 유효한 요청으로 사용자 생성 후 201 반환")
    void createUser_success() throws Exception {
      // given: 유효한 회원가입 요청 DTO
      UserCreateRequest req
          = new UserCreateRequest("kim", "kim@example.com", "password");

      // when & then: POST multipart 요청 -> 실제 DB 저장 -> 201 Created + JSON 검증
      mockMvc.perform(
              multipart("/api/users")
                  .part(jsonPart("userCreateRequset", req))
          )
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").isNotEmpty())
          .andExpect(jsonPath("$.username").value("kim"))
          .andExpect(jsonPath("$.email").value("kim@example.com"))
          .andExpect(jsonPath("$.online").value(true)
          );
    }

    @Test
    @DisplayName("실패 - 이미 사용 중인 이메일로 생성 시 409 Conflict 반환")
    void createUser_duplicateEmail_conflict() throws Exception {
      // given
      createUserAndGetId()

      // when & then
    }
  }
}

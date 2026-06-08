package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.dto.Dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.requestRespose.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// 컨트롤러 레이어의 슬라이스 테스트
// Spring MVC 인프라(DispatcherServlet, MockMvc, ObjectMapper, @ControllerAdvice 등)와 UserController만 빈으로 등록
@WebMvcTest(UserController.class)
public class UserControllerTest {

  /*
  MockMvc : Spring MVC의 DispatcherServlet을 mock하여 실제 HTTP 서버를 띄우지 않고도
  컨트롤러에 HTTP 요청을 보내고 응답을 검증할 수 있게 해주는 테스트 유틸리티
   */
  @Autowired
  private MockMvc mockMvc;

  // ObjectMapper : 요청 body(Java 객체 -> JSON 문자열) 직렬화에 사용
  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  // GET /api/users - 전체 User 목록 조회
  @Test
  @DisplayName("성공 - 전체 User 목록을 200 OK와 함께 반환")
  void findAll_success() throws Exception {
    // given: 서비스가 사용자 1명을 반환하도록 stub
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto(userId, "kim", "kim@example.com", null, true);
    given(userService.findAll()).willReturn(List.of(userDto));

    // when & then: GET 요청 -> 200 OK + JSON 필드 검증
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(userId.toString()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value("kim"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value("kim@example.com"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].online").value(true));
  }

  @Test
  @DisplayName("성공 - User가 없으면 빈 배열 반환")
  void findAll_empty() throws Exception {
    // given
    given(userService.findAll()).willReturn(List.of());

    // when & then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // DELETE /api/users/{userId}
  @Nested
  @DisplayName("DELETE /api/users/{userId}")
  class Delete {

    @Test
    @DisplayName("성공 - User 삭제 후 204 No Content 반환")
    void delete_success() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      // willDoNothing - void 메서드의 정상 동작을 stub하는 BDDMockito 관용구
      willDoNothing().given(userService).delete(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}", userId))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 userId면 404 Not Found 반환")
    void delete_userNotFound() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      willThrow(new UserNotFoundException(Map.of("userId", userId)))
          .given(userService).delete(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}", userId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
          .andExpect(jsonPath("$.status").value(404));
    }
  }

  // PATCH /api/users/{userId}
  @Nested
  @DisplayName("PATCH /api/users/{userId}")
  class UpdateUser {

    @Test
    @DisplayName("성공 - 사용자 정보 수정 시 200 OK 반환")
    void update_success() throws Exception {
      // given: 서비스가 수정된 UserDto를 반환하도록 stub
      UUID userId = UUID.randomUUID();
      UserDto updateUser
          = new UserDto(userId, "kim_updated", "kim_updated@example.com", null, true);
      given(userService.update(any(), any(), any())).willReturn(updateUser);

      // Multipart 파트 구성
      UserUpdateRequest request
          = new UserUpdateRequest("kim_updated", "kim_updated@example.com", null);
      MockMultipartFile userUpdateRequestPart = new MockMultipartFile(
          "userUpdateRequest",
          null,
          MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request)
      );

      // when & then
      mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userId)
              .file(userUpdateRequestPart))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.username").value("kim_updated"))
          .andExpect(jsonPath("$.email").value("kim_updated@example.com"));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 userId면 404 Not Found 반환")
    void update_userNotFound() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      given(userService.update(any(), any(), any()))
          .willThrow(new UserNotFoundException(Map.of("userId", userId)));

      UserUpdateRequest request = new UserUpdateRequest("newName", null, null);
      MockMultipartFile userUpdateRequestPart = new MockMultipartFile(
          "userUpdateRequest", null, MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request)
      );

      // when & then
      mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userId)
              .file(userUpdateRequestPart))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
          .andExpect(jsonPath("$.status").value(404));
    }
  }

  // PATCH /api/users/{userId}/userStatus - User 온라인 상태 업데이트
  @Nested
  @DisplayName("PATCH /api/users/{userId}/userStatus")
  class UpdateUserStatus {

    @Test
    @DisplayName("성공 - UserStatus 업데이트 후 200 OK와 응답 반환")
    void updateUserStatus_success() throws Exception {
      // given
      UUID userId = UUID.randomUUID();
      Instant now = Instant.now();
      UserStatusUpdateRequest request = new UserStatusUpdateRequest(now);
      UserStatusDto responseDto = new UserStatusDto(UUID.randomUUID(), userId, now);
      given(userStatusService.updateByUserId(any(), any())).willReturn(responseDto);

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))) // 요청 바디 직렬화
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.userId").value(userId.toString()));
    }
  }
}
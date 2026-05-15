package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.data.UserDto;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

  private final UserDto userDto;
  private final String password;

  public UserDto getUserDto() {
    return userDto;
  }

  // 사용자의 권한 목록
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.role().name()));
  }

  // PasswordEncoder가 비밀번호 검증 시 사용
  @Override
  public String getPassword() {
    return password;
  }

  // 인증 시 식별자로 사용되는 값
  @Override
  public String getUsername() {
    return userDto.username();
  }

  // 계정 자체의 만료 여부
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  // 계정 잠금 여부
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  // 비밀번호 만료 여부
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  // 계정 활성화 여부
  @Override
  public boolean isEnabled() {
    return true;
  }

  /*
  - SessionRegistry는 내부적으로 Map을 사용하여 UserDetails 객체를 관리하기 때문에,
    equals와 hashCode를 UserDto의 id로 구현 (Map의 key 비교는 equals()와 hashCode()를 사용하기 때문)
  - 기본 equals()는 메모리 주소를 비교하기 때문에, 같은 사용자를 DB에서 두 번 조회하면 다른 객체로 판단
    -> userId로 오버라이딩 해서 id가 같으면 같은 사용자로 인식하도록 설정
  - 권한 변경 후 해당 사용자의 세션을 강제 만료시켜야 함 -> SessionRegistry에서 해당 사용자 세션을 찾아야 함
    -> SessionRegistry 내부 Map의 key(UserDetails)와 비교
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DiscodeitUserDetails other)) {
      return false;
    }
    return Objects.equals(userDto.id(), other.userDto.id()); // userId로 동일성 판단
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(userDto.id());
  }
}

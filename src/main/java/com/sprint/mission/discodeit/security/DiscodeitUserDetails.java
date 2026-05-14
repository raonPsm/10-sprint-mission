package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.data.UserDto;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
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
    return List.of();
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
}

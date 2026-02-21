package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
  // 자동으로 getter 메서드를 생성해준다. -> Lombok 적용
public class User extends BaseEntity {
    private String username;
    private String email;
    private String password;

    // 프로필 이미지 id (User 1..0 BinaryContent)
    private UUID profileId;

    // 첨부파일이 있는 경우 (생성자 오버로드)
    public User(String username, String email, String password, UUID profileId) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void update(String newUsername, String newEmail, String newPassword, UUID newProfileId) {
        boolean isAnyValueUpdated = false;
        if (newUsername != null && !newUsername.equals(this.username)) {
            this.username = newUsername;
            isAnyValueUpdated = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            isAnyValueUpdated = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)) {
            this.password = newPassword;
            isAnyValueUpdated = true;
        }
        if (newProfileId != null && !newProfileId.equals(this.profileId)) {  // TODO: (Later) 기본 이미지로 돌아가기 기능 추가 시 수정되어야 함
            this.profileId = newProfileId;
            isAnyValueUpdated = true;
        }

        if (isAnyValueUpdated) {
            updateInstant();
        }
    }
}

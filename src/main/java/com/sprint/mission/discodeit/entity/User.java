package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {
    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 60, nullable = false)
    private String password;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private BinaryContent profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // OneToOne : 1:1 관계 설정
    // mappedBy : user 연관관계의 주인이 아님. 외래키 관리 권한 없음. 조회만 가능
    // cascade = CascadeType.ALL : 영속성 전이. 부모 엔티티의 상태 변화가 자식 엔티티로 전파됨.
    // orphanRemoval = true : 부모 엔티티와의 연관 관계가 끊어진 자식 엔티티(고아 객체)를 자동으로 삭제함.
    private UserStatus userStatus;

    // 첨부파일이 있는 경우 (생성자 오버로드)
    public User(String username, String email, String password, BinaryContent profile) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
    }

    public void update(String newUsername, String newEmail, String newPassword, BinaryContent newProfile) {
        if (newUsername != null && !newUsername.equals(this.username)) {
            this.username = newUsername;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
        }
        if (newPassword != null && !newPassword.equals(this.password)) {
            this.password = newPassword;
        }
        if (newProfile != null) {  // TODO: (Later) 기본 이미지로 돌아가기 기능 추가 시 수정되어야 함
            this.profile = newProfile;
        }
    }

    public void assignUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}

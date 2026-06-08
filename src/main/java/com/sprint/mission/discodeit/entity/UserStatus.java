package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

/**
 * UserStatus
 * 사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델. 사용자의 온라인 상태를 확인하기 위해 활용
 */

@Entity
@Table(name = "user_statuses")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Instant lastActiveAt;

    public UserStatus(User user, Instant lastActiveAt) {
        this.user = user;
        this.lastActiveAt = lastActiveAt;
    }

    public void update(Instant lastActiveAt) {
        if (lastActiveAt != null && !lastActiveAt.equals(this.lastActiveAt)) {
            this.lastActiveAt = lastActiveAt;
        }
    }

    // 현재 '온라인' 상태인지 확인 (5분 이내 활동 시 true)
    // TODO: (Later) 온라인 / 자리 비움 / 방해 금지 / 오프라인 표시 / 오프라인 -> Enum 으로 상태 구현
    public Boolean isOnline() {
        Instant instantFiveMinutesAgo = Instant.now().minus(Duration.ofMinutes(5));
        return lastActiveAt.isAfter(instantFiveMinutesAgo);
    }
}

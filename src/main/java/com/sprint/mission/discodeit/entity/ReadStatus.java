package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * ReadStatus
 * 사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델. 사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용한다.
 */

@Entity
@Table(
        name = "read_statuses",
        uniqueConstraints = { // 유니크 제약 추가
                @UniqueConstraint(
                        name = "uk_user_channel", // 유니크 제약 이름
                        columnNames = {"user_id", "channel_id"} // 해당 컬럼 조합 유일 설정
                )
        }
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private Instant lastReadAt;

    public ReadStatus(User user, Channel channel, Instant lastReadAt) {
        this.user = user;
        this.channel = channel;
        this.lastReadAt = lastReadAt;
    }

    // 읽은 시간 갱신
    public void updateLastReadAt(Instant lastReadAt) {
        if (lastReadAt != null && !lastReadAt.equals(this.lastReadAt)) {
            this.lastReadAt = lastReadAt;
        }
    }
}

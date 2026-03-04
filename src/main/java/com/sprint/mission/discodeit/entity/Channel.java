package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity // JPA가 관리하는 데이터베이스 테이블과 매핑되는 '엔티티'임을 선
@Table(name = "channels") // 자바 클래스 이름과 DB 테이블 이름을 다르게 매핑하기 위해 사용
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// TODO: (LATER) 왜 @NoArgsConstructor가 필요한지 -> JPA 관련 등 + 왜 PROTECTED인지? -> Proxy 조회 관련 -> 정리할 것
// 일단은 간단하게 JPA 엔티티를 만들 때 꼭 사용해야 된다고 생각하기 -> 자세한 내용은 따로 정리
public class Channel extends BaseUpdatableEntity {
    @Enumerated(EnumType.STRING) // 해당 설정 지정해야 JPA가 숫자가 아니라 문자열 자체를 DB에 저장
    // 만약 숫자로 저장할 경우, 다른 값이 중간에 추가되면 데이터가 꼬일 수 있음 -> 그래서 꼭 문자열을 저장하도록 지정해야 함
    @Column(nullable = false, length = 10)
    private ChannelType type;
    @Column(length = 100)
    private String name;
    @Column(length = 500)
    private String description;

    public Channel(ChannelType type, String name, String description) {
        // super(); -> 컴파일러가 자동으로 삽입함
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public void update(String newName, String newDescription) {
        if (newName != null && !newName.equals(this.name)) {
            this.name = newName;
        }
        if (newDescription != null && !newDescription.equals(this.description)) {
            this.description = newDescription;
        }
    }

    public static Channel createPublicChannel(String name, String description) {
        return new Channel(ChannelType.PUBLIC, name, description);
    }

    public static Channel createPrivateChannel() {
        return new Channel(ChannelType.PRIVATE, null, null);
    }
}

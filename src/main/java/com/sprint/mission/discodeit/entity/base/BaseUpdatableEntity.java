package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity{
    @LastModifiedDate
    private Instant updatedAt;
}
// TODO: (LATER) columnDefinition -> 필요성에 대해서 고민 + 관련 조사 내용 정리
// columnDefinition -> 사용시 DB 의존성 생길 수 있음
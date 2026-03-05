package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseUpdatableEntity {

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // FetchType.LAZY : 프록시 객체만 넣어두고, 실제로 해당 객체를 사용할 때 쿼리를 날려 가져온다.
    // optional = false : 연관된 객체가 반드시 존재함을 보장하여 INNER JOIN 수행 (DDL을 강제하지는 않음)
    @JoinColumn(name = "channel_id", nullable = false)
    // name : 매핑할 외래키 컬럼의 이름을 지정
    // nullable = false : ddl-auto 속성으로 테이블을 자동 생성할 때, 해당 외래키 컬럼에 NOT NULL DLL 강제
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @BatchSize(size = 100) // N+1 문제 해결을 위한 In Query 최적화 (size = 100~1000 사이로 보통 설정)
    // 100개의 메시지 첨부파일을 한 번에 조회. WHERE message_id IN (1, 2, ..., 100) 형태로 쿼리가 날아감
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    // 1:N 관계 매핑
    @JoinTable(
            name = "message_attachments", // 매핑 테이블의 이름
            joinColumns = @JoinColumn(name = "message_id"), // Message의 기본키를 참조할 매핑 테이블의 외래키 컬럼명
            inverseJoinColumns = @JoinColumn(name = "attachment_id") // BinaryContent의 기본키를 참조할 매핑 테이블의 외래키 컬럼명
    )
    private List<BinaryContent> attachments = new ArrayList<>();

    public Message(String content, Channel channel, User author, List<BinaryContent> attachments) {
        this.content = content;
        this.channel = channel;
        this.author = author;
        this.attachments = attachments;
    }

    public void update(String newContent) {
        if (newContent != null && !newContent.equals(this.content)) {
            this.content = newContent;
        }
    }
}
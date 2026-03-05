package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

    @Column(nullable = false) // length 속성의 기본값은 255
    private String fileName;

    @Column(nullable = false)
    private long size;

    @Column(length = 100, nullable = false)
    private String contentType;

    @Lob // Large Object
    @Column(nullable = false)
    private byte[] bytes;

    public BinaryContent(String fileName, long size, String contentType, byte[] bytes) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.bytes = bytes;
    }
}
package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.Dto.BinaryContentDto;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.UUID;

/**
 * 바이너리 데이터의 저장/로드를 담당하는 컴포넌트
 */
public interface BinaryContentStorage {
    /**
     * UUID 키 정보를 바탕으로 byte[] 데이터를 저장
     * UUID는 BinaryContent의 Id
     * @param binaryContentId
     * @param bytes
     * @return
     */
    UUID put(UUID binaryContentId, byte[] bytes);

    /**
     * 키 정보를 바탕으로 byte[] 데이터를 읽어 InputStream 타입으로 반환
     * UUID는 BinaryContent의 Id
      * @param binaryContentId
      * @return
     */
    InputStream get(UUID binaryContentId);

    /**
     * HTTP API로 다운로드 기능을 제공
     * BinaryContentDto 정보를 바탕으로 파일을 다운로드할 수 있는 응답을 반환
     * @param metaData
     * @return
     */
    ResponseEntity<?> download(BinaryContentDto metaData);
}

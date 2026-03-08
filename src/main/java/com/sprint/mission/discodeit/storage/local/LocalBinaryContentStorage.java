package com.sprint.mission.discodeit.storage.local;

import com.sprint.mission.discodeit.controller.exception.FileProcessException;
import com.sprint.mission.discodeit.dto.Dto.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 로컬 디스크를 BinaryContentStorage 구현체로 사용하는 클래스
 * 바이너리 파일 자체를 DB가 아니라 서버의 특정 폴더에 저장하고
 * 다운로드할 때는 그 파일을 읽어서 HTTP 응답으로 보낸다.
 */

@Component
@ConditionalOnProperty(
        name = "discodeit.storage.type", // 활성화 조건 -> discodeit.stroage.type = local
        havingValue = "local" // discodeit.storage.local.root-path 프로퍼티로 지정된 경로
)
public class LocalBinaryContentStorage implements BinaryContentStorage {
    private final Path root;
    // root는 파일이 저장될 최상위 폴더(기본 경로)를 의미
    // Path는 파일 시스템의 경로를 표현하는 인터페이스

    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") Path rootPath
            // @Value -> 애플리케이션 설정 파일에 정의된 값을 자바 변수에 자동으로 할당
            // 저장 위치를 설정 파일을 통해 변경할 수 있음
    ) {
        this.root = rootPath;
    }

    // 스프링 빈이 생성된 직후, 파일을 저장할 디겍터리가 실제로 존재하는지 확인하고 없으면 자동으로 생성하는 메서드
    @PostConstruct // -> 의존성 주입이 완료된 후, 해당 빈을 사용하기 전에 한 번만 실행되어야 하는 메서드에 붙임
    public void init() {
        try {
            if (!Files.exists(root)) { // 지정된 경로에 폴더가 존재하지 않으면
                Files.createDirectories(root); // 디렉터리 생성
                // createDirectory와 달리 중간 단계의 부모 폴더들이 없으면 그것들까지 한꺼번에 생성
            }
        } catch (IOException e) {
            throw new FileProcessException("저장소 루트 디렉토리를 생성할 수 없습니다.", e); // 커스텀 예외
        }
    }

    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        // Objects.requireNonNull(binaryContentId, "binaryContentId must not be null") -> TODO: 이런 방어적 코드 필요한지 검토 및 고려
        try {
            Files.write(resolvePath(binaryContentId), bytes); // 지정된 경로에 바이트 배열의 내용을 기록
            // 파일이 존재하지 않으면 새 파일 생성
            // 파일이 이미 존재한다면 기존 내용을 모두 지우고 새로운 데이터로 덮어씀
        } catch(IOException e) {
            throw new FileProcessException("파일 저장 중 오류가 발생했습니다. binaryContentId: " + binaryContentId, e);
        }
        return binaryContentId;
    }

    @Override
    public InputStream get(UUID binaryContentId) {
        try {
            return Files.newInputStream(resolvePath(binaryContentId));
            // 지정된 경로의 파일을 읽기 위한 InputStream 객체를 생성
            // 이 스트림을 반환하면, 이 메서드를 호출한 쪽에서 이 스트림을 통해 파일의 실제 바이트 데이터를 읽어갈 수 있음
        } catch(IOException e) {
            throw new FileProcessException("파일을 읽는 중 오류가 발생했습니다. binaryContentId: " + binaryContentId, e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto metaData) {
        InputStream inputStream = get(metaData.id());
        Resource resource = new InputStreamResource(inputStream);

        // Content-Disposition 헤더는 HTTP 응답에서 파일 다운로드와 관련된 정보를 전달하는 데 사용
        // Content-Disposition HTTP 헤더 값을 객체 형태로 만드는 코드

        // attachment -> 파일 다운로드로 처리
        // inline -> 브라우저 안에서 바로 표시

        // filename() -> 다운로드될 파일명을 지정 -> UTF-8 인코딩으로 처리
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(metaData.fileName(), StandardCharsets.UTF_8)
                .build();


        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM; // 기본값 설정
        if(metaData.contentType() != null && !metaData.contentType().isBlank()) {
            mediaType = MediaType.parseMediaType(metaData.contentType());
            // 메타데이터에 저장된 content type이 실제로 있으면 그 값을 사용
        }

        return ResponseEntity.ok() // 200 OK
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(mediaType) // 응답 데이터의 종류를 지정 (image/png)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metaData.size())) // 파일의 전체 크기를 바이트 단위로 알려줌
                .body(resource); // 최종적으로 파일 데이터가 담긴 resource 객체를 HTTP body에 담는다
    }

    // 저장될 파일의 전체 경로를 반환하는 헬퍼 메서드
    // resolve() -> 현재 경로 뒤에 인자로 받은 경로로를 붙여서 새로운 Path 객체를 만듦
    private Path resolvePath(UUID binaryContentId) {
        return root.resolve(binaryContentId.toString());
    }
}

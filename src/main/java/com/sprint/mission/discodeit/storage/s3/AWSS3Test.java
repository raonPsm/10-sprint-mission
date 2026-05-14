package com.sprint.mission.discodeit.storage.s3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AWS S3 API 간단 연동 테스트.
 *
 * <p>Spring 컨텍스트 없이 순수 AWS SDK(S3Client, S3Presigner)만 사용하여
 * S3의 기본 동작(업로드/다운로드/Presigned URL 생성)을 검증합니다.</p>
 *
 * <p>프로젝트 루트의 .env 파일에서 AWS 자격증명을 java.util.Properties로 직접 로드합니다.
 * .env 파일이 없거나 자격증명이 유효하지 않으면 테스트가 실패합니다.</p>
 *
 * <p>참고: 이 파일은 src/main에 위치하지만 테스트 성격의 코드입니다.
 * upload → download → generatePresignedUrl 순서로 실행해야
 * download와 generatePresignedUrl에서 사용할 객체가 S3에 존재합니다.</p>
 */
class AWSS3Test {

  // S3 업로드/다운로드용 클라이언트 (static — 모든 테스트 메소드에서 공유)
  static S3Client s3Client;
  // Presigned URL 생성 전용 클라이언트 (S3Client와 역할이 분리됨)
  static S3Presigner s3Presigner;
  // S3 버킷 이름 (모든 테스트에서 동일한 버킷 사용)
  static String bucketName;
  // 테스트마다 고유한 S3 객체 키 (테스트 간 격리를 위해 매번 새로 생성)
  String testKey;

  /**
   * 모든 테스트 실행 전 1회 호출 — .env에서 AWS 자격증명을 로드하고 클라이언트를 초기화합니다.
   *
   * <p>.env 파일 형식 예시:
   * <pre>
   * AWS_ACCESS_KEY_ID=AKIA...
   * AWS_SECRET_ACCESS_KEY=wJalr...
   * AWS_REGION=ap-northeast-2
   * AWS_S3_BUCKET=discodeit-binary-content-storage-abc
   * </pre></p>
   *
   * <p>참고: 이 파일의 .env 키 이름(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION)은
   * application.yaml에서 사용하는 키 이름(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY, AWS_S3_REGION)과
   * 다릅니다. .env 파일에 두 형식 모두 정의해두어야 합니다.</p>
   *
   * @throws IOException .env 파일을 읽지 못할 때
   */
  @BeforeAll
  static void setUp() throws IOException {
    // java.util.Properties를 사용하여 .env 파일을 "키=값" 형식으로 파싱
    // Spring 없이 순수 자바만으로 설정값을 로드하는 방식
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    }

    // .env에서 AWS 자격증명 4개 값을 추출
    String accessKey = props.getProperty("AWS_ACCESS_KEY_ID");    // IAM 액세스 키
    String secretKey  = props.getProperty("AWS_SECRET_ACCESS_KEY"); // IAM 시크릿 키
    String region     = props.getProperty("AWS_REGION");            // AWS 리전 (예: ap-northeast-2)
    bucketName        = props.getProperty("AWS_S3_BUCKET");         // S3 버킷 이름

    // StaticCredentialsProvider: 액세스 키/시크릿 키를 코드에서 직접 전달하는 방식
    // (AWS CLI 프로필이나 환경변수 자동 탐색이 아닌 명시적 자격증명)
    StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey)
    );
    // 문자열 리전을 AWS SDK의 Region 객체로 변환
    Region awsRegion = Region.of(region);

    // S3Client 생성 — 실제 S3 API 호출(업로드/다운로드)에 사용
    s3Client = S3Client.builder()
        .region(awsRegion)
        .credentialsProvider(credentials)
        .build();

    // S3Presigner 생성 — Presigned URL 생성 전용
    // S3Client와 별도인 이유: URL 서명은 로컬에서 수행되며 S3에 직접 요청을 보내지 않음
    s3Presigner = S3Presigner.builder()
        .region(awsRegion)
        .credentialsProvider(credentials)
        .build();
  }

  /**
   * 각 테스트 메소드 실행 전 호출 — 고유한 테스트 키를 생성합니다.
   *
   * <p>매 테스트마다 UUID 기반의 고유 키를 사용하여 테스트 간 간섭을 방지합니다.
   * 예: "test/550e8400-e29b-41d4-a716-446655440000"</p>
   */
  @BeforeEach
  void initTestKey() {
    testKey = "test/" + UUID.randomUUID();
  }

  /**
   * 각 테스트 메소드 실행 후 호출 — 테스트에서 생성한 S3 객체를 삭제합니다.
   *
   * <p>S3 버킷에 테스트 찌꺼기가 쌓이지 않도록 정리합니다.
   * 삭제 실패 시에도 테스트 자체는 실패로 처리하지 않습니다.</p>
   */
  @AfterEach
  void cleanup() {
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(testKey)
          .build());
    } catch (S3Exception e) {
      // 정리 실패는 무시 — best-effort
    }
  }

  /**
   * S3에 파일을 업로드하는 테스트.
   *
   * <p>"test/hello.txt"라는 키로 텍스트 파일을 업로드합니다.
   * S3의 키(key)는 파일 경로처럼 "/"를 포함할 수 있으며,
   * S3 콘솔에서는 "test" 폴더 안의 "hello.txt"처럼 표시됩니다.</p>
   *
   * <p>PutObjectRequest로 버킷/키/contentType을 지정하고,
   * RequestBody.fromBytes()로 바이트 배열을 전송합니다.
   * 응답의 HTTP 상태코드가 2xx(성공)인지 확인합니다.</p>
   */
  @Test
  void upload() {
    // 업로드할 파일 내용을 바이트 배열로 변환
    byte[] content = "Hello, S3!".getBytes();

    // S3에 파일 업로드 실행
    // PutObjectRequest: 어디에(bucket), 무슨 이름으로(testKey), 어떤 타입(contentType)으로 저장할지 지정
    // RequestBody.fromBytes(): 바이트 배열을 S3 전송 가능한 형태로 변환
    PutObjectResponse response = s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(testKey)
            .contentType("text/plain")
            .build(),
        RequestBody.fromBytes(content)
    );

    // HTTP 응답이 성공(2xx)인지 검증
    // sdkHttpResponse(): AWS SDK 응답에서 HTTP 레벨의 상태를 확인할 수 있음
    assertThat(response.sdkHttpResponse().isSuccessful()).isTrue();
  }

  /**
   * S3에서 파일을 다운로드하는 테스트.
   *
   * <p>upload() 테스트에서 업로드한 "test/hello.txt" 객체를 다운로드합니다.
   * 따라서 upload() 테스트가 먼저 실행되어 객체가 S3에 존재해야 합니다.</p>
   *
   * <p>getObject()는 ResponseInputStream을 반환하며,
   * readAllBytes()로 전체 내용을 바이트 배열로 읽은 뒤 문자열로 변환하여 검증합니다.</p>
   *
   * @throws IOException 스트림 읽기 실패 시
   */
  @Test
  void download() throws IOException {
    // === Given: 다운로드 테스트를 위한 파일 먼저 업로드 (테스트 독립성 보장) ===
    String content = "Hello, S3!";
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(testKey)
            .contentType("text/plain")
            .build(),
        RequestBody.fromString(content)
    );

    // === When: S3에서 객체를 스트림으로 다운로드 ===
    // ResponseInputStream: S3 응답 메타데이터(GetObjectResponse) + 파일 내용(InputStream)을 함께 제공
    ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(bucketName)
            .key(testKey)
            .build()
    );

    // === Then: 스트림에서 전체 바이트를 읽어 문자열로 변환 후 검증 ===
    byte[] bytes = response.readAllBytes();
    assertThat(new String(bytes)).isEqualTo(content);
  }

  /**
   * S3 객체에 대한 Presigned URL을 생성하는 테스트.
   *
   * <p>Presigned URL은 AWS 자격증명 없이도 S3 객체에 접근할 수 있는 임시 서명 URL입니다.
   * 버킷이 퍼블릭 액세스 차단 상태여도 Presigned URL로는 다운로드가 가능합니다.
   * URL에 서명 파라미터가 쿼리스트링으로 포함되어 유효 기간 동안만 접근 가능합니다.</p>
   *
   * <p>생성된 URL 형식 예시:
   * {@code https://bucket.s3.ap-northeast-2.amazonaws.com/test/hello.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=...}</p>
   *
   * <p>이 방식이 S3BinaryContentStorage.download()에서 활용됩니다:
   * 서버가 파일을 직접 스트리밍하지 않고, Presigned URL로 302 리다이렉트하여
   * 클라이언트가 S3에서 직접 다운로드하게 합니다. (서버 대역폭/메모리 절약)</p>
   */
  @Test
  void generatePresignedUrl() {
    // === Given: Presigned URL 생성을 위한 파일 먼저 업로드 (테스트 독립성 보장) ===
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(testKey)
            .contentType("text/plain")
            .build(),
        RequestBody.fromString("Test content for presigned URL")
    );

    // === When: Presigned URL 생성 ===
    // 1) GetObjectPresignRequest: 어떤 객체에 대해, 얼마나 유효한 URL을 만들지 지정
    // 2) signatureDuration: URL 유효 기간 (10분) — 이 시간이 지나면 URL 만료
    // 3) getObjectRequest: 대상 S3 객체의 버킷과 키 지정
    PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(testKey)
                .build())
            .build()
    );

    // === Then: 생성된 Presigned URL 검증 ===
    // 이 URL을 브라우저에 붙여넣으면 인증 없이 파일을 다운로드할 수 있음
    URL url = presigned.url();
    assertThat(url).isNotNull();
    // 생성된 URL을 콘솔에 출력하여 확인 (브라우저에서 직접 테스트 가능)
    System.out.println("Presigned URL: " + url);
  }
}

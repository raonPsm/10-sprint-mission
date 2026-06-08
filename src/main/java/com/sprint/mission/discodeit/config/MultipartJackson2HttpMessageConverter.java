package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * Content-Type 'application/octet-stream' is not supported - 오류 해결을 위한 MultipartJackson2HttpMessageConverter 재정의
 *      Swagger가 여러개의 Content-Type을 동시에 설정하는 것이 불가능하여, 파일과 DTO가 보내는 요청에서 자동으로 application/octet-stream 타입으로 인식
 *      이 타입은 Spring에서 기본적으로 지원하지 않기 때문에 발생하는 오류
 *      Content-Type 설정이 누락되면, null로 들어가게 되면서 어떤 타입인지 알 수가 없어 자동으로 application/octet-stream으로 처리됨
 *      Postman에서는 Content-Type을 명시적으로 설정할 수 있기 때문에 이 문제가 발생하지 않음
 *
 * Spring에서 기본적으로 Json 데이터를 처리할 때는 MappingJackson2HttpMessageConverter를 사용하고, 파일 업로드를 처리할 때는 MultipartHttpMessageConverter를 사용
 * multipart/form-data 형식으로 JSON 데이터와 파일 데이터를 동시에 처리해야 하는 경우, 이 두 가지를 처리할 수 있는 MultipartJackson2HttpMessageConverter이 필요하다.
 * MultipartJackson2HttpMessageConverter는 AbstractJackson2HttpMessageConverter를 확장하여 multipart/form-data 요청을 처리할 수 있도록 커스터마이징한 클래스
 */

// TODO: (Later) 관련 이슈 블로깅
@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    /**
     * 기본 생성자
     * MediaType.APPLICATION_OCTET_STREAM을 지원하는 Jackson2HttpMessageConverter를 생성합니다.
     * 이를 통해 Swagger가 보내는 기본 미디어 타입(octet-stream)을 JSON 역직렬화 대상으로 포함시킵니다.
     */
    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    /**
     * 해당 컨버터는 '읽기(Read)' 전용으로만 사용해야 합니다.
     * 서버가 클라이언트에게 응답(Write)할 때 모든 JSON을 octet-stream으로 내보내면 안 되므로,
     * 쓰기 작업에 대해서는 항상 false를 반환하도록 오버라이딩합니다.
     */
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}
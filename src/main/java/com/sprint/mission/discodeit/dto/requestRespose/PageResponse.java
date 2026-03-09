package com.sprint.mission.discodeit.dto.requestRespose;

import java.util.List;

public record PageResponse<T>(
        List<T> content, // 실제 데이터
        int number, // 페이지 번호
        int size, // 페이지당 데이터 크기 - 요구사항에 따라 50으로 설정
        boolean hasNext, // 다음 페이지가 존재하는지 여부를 나타내는 플래그
        Long totalElements // T데이터의 총 객수를 의미하며, null일 수 있음
) {}

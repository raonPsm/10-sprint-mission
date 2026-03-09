package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.requestRespose.PageResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

@Mapper(componentModel = "spring")
public interface PageResponseMapper {
    // TODO: (Later) Object nextCursor -> Instant, Long, UUID, String... 다 받을 수 있도록 할 것인지 고려
    default <T> PageResponse<T> fromSlice(Slice<T> slice, String nextCursor) {
        return new PageResponse<>(
                slice.getContent(),
                nextCursor,
                slice.getSize(),
                slice.hasNext(),
                null
        );
    }

    default <T> PageResponse<T> fromPage(Page<T> page, String nextCursor) {
        return new PageResponse<>(
                page.getContent(),
                nextCursor,
                page.getSize(),
                page.hasNext(),
                page.getTotalElements()
        );
    }
}

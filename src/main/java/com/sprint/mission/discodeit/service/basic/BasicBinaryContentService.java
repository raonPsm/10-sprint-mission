package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContent create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.contentType(),
                request.bytes()
        );

        return binaryContentRepository.save(binaryContent);
    }
    // TODO: (Later) createAll - 다건 저장 로직 추가

    @Override
    public BinaryContent find(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 첨부파일(BinaryContent)을 찾을 수 없습니다. id: " + id));
    }

    // 여러 개의 id 목록을 받아, 실제로 존재하는 바이너리 콘텐츠들만 조회후 반환
    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        return binaryContentRepository.findAllByIdIn(ids);
    }

    @Override
    public void delete(UUID id) {
        if (binaryContentRepository.existsById(id)) {
            throw new NoSuchElementException("삭제할 첨부파일(BinaryContent)을 찾을 수 없습니다. id: " + id);
        }
        binaryContentRepository.deleteById(id);
    }
}

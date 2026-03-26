package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
  @Override
  public BinaryContentDto create(BinaryContentCreateRequest request) {
    BinaryContent binaryContent = new BinaryContent(
        request.fileName(),
        (long) request.bytes().length,
        request.contentType()
    );
    binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(binaryContent.getId(), request.bytes());

    return binaryContentMapper.toDto(binaryContent);
  }
  // TODO: (Later) createAll - 다건 저장 로직 추가

  @Override
  public BinaryContentDto find(UUID id) {
    return binaryContentMapper.toDto(binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(Map.of("binaryContentId", id)))
    );
  }

  // 여러 개의 id 목록을 받아, 실제로 존재하는 바이너리 콘텐츠들만 조회후 반환
  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }

    return binaryContentMapper.toDtoList(binaryContentRepository.findAllByIdIn(ids));
  }

  @Override
  public void delete(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(Map.of("binaryContentId", id)));
    binaryContentRepository.delete(binaryContent);
  }
  // TODO: (Later) delete -> 실제 파일도 storage에서 삭제 필요
}

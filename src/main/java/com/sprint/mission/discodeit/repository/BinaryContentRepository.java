package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {
    // save, findById, delete
        // BinaryContent save(BinaryContent entity);
        // Optional<BinaryContent> findById(UUID id);
        // delete(BinaryContent entity);

    // 여러 개의 id 목록을 받아, 해당하는 모든 BinaryContent 엔티티를 한 번에 조회
    // JPQL -> SELECT bc FROM BinaryContent bc WHERE b.id IN :ids
    // SQL  -> SELECT * FROM binary_contents WHERE id IN (?, ?, ...)
        // IN -> 특정 컬럼의 값이 여러 개의 지정된 값 중 하나와 일치하는지 확인하는 조건문
    List<BinaryContent> findAllByIdIn(List<UUID> ids);


    // deleteById vs delete
    // deleteById(ID id): ID로 조회 -> 객체 삭제 / 대상이 없으면 예외 발생 가능성 있음
        // findById()로 해당 엔티티를 찾고 delete()를 호출
        // findById()로 해당 엔티티를 찾지 못한다면 EmptyResultDataAccessException을 발생
        // 서비스 로직에서 메서드를 하나만 사용해도 조회와 삭제가 다 된다는 장점이 있음
        // 내부적으로 id에 대한 null 체크를 해주기 때문에 서비스 로직에서 id의 null 체크를 하지 않았더라도 의도치 않은 NPE 발생을 예방할 수 있음

    // delete(T entity): (영속성 컨텍스트에 없다면) 조회 -> 객체 삭제 / 인자가 null이 아니면 대상이 없어도 예외 없이 통과
        // findById와 조합해서 쓰는 경우 많음
        // findById를 직접 사용하여 예외처리를 커스텀할 수 있음

}

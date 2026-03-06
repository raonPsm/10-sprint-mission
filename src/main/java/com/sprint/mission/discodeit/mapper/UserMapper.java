package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.Dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring", // MapStruct가 생성하는 Mapper 구현체에 자동으로 @Component 어노테이션이 붙음
        uses = {BinaryContentMapper.class} // 다른 Mapper 클래스를 참조하여 복잡한 데이터 변환을 수행
        // 매퍼가 필드를 변환하는 과정에서 직접 처리할 수 없는 타입을 만났을 때 참조한 Mapper에 정의된 메서드를 찾아 자동으로 사용
)
// MapStruct는 컴파일 시점에 인터페이스를 분석하여 실제 변환 로직이 담긴 구현체(.class 파일)를 자동으로 생성
// 개발자가 인터페이스에 메서드 시그니처만 정의
// MapStruct -> 컴파일 할 때 UserMapperImpl 이라는 클래스를 만들고, 구현체 코드 작성

public interface UserMapper {
    @Mapping(target = "profile", source = "profile")
    @Mapping(target = "online",
            expression = "java(entity.getUserStatus() != null ? entity.getUserStatus().isOnline() : null)")
    UserDto toDto(User entity);
    List<UserDto> toDtoList(List<User> entities);
}

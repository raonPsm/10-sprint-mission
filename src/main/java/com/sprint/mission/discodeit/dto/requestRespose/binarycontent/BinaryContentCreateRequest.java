package com.sprint.mission.discodeit.dto.requestRespose.binarycontent;

public record BinaryContentCreateRequest(
    String fileName,
    String contentType,
    byte[] bytes
) {

}

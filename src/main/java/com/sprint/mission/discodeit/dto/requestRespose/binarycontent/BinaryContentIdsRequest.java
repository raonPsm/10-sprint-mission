package com.sprint.mission.discodeit.dto.requestRespose.binarycontent;

import java.util.List;
import java.util.UUID;

public record BinaryContentIdsRequest(
        List<UUID> ids
) {}

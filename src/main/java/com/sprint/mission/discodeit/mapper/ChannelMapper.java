//package com.sprint.mission.discodeit.mapper;
//
//import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
//import com.sprint.mission.discodeit.entity.Channel;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ChannelMapper {
//    public ChannelResponse toResponse(Channel channel) {
//        if (channel == null) return null;
//        return new ChannelResponse(
//                channel.getId(),
//                channel.getCreatedAt(),
//                channel.getUpdatedAt(),
//                channel.getType(),
//                channel.getName(),
//                channel.getDescription()
//        );
//    }
//}

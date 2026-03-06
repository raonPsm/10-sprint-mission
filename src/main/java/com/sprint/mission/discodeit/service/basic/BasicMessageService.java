package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.requestRespose.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.requestRespose.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;


// FIXME: 메서드 내부 DTO 사용 부분 entity로 수정 및 기타 부수효과 문제 수정
@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Message create(MessageCreateRequest request, List<BinaryContentCreateRequest> attachments) {

        // TODO: getReferenceById -> 프록시 사용 가능 -> 고려
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("채널이 존재하지 않습니다. id: " + request.channelId()));
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new NoSuchElementException("작성자가 존재하지 않습니다. id: " + request.authorId()));

        // 첨부파일 엔티티 생성
        List<BinaryContent> attachmentEntities = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for (BinaryContentCreateRequest fileReq : attachments) {
                attachmentEntities.add(new BinaryContent(fileReq.fileName(), (long) fileReq.bytes().length, fileReq.contentType()));
            }
        }

        // Message 엔티티 생성
        Message message = new Message(request.content(), channel, author, attachmentEntities);

        // 영속성 전이
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    @Override
    public Message findById(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        if(!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("채널이 존재하지 않습니다. id: " + channelId);
        }

        return messageRepository.findAllByChannelIdOrderByCreatedAtAsc(channelId);
    }

    // 사진은 update 불가능
    @Transactional
    @Override
    public Message update(UUID messageId, MessageUpdateRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));
        message.update(request.newContent());
        return message;
    }

    @Transactional
    @Override
    public void delete(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("해당 메시지가 존재하지 않습니다. id: " + messageId));

        // 영속성 전이 + 고아 객체 제거
        messageRepository.delete(message);
    }
}

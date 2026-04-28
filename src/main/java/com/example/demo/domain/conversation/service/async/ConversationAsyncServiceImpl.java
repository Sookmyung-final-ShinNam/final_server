package com.example.demo.domain.conversation.service.async;

import com.example.demo.domain.conversation.service.command.ConversationCompleteMediaCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationAsyncServiceImpl implements ConversationAsyncService {

    private final ConversationCompleteMediaCommandService conversationCompleteMediaCommandService;

    @Async
    @Override
    @Transactional
    public void generateStoryVideo(Long storyId) {
        conversationCompleteMediaCommandService.generateStoryMedia(storyId, "video");
        System.out.println("비동기 작업 완료: storyId=" + storyId + " 동영상 생성 완료");
    }
}
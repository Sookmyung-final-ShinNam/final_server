package com.example.demo.domain.dashboard.listener;

import com.example.demo.domain.conversation.event.StoryCompletedEvent;
import com.example.demo.domain.dashboard.service.command.DashboardCommandService;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class StoryCompletedEventListener {

    private final DashboardCommandService dashboardCommandService;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStoryCompleted(StoryCompletedEvent event) {
        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        DashboardResponse dashboardResponse = dashboardCommandService.updateByStory(event.getStoryId(), user);
        System.out.println("대시보드 업데이트 완료 : " + dashboardResponse);
    }

}
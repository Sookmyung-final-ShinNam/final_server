package com.example.demo.domain.user.scheduler;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")  // 매일 00:00 실행
    public void deleteScheduledUsers() {

        LocalDate today = LocalDate.now();

        // 오늘 날짜 0시 ~ 23:59:59 사이에 탈퇴 요청된 사용자만 삭제
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = today.atStartOfDay();

        List<User> usersToDelete = userRepository.findByStatusAndDeletedAtBetween(
                User.UserStatus.DELETED,
                startOfYesterday,
                endOfYesterday
        );

        if (!usersToDelete.isEmpty()) {
            log.info("삭제 예정 사용자 수: {}", usersToDelete.size());
            userRepository.deleteAll(usersToDelete);
            log.info("삭제 완료");
        }
    }

}
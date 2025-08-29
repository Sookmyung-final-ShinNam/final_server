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

    @Scheduled(fixedRate = 60_000) // 테스트용 1분마다 실행
    // @Scheduled(cron = "0 0 0 * * *")  // 매일 00:00 실행
    public void deleteScheduledUsers() {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay(); // 전날 0시
        LocalDateTime endOfYesterday = today.atStartOfDay();                 // 오늘 0시

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
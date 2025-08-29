package com.example.demo.domain.user.scheduler;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Scheduled(fixedRate = 60_000) // 테스트용: 1분마다 실행
    // 매일 자정 0시에 실행
    // @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void deleteScheduledUsers() {

        // 서울 기준 오늘 날짜
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 과거부터 오늘 자정까지
        LocalDateTime startOfAll = LocalDateTime.of(1970, 1, 1, 0, 0); // DB에 존재하는 모든 사용자 포함
        LocalDateTime endOfToday = nowSeoul.toLocalDate().plusDays(1).atStartOfDay(); // 오늘 밤 12시 (내일 0시)

        List<User> usersToDelete = userRepository.findByStatusAndDeletedAtBetween(
                User.UserStatus.DELETED,
                startOfAll,
                endOfToday
        );

        if (!usersToDelete.isEmpty()) {
            log.info("삭제 예정 사용자 수: {}", usersToDelete.size());
            userRepository.deleteAll(usersToDelete);
            log.info("삭제 완료");
        } else {
            log.info("삭제할 사용자 없음");
        }
    }

}
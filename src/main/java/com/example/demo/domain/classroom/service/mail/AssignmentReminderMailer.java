package com.example.demo.domain.classroom.service.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 과제 미제출 학생 알림 메일 발송
 *
 * - 학생별로 따로 발송 (다른 학생의 이메일이 노출되지 않도록 수신자를 합치지 않음)
 * - 비동기 발송: 한 명의 실패가 나머지 발송이나 선생님 요청 응답에 영향을 주지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentReminderMailer {

    private static final DateTimeFormatter DUE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd a h:mm", Locale.KOREAN);

    private final JavaMailSender javaMailSender;

    @Async
    public void sendReminders(List<String> emails, String teacherName, String classroomName,
                              String assignmentTitle, LocalDateTime dueAt) {

        // 선생님이 입력한 값이 메일 HTML에 들어가므로 이스케이프
        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                    <h2 style="color: #F3A600;">Storictor 과제 알림</h2>
                    <p><b>%s</b> 학급의 <b>%s</b> 선생님께서<br>
                    아직 제출하지 않은 과제가 있다고 알려 주셨어요.</p>
                    <div style="background: #FEF6E4; padding: 20px; border-radius: 8px;">
                        <div style="font-size: 18px; font-weight: bold;">%s</div>
                        <div style="color: #888; margin-top: 8px;">마감: %s</div>
                    </div>
                    <p>Storictor 앱의 <b>설정 &gt; 나의 학급</b>에서 과제를 확인하고<br>
                    동화를 만들어 제출해 주세요!</p>
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(classroomName),
                HtmlUtils.htmlEscape(teacherName),
                HtmlUtils.htmlEscape(assignmentTitle),
                dueAt.format(DUE_FORMAT));

        for (String email : emails) {
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                helper.setTo(email);
                helper.setSubject("[Storictor] 아직 제출하지 않은 과제가 있어요");
                helper.setText(html, true);
                javaMailSender.send(message);
            } catch (Exception e) {
                // 일부 실패해도 나머지 학생에게는 계속 발송
                log.warn("과제 알림 메일 발송 실패 - assignment: {}, reason: {}", assignmentTitle, e.getMessage());
            }
        }
    }
}

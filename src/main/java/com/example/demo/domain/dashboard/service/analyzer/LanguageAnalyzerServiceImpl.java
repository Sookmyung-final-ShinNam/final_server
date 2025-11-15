package com.example.demo.domain.dashboard.service.analyzer;

import com.example.demo.domain.conversation.entity.ConversationFeedback;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.dashboard.converter.DashboardLanguageConverter;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import com.example.demo.domain.dashboard.entity.FeedbackAttemptStats;
import com.example.demo.domain.dashboard.repository.DashboardStoryStatsRepository;
import com.example.demo.domain.story.entity.Story;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import scala.collection.Seq;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 언어 통계 처리 서비스
 */
@Service
@RequiredArgsConstructor
public class LanguageAnalyzerServiceImpl implements DashboardAnalyzerService {

    private final DashboardStoryStatsRepository statsRepo;
    private final DashboardLanguageConverter converter;

    @Override
    public void apply(Dashboard dashboard, Story story) {

        // 기존 데이터 조회 (없으면 신규 생성)
        DashboardStoryStats stats = statsRepo.findByDashboardAndStoryId(dashboard, story.getId())
                .orElseGet(() -> DashboardStoryStats.builder()
                        .dashboard(dashboard)
                        .storyId(story.getId())
                        .build()
                );

        // 1. 기/승/전/결 시도 횟수
        int[] attempts = calculateAttemptCounts(story);

        FeedbackAttemptStats attemptStats = converter.toAttemptStats(
                attempts[0], attempts[1], attempts[2], attempts[3]
        );

        // 2. 전체 평균 시도수
        double avgAttemptPerStage = Arrays.stream(attempts).average().orElse(0);

        // 3. 평균 답변 길이
        int avgAnswerLength = calculateAverageAnswerLength(story);

        // 4. 새로운 단어 추출
        List<String> newWords = extractNewWords(dashboard, story);

        // 5. 최종 DashboardStoryStats 생성
        stats.setFeedbackAttemptStats(attemptStats);
        stats.setAvgAttemptPerStage(avgAttemptPerStage);
        stats.setAvgAnswerLength(avgAnswerLength);
        stats.setNewWords(newWords);

        statsRepo.save(stats);

    }

    // 메시지별 feedback 개수 기준 기/승/전/결 카운트
    private int[] calculateAttemptCounts(Story story) {

        int[] counts = new int[4]; // gi, seung, jeon, gyeol

        for (ConversationSession session : story.getStorySessions()) {
            List<ConversationMessage> messages = session.getMessages();

            for (int i = 0; i < messages.size(); i++) {
                ConversationMessage msg = messages.get(i);
                int feedbackCount = msg.getFeedbacks().size(); // 메시지가 가진 feedback 수
                int stepIndex;

                // 메시지 순서대로 기승전결 단계 매핑 (START는 제외)
                switch (i) {
                    case 1 -> stepIndex = 0; // STEP_01 → GI
                    case 2 -> stepIndex = 1; // STEP_02 → SEUNG
                    case 3 -> stepIndex = 2; // STEP_03 → JEON
                    case 4 -> stepIndex = 3; // END → GYEOL
                    default -> {
                        continue; // START 메시지 제외
                    }
                }

                counts[stepIndex] += feedbackCount;
            }
        }

        return counts;
    }

    // 평균 user_answer 길이
    private int calculateAverageAnswerLength(Story story) {
        List<String> answers = story.getStorySessions().stream()
                .flatMap(s -> s.getMessages().stream())
                .flatMap(m -> m.getFeedbacks().stream())
                .map(ConversationFeedback::getUserAnswer)
                .filter(Objects::nonNull)
                .toList();

        if (answers.isEmpty()) return 0;

        int totalLength = answers.stream()
                .mapToInt(String::length)
                .sum();

        return totalLength / answers.size();
    }

    // 새 단어 추출
    private List<String> extractNewWords(Dashboard dashboard, Story story) {

        Set<String> existingWords = statsRepo.findAllByDashboard(dashboard)
                .stream()
                .flatMap(s -> Optional.ofNullable(s.getNewWords()).orElse(Collections.emptyList()).stream())
                .collect(Collectors.toSet());

        List<String> currentWords = extractWordsFromStory(story);

        if (currentWords.isEmpty()) return Collections.emptyList();

        return currentWords.stream()
                .filter(w -> !existingWords.contains(w))
                .distinct()
                .toList();
    }

    // user_answer 에서 단어 추출 (한국어 형태소 분석)
    private List<String> extractWordsFromStory(Story story) {
        List<String> result = new ArrayList<>();

        for (ConversationFeedback f : story.getStorySessions().stream()
                .flatMap(s -> s.getMessages().stream())
                .flatMap(m -> m.getFeedbacks().stream())
                .toList()) {

            String answer = f.getUserAnswer();
            if (answer != null && !answer.isBlank()) {
                Seq<KoreanToken> tokensSeq = OpenKoreanTextProcessorJava.tokenize(answer);
                scala.collection.Iterator<KoreanToken> iter = tokensSeq.iterator();
                while (iter.hasNext()) {
                    KoreanToken token = iter.next();
                    result.add(token.text().toLowerCase());
                }
            }
        }
        return result;
    }

}
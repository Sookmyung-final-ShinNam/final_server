package com.example.demo.domain.dashboard.service.analyzer;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.SessionStep;
import com.example.demo.domain.conversation.entity.StepAttempt;
import com.example.demo.domain.dashboard.converter.DashboardLanguageConverter;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import com.example.demo.domain.dashboard.entity.FeedbackAttemptStats;
import com.example.demo.domain.dashboard.repository.DashboardAnalyticsRepository;
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
    private final DashboardAnalyticsRepository analyticsRepo;
    private final DashboardLanguageConverter converter;

    @Override
    public void apply(Dashboard dashboard, Story story) {

        List<StepAttempt> attempts = analyticsRepo.findAllAttemptsByStoryId(story.getId());

        // 기존 데이터 조회 (없으면 신규 생성)
        DashboardStoryStats stats = statsRepo.findByDashboardAndStoryId(dashboard, story.getId())
                .orElseGet(() -> DashboardStoryStats.builder()
                        .dashboard(dashboard)
                        .storyId(story.getId())
                        .build()
                );

        // 1. 기/승/전/결 시도 횟수
        int[] counts = calculateAttemptCounts(attempts);

        FeedbackAttemptStats attemptStats = converter.toAttemptStats(
                counts[0], counts[1], counts[2], counts[3]
        );

        // 2. 평균 시도 수
        double avgAttemptPerStage = Arrays.stream(counts).average().orElse(0);

        // 3. 평균 답변 길이
        int avgAnswerLength = calculateAverageAnswerLength(attempts);

        // 4. 새로운 단어
        List<String> newWords = extractNewWords(dashboard, attempts);

        stats.setFeedbackAttemptStats(attemptStats);
        stats.setAvgAttemptPerStage(avgAttemptPerStage);
        stats.setAvgAnswerLength(avgAnswerLength);
        stats.setNewWords(newWords);

        statsRepo.save(stats);
    }

    /**
     * 기/승/전/결 단계별 시도 횟수 집계
     * 기준: StepAttempt 개수
     * */
    private int[] calculateAttemptCounts(List<StepAttempt> attempts) {

        int[] counts = new int[4]; // 기, 승, 전, 결

        for (StepAttempt attempt : attempts) {

            SessionStep step = attempt.getStep();
            ConversationSession.ConversationStep type = step.getStepType();

            if (type == ConversationSession.ConversationStep.START ||
                    type == ConversationSession.ConversationStep.END) continue;

            switch (type) {
                case 기 -> counts[0]++;
                case 승 -> counts[1]++;
                case 전 -> counts[2]++;
                case 결 -> counts[3]++;
            }
        }

        return counts;
    }

    /**
     * 평균 userAnswer 길이
     * 기준: 모든 StepAttempt.userAnswer
     */
    private int calculateAverageAnswerLength(List<StepAttempt> attempts) {

        List<String> answers = attempts.stream()
                .map(StepAttempt::getUserAnswer)
                .filter(Objects::nonNull)
                .filter(a -> !a.isBlank())
                .toList();

        if (answers.isEmpty()) return 0;

        int total = answers.stream()
                .mapToInt(String::length)
                .sum();

        return total / answers.size();
    }

    /**
     * 새로운 단어 추출
     * 기존 dashboard에 저장된 단어 제외
     */
    private List<String> extractNewWords(Dashboard dashboard, List<StepAttempt> attempts) {

        Set<String> existingWords = statsRepo.findAllByDashboard(dashboard)
                .stream()
                .flatMap(s -> Optional.ofNullable(s.getNewWords())
                        .orElse(Collections.emptyList())
                        .stream())
                .collect(Collectors.toSet());

        List<String> currentWords = extractWords(attempts);

        if (currentWords.isEmpty()) return Collections.emptyList();

        return currentWords.stream()
                .filter(w -> !existingWords.contains(w))
                .distinct()
                .toList();
    }

    /**
     * StepAttempt.userAnswer 기반 형태소 분석
     */
    private List<String> extractWords(List<StepAttempt> attempts) {

        List<String> result = new ArrayList<>();

        for (StepAttempt attempt : attempts) {

            String answer = attempt.getUserAnswer();
            if (answer == null || answer.isBlank()) continue;

            Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(answer);
            var iter = tokens.iterator();

            while (iter.hasNext()) {
                result.add(iter.next().text().toLowerCase());
            }
        }

        return result;
    }

}
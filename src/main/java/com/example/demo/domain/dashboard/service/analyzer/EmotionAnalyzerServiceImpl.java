package com.example.demo.domain.dashboard.service.analyzer;

import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import com.example.demo.domain.dashboard.repository.DashboardRepository;
import com.example.demo.domain.dashboard.repository.DashboardStoryStatsRepository;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 정서 통계 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAnalyzerServiceImpl implements DashboardAnalyzerService {

    private final DashboardRepository dashboardRepository;
    private final DashboardStoryStatsRepository statsRepository;
    private final LlmClient llmClient;

    @Override
    @Transactional
    public void apply(Dashboard dashboard, Story story) {

        log.info("EmotionAnalyzer 시작 - Dashboard ID: {}, Story ID: {}", dashboard.getId(), story.getId());

        // 1. 스토리 페이지 내용 합치기
        String combinedText = story.getStoryPages().stream()
                .sorted(Comparator.comparingInt(StoryPage::getPageNumber))
                .map(StoryPage::getContent)
                .filter(s -> s != null)
                .map(s -> s.replace("\n", " ").replace("\r", " ")) // 엔터 제거
                .collect(Collectors.joining(" "));

        // 2. LLM 감정 분석 호출
        String prompt = llmClient.buildPrompt("emotion_analysis_prompt.json", llmClient.jsonEscape(combinedText));
        log.info("LLM Prompt:\n{}", prompt);

        String llmResponse = llmClient.callChatGpt(prompt);
        log.info("LLM Response:\n{}", llmResponse);

        // 3. 감정 결과 저장
        DashboardStoryStats stats = statsRepository.findByDashboardAndStoryId(dashboard, story.getId())
                .orElseGet(() -> DashboardStoryStats.builder()
                        .dashboard(dashboard)
                        .storyId(story.getId())
                        .build()
                );

        stats.setJoy(parseDoubleOrZero(llmClient.extractFieldValue(llmResponse, "joy")));
        stats.setSadness(parseDoubleOrZero(llmClient.extractFieldValue(llmResponse, "sadness")));
        stats.setAnger(parseDoubleOrZero(llmClient.extractFieldValue(llmResponse, "anger")));
        stats.setFear(parseDoubleOrZero(llmClient.extractFieldValue(llmResponse, "fear")));
        stats.setSurprise(parseDoubleOrZero(llmClient.extractFieldValue(llmResponse, "surprise")));
        stats.setNeutral(parseDoubleOrZero(llmClient.extractFieldValue(llmResponse, "neutral")));
        stats.setSummary(llmClient.extractFieldValue(llmResponse, "summary"));
        statsRepository.save(stats);

        // 4. 최근 5개 스토리 감정 조회
        List<DashboardStoryStats> last5 = statsRepository
                .findTop5ByDashboard_User_IdOrderByCreatedAtDesc(dashboard.getUser().getId());

        // 5. 최근 5개 감정 문자열 변환
        String emotionsSummary = last5.stream()
                .map(e -> String.format(
                        "{joy: %.2f, sadness: %.2f, anger: %.2f, fear: %.2f, surprise: %.2f, neutral: %.2f, summary: %s}",
                        e.getJoy(), e.getSadness(), e.getAnger(),
                        e.getFear(), e.getSurprise(), e.getNeutral(),
                        e.getSummary() != null ? e.getSummary() : ""
                ))
                .collect(Collectors.joining("\n"));
        log.info("Emotions Summary:\n{}", emotionsSummary);

        // 6. 부모 조언 생성
        String sanitizedSummary = emotionsSummary.replace("\n", " ").replace("\r", " "); // 줄바꿈 제거
        String parentPrompt = llmClient.buildPrompt(
                "parent_advice_prompt.json",
                llmClient.jsonEscape("최근 5개 동화 정서:" + sanitizedSummary)
        );

        String parentAdviceResponse = llmClient.callChatGpt(parentPrompt);
        log.info("Parent Advice LLM Response:\n{}", parentAdviceResponse);

        String parentAdvice = llmClient.extractFieldValue(parentAdviceResponse, "parentAdvice");
        log.info("Generated Parent Advice: {}", parentAdvice);

        // 7. Dashboard에 부모 조언 저장
        dashboard.setParentAdvice(parentAdvice);
        dashboardRepository.save(dashboard);
    }

    private double parseDoubleOrZero(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("parseDouble 실패, value: {}", value);
            return 0.0;
        }
    }

}
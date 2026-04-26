package com.example.demo.domain.conversation.service.async;

public interface ConversationAsyncService {

    /**
     * 다음 단계 시작 (특정 단계에 대한 사전 생성 - 시작 조건은
     * 1. 현재 단계가 start 이면 ConversationSession.ConversationStep = START일때 -> 기 단계 사전 생성,
     * 2. 현재 단계가 "기" 이면  - 기의 상태가 완료일때 -> 승 단계 사전 생성,
     * 3. 현재 단계가 "승" 이면 - 승의 상태가 완료일때 -> 전 단계 사전 생성,
     * 4. 현재 단계가 "전" 이면 - 전의 상태가 완료일때 -> 결 단계 사전 생성,
     * 5. 4개 중 하나가 아니면 -> 잘못된 요청임.
     *
     * 동작
     * 1. session.currentStep → 다음 step으로 이동
     * 2. 해당 SessionStep.status -> IN_PROGRESS
     * 3. 해당 SessionStep.prevContext 저장 -> session.fullStory
     * 4. llm 호출
     *    - input : step.prevContext
     *    - output : nextStory & llmQuestion
     * 5. llm 호출 결과 저장
     * 	 - step.nextStory
     * 	 - step.llmQuestion
     */
    void startNextStep(Long sessionId);

    /**
     * 스토리 완료 처리
     * - Story 상태 MAKING 으로 변경
     * - Story 업데이트 (제목, 3줄 요약)
     * - StoryPage 생성 후 저장
     * - StoryCharacter 업데이트 (성격, 기본 이미지)
     * - StoryPage 업데이트 (각 장별 이미지)
     * - Story, StoryCharacter 상태 COMPLETED 으로 변경
     */
    void storyComplete(Long sessionId);

    /**
     * 동영상 동화 생성
     * - StoryCharacter 기본 이미지로 동영상 생성
     */
    void generateStoryVideo(Long storyId);

}
package com.example.demo.domain.conversation.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.CharacterAppearance;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.CharacterAppearanceRepository;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.event.PageImageCompletedEvent;
import com.example.demo.domain.conversation.event.PageImageStartedEvent;
import com.example.demo.domain.conversation.event.StoryCompletedEvent;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.service.model.S3Uploader;
import com.example.demo.domain.conversation.service.model.image.AvatarGeneratorService;
import com.example.demo.domain.conversation.service.model.image.FluxResponse;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.conversation.service.model.video.RunwayService;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.repository.StoryPageRepository;
import com.example.demo.domain.story.repository.StoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationCompleteCommandServiceImpl implements ConversationCompleteCommandService {

    private final StoryPageRepository storyPageRepo;
    private final StoryRepository storyRepo;
    private final ConversationSessionRepository sessionRepo;
    private final CharacterAppearanceRepository characterAppearanceRepo;

    private final LlmClient llmClient;
    private final S3Uploader s3Uploader;
    private final ApplicationEventPublisher eventPublisher;

    private final AvatarGeneratorService avatarGeneratorService;
    private final RunwayService runwayService;

    @Override
    @Transactional
    public void completeConversation(Long sessionId) {

        // 1. Story 및 Session 조회
        Story story = storyRepo.findByStorySessions_Id(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        // 2. 현재 단계가 END 인지 확인
        if (session.getCurrentStep() != ConversationSession.ConversationStep.END) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }

        // 3. 마지막 메시지 조회
        ConversationMessage lastMessage = session.getMessages().isEmpty()
                ? null
                : session.getMessages().get(session.getMessages().size() - 1);

        if (lastMessage == null || lastMessage.getLlmAnswer() == null) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }

        // 4. 상태 변경 -> MAKING 에서는 이어하기 불가
        if (story.getStatus() == Story.StoryStatus.IN_PROGRESS) {
            story.setStatus(Story.StoryStatus.MAKING);
        }
    }

    @Override
    @Transactional
    public void completeStoryFromLlm(Long storyId, String context) {

        // 1. LLM 호출 - story_complete.json
        String variable = llmClient.jsonEscape("원본 스토리: " + context);
        String prompt = llmClient.buildPrompt("story_complete.json", variable);
        String completeResponse = llmClient.callChatGpt(prompt);

        // 2. LLM 응답 파싱
        String title = llmClient.extractFieldValue(completeResponse, "title");
        String summary = llmClient.extractFieldValue(completeResponse, "summary");
        String characterPersonality = llmClient.extractFieldValue(completeResponse, "characterPersonality");
        String firstPage = llmClient.extractFieldValue(completeResponse, "firstPage");
        String secondPage = llmClient.extractFieldValue(completeResponse, "secondPage");
        String thirdPage = llmClient.extractFieldValue(completeResponse, "thirdPage");
        String fourthPage = llmClient.extractFieldValue(completeResponse, "fourthPage");

        // 3. Story 조회 및 업데이트
        Story story = storyRepo.findById(storyId).orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        story.setTitle(title);
        story.setDescription(summary);

        // 4. Character 업데이트
        story.getCharacter().setPersonality(characterPersonality);

        // 5. LLM 호출 - story_for_image.json
        variable = llmClient.jsonEscape(
                "title: " + title +
                        ", summary: " + summary +
                        ", characterName: " + story.getCharacter().getName() +
                        ", characterAge: " + story.getCharacter().getAge() +
                        ", characterGender: " + story.getCharacter().getGender() +
                        ", characterEyeColor: " + story.getCharacter().getAppearance().getEyeColor() +
                        ", characterHairColor:" + story.getCharacter().getAppearance().getHairColor() +
                        ", characterHairStyle:" + story.getCharacter().getAppearance().getHairStyle() +
                        ", characterPersonality:" + characterPersonality +
                        ", storyTheme: " + story.getStoryThemes() +
                        ", storyBackground: " + story.getStoryBackgrounds() +
                        ", firstPage:" + firstPage +
                        ", secondPage:" + secondPage +
                        ", thirdPage: " + thirdPage +
                        ", fourthPage:" + fourthPage
        );
        prompt = llmClient.buildPrompt("story_for_image.json", variable);
        String imageResponse = llmClient.callChatGpt(prompt);

        // 6. LLM 응답 파싱
        String firstPageEn = llmClient.extractFieldValue(imageResponse, "firstPageEn");
        String secondPageEn = llmClient.extractFieldValue(imageResponse, "secondPageEn");
        String thirdPageEn = llmClient.extractFieldValue(imageResponse, "thirdPageEn");
        String fourthPageEn = llmClient.extractFieldValue(imageResponse, "fourthPageEn");
        String characterPromptEn = llmClient.extractFieldValue(imageResponse, "characterPromptEn");
        String characterImagePromptEn = llmClient.extractFieldValue(imageResponse, "characterImagePromptEn");

        // 7. StoryPage 저장
        String[] pages = {firstPage, secondPage, thirdPage, fourthPage};
        String[] pagesEn = {firstPageEn, secondPageEn, thirdPageEn, fourthPageEn};

        for (int i = 0; i < pages.length; i++) {
            StoryPage page = StoryPage.builder()
                    .pageNumber(i + 1)
                    .content(pages[i])
                    .contentEn(pagesEn[i])
                    .videoStatus(StoryPage.VideoStatus.NONE)
                    .story(story)
                    .build();

            story.getStoryPages().add(page);
            storyPageRepo.save(page);
        }

        // 8. CharacterAppearance 업데이트
        CharacterAppearance appearance = story.getCharacter().getAppearance();
        appearance.setCharacterPromptEn(characterPromptEn);
        appearance.setCharacterImagePromptEn(characterImagePromptEn);
        characterAppearanceRepo.save(appearance);

        story.setStatus(Story.StoryStatus.COMPLETED); // 스토리 상태 업데이트 - 텍스트 생성 완료
    }

    @Override
    @Transactional
    public void generateStoryMedia(Long storyId, String imageType) {

        log.info("[Media] generateStoryMedia 시작, storyId={}, imageType={}", storyId, imageType);

        // 1. imageType 유효성 검사
        validateImageType(imageType);

        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        StoryCharacter character = story.getCharacter();

        // 2. 페이지별 미디어 생성 (이미지 or 영상) - (S3 업로드 포함)
        if (isImage(imageType)) {
            // 스토리 캐릭터 이미지 생성
            generateCharacterBaseImage(character);
            story.getCharacter().setStatus(StoryCharacter.CharacterStatus.COMPLETED); // 캐릭터 상태 업데이트 - 이미지 생성 완료

            // 스토리 페이지 이미지 생성
            generateStoryImages(story, character); // 페이지별 생성 이벤트 발행
        } else {
            // 스토리 페이지별 동영상 생성
            generateStoryVideos(story, character);
            // 동영상 생성 여부 업데이트
            for (StoryPage page : story.getStoryPages()) {
                if (page.getVideoUrl() != null && page.getVideoUrl().endsWith(".mp4")) {
                    page.setVideoStatus(StoryPage.VideoStatus.COMPLETED);
                }
            }
            // 스토리 전체 상태 업데이트
            story.setVideoStatus(StoryPage.VideoStatus.COMPLETED);
        }

        log.info("[Media] generateStoryMedia 완료, storyId={}", story.getId());
    }

    private void validateImageType(String imageType) {
        if (!isImage(imageType) && !isVideo(imageType)) {
            log.warn("[Media] 잘못된 imageType={}", imageType);
            throw new CustomException(ErrorStatus.MEDIA_INVALID_INPUT_VALUE);
        }
    }

    private boolean isImage(String type) {
        return "image".equalsIgnoreCase(type);
    }

    private boolean isVideo(String type) {
        return "video".equalsIgnoreCase(type);
    }

    /**
     * 캐릭터 기본 이미지 생성 및 S3 업로드
     * - 프롬프트 기반 아바타 생성
     * - seed 저장 (추후 일관된 이미지 생성을 위해 사용)
     * - 캐릭터 대표 이미지 URL 저장
     */
    private void generateCharacterBaseImage(StoryCharacter character) {

        // 1. 이미 생성된 이미지의 경우 다시 생성 X
        if (character.getImageUrl() != null && !character.getImageUrl().isBlank()) {
            log.error("===== [Avatar] 캐릭터 이미지 이미 생성됨, characterId={} =====", character.getId());
            return;
        }

        String prompt = character.getAppearance().getCharacterImagePromptEn();
        log.error("===== [Avatar] START generateCharacterBaseImage =====");
        log.error("[Avatar] Prompt = {}", prompt);

        try {
            // 2. Avatar API 호출
            log.error("[Avatar] 요청 시작: generateAvatarWithReference");
            FluxResponse.FluxEndResponse result = avatarGeneratorService
                    .generateAvatarWithReference(prompt, null, null, true)
                    .block();
            log.error("[Avatar] 요청 완료, result null 여부 = {}", (result == null));

            if (result == null) {
                log.error("[Avatar] result == null, 이미지 생성 실패");
                throw new RuntimeException("Avatar API returned null");
            }

            log.error("[Avatar] result.getImgUrl() = {}", result.getImgUrl());
            log.error("[Avatar] result.getSeed() = {}", result.getSeed());

            // 3. 파일 다운로드
            log.error("[Avatar] 파일 다운로드 시작 (URL={})", result.getImgUrl());
            handleFileWithTemp(result.getImgUrl(), character.getId(), 0, tempFile -> {

                log.error("[Avatar] 다운로드 완료, tempFile exists={}, size={}",
                        tempFile.exists(), tempFile.length());

                if (!tempFile.exists() || tempFile.length() == 0) {
                    throw new RuntimeException("Downloaded temp file is empty");
                }

                // 4. S3 업로드
                log.error("[Avatar] S3 업로드 시작");
                String s3Url = s3Uploader.uploadFileFromFile(tempFile, "characters",
                        "character_" + character.getId() + ".png");
                log.error("[Avatar] S3 업로드 완료, URL={}", s3Url);

                character.getAppearance().setCharacterSeed(result.getSeed());
                character.setImageUrl(s3Url);
            });

            log.error("===== [Avatar] END SUCCESS generateCharacterBaseImage =====");

        } catch (Exception e) {
            log.error("===== [Avatar] ERROR OCCURRED =====");
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Exception stacktrace ↓");
            e.printStackTrace();
            throw e;  // CustomException 던지지 말고 원본 예외 그대로
        }
    }

    /**
     * 스토리 페이지 이미지 생성
     * - 캐릭터 basePrompt, seed 사용 → 일관된 스타일 유지
     * - 페이지별 생성 이벤트 발행함으로써 이미지 생성
     */
    private void generateStoryImages(Story story, StoryCharacter character) {
        String basePrompt = character.getAppearance().getCharacterPromptEn(); // 포즈 없이 외형만 정리된 프롬프트
        Long seed = character.getAppearance().getCharacterSeed(); // 캐릭터 고정 시드

        // 페이지별 이미지 생성 이벤트 발행 → 리스너 generatePageImage 처리
        for (StoryPage page : story.getStoryPages()) {
            if (page.getStatus() == StoryPage.PageStatus.TEXT) {
                eventPublisher.publishEvent(new PageImageStartedEvent(story.getId(), page.getId(), basePrompt, seed));
            }
        }
    }

    /**
     * 이미지 생성 이벤트 처리 로직
     * - 스토리 각 페이지별 이미지 생성 및 상태 업데이트
     * - 페이지별 prompt 조합 후 이미지 생성 및 S3 업로드
     */
    @Override
    @Transactional
    public void generateStoryImage(Long storyId, Long pageId, String basePrompt, Long seed) {

        // 1. Page 조회
        StoryPage page = storyPageRepo.findById(pageId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_PAGE_NOT_FOUND));

        // 2. Page 상태가 TEXT인지 확인
        if (page.getStatus() != StoryPage.PageStatus.TEXT) {
            log.info("===== [Page] {}번째 페이지 이미지 이미 생성됨: pageId = {}, storyId = {} =====", page.getPageNumber(), page.getId(), storyId);
            return;
        }

        // 3. 이미지 생성 시작
        log.info("===== [Page] {}번째 페이지 이미지 생성 시작: pageId = {}, storyId = {} =====", page.getPageNumber(), page.getId(), storyId);

        try {
            // 4. 이미지 API 호출
            FluxResponse.FluxEndResponse result = avatarGeneratorService
                    .generateAvatarWithReference(basePrompt, page.getContentEn(), seed, false)
                    .block();

            // 5. S3 업로드
            if (result != null) {
                handleFileWithTemp(result.getImgUrl(), storyId, page.getPageNumber(), tempFile -> {
                    String s3Url = s3Uploader.uploadFileFromFile(tempFile,
                            "stories/" + storyId,
                            "page_" + page.getPageNumber() + ".png");
                    page.setImageUrl(s3Url);
                });
            }

            // 6. DB 저장 (Page.status = IMAGE)
            page.setStatus(StoryPage.PageStatus.IMAGE); // 페이지 상태 업데이트 - 이미지 생성 완료
            log.info("===== [Page] {}번째 페이지 이미지 생성 완료: pageId = {}, storyId = {} =====", page.getPageNumber(), page.getId(), storyId);

            // 7. 이미지 생성 완료 이벤트 발행 → 리스너 aggregateStoryPage 처리
            eventPublisher.publishEvent(new PageImageCompletedEvent(storyId));

        } catch (Exception e) {
            log.error("===== [Page] {}번째 페이지 ERROR OCCURRED: pageId = {} =====", page.getPageNumber(), page.getId(), e);
            throw e;  // CustomException 던지지 말고 원본 예외 그대로
        }
    }

    /**
     * 이미지 생성 완료 이벤트 처리 로직
     * - 이미지 생성 완료된 페이지 개수 확인
     * - 이후 스토리 상태 업데이트
     */
    @Override
    @Transactional
    public void aggregateStoryPage(Long storyId) {

        // 1. 이미지 생성 완료된 페이지 개수 조회
        int imageCount = storyPageRepo.countByStoryIdAndStatus(storyId, StoryPage.PageStatus.IMAGE);

        // 2. 모든 페이지가 모두 생성 완료된 경우 스토리 상태 업데이트 (페이지 개수 = 4)
        if(imageCount == 4) {
            Story story = storyRepo.findById(storyId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

            if (story.getStatus() != Story.StoryStatus.READY_IMAGE) { // 중복 방지
                // 3. 스토리 상태 업데이트 - 모든 이미지 생성 완료
                story.setStatus(Story.StoryStatus.READY_IMAGE);
                log.info("===== [Story] 스토리 이미지 모두 생성 완료: storyId = {} =====", storyId);

                // 4. 스토리 생성 완료 이벤트 발행
                eventPublisher.publishEvent(new StoryCompletedEvent(this, story.getId(), story.getUser().getId()));
            }
        }
    }

    /**
     * 스토리 각 페이지별 영상 생성
     * - 캐릭터 이미지 → Runway API 기반 영상 변환
     * - 최대 3번 재시도 (네트워크 오류 등 대응)
     * - 성공 시 S3 업로드
     */
    private void generateStoryVideos(Story story, StoryCharacter character) {
        String characterImageUrl = character.getImageUrl();

        for (StoryPage page : story.getStoryPages()) {

            // 이미 생성 진행 중이거나 완료된 경우 스킵
            if (page.getVideoStatus() != StoryPage.VideoStatus.NONE) {
                log.info("[Media] Skip video generation (status={}), storyId={}, page={}",
                        page.getVideoStatus(), story.getId(), page.getPageNumber());
                continue;
            }

            // 생성 시작
            page.setVideoStatus(StoryPage.VideoStatus.MAKING);
            storyPageRepo.save(page);

            int maxAttempts = 3;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    String videoUrl = createVideo(characterImageUrl, story, page, attempt);

                    handleFileWithTemp(videoUrl, story.getId(), page.getPageNumber(), videoFile -> {
                        String s3Url = s3Uploader.uploadFileFromFile(videoFile,
                                "stories/" + story.getId() + "/videos",
                                "page_" + page.getPageNumber() + ".mp4");
                        page.setVideoUrl(s3Url);
                        storyPageRepo.save(page); // DB 저장
                    });

                    break; // 성공 시 attempt 루프 종료

                } catch (Exception e) {
                    log.warn("[Media] Attempt {} failed for storyId={}, page={}, error={}", attempt, story.getId(), page.getPageNumber(), e.getMessage());
                    if (attempt == maxAttempts) {
                        throw new RuntimeException("❌ Video generation failed after " + maxAttempts + " attempts", e);
                    }
                }
            }
        }
    }

    /**
     * Runway API 호출을 통해 영상 생성
     * - 캐릭터 이미지 파일 다운로드 → API 호출
     * - 프롬프트가 비어있으면 실패 처리
     */
    private String createVideo(String characterImageUrl, Story story, StoryPage page, int attempt) throws IOException, InterruptedException {

        if (page.getContentEn() == null || page.getContentEn().isEmpty()) {
            throw new IOException("Page prompt is empty");
        }

        File tempFile = downloadTempFile(characterImageUrl, story.getId(), page.getPageNumber());
        if (!tempFile.exists() || tempFile.length() == 0) {
            throw new IOException("Downloaded character image is empty");
        }

        log.info("[Media] Attempt {}: Creating video, storyId={}, page={}, file={}, prompt={}",
                attempt, story.getId(), page.getPageNumber(), tempFile.getAbsolutePath(), page.getContentEn());

        return runwayService.createImageToVideoAndWait(tempFile, page.getContentEn());
    }

    /**
     * 파일 다운로드/업로드/삭제 공통 처리
     */
    private void handleFileWithTemp(String url, Long storyId, int pageNumber, FileHandler handler) {

        File tempFile = null;
        try {
            tempFile = downloadTempFile(url, storyId, pageNumber);
            handler.handle(tempFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (tempFile != null && tempFile.exists()) tempFile.delete();
        }
    }

    @FunctionalInterface
    private interface FileHandler {
        void handle(File file) throws Exception;
    }

    /**
     * 임시 파일 다운로드
     * URL 기반 파일 다운로드 후 임시 저장 (이후 삭제까지 구현 완료)
     */
    private File downloadTempFile(String url, Long storyId, int pageNumber) throws IOException {

        File tempFile = File.createTempFile("story_" + storyId + "_page_" + pageNumber, ".png");
        tempFile.deleteOnExit();
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

}

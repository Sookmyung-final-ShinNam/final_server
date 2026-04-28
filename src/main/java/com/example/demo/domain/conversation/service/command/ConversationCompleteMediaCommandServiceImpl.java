package com.example.demo.domain.conversation.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.conversation.event.PageImageCompletedEvent;
import com.example.demo.domain.conversation.event.PageImageStartedEvent;
import com.example.demo.domain.conversation.event.StoryCompletedEvent;
import com.example.demo.domain.conversation.service.model.S3Uploader;
import com.example.demo.domain.conversation.service.model.image.AvatarGeneratorService;
import com.example.demo.domain.conversation.service.model.image.FluxResponse;
import com.example.demo.domain.conversation.service.model.video.RunwayService;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.repository.StoryPageRepository;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationCompleteMediaCommandServiceImpl implements ConversationCompleteMediaCommandService {

    private final StoryPageRepository storyPageRepo;
    private final StoryRepository storyRepo;

    private final ApplicationEventPublisher eventPublisher;

    private final S3Uploader s3Uploader;
    private final AvatarGeneratorService avatarGeneratorService;
    private final RunwayService runwayService;

    @Override
    @Transactional
    public void generateStoryMedia(Long storyId, String imageType) {

        log.info("[Media] generateStoryMedia 시작, storyId={}, imageType={}", storyId, imageType);

        // 1. imageType 유효성 검사
        validateImageType(imageType);

        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        StoryCharacter character = story.getCharacter();

        // 페이지별 미디어 생성 (이미지 or 영상) - (S3 업로드 포함)
        if (isImage(imageType)) {
            // -- 이미지 생성
            // 2-1. 캐릭터 이미지 생성 및 상태 업테이트
            generateCharacterBaseImage(character);
            character.setStatus(StoryCharacter.CharacterStatus.COMPLETED); // 이미지 생성 완료

            // 2-2. 스토리 페이지 이미지 생성
            generateStoryImages(story, character); // 페이지별 생성 이벤트 발행
        } else {
            // -- 동영상 생성
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

        // 1. 고정 값 조회
        String basePrompt = character.getAppearance().getCharacterPromptEn(); // 포즈 없이 외형만 정리된 프롬프트
        Long seed = character.getAppearance().getCharacterSeed(); // 캐릭터 고정 시드
        Long storyId = story.getId();

        // 2. 페이지 id만 추출
        List<Long> pageIds = story.getStoryPages().stream()
                .filter(page -> page.getStatus() == StoryPage.PageStatus.TEXT)
                .map(StoryPage::getId)
                .toList();

        // 3. 페이지별 이미지 생성 이벤트 발행 → 리스너 generatePageImage 처리
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        for (Long pageId : pageIds) {
                            eventPublisher.publishEvent(
                                    new PageImageStartedEvent(storyId, pageId, basePrompt, seed)
                            );
                        }
                    }
                }
        );
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
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            eventPublisher.publishEvent(new PageImageCompletedEvent(storyId));
                        }
                    }
            );
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

            if (story.getStatus() != Story.StoryStatus.IMAGE_COMPLETED) { // 중복 방지

                // 3. 스토리 상태 업데이트 - 모든 이미지 생성 완료
                story.setStatus(Story.StoryStatus.IMAGE_COMPLETED);

                // 4. 스토리 생성 완료 이벤트 발행
                Long userId = story.getUser().getId();
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronizationAdapter() {
                            @Override
                            public void afterCommit() {
                                eventPublisher.publishEvent(
                                        new StoryCompletedEvent(this, storyId, userId)
                                );
                            }
                        }
                );

                log.info("===== [Story] 스토리 이미지 모두 생성 완료: storyId = {} =====", storyId);
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
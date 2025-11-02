package com.example.demo.domain.conversation.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.CharacterAppearance;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.CharacterAppearanceRepository;
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
    private final StoryRepository storyRepository;
    private final CharacterAppearanceRepository characterAppearanceRepo;

    private final LlmClient llmClient;
    private final S3Uploader s3Uploader;

    private final AvatarGeneratorService avatarGeneratorService;
    private final RunwayService runwayService;

    @Override
    @Transactional
    public void completeStoryFromLlm(Story story, String context) {

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

        // 3. Story 업데이트
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
    }

    @Override
    @Transactional
    public void generateStoryMedia(Long storyId, String imageType) {

        log.info("[Media] generateStoryMedia 시작, storyId={}, imageType={}", storyId, imageType);

        // 1. imageType 유효성 검사
        validateImageType(imageType);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        StoryCharacter character = story.getCharacter();

        // 2. 페이지별 미디어 생성 (이미지 or 영상) - (S3 업로드 포함)
        if (isImage(imageType)) {
            // 캐릭터 기본 이미지 생성
            generateCharacterBaseImage(character);
            // 스토리 페이지별 이미지 생성
            generateStoryImages(story, character);
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

        String prompt = character.getAppearance().getCharacterImagePromptEn();

        try {
            FluxResponse.FluxEndResponse result = avatarGeneratorService
                    .generateAvatarWithReference(prompt, null, null, true)
                    .block();

            if (result == null) throw new CustomException(ErrorStatus.FILE_UPLOAD_FAILED);

            handleFileWithTemp(result.getImgUrl(), character.getId(), 0, tempFile -> {
                String s3Url = s3Uploader.uploadFileFromFile(tempFile, "characters",
                        "character_" + character.getId() + ".png");
                character.getAppearance().setCharacterSeed(result.getSeed());
                character.setImageUrl(s3Url);
            });

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 스토리 각 페이지별 이미지 생성
     * - 캐릭터 seed 사용 → 일관된 스타일 유지
     * - 페이지별 prompt 조합 후 이미지 생성 및 S3 업로드
     */
    private void generateStoryImages(Story story, StoryCharacter character) {

        String basePrompt = character.getAppearance().getCharacterPromptEn(); // 포즈 없이 외형만 정리된 프롬프트

        for (StoryPage page : story.getStoryPages()) {

            FluxResponse.FluxEndResponse result = avatarGeneratorService
                    .generateAvatarWithReference(basePrompt, page.getContentEn(), character.getAppearance().getCharacterSeed(), false)
                    .block();

            if (result != null) {
                handleFileWithTemp(result.getImgUrl(), story.getId(), page.getPageNumber(), tempFile -> {
                    String s3Url = s3Uploader.uploadFileFromFile(tempFile,
                            "stories/" + story.getId(),
                            "page_" + page.getPageNumber() + ".png");
                    page.setImageUrl(s3Url);
                });
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
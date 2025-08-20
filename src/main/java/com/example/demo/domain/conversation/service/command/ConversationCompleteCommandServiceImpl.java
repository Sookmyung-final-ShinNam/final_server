package com.example.demo.domain.conversation.service.command;

import com.example.demo.domain.character.entity.CharacterAppearance;
import com.example.demo.domain.character.repository.CharacterAppearanceRepository;
import com.example.demo.domain.conversation.service.model.S3Uploader;
import com.example.demo.domain.conversation.service.model.image.AvatarGeneratorService;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.conversation.service.model.video.RunwayService;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.repository.StoryPageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationCompleteCommandServiceImpl implements ConversationCompleteCommandService {

    private final StoryPageRepository storyPageRepo;
    private final CharacterAppearanceRepository characterAppearanceRepo;

    private final LlmClient llmClient;

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

}
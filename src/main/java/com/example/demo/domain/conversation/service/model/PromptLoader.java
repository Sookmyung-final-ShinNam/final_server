package com.example.demo.domain.conversation.service.model;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptLoader {

    public String loadPrompt(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + fileName);
            byte[] data = resource.getInputStream().readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CustomException(ErrorStatus.CHAT_GPT_API_CALL_FAILED);
        }
    }

}
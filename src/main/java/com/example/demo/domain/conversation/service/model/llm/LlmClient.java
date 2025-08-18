package com.example.demo.domain.conversation.service.model.llm;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LlmClient {

    @Value("${chatgpt.api-key}")
    private String apiKey;

    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // JSON 이스케이프 메서드
    public String jsonEscape(String input) {
        try {
            return new ObjectMapper().writeValueAsString(input);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.CHAT_GPT_API_JSON_ESCAPE_FAILED);
        }
    }

    // userContent 오버라이트 메서드
    public String buildPrompt(String promptFileName, String variable) {
        String template = promptLoader.loadPrompt(promptFileName);

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(template), "promptTemplate");

        StringWriter writer = new StringWriter();

        try {
            // Map 으로 감싸서 변수 이름(userContent)에 variable 값 할당
            Map<String, Object> data = new HashMap<>();
            data.put("userContent", variable);

            mustache.execute(writer, data).flush();
        } catch (IOException e) {
            throw new CustomException(ErrorStatus.CHAT_GPT_API_FILE_CALL_FAILED);
        }

        return writer.toString();
    }

    // LLM 응답 파싱 메서드
    public String extractFieldValue(String llmResponse, String fieldName) {
        try {
            JsonNode root = objectMapper.readTree(llmResponse);
            return root.path(fieldName).asText(null);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.CHAT_GPT_API_RESPONSE_FAILED);
        }
    }

    /**
     * OpenAI API 호출
     * @param body JSON 문자열 (buildPrompt()에서 생성된 완성된 JSON)
     * @return OpenAI가 생성한 텍스트 결과
     */
    public String callChatGpt(String body) {
        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.toString());
                return jsonNode.get("choices").get(0).get("message").get("content").asText();
            }

        } catch (IOException e) {
            throw new CustomException(ErrorStatus.CHAT_GPT_API_CALL_FAILED);
        }
    }

}
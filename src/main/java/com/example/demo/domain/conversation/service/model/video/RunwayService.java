package com.example.demo.domain.conversation.service.model.video;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class RunwayService {

    @Value("${runway.api.key}")
    private String runwayApiKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 이미지 → 비디오 작업 생성 (Task ID 반환)
     */
    private String createImageToVideo(File imageFile, String promptText, String ratio, int duration) throws IOException {

        String dataUri = toBase64DataUri(imageFile);

        String jsonBody = objectMapper.createObjectNode()
                .put("promptImage", dataUri)
                .put("promptText", promptText)
                .put("model", "gen4_turbo")
                .put("ratio", ratio)
                .put("duration", duration)
                .toString();

        Request request = new Request.Builder()
                .url("https://api.dev.runwayml.com/v1/image_to_video")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + runwayApiKey)
                .addHeader("X-Runway-Version", "2024-11-06")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + body);
            }
            JsonNode node = objectMapper.readTree(body);
            if (node.has("id")) {
                return node.get("id").asText();
            } else if (node.has("task_id")) {
                return node.get("task_id").asText();
            } else {
                throw new IOException("Unexpected response: " + body);
            }
        }
    }

    public JsonNode getTaskStatus(String taskId) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.dev.runwayml.com/v1/tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + runwayApiKey)
                .addHeader("X-Runway-Version", "2024-11-06")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Status check failed: " + body);
            }
            return objectMapper.readTree(body);
        }
    }

    private String toBase64DataUri(File imageFile) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = Files.probeContentType(imageFile.toPath());
        return "data:" + mimeType + ";base64," + base64;
    }

    /**
     * 재시도 + fallback 포함 동기 실행
     */
    public String createImageToVideoAndWait(File imageFile, String promptText) throws IOException, InterruptedException {

        String ratio = "1280:720"; // 가능한 것 - 6개 중 하나 : 1280:720, 720:1280, 1104:832, 832:1104, 960:960, 1584:672
        int duration = 5;

        int maxRetries = 3;
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String taskId = createImageToVideo(imageFile, promptText, ratio, duration);
                System.out.println("✅ Task created: " + taskId + " (attempt " + attempt + ")");
                return waitForCompletion(taskId);
            } catch (IOException e) {
                lastException = e;
                String msg = e.getMessage();
                System.err.println("⚠️ Attempt " + attempt + " failed: " + msg);
                Thread.sleep(2000L); // 재시도 전 대기
            }
        }

        throw new IOException("❌ Video generation failed after " + maxRetries + " attempts", lastException);
    }

    private String waitForCompletion(String taskId) throws IOException, InterruptedException {

        int maxWaitSeconds = 300;
        int pollIntervalSeconds = 5;
        int waited = 0;

        while (waited < maxWaitSeconds) {
            JsonNode statusJson = getTaskStatus(taskId);
            String status = statusJson.has("status") ? statusJson.get("status").asText() : "UNKNOWN";

            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                JsonNode outputs = statusJson.get("output");
                if (outputs != null && outputs.isArray() && outputs.size() > 0) {
                    String videoUrl = outputs.get(0).asText();
                    System.out.println("🎬 Video generated: " + videoUrl);
                    return videoUrl;
                }
                throw new IOException("Task succeeded but no output found: " + statusJson.toPrettyString());
            } else if ("FAILED".equalsIgnoreCase(status)) {
                throw new IOException("❌ Video generation failed: " + statusJson.toPrettyString());
            }

            System.out.println("⏳ Still processing... status=" + status);
            Thread.sleep(pollIntervalSeconds * 1000L);
            waited += pollIntervalSeconds;
        }

        throw new IOException("⏰ Timed out waiting for video generation (taskId=" + taskId + ")");
    }

}
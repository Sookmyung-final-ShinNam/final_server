package com.example.demo.domain.conversation.service.model.image;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvatarGeneratorService {

    private final WebClient.Builder webClientBuilder;

    @Value("${flux.api.url}")
    private String fluxApiUrl;

    @Value("${flux.api.key}")
    private String fluxApiKey;

    /**
     * 이미지 생성 요청
     *
     * @param basePrompt 기본 프롬프트 (캐릭터 외형)
     * @param additionalPrompt 추가 프롬프트 (각 페이지별 내용)
     * @param seed 동일 얼굴 유지용 seed (초기 -> null : 랜덤 생성)
     * @param isFirst 최초 생성 여부
     */
    public Mono<FluxResponse.FluxEndResponse> generateAvatarWithReference(String basePrompt, String additionalPrompt, Long seed, boolean isFirst) {

        // seed 값 설정 (없으면 랜덤)
        long finalSeed = (seed != null) ? seed : (System.currentTimeMillis() & 0xFFFFFFFFL);

        // 프롬프트 조립
        String finalPrompt;
        if (isFirst) {
            // 첫 생성 → 기본 프롬프트만 (대신 포즈가 함께 있는 기본 프롬프트를 사용함)
            finalPrompt = basePrompt;
        } else {
            // 이후 생성 → 기본 프롬프트 + 추가 프롬프트
            finalPrompt = basePrompt;
            if (additionalPrompt != null && !additionalPrompt.isBlank()) {
                finalPrompt += ", " + additionalPrompt;
            }
        }

        // 요청 body 구성
        Map<String, Object> body = new HashMap<>();
        body.put("prompt", finalPrompt);
        body.put("seed", finalSeed);
        body.put("aspect_ratio", "1:1");

        WebClient client = webClientBuilder.build();

        // Flux API 호출 → polling 방식으로 최종 이미지 URL 획득
        return client.post()
                .uri(fluxApiUrl + "/v1/flux-kontext-pro")
                .header("x-key", fluxApiKey)
                .header("Content-Type", "application/json;charset=UTF-8")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(FluxResponse.FluxSubmitResponse.class)
                .flatMap(submitResp -> {

                    if (submitResp == null || submitResp.getPolling_url() == null) {
                        return Mono.error(new RuntimeException("Flux submit response invalid: " + submitResp));
                    }

                    // polling_url 로 결과 조회
                    return Mono.defer(() -> client.get()
                                    .uri(submitResp.getPolling_url())
                                    .header("x-key", fluxApiKey)
                                    .retrieve()
                                    .bodyToMono(FluxResponse.FluxResultResponse.class)
                                    .flatMap(result -> {

                                        System.out.println("[Flux Polling] Status: " + result.getStatus());

                                        if ("Ready".equalsIgnoreCase(result.getStatus()) && result.getResult() != null) {
                                            String imageUrl = result.getResult().getImageUrl();
                                            return Mono.just(new FluxResponse.FluxEndResponse(finalSeed, imageUrl));
                                        } else if ("Failed".equalsIgnoreCase(result.getStatus())) {
                                            return Mono.error(new RuntimeException("Flux generation failed"));
                                        }

                                        return Mono.empty(); // 아직 진행 중 → 재시도
                                    })
                            )
                            .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofSeconds(2)).take(10)) // 최대 10번 polling
                            .switchIfEmpty(Mono.error(new RuntimeException("Flux polling timeout")));
                });
    }

}
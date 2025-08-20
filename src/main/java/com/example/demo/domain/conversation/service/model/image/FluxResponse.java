package com.example.demo.domain.conversation.service.model.image;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class FluxResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FluxSubmitResponse {
        private String polling_url;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FluxResultResponse {
        private String status;
        private ResultData result;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResultData {
            @com.fasterxml.jackson.annotation.JsonProperty("sample")
            private String imageUrl;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FluxEndResponse {
        private Long seed;   // seed (고정 + 같은 프롬프트 -> 같은 사진)
        private String imgUrl; // 최종 이미지 URL
    }

}
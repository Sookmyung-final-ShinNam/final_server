package com.example.demo.domain.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class FeedbackAttemptStats {

    @Column(name = "attempt_gi")
    private Integer giCount;

    @Column(name = "attempt_seung")
    private Integer seungCount;

    @Column(name = "attempt_jeon")
    private Integer jeonCount;

    @Column(name = "attempt_gyeol")
    private Integer gyeolCount;

}
package com.example.demo.domain.conversation.entity;

import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "step_slot")
public class StepSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_slot_id")
    private Long id;

    @Column(name = "is_filled", nullable = false)
    private Boolean isFilled;

    @Column(name = "value")
    private String value; // 채워진 값 -> 한 번 등록 시 확정. 갱신 x

    @Column(name = "source")
    private String source; // USER, INFERRED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private SessionStep step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private SlotDefinition slotDefinition;
}
package com.example.demo.domain.conversation.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "slot_definition")
public class SlotDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long id;

    @Column(name = "step_type", nullable = false)
    private String stepType; // 기, 승, 전, 결

    /*
    * -- 현재 총 9개 항목. 향후 언어 관련 학습이나 아동 행동 패턴 관찰 후 추가 or 변경 가능
    * 기 : THEME / PLACE / ACTION
    * 승 : NEW_ENTITY / INTERACTION
    * 전 : TURNING_POINT / FEELING
    * 결 : MORAL / FINAL_ACTION
    * */
    @Column(name = "slot_name", nullable = false)
    private String slotName;
}
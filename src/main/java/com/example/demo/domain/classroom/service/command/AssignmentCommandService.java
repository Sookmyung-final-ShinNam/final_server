package com.example.demo.domain.classroom.service.command;

import com.example.demo.domain.classroom.web.dto.ClassroomRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.user.entity.User;

public interface AssignmentCommandService {

    void createAssignment(User teacher, Long classroomId, ClassroomRequestDto.CreateAssignmentRequest request);

    ConversationResponseDto.ConversationStartResponseDto startAssignment(
            User student, Long classroomId, Long assignmentId,
            ConversationRequestDto.ConversationStartRequestDto request);
}

package com.example.demo.domain.classroom.service.query;

import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.user.entity.User;

public interface AssignmentQueryService {

    Object getClassroomAssignments(User user, Long classroomId);

    CompletedCharacterResponse.CharacterListResponse getClassroomStories(User user, Long classroomId, Long assignmentId);
}

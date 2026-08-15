package com.example.demo.domain.classroom.service.query;

import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;

public interface ClassroomQueryService {

    Object getClassrooms(User user);

    Object getClassroomDetail(User user, Long classroomId);

    CompletedCharacterResponse.CharacterListResponse getClassroomStories(User user, Long classroomId, Integer week);
}

package com.example.demo.domain.classroom.service.query;

import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;

import java.util.List;

public interface AssignmentQueryService {

    List<ClassroomResponseDto.TeacherAssignmentResponse> getTeacherAssignments(User teacher);

    List<ClassroomResponseDto.AssignmentResponse> getClassroomAssignments(User user, Long classroomId);
}

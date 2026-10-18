package com.example.demo.domain.classroom.service.command;

import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;

public interface ClassroomCommandService {

    ClassroomResponseDto.CreateClassroomResponse createClassroom(User teacher, String name);

    void approveStudent(User teacher, Long classroomId, Long studentId);

    void chargeAcorn(User teacher, Long classroomId);

    void joinClassroom(User student, String code);
}

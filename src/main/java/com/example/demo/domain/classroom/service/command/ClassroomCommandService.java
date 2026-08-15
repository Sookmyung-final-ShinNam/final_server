package com.example.demo.domain.classroom.service.command;

import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;

public interface ClassroomCommandService {

    void sendEmailVerification(User user, String email);

    void verifyEmail(User user, String code);

    ClassroomResponseDto.CreateClassroomResponse createClassroom(User teacher, String name);

    void approveStudent(User teacher, Long classroomId, Long studentId);

    void joinClassroom(User student, String code);

    void chargeAcorn(User user);
}

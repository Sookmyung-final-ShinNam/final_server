package com.example.demo.domain.classroom.service.query;

import com.example.demo.domain.user.entity.User;

public interface ClassroomQueryService {

    Object getClassrooms(User user);

    Object getClassroomDetail(User user, Long classroomId);
}

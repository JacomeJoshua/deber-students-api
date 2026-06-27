package com.example.demo.mappers

import com.example.demo.dtos.EnrollmentResponse
import com.example.demo.entities.Enrollment
import org.springframework.stereotype.Component

@Component
class EnrollmentMapper(
    private val studentMapper: StudentMapper,
    private val subjectMapper: SubjectMapper
) {
    fun toResponse(e: Enrollment) = EnrollmentResponse(
        id = e.id,
        status = e.status,
        createdAt = e.createdAt,
        student = studentMapper.toResponse(e.student),
        subject = subjectMapper.toResponse(e.subject)
    )
}
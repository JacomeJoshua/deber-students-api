package com.example.demo.mappers

import com.example.demo.dtos.StudentRequest
import com.example.demo.dtos.StudentResponse
import com.example.demo.entities.Student
import org.springframework.stereotype.Component

@Component
class StudentMapper {
    fun toEntity(req: StudentRequest) = Student(name = req.name, email = req.email)

    fun toResponse(s: Student) = StudentResponse(
        id = s.id,
        name = s.name,
        email = s.email
    )
}
package com.example.demo.service

import com.example.demo.dto.StudentRequest
import com.example.demo.dto.StudentResponse
import com.example.demo.entity.Student
import com.example.demo.repository.StudentRepository
import org.springframework.stereotype.Service

@Service
class StudentService(private val studentRepository: StudentRepository) {

    fun createStudent(request: StudentRequest): StudentResponse {
        val student = Student(name = request.name, email = request.email)
        val savedStudent = studentRepository.save(student)
        return StudentResponse(
            id = savedStudent.id!!,
            name = savedStudent.name,
            email = savedStudent.email
        )
    }

    fun getAllStudents(): List<StudentResponse> {
        return studentRepository.findAll().map { student ->
            StudentResponse(
                id = student.id!!,
                name = student.name,
                email = student.email
            )
        }
    }
}
package com.example.demo.controller

import com.example.demo.dto.StudentRequest
import com.example.demo.dto.StudentResponse
import com.example.demo.service.StudentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/students")
class StudentController(private val studentService: StudentService) {

    @PostMapping
    fun createStudent(@RequestBody request: StudentRequest): ResponseEntity<StudentResponse> {
        val response = studentService.createStudent(request)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllStudents(): ResponseEntity<List<StudentResponse>> {
        val response = studentService.getAllStudents()
        return ResponseEntity(response, HttpStatus.OK)
    }
}
package com.example.demo.mappers

import com.example.demo.dtos.SubjectRequest
import com.example.demo.dtos.SubjectResponse
import com.example.demo.entities.Professor
import com.example.demo.entities.Subject
import org.springframework.stereotype.Component

@Component
class SubjectMapper(private val professorMapper: ProfessorMapper) {

    fun toEntity(req: SubjectRequest, prof: Professor) = Subject(
        name = req.name,
        code = req.code,
        professor = prof
    )

    fun toResponse(sub: Subject) = SubjectResponse(
        sub.id,
        sub.name,
        sub.code,
        professorMapper.toResponse(sub.professor)
    )
}